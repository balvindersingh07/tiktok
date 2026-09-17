-- Tashan Production PostgreSQL Schema
-- Migration 001: Initial Schema

-- Users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    handle VARCHAR(64) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(32) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER', -- 'USER', 'ADMIN', 'MODERATOR'
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Sessions
CREATE TABLE IF NOT EXISTS sessions (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL,
    device_info TEXT,
    ip_address VARCHAR(45),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_expires_at ON sessions(expires_at);

-- Profiles
CREATE TABLE IF NOT EXISTS profiles (
    user_id VARCHAR(64) PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(128) NOT NULL,
    handle VARCHAR(64) NOT NULL,
    bio TEXT DEFAULT '',
    avatar_url TEXT DEFAULT '',
    cover_url TEXT DEFAULT '',
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    allow_comments VARCHAR(32) NOT NULL DEFAULT 'EVERYONE', -- 'EVERYONE', 'FRIENDS', 'NO_ONE'
    allow_duet VARCHAR(32) NOT NULL DEFAULT 'EVERYONE',
    allow_stitch VARCHAR(32) NOT NULL DEFAULT 'EVERYONE',
    allow_downloads BOOLEAN NOT NULL DEFAULT TRUE,
    allow_direct_messages VARCHAR(32) NOT NULL DEFAULT 'EVERYONE',
    show_liked_videos BOOLEAN NOT NULL DEFAULT TRUE,
    show_following_list BOOLEAN NOT NULL DEFAULT TRUE,
    following_count INT NOT NULL DEFAULT 0,
    followers_count INT NOT NULL DEFAULT 0,
    likes_count BIGINT NOT NULL DEFAULT 0,
    videos_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_profiles_handle ON profiles(handle);

-- Notification Preferences
CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id VARCHAR(64) PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    likes_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    comments_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    follows_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    mentions_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    dms_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    reposts_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    system_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Device Tokens (Push Notifications)
CREATE TABLE IF NOT EXISTS device_tokens (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token TEXT NOT NULL,
    platform VARCHAR(32) NOT NULL DEFAULT 'ANDROID',
    last_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_device_token UNIQUE (user_id, token)
);

-- Sounds
CREATE TABLE IF NOT EXISTS sounds (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    duration_seconds INT NOT NULL DEFAULT 15,
    audio_url TEXT NOT NULL,
    cover_url TEXT DEFAULT '',
    category VARCHAR(64) NOT NULL DEFAULT 'trending',
    usage_count BIGINT NOT NULL DEFAULT 0,
    is_original BOOLEAN NOT NULL DEFAULT FALSE,
    creator_id VARCHAR(64) REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_sounds_usage ON sounds(usage_count DESC);
CREATE INDEX IF NOT EXISTS idx_sounds_category ON sounds(category);

-- Sound Usage
CREATE TABLE IF NOT EXISTS sound_usage (
    id VARCHAR(64) PRIMARY KEY,
    sound_id VARCHAR(64) NOT NULL REFERENCES sounds(id) ON DELETE CASCADE,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    video_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_sound_usage_sound ON sound_usage(sound_id);

-- Sound Favorites
CREATE TABLE IF NOT EXISTS sound_favorites (
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sound_id VARCHAR(64) NOT NULL REFERENCES sounds(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, sound_id)
);

-- Videos
CREATE TABLE IF NOT EXISTS videos (
    id VARCHAR(64) PRIMARY KEY,
    author_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    caption TEXT NOT NULL DEFAULT '',
    sound_id VARCHAR(64) REFERENCES sounds(id) ON DELETE SET NULL,
    sound_title VARCHAR(255) NOT NULL DEFAULT 'Original Sound',
    sound_author VARCHAR(255) NOT NULL DEFAULT 'Unknown Artist',
    source_storage_key TEXT,
    video_url TEXT NOT NULL DEFAULT '',
    thumbnail_url TEXT NOT NULL DEFAULT '',
    cover_res_name VARCHAR(128) DEFAULT 'video_cover_dance',
    width INT NOT NULL DEFAULT 720,
    height INT NOT NULL DEFAULT 1280,
    duration_seconds INT NOT NULL DEFAULT 15,
    aspect_ratio VARCHAR(32) NOT NULL DEFAULT '9:16',
    status VARCHAR(32) NOT NULL DEFAULT 'READY', -- 'UPLOAD_PENDING', 'PROCESSING', 'READY', 'FAILED', 'DELETED'
    visibility VARCHAR(32) NOT NULL DEFAULT 'PUBLIC', -- 'PUBLIC', 'FOLLOWERS_ONLY', 'PRIVATE'
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    allow_comments BOOLEAN NOT NULL DEFAULT TRUE,
    allow_duet BOOLEAN NOT NULL DEFAULT TRUE,
    allow_stitch BOOLEAN NOT NULL DEFAULT TRUE,
    duet_with_video_id VARCHAR(64) REFERENCES videos(id) ON DELETE SET NULL,
    stitch_with_video_id VARCHAR(64) REFERENCES videos(id) ON DELETE SET NULL,
    category VARCHAR(64) NOT NULL DEFAULT 'fyp',
    hashtags TEXT NOT NULL DEFAULT '',
    views_count BIGINT NOT NULL DEFAULT 0,
    likes_count BIGINT NOT NULL DEFAULT 0,
    comments_count BIGINT NOT NULL DEFAULT 0,
    shares_count BIGINT NOT NULL DEFAULT 0,
    saves_count BIGINT NOT NULL DEFAULT 0,
    reposts_count BIGINT NOT NULL DEFAULT 0,
    moderation_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED', -- 'PENDING', 'APPROVED', 'FLAGGED', 'REMOVED'
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_videos_author ON videos(author_id);
CREATE INDEX IF NOT EXISTS idx_videos_status ON videos(status);
CREATE INDEX IF NOT EXISTS idx_videos_category ON videos(category);
CREATE INDEX IF NOT EXISTS idx_videos_created_at ON videos(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_videos_views_likes ON videos(views_count DESC, likes_count DESC);

-- Video Processing Jobs (Worker Queue)
CREATE TABLE IF NOT EXISTS video_processing_jobs (
    id VARCHAR(64) PRIMARY KEY,
    video_id VARCHAR(64) NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    source_storage_key TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'
    attempts INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 3,
    error_message TEXT,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_video_jobs_status ON video_processing_jobs(status);

-- Hashtags
CREATE TABLE IF NOT EXISTS hashtags (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) UNIQUE NOT NULL,
    usage_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_hashtags_usage ON hashtags(usage_count DESC);

-- Video Hashtags Junction
CREATE TABLE IF NOT EXISTS video_hashtags (
    video_id VARCHAR(64) NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    hashtag_id VARCHAR(64) NOT NULL REFERENCES hashtags(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (video_id, hashtag_id)
);

-- Video Views
CREATE TABLE IF NOT EXISTS video_views (
    id VARCHAR(64) PRIMARY KEY,
    video_id VARCHAR(64) NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    viewer_id VARCHAR(64) REFERENCES users(id) ON DELETE SET NULL,
    watch_duration_ms BIGINT NOT NULL DEFAULT 0,
    completion_percent REAL NOT NULL DEFAULT 0,
    is_complete BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_video_views_video ON video_views(video_id);
CREATE INDEX IF NOT EXISTS idx_video_views_viewer ON video_views(viewer_id);

-- Video Likes
CREATE TABLE IF NOT EXISTS video_likes (
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    video_id VARCHAR(64) NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, video_id)
);
CREATE INDEX IF NOT EXISTS idx_video_likes_user ON video_likes(user_id);
CREATE INDEX IF NOT EXISTS idx_video_likes_video ON video_likes(video_id);

-- Video Saves (Bookmarks)
CREATE TABLE IF NOT EXISTS video_saves (
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    video_id VARCHAR(64) NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, video_id)
);
CREATE INDEX IF NOT EXISTS idx_video_saves_user ON video_saves(user_id);

-- Video Reposts
CREATE TABLE IF NOT EXISTS video_reposts (
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    video_id VARCHAR(64) NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, video_id)
);

-- Comments
CREATE TABLE IF NOT EXISTS comments (
    id VARCHAR(64) PRIMARY KEY,
    video_id VARCHAR(64) NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    author_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_comment_id VARCHAR(64) REFERENCES comments(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    likes_count BIGINT NOT NULL DEFAULT 0,
    replies_count INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'VISIBLE', -- 'VISIBLE', 'HIDDEN', 'DELETED'
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_comments_video ON comments(video_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent ON comments(parent_comment_id);
CREATE INDEX IF NOT EXISTS idx_comments_author ON comments(author_id);

-- Comment Likes
CREATE TABLE IF NOT EXISTS comment_likes (
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    comment_id VARCHAR(64) NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, comment_id)
);

-- Follows
CREATE TABLE IF NOT EXISTS follows (
    follower_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id),
    CONSTRAINT chk_not_self_follow CHECK (follower_id <> following_id)
);
CREATE INDEX IF NOT EXISTS idx_follows_follower ON follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following ON follows(following_id);

-- Blocked Users
CREATE TABLE IF NOT EXISTS blocked_users (
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, blocked_user_id),
    CONSTRAINT chk_not_self_block CHECK (user_id <> blocked_user_id)
);

-- Muted Users
CREATE TABLE IF NOT EXISTS muted_users (
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    muted_user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, muted_user_id)
);

-- Direct Conversations
CREATE TABLE IF NOT EXISTS direct_conversations (
    id VARCHAR(64) PRIMARY KEY,
    user_one_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_two_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    last_message_text TEXT DEFAULT '',
    last_message_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_conversation_participants UNIQUE (user_one_id, user_two_id)
);
CREATE INDEX IF NOT EXISTS idx_conversations_participants ON direct_conversations(user_one_id, user_two_id);

-- Direct Messages
CREATE TABLE IF NOT EXISTS direct_messages (
    id VARCHAR(64) PRIMARY KEY,
    conversation_id VARCHAR(64) NOT NULL REFERENCES direct_conversations(id) ON DELETE CASCADE,
    sender_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_messages_conversation ON direct_messages(conversation_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_messages_receiver ON direct_messages(receiver_id, is_read);

-- Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(64) PRIMARY KEY,
    recipient_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL, -- 'like', 'comment', 'reply', 'follow', 'repost', 'mention', 'dm', 'system'
    action_text TEXT NOT NULL,
    target_video_id VARCHAR(64) REFERENCES videos(id) ON DELETE CASCADE,
    target_comment_id VARCHAR(64) REFERENCES comments(id) ON DELETE CASCADE,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(recipient_id, is_read);

-- Drafts (Remote Draft Backup)
CREATE TABLE IF NOT EXISTS drafts (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    caption TEXT DEFAULT '',
    sound_title VARCHAR(255) DEFAULT 'Original Sound',
    sound_author VARCHAR(255) DEFAULT 'Local Audio',
    cover_res_name VARCHAR(128) DEFAULT 'video_cover_dance',
    video_storage_key TEXT,
    duration_seconds INT DEFAULT 15,
    is_private BOOLEAN DEFAULT FALSE,
    allow_comments BOOLEAN DEFAULT TRUE,
    allow_duet BOOLEAN DEFAULT TRUE,
    allow_stitch BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_drafts_user ON drafts(user_id);

-- Search History
CREATE TABLE IF NOT EXISTS search_history (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    query TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_search_history_user ON search_history(user_id, created_at DESC);

-- Analytics Events
CREATE TABLE IF NOT EXISTS analytics_events (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) REFERENCES users(id) ON DELETE SET NULL,
    event_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(64),
    target_type VARCHAR(32), -- 'video', 'profile', 'sound', 'search', 'system'
    category_tag VARCHAR(64) DEFAULT '',
    watch_duration_ms BIGINT DEFAULT 0,
    completion_percent REAL DEFAULT 0,
    metadata JSONB DEFAULT '{}',
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_analytics_event_type ON analytics_events(event_type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_analytics_user ON analytics_events(user_id);

-- Reports & Moderation
CREATE TABLE IF NOT EXISTS reports (
    id VARCHAR(64) PRIMARY KEY,
    reporter_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type VARCHAR(32) NOT NULL, -- 'VIDEO', 'COMMENT', 'USER'
    target_id VARCHAR(64) NOT NULL,
    reason VARCHAR(128) NOT NULL,
    details TEXT DEFAULT '',
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'REVIEWED', 'ACTIONED', 'DISMISSED'
    moderator_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);
