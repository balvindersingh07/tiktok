import { Hono } from 'hono';
import { streamSSE } from 'hono/streaming';
import { messageService } from '../services/message.service.js';
import { eventBus } from '../utils/eventBus.js';
import { requireAuth } from '../middleware/auth.js';
import { sendMessageSchema } from '../schemas/index.js';

export const messagesRouter = new Hono();

// GET /api/messages/conversations
messagesRouter.get('/conversations', requireAuth, async (c) => {
  const user = c.get('user');
  const conversations = await messageService.getConversations(user.userId);
  return c.json({
    success: true,
    conversations,
  });
});

// GET /api/messages/conversations/:id/messages
messagesRouter.get('/conversations/:id/messages', requireAuth, async (c) => {
  const user = c.get('user');
  const convId = c.req.param('id');
  const limit = Math.min(parseInt(c.req.query('limit') || '100', 10), 100);

  const messages = await messageService.getMessages(convId, user.userId, limit);
  return c.json({
    success: true,
    messages,
  });
});

// POST /api/messages/send
messagesRouter.post('/send', requireAuth, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();
  const data = sendMessageSchema.parse(body);

  const message = await messageService.sendMessage(user.userId, data.receiverId, data.content);
  return c.json({
    success: true,
    message,
  });
});

// GET /api/messages/stream - Real-time SSE event stream for live DMs and notifications
messagesRouter.get('/stream', requireAuth, async (c) => {
  const user = c.get('user');

  return streamSSE(c, async (stream) => {
    const onDm = async (message: any) => {
      try {
        await stream.writeSSE({
          event: 'dm',
          data: JSON.stringify(message),
        });
      } catch (err) {}
    };

    const onNotif = async (notif: any) => {
      try {
        await stream.writeSSE({
          event: 'notification',
          data: JSON.stringify(notif),
        });
      } catch (err) {}
    };

    eventBus.on(`dm:${user.userId}`, onDm);
    eventBus.on(`notif:${user.userId}`, onNotif);

    stream.onAbort(() => {
      eventBus.off(`dm:${user.userId}`, onDm);
      eventBus.off(`notif:${user.userId}`, onNotif);
    });

    // Send initial connected event
    await stream.writeSSE({
      event: 'connected',
      data: JSON.stringify({ userId: user.userId, timestamp: Date.now() }),
    });

    // Keepalive loop
    while (!stream.aborted) {
      await stream.sleep(25000);
      try {
        await stream.writeSSE({
          event: 'ping',
          data: JSON.stringify({ timestamp: Date.now() }),
        });
      } catch (e) {
        break;
      }
    }
  });
});
