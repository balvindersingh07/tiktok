import { Hono } from 'hono';
import { query } from '../db/index.js';

export const healthRouter = new Hono();

healthRouter.get('/', async (c) => {
  let dbStatus = 'disconnected';
  let dbLatencyMs = 0;

  try {
    const start = Date.now();
    await query('SELECT 1 as ping');
    dbLatencyMs = Date.now() - start;
    dbStatus = 'connected';
  } catch (err: any) {
    dbStatus = `error: ${err.message}`;
  }

  const memoryUsage = process.memoryUsage();

  return c.json({
    status: 'ok',
    service: 'tashan-backend',
    timestamp: new Date().toISOString(),
    uptimeSeconds: Math.floor(process.uptime()),
    database: {
      status: dbStatus,
      latencyMs: dbLatencyMs,
    },
    memory: {
      rssMb: Math.round(memoryUsage.rss / 1024 / 1024),
      heapUsedMb: Math.round(memoryUsage.heapUsed / 1024 / 1024),
    },
  });
});
