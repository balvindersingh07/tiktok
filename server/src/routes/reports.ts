import { Hono } from 'hono';
import { reportService } from '../services/analytics.service.js';
import { requireAuth } from '../middleware/auth.js';
import { createReportSchema } from '../schemas/index.js';

export const reportsRouter = new Hono();

// POST /api/reports - Report content or user
reportsRouter.post('/', requireAuth, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();
  const data = createReportSchema.parse(body);

  const report = await reportService.reportContent({
    reporterId: user.userId,
    targetType: data.targetType,
    targetId: data.targetId,
    reason: data.reason,
    details: data.details,
  });

  return c.json({
    success: true,
    reportId: report.id,
    message: 'Report submitted for review',
  }, 201);
});
