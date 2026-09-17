import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { getDb, query } from './index.js';
import bcrypt from 'bcryptjs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export async function runMigrations(): Promise<void> {
  await getDb();

  // Create migrations tracker table
  await query(`
    CREATE TABLE IF NOT EXISTS schema_migrations (
      version VARCHAR(255) PRIMARY KEY,
      applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
  `);

  const migrationsDir = path.resolve(__dirname, '../../migrations');
  if (!fs.existsSync(migrationsDir)) {
    return;
  }

  const files = fs.readdirSync(migrationsDir).filter((f) => f.endsWith('.sql')).sort();

  for (const file of files) {
    const check = await query('SELECT version FROM schema_migrations WHERE version = $1', [file]);
    if (check.rows.length === 0) {
      console.log(`Applying migration: ${file}`);
      const sql = fs.readFileSync(path.join(migrationsDir, file), 'utf8');
      
      // Split statements cleanly
      const statements = sql
        .split(/;\s*$/m)
        .map((s) => s.trim())
        .filter((s) => s.length > 0);

      for (const statement of statements) {
        await query(statement);
      }

      await query('INSERT INTO schema_migrations (version) VALUES ($1)', [file]);
      console.log(`Migration ${file} applied successfully.`);
    }
  }

  // Seed default data if database is fresh
  await seedInitialData();
}

