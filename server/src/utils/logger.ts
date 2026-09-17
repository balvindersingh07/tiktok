export enum LogLevel {
  DEBUG = 0,
  INFO = 1,
  WARN = 2,
  ERROR = 3,
}

const currentLogLevel = process.env.LOG_LEVEL === 'debug' ? LogLevel.DEBUG : LogLevel.INFO;

export const logger = {
  debug(message: string, meta?: Record<string, any>) {
    if (currentLogLevel <= LogLevel.DEBUG) {
      console.debug(`[DEBUG] ${new Date().toISOString()} - ${message}`, meta ? JSON.stringify(meta) : '');
    }
  },
  info(message: string, meta?: Record<string, any>) {
    if (currentLogLevel <= LogLevel.INFO) {
      console.log(`[INFO]  ${new Date().toISOString()} - ${message}`, meta ? JSON.stringify(meta) : '');
    }
  },
  warn(message: string, meta?: Record<string, any>) {
    if (currentLogLevel <= LogLevel.WARN) {
      console.warn(`[WARN]  ${new Date().toISOString()} - ${message}`, meta ? JSON.stringify(meta) : '');
    }
  },
  error(message: string, error?: any) {
    console.error(`[ERROR] ${new Date().toISOString()} - ${message}`, error?.stack || error || '');
  },
};
