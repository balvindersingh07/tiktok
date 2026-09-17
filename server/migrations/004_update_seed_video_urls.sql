-- Migration 004: Update seed videos to point to real remote MP4 media files
UPDATE videos
SET video_url = '/storage/videos/' || id || '.mp4',
    thumbnail_url = '/assets/' || cover_res_name || '.jpg'
WHERE (video_url = '' OR video_url IS NULL OR video_url LIKE '%sample_clip%')
  AND id IN ('vid_001', 'vid_002', 'vid_003', 'vid_004', 'vid_005');
