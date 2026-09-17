import { Context, Next } from 'hono';

interface RateLimitOptions {
  windowMs: number;
  max: number;
  message?: string;
}

interface ClientRecord {
  count: number;
  resetTime: number;
}

const clientMap = new Map<string, ClientRecord>();

// Cleanup stale rate limit records periodically
setInterval(() => {
  const now = Date.now();
  for (const [key, record] of clientMap.entries()) {
    if (now > record.resetTime) {
      clientMap.delete(key);
    }
  }
}, 60000);

export function rateLimit(options: RateLimitOptions) {
  const { windowMs, max, message = 'Too many requests, please try again later.' } = options;

  return async (c: Context, next: Next) => {
    // Determine client identifier: authenticated user ID or IP/forwarded-for
    const userId = c.get('userId');
    const ip = c.req.header('x-forwarded-for') || c.req.header('x-real-ip') || '127.0.0.1';
    const path = c.req.path;
    const key = `${userId || ip}:${path}`;

    const now = Date.now();
    let record = clientMap.get(key);

    if (!record || now > record.resetTime) {
      record = {
        count: 1,
        resetTime: now + windowMs,
      };
      clientMap.set(key, record);
    } else {
      record.count += 1;
    }

    const remaining = Math.max(0, max - record.count);
    const resetSeconds = Math.ceil((record.resetTime - now) / 1000);

    c.header('X-RateLimit-Limit', String(max));
    c.header('X-RateLimit-Remaining', String(remaining));
    c.header('X-RateLimit-Reset', String(resetSeconds));

    if (record.count > max) {
      c.header('Retry-After', String(resetSeconds));
      return c.json(
        {
          error: 'RateLimitExceeded',
          message,
          retryAfter: resetSeconds,
        },
        429
      );
    }

    await next();
  };
}

export const uploadLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 30,
  message: 'Upload rate limit exceeded. Please wait a few minutes before trying again.',
});

export const apiLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 150,
  message: 'Too many API requests, please slow down.',
});

export const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20,
  message: 'Too many authentication attempts. Please wait 15 minutes.',
});
