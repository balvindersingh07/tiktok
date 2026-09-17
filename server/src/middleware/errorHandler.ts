import { Context } from 'hono';
import { ZodError } from 'zod';
import { AppError } from '../utils/errors.js';
import { logger } from '../utils/logger.js';

export function errorHandler(err: Error, c: Context) {
  logger.error(err.message, err);

  if (err instanceof ZodError) {
    return c.json(
      {
        success: false,
        error: 'Validation Error',
        details: err.errors.map((e) => ({
          path: e.path.join('.'),
          message: e.message,
        })),
      },
      400
    );
  }

  if (err instanceof AppError) {
    return c.json(
      {
        success: false,
        error: err.message,
        details: err.details,
      },
      err.statusCode as any
    );
  }

  return c.json(
    {
      success: false,
      error: 'Internal Server Error',
      message: process.env.NODE_ENV === 'production' ? 'An unexpected error occurred' : err.message,
    },
    500
  );
}
