import { Context } from 'hono';

/**
 * Extracts and sanitizes the client IP address from request headers.
 * Safely handles proxy chains (comma-separated x-forwarded-for) and limits string length.
 */
export function getClientIp(c: Context): string {
  const forwarded = c.req.header('x-forwarded-for');
  if (forwarded) {
    const first = forwarded.split(',')[0].trim();
    if (first) {
      return first.slice(0, 100);
    }
  }
  const realIp = c.req.header('x-real-ip');
  if (realIp) {
    return realIp.trim().slice(0, 100);
  }
  return '127.0.0.1';
}

/**
 * Sanitizes an IP address string before database storage
 */
export function sanitizeIp(rawIp?: string | null): string | null {
  if (!rawIp) return null;
  const first = rawIp.split(',')[0].trim();
  return first ? first.slice(0, 100) : null;
}
