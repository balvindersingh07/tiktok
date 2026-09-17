-- Widen ip_address columns to accommodate proxy chains and IPv6 addresses
ALTER TABLE video_views ALTER COLUMN ip_address TYPE VARCHAR(255);
ALTER TABLE analytics_events ALTER COLUMN ip_address TYPE VARCHAR(255);
ALTER TABLE sessions ALTER COLUMN ip_address TYPE VARCHAR(255);