async function seedInitialData(): Promise<void> {
  // Check if system default creators exist
  const alexUser = await query("SELECT id FROM users WHERE handle = '@alex_creative'");
  if (alexUser.rows.length === 0) {
    const salt = await bcrypt.genSalt(10);
    const pinHash = await bcrypt.hash('1234', salt);

    // Create Alex Rivera
    await query(`
      INSERT INTO users (id, handle, email, password_hash, role, is_verified)
      VALUES ($1, $2, $3, $4, 'USER', TRUE)
      ON CONFLICT (handle) DO NOTHING;
    `, ['user_alex_001', '@alex_creative', 'alex@tashan.app', pinHash]);

    await query(`
      INSERT INTO profiles (user_id, display_name, handle, bio, is_verified, following_count, followers_count, likes_count, videos_count)
      VALUES ($1, 'Alex Rivera', '@alex_creative', '✨ Visual Storyteller & Creator | Daily Reels 🚀\n📍 Los Angeles, CA | Collabs: alex@creativelab.io', TRUE, 184, 42900, 1200000, 3)
      ON CONFLICT (user_id) DO NOTHING;
    `, ['user_alex_001']);

    await query(`
      INSERT INTO notification_preferences (user_id) VALUES ($1) ON CONFLICT DO NOTHING;
    `, ['user_alex_001']);

    // Create Priya Sharma
    await query(`
      INSERT INTO users (id, handle, email, password_hash, role, is_verified)
      VALUES ($1, $2, $3, $4, 'USER', TRUE)
      ON CONFLICT (handle) DO NOTHING;
    `, ['user_priya_002', '@priya_beats', 'priya@tashan.app', pinHash]);

    await query(`
      INSERT INTO profiles (user_id, display_name, handle, bio, is_verified, following_count, followers_count, likes_count, videos_count)
      VALUES ($1, 'Priya Sharma', '@priya_beats', '🎵 Bollywood Choreographer & Beats Producer\nMumbai ↔ Delhi | Dance is Life 💃', TRUE, 312, 128500, 3400000, 4)
      ON CONFLICT (user_id) DO NOTHING;
    `, ['user_priya_002']);

    await query(`
      INSERT INTO notification_preferences (user_id) VALUES ($1) ON CONFLICT DO NOTHING;
    `, ['user_priya_002']);

    // Create Marcus Tech
    await query(`
      INSERT INTO users (id, handle, email, password_hash, role, is_verified)
      VALUES ($1, $2, $3, $4, 'USER', FALSE)
      ON CONFLICT (handle) DO NOTHING;
    `, ['user_marcus_003', '@marcus_tech', 'marcus@tashan.app', pinHash]);

    await query(`
      INSERT INTO profiles (user_id, display_name, handle, bio, is_verified, following_count, followers_count, likes_count, videos_count)
      VALUES ($1, 'Marcus Vance', '@marcus_tech', '⚡ Cutting-Edge Android & AI Gadgets\nDaily teardowns and quick reviews 📱', FALSE, 94, 21400, 480000, 2)
      ON CONFLICT (user_id) DO NOTHING;
    `, ['user_marcus_003']);

    await query(`
      INSERT INTO notification_preferences (user_id) VALUES ($1) ON CONFLICT DO NOTHING;
    `, ['user_marcus_003']);
  }

  // Seed default sounds
  const soundCheck = await query('SELECT COUNT(*) as count FROM sounds');
  if (parseInt(soundCheck.rows[0].count, 10) === 0) {
    const sounds = [
      { id: 'snd_001', title: 'Cyber Pulse (Original Mix)', author: 'Kavinsky BeatLab', duration: 30, category: 'trending' },
      { id: 'snd_002', title: 'Desi Dhol Trap Drop', author: 'DJ Amit Mumbai', duration: 25, category: 'dance' },
      { id: 'snd_003', title: 'Sunset Chill & Lofi Synth', author: 'Chroma Horizon', duration: 45, category: 'lofi' },
      { id: 'snd_004', title: 'Acoustic Morning Breeze', author: 'Elena Rostova', duration: 20, category: 'acoustic' },
      { id: 'snd_005', title: 'Hyperpop Energy Glitch', author: 'Neon Samurai', duration: 15, category: 'trending' },
      { id: 'snd_006', title: 'Bassline Groove 2026', author: 'SubZero Sound', duration: 28, category: 'dance' },
    ];

    for (const s of sounds) {
      await query(`
        INSERT INTO sounds (id, title, author, duration_seconds, audio_url, category, usage_count)
        VALUES ($1, $2, $3, $4, $5, $6, $7)
        ON CONFLICT DO NOTHING;
      `, [s.id, s.title, s.author, s.duration, `/cdn/sounds/${s.id}.mp3`, s.category, Math.floor(Math.random() * 50000) + 1200]);
    }
  }

  // Seed default seed videos if empty
  const videoCheck = await query('SELECT COUNT(*) as count FROM videos');
  if (parseInt(videoCheck.rows[0].count, 10) === 0) {
    const sampleVideos = [
      {
        id: 'vid_001',
        authorId: 'user_priya_002',
        caption: 'High-voltage energy at Mumbai Street Fest! 💃🕺 Did we nail the synchronized drop? #dance #bollywood #fest #energy',
        soundTitle: 'Desi Dhol Trap Drop',
        soundAuthor: 'DJ Amit Mumbai',
        coverResName: 'video_cover_dance',
        category: 'dance',
        hashtags: '#dance #bollywood #fest #energy',
        views: 89400,
        likes: 18400,
        comments: 1240,
        shares: 640,
        saves: 890,
        duration: 22,
      },
      {
        id: 'vid_002',
        authorId: 'user_marcus_003',
        caption: 'Unboxing the transparent cyberpunk foldable phone with active liquid-cooling! 🤯⚡ #tech #gadgets #cyberpunk #future',
        soundTitle: 'Cyber Pulse (Original Mix)',
        soundAuthor: 'Kavinsky BeatLab',
        coverResName: 'video_cover_tech',
        category: 'tech',
        hashtags: '#tech #gadgets #cyberpunk #future',
        views: 62100,
        likes: 9800,
        comments: 630,
        shares: 310,
        saves: 420,
        duration: 18,
      },
      {
        id: 'vid_003',
        authorId: 'user_alex_001',
        caption: 'Golden hour drone cinematic across the Pacific Coast cliffs 🌅 Golden light hits different. #nature #sunset #cinematic #travel',
        soundTitle: 'Sunset Chill & Lofi Synth',
        soundAuthor: 'Chroma Horizon',
        coverResName: 'video_cover_sunset',
        category: 'fyp',
        hashtags: '#nature #sunset #cinematic #travel',
        views: 142000,
        likes: 31200,
        comments: 2150,
        shares: 1890,
        saves: 3410,
        duration: 24,
      },
      {
        id: 'vid_004',
        authorId: 'user_priya_002',
        caption: 'Street food masters making 50 layered parathas in 60 seconds flat! 🤤🔥 #food #streetfood #delhi #cooking',
        soundTitle: 'Desi Dhol Trap Drop',
        soundAuthor: 'DJ Amit Mumbai',
        coverResName: 'video_cover_food',
        category: 'food',
        hashtags: '#food #streetfood #delhi #cooking',
        views: 210000,
        likes: 45600,
        comments: 3820,
        shares: 4120,
        saves: 5890,
        duration: 26,
      },
      {
        id: 'vid_005',
        authorId: 'user_alex_001',
        caption: 'Urban night photography tutorial: capturing neon light trails with manual shutter! 📸✨ #photography #neon #creative #tutorials',
        soundTitle: 'Hyperpop Energy Glitch',
        soundAuthor: 'Neon Samurai',
        coverResName: 'video_cover_urban',
        category: 'trending',
        hashtags: '#photography #neon #creative #tutorials',
        views: 78500,
        likes: 14200,
        comments: 980,
        shares: 520,
        saves: 1140,
        duration: 20,
      }
    ];

    for (const v of sampleVideos) {
      await query(`
        INSERT INTO videos (
          id, author_id, caption, sound_title, sound_author,
          cover_res_name, category, hashtags, views_count, likes_count,
          comments_count, shares_count, saves_count, duration_seconds, status
        ) VALUES (
          $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, 'READY'
        ) ON CONFLICT DO NOTHING;
      `, [
        v.id, v.authorId, v.caption, v.soundTitle, v.soundAuthor,
        v.coverResName, v.category, v.hashtags, v.views, v.likes,
        v.comments, v.shares, v.saves, v.duration
      ]);

      // Seed sample comments
      await query(`
        INSERT INTO comments (id, video_id, author_id, content, likes_count)
        VALUES 
          ($1, $2, 'user_alex_001', 'This is legendary! The editing is so smooth 🔥', 142),
          ($3, $2, 'user_marcus_003', 'That drop at 0:08 was unbelievable 👏', 89)
        ON CONFLICT DO NOTHING;
      `, [`cmt_${v.id}_1`, v.id, `cmt_${v.id}_2`]);
    }
  }
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  runMigrations()
    .then(() => {
      console.log('Database migrations completed.');
      process.exit(0);
    })
    .catch((err) => {
      console.error('Migration failed:', err);
      process.exit(1);
    });
}
