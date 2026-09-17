-- Migration 003: Upload Sessions for presigned upload tracking and failure recovery
CREATE TABLE IF NOT EXISTS upload_sessions (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    storage_key TEXT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    max_size_bytes BIGINT NOT NULL,
    bytes_uploaded BIGINT DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED', -- 'CREATED', 'UPLOADING', 'UPLOADED', 'CONFIRMED', 'EXPIRED', 'FAILED', 'CANCELLED'
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_upload_sessions_user ON upload_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_upload_sessions_status ON upload_sessions(status);
