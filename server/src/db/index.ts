import pg from 'pg';
import { PGlite } from '@electric-sql/pglite';
import path from 'path';
import fs from 'fs';
import { config } from '../config/index.js';

export interface QueryResult<T = any> {
  rows: T[];
  rowCount: number;
}

export interface DbClient {
  query<T = any>(text: string, params?: any[]): Promise<QueryResult<T>>;
}

let pgPool: pg.Pool | null = null;
let pgliteInstance: PGlite | null = null;
let isInitialized = false;
let initPromise: Promise<DbClient> | null = null;

export async function getDb(): Promise<DbClient> {
  if (isInitialized) {
    if (pgPool) {
      return {
        query: async <T = any>(text: string, params?: any[]) => {
          const res = await pgPool!.query(text, params);
          return { rows: res.rows as T[], rowCount: res.rowCount ?? res.rows.length };
        },
      };
    }
    if (pgliteInstance) {
      return {
        query: async <T = any>(text: string, params?: any[]) => {
          const res = await pgliteInstance!.query(text, params);
          return { rows: res.rows as T[], rowCount: res.rows.length };
        },
      };
    }
  }

  if (initPromise) {
    return initPromise;
  }

  initPromise = (async () => {
    try {
      if (config.databaseUrl && config.databaseUrl.startsWith('postgres')) {
        pgPool = new pg.Pool({
          connectionString: config.databaseUrl,
          max: 20,
          idleTimeoutMillis: 30000,
          connectionTimeoutMillis: 5000,
        });
        isInitialized = true;
        return {
          query: async <T = any>(text: string, params?: any[]) => {
            const res = await pgPool!.query(text, params);
            return { rows: res.rows as T[], rowCount: res.rowCount ?? res.rows.length };
          },
        };
      }

      // Fallback to embedded PostgreSQL (PGlite)
      const dbDir = path.join(config.storage.localDir, 'pglite_data');
      if (!fs.existsSync(config.storage.localDir)) {
        fs.mkdirSync(config.storage.localDir, { recursive: true });
      }

      // Clean stale lock files if node crashed or was killed previously
      const pidFile = path.join(dbDir, 'postmaster.pid');
      const optsFile = path.join(dbDir, 'postmaster.opts');
      try {
        if (fs.existsSync(pidFile)) {
          fs.unlinkSync(pidFile);
        }
        if (fs.existsSync(optsFile)) {
          fs.unlinkSync(optsFile);
        }
      } catch (cleanErr) {
        console.warn('Could not clean stale postmaster files:', cleanErr);
      }

      // Initialize PGlite with automatic corruption recovery
      try {
        pgliteInstance = new PGlite(dbDir);
        await pgliteInstance.waitReady;
      } catch (err: any) {
        console.error('Failed to open on-disk PGlite database, recovering:', err);
        try {
          if (fs.existsSync(dbDir)) {
            const corruptedBackup = path.join(config.storage.localDir, `pglite_corrupted_${Date.now()}`);
            fs.renameSync(dbDir, corruptedBackup);
          }
          fs.mkdirSync(dbDir, { recursive: true });
          pgliteInstance = new PGlite(dbDir);
          await pgliteInstance.waitReady;
        } catch (recoverErr) {
          console.error('On-disk recovery failed, using resilient in-memory PGlite:', recoverErr);
          pgliteInstance = new PGlite();
          await pgliteInstance.waitReady;
        }
      }

      isInitialized = true;

      return {
        query: async <T = any>(text: string, params?: any[]) => {
          if (!pgliteInstance) {
            throw new Error('PGlite instance not initialized');
          }
          const res = await pgliteInstance.query(text, params);
          return { rows: res.rows as T[], rowCount: res.rows.length };
        },
      };
    } finally {
      initPromise = null;
    }
  })();

  return initPromise;
}

export async function query<T = any>(text: string, params?: any[]): Promise<QueryResult<T>> {
  const db = await getDb();
  return db.query<T>(text, params);
}

export async function transaction<T>(
  callback: (client: DbClient) => Promise<T>
): Promise<T> {
  const db = await getDb();
  if (pgPool) {
    const client = await pgPool.connect();
    try {
      await client.query('BEGIN');
      const wrapped: DbClient = {
        query: async <R = any>(text: string, params?: any[]) => {
          const res = await client.query(text, params);
          return { rows: res.rows as R[], rowCount: res.rowCount ?? res.rows.length };
        },
      };
      const result = await callback(wrapped);
      await client.query('COMMIT');
      return result;
    } catch (err) {
      await client.query('ROLLBACK');
      throw err;
    } finally {
      client.release();
    }
  }

  // PGlite transaction
  if (pgliteInstance) {
    return pgliteInstance.transaction(async (tx) => {
      const wrapped: DbClient = {
        query: async <R = any>(text: string, params?: any[]) => {
          const res = await tx.query(text, params);
          return { rows: res.rows as R[], rowCount: res.rows.length };
        },
      };
      return callback(wrapped);
    });
  }

  throw new Error('Database client not initialized');
}

export async function closeDb(): Promise<void> {
  if (pgPool) {
    try {
      await pgPool.end();
    } catch {}
    pgPool = null;
  }
  if (pgliteInstance) {
    try {
      await pgliteInstance.close();
    } catch {}
    pgliteInstance = null;
  }
  isInitialized = false;
  initPromise = null;
}
