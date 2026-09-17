-- Migration 002: Performance Indexes & Schema Enhancements
CREATE INDEX IF NOT EXISTS idx_blocked_users_target ON blocked_users(blocked_user_id);
CREATE INDEX IF NOT EXISTS idx_muted_users_target ON muted_users(muted_user_id);
CREATE INDEX IF NOT EXISTS idx_video_hashtags_tag ON video_hashtags(hashtag_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent_created ON comments(parent_comment_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_videos_duet ON videos(duet_with_video_id);
CREATE INDEX IF NOT EXISTS idx_videos_stitch ON videos(stitch_with_video_id);
CREATE INDEX IF NOT EXISTS idx_analytics_target ON analytics_events(target_id, event_type);
