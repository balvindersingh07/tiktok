import { Context, Next } from 'hono';
import { verifyAccessToken, TokenPayload } from '../utils/jwt.js';
import { UnauthorizedError } from '../utils/errors.js';

declare module 'hono' {
  interface ContextVariableMap {
    user: TokenPayload;
  }
}

export async function requireAuth(c: Context, next: Next) {
  const authHeader = c.req.header('Authorization');
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    throw new UnauthorizedError('Authentication token required');
  }

  const token = authHeader.substring(7);
  try {
    const payload = verifyAccessToken(token);
    c.set('user', payload);
    await next();
  } catch (err) {
    throw new UnauthorizedError('Invalid or expired token');
  }
}

export async function optionalAuth(c: Context, next: Next) {
  const authHeader = c.req.header('Authorization');
  if (authHeader && authHeader.startsWith('Bearer ')) {
    const token = authHeader.substring(7);
    try {
      const payload = verifyAccessToken(token);
      c.set('user', payload);
    } catch {
      // Ignored for optional auth
    }
  }
  await next();
}
