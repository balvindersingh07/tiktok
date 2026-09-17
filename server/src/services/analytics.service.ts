import { analyticsRepository } from '../repositories/analytics.repository.js';
import { reportRepository } from '../repositories/report.repository.js';

export class AnalyticsService {
  async recordEvent(event: {
    userId?: string | null;
    eventType: string;
    targetId?: string | null;
    targetType?: string;
    categoryTag?: string;
    watchDurationMs?: number;
    completionPercent?: number;
    metadata?: Record<string, any>;
    ipAddress?: string | null;
    userAgent?: string | null;
  }) {
    return analyticsRepository.recordEvent(event);
  }

  async getCreatorMetrics(userId: string) {
    return analyticsRepository.getCreatorMetrics(userId);
  }
}

export class ReportService {
  async reportContent(data: {
    reporterId: string;
    targetType: 'VIDEO' | 'COMMENT' | 'USER';
    targetId: string;
    reason: string;
    details?: string;
  }) {
    return reportRepository.createReport(data);
  }
}

export const analyticsService = new AnalyticsService();
export const reportService = new ReportService();
