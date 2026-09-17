import { query } from '../db/index.js';
import { v4 as uuidv4 } from 'uuid';

export class ReportRepository {
  async createReport(data: {
    reporterId: string;
    targetType: 'VIDEO' | 'COMMENT' | 'USER';
    targetId: string;
    reason: string;
    details?: string;
  }): Promise<any> {
    const reportId = `rep_${uuidv4().replace(/-/g, '').slice(0, 16)}`;
    const res = await query(
      `INSERT INTO reports (id, reporter_id, target_type, target_id, reason, details, status)
       VALUES ($1, $2, $3, $4, $5, $6, 'OPEN')
       RETURNING *`,
      [
        reportId,
        data.reporterId,
        data.targetType,
        data.targetId,
        data.reason,
        data.details || '',
      ]
    );
    return res.rows[0];
  }

  async listReports(status?: string, limit: number = 50): Promise<any[]> {
    let sql = 'SELECT * FROM reports';
    const params: any[] = [];
    if (status) {
      params.push(status);
      sql += ` WHERE status = $${params.length}`;
    }
    sql += ` ORDER BY created_at DESC LIMIT $${params.length + 1}`;
    params.push(limit);

    const res = await query(sql, params);
    return res.rows;
  }
}

export const reportRepository = new ReportRepository();
