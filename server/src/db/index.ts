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
  pgliteInstance = new PGlite(dbDir);
  await pgliteInstance.waitReady;
  isInitialized = true;

  return {
    query: async <T = any>(text: string, params?: any[]) => {
      const res = await pgliteInstance!.query(text, params);
      return { rows: res.rows as T[], rowCount: res.rows.length };
    },
  };
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
    await pgPool.end();
    pgPool = null;
  }
  if (pgliteInstance) {
    await pgliteInstance.close();
    pgliteInstance = null;
  }
  isInitialized = false;
}
