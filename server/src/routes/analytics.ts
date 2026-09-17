import { Hono } from 'hono';
import { analyticsService } from '../services/analytics.service.js';
import { optionalAuth, requireAuth } from '../middleware/auth.js';
import { analyticsEventSchema } from '../schemas/index.js';
import { getClientIp } from '../utils/ip.js';

export const analyticsRouter = new Hono();

// POST /api/analytics/event - Ingest telemetry event
analyticsRouter.post('/event', optionalAuth, async (c) => {
  const currentUser = c.get('user');
  const body = await c.req.json().catch(() => ({}));
  const data = analyticsEventSchema.parse(body);

  const ip = getClientIp(c);
  const userAgent = c.req.header('user-agent') || null;

  await analyticsService.recordEvent({
    userId: currentUser?.userId || null,
    eventType: data.eventType,
    targetId: data.targetId,
    targetType: data.targetType,
    categoryTag: data.categoryTag,
    watchDurationMs: data.watchDurationMs,
    completionPercent: data.completionPercent,
    metadata: data.metadata,
    ipAddress: ip,
    userAgent,
  });

  return c.json({ success: true });
});

// POST /api/analytics/batch - Batch ingestion
analyticsRouter.post('/batch', optionalAuth, async (c) => {
  const currentUser = c.get('user');
  const body = await c.req.json().catch(() => ({ events: [] }));
  const events = Array.isArray(body.events) ? body.events : [];

  const ip = getClientIp(c);
  const userAgent = c.req.header('user-agent') || null;

  for (const ev of events.slice(0, 50)) {
    try {
      const data = analyticsEventSchema.parse(ev);
      await analyticsService.recordEvent({
        userId: currentUser?.userId || null,
        eventType: data.eventType,
        targetId: data.targetId,
        targetType: data.targetType,
        categoryTag: data.categoryTag,
        watchDurationMs: data.watchDurationMs,
        completionPercent: data.completionPercent,
        metadata: data.metadata,
        ipAddress: ip,
        userAgent,
      });
    } catch (e) {}
  }

  return c.json({ success: true, processed: events.length });
});

// GET /api/analytics/creator - Creator analytics dashboard
analyticsRouter.get('/creator', requireAuth, async (c) => {
  const user = c.get('user');
  const metrics = await analyticsService.getCreatorMetrics(user.userId);
  return c.json({ success: true, metrics });
});
