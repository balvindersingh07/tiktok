import { Hono } from 'hono';
import { signupSchema, loginSchema, changePinSchema } from '../schemas/index.js';
import { authService } from '../services/auth.service.js';
import { requireAuth } from '../middleware/auth.js';
import { rateLimit } from '../middleware/rateLimit.js';

export const authRouter = new Hono();

// Rate limit signup and login endpoints (e.g. 20 requests per 15 minutes)
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 30,
  message: 'Too many authentication attempts, please try again later.',
});

authRouter.post('/signup', authLimiter, async (c) => {
  const body = await c.req.json();
  const data = signupSchema.parse(body);

  const result = await authService.signup({
    handle: data.handle,
    email: data.email,
    password: data.password,
    passwordPin: data.passwordPin,
    displayName: data.displayName,
    bio: data.bio,
  });

  return c.json({
    success: true,
    token: result.token,
    tokens: {
      accessToken: result.token,
      refreshToken: result.token,
    },
    user: result.user,
  }, 201);
});

authRouter.post('/login', authLimiter, async (c) => {
  const body = await c.req.json();
  const data = loginSchema.parse(body);

  const result = await authService.login({
    handle: data.handle,
    handleOrEmail: data.handleOrEmail,
    password: data.password,
    passwordPin: data.passwordPin,
  });

  return c.json({
    success: true,
    token: result.token,
    tokens: {
      accessToken: result.token,
      refreshToken: result.token,
    },
    user: result.user,
  });
});

authRouter.get('/me', requireAuth, async (c) => {
  const currentUser = c.get('user');
  const user = await authService.getCurrentUser(currentUser.userId);

  return c.json({
    success: true,
    user,
  });
});

authRouter.post('/change-pin', requireAuth, async (c) => {
  const currentUser = c.get('user');
  const body = await c.req.json();
  const data = changePinSchema.parse(body);

  const result = await authService.changePin(currentUser.userId, data.oldPin, data.newPin);
  return c.json(result);
});

authRouter.post('/logout', requireAuth, async (c) => {
  return c.json({
    success: true,
    message: 'Logged out successfully',
  });
});
