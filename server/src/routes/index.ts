import { Hono } from 'hono';
import { healthRouter } from './health.js';
import { authRouter } from './auth.js';
import { usersRouter } from './users.js';
import { feedRouter } from './feed.js';
import { videosRouter } from './videos.js';
import { notificationsRouter } from './notifications.js';
import { messagesRouter } from './messages.js';
import { soundsRouter } from './sounds.js';
import { searchRouter } from './search.js';
import { analyticsRouter } from './analytics.js';
import { draftsRouter } from './drafts.js';
import { reportsRouter } from './reports.js';

export const apiRouter = new Hono();

apiRouter.route('/health', healthRouter);
apiRouter.route('/auth', authRouter);
apiRouter.route('/users', usersRouter);
apiRouter.route('/feed', feedRouter);
apiRouter.route('/videos', videosRouter);
apiRouter.route('/notifications', notificationsRouter);
apiRouter.route('/messages', messagesRouter);
apiRouter.route('/sounds', soundsRouter);
apiRouter.route('/search', searchRouter);
apiRouter.route('/analytics', analyticsRouter);
apiRouter.route('/drafts', draftsRouter);
apiRouter.route('/reports', reportsRouter);

