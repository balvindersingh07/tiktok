import { Hono } from 'hono';
import { notificationService } from '../services/notification.service.js';
import { requireAuth } from '../middleware/auth.js';
import { notificationPreferencesSchema } from '../schemas/index.js';

export const notificationsRouter = new Hono();

// GET /api/notifications
notificationsRouter.get('/', requireAuth, async (c) => {
  const user = c.get('user');
  const limit = Math.min(parseInt(c.req.query('limit') || '50', 10), 100);

  const notifications = await notificationService.getNotifications(user.userId, limit);
  return c.json({
    success: true,
    notifications,
  });
});

// GET /api/notifications/unread-count
notificationsRouter.get('/unread-count', requireAuth, async (c) => {
  const user = c.get('user');
  const unreadCount = await notificationService.getUnreadCount(user.userId);
  return c.json({
    success: true,
    unreadCount,
  });
});

// POST /api/notifications/read-all
notificationsRouter.post('/read-all', requireAuth, async (c) => {
  const user = c.get('user');
  await notificationService.markAllAsRead(user.userId);
  return c.json({ success: true, message: 'All notifications marked as read' });
});

// POST /api/notifications/:id/read
notificationsRouter.post('/:id/read', requireAuth, async (c) => {
  const user = c.get('user');
  const notifId = c.req.param('id');
  await notificationService.markAsRead(notifId, user.userId);
  return c.json({ success: true });
});

// GET /api/notifications/preferences
notificationsRouter.get('/preferences', requireAuth, async (c) => {
  const user = c.get('user');
  const preferences = await notificationService.getPreferences(user.userId);
  return c.json({
    success: true,
    preferences,
  });
});

// PUT /api/notifications/preferences
notificationsRouter.put('/preferences', requireAuth, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();
  const data = notificationPreferencesSchema.parse(body);

  const preferences = await notificationService.updatePreferences(user.userId, data);
  return c.json({
    success: true,
    preferences,
  });
});
