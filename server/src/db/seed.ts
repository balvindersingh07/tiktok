import { query } from './index.js';
import { hashPassword } from '../utils/password.js';
import { logger } from '../utils/logger.js';

export async function seedInitialDataIfNeeded() {
  try {
    const existingUsers = await query('SELECT COUNT(*) as count FROM users');
    const userCount = parseInt(existingUsers.rows[0]?.count || '0', 10);
    if (userCount > 0) {
      logger.info(`Database already populated with ${userCount} users.`);
      return;
    }

    logger.info('Seeding database with initial creators, videos, sounds, and social content...');

    const defaultPasswordHash = await hashPassword('1234');

    // 1. Seed Users and Profiles
    const seedUsers = [
      {
        id: 'user_alex',
        handle: '@alex_creative',
        email: 'alex@creativelab.io',
        displayName: 'Alex Rivera',
        bio: '✨ Visual Storyteller & Creator | Daily Reels 🚀\n📍 Los Angeles, CA | Collabs: alex@creativelab.io',
        avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80',
        followersCount: 42900,
        followingCount: 184,
        likesCount: 1200000,
        isVerified: true,
      },
      {
        id: 'user_elena',
        handle: '@elena_dance',
        email: 'elena@danceacademy.org',
        displayName: 'Elena Rostova',
        bio: '🩰 Principal Ballerina & Choreographer | NYC 🗽',
        avatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=300&q=80',
        followersCount: 189000,
        followingCount: 210,
        likesCount: 4500000,
        isVerified: true,
      },
      {
        id: 'user_marcus',
        handle: '@marcus_moves',
        email: 'marcus@movestudio.tv',
        displayName: 'Marcus Moves',
        bio: '🕺 Street Dancer, Choreographer & Trendsetter | LA & Tokyo',
        avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80',
        followersCount: 67000,
        followingCount: 95,
        likesCount: 890000,
        isVerified: false,
      },
      {
        id: 'creator_ramen',
        handle: '@chef_ramen',
        email: 'ramen@culinary.kitchen',
        displayName: 'Chef Ramen King',
        bio: '🍜 Master of 18-hour broths & handmade noodles | Tokyo & SF',
        avatarUrl: 'https://images.unsplash.com/photo-1556157382-97eda2d62296?auto=format&fit=crop&w=300&q=80',
        followersCount: 312000,
        followingCount: 48,
        likesCount: 5800000,
        isVerified: true,
      },
      {
        id: 'creator_cyber',
        handle: '@cyber_future',
        email: 'cyber@techlab.io',
        displayName: 'Aria Takahashi',
        bio: '🤖 Creative Technologist & Cyberpunk Visual Artist | AR/VR',
        avatarUrl: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=300&q=80',
        followersCount: 145000,
        followingCount: 112,
        likesCount: 2900000,
        isVerified: true,
      },
    ];

    for (const u of seedUsers) {
      await query(
        `INSERT INTO users (id, handle, email, password_hash, role, is_verified, is_active)
         VALUES ($1, $2, $3, $4, 'USER', $5, TRUE)
         ON CONFLICT (id) DO NOTHING`,
        [u.id, u.handle, u.email, defaultPasswordHash, u.isVerified]
      );

      await query(
        `INSERT INTO profiles (
          user_id, display_name, handle, bio, avatar_url,
          followers_count, following_count, likes_count, is_verified
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
        ON CONFLICT (user_id) DO NOTHING`,
        [u.id, u.displayName, u.handle, u.bio, u.avatarUrl, u.followersCount, u.followingCount, u.likesCount, u.isVerified]
      );

      await query(
        `INSERT INTO notification_preferences (user_id) VALUES ($1) ON CONFLICT DO NOTHING`,
        [u.id]
      );
    }

    // 2. Seed Sounds
    const seedSounds = [
      {
        id: 'snd_1',
        title: 'Electro Funk Shuffle Remix 2026',
        author: 'DJ Neon Grooves',
        audioUrl: '/audio/electro_funk.mp3',
        category: 'dance',
        usageCount: 142000,
        durationSeconds: 15,
      },
      {
        id: 'snd_2',
        title: 'Lofi Kitchen Vibes - Cooking Chill',
        author: 'Ramen Lo-Fi Project',
        audioUrl: '/audio/lofi_chill.mp3',
        category: 'lifestyle',
        usageCount: 89400,
        durationSeconds: 20,
      },
      {
        id: 'snd_3',
        title: 'Synthwave Night Driver (80s Analog)',
        author: 'Cyber Samurai',
        audioUrl: '/audio/synthwave_night.mp3',
        category: 'trending',
        usageCount: 231500,
        durationSeconds: 18,
      },
      {
        id: 'snd_4',
        title: 'Calm Piano Study Session',
        author: 'Acoustic Soul',
        audioUrl: '/audio/calm_piano.mp3',
        category: 'music',
        usageCount: 45000,
        durationSeconds: 30,
      },
    ];

    for (const s of seedSounds) {
      await query(
        `INSERT INTO sounds (id, title, author, audio_url, category, usage_count, duration_seconds)
         VALUES ($1, $2, $3, $4, $5, $6, $7)
         ON CONFLICT (id) DO NOTHING`,
        [s.id, s.title, s.author, s.audioUrl, s.category, s.usageCount, s.durationSeconds]
      );
    }

    // 3. Seed Videos
    const seedVideos = [
      {
        id: '1',
        authorId: 'user_marcus',
        caption: 'Learning the new shuffle choreo in the warehouse studio! 🕺🔥 Tag a friend who needs to try this sequence! #shuffle #dancechallenge #streetstyle #fyp',
        soundId: 'snd_1',
        soundTitle: 'Electro Funk Shuffle Remix 2026',
        soundAuthor: 'DJ Neon Grooves',
        coverResName: '/assets/video_cover_dance.jpg',
        videoUrl: '/assets/video_cover_dance.jpg',
        likesCount: 842500,
        commentsCount: 14200,
        sharesCount: 67300,
        savesCount: 38400,
        viewsCount: 3120000,
        durationSeconds: 15,
        category: 'dance',
        hashtags: '#shuffle #dancechallenge #streetstyle #fyp',
      },
      {
        id: '2',
        authorId: 'creator_ramen',
        caption: 'Secret 18-hour Tonkotsu broth recipe revealed! 🍜✨ The secret ingredient at 0:08 will blow your mind! #ramen #foodietok #cookingtips #chefsecret',
        soundId: 'snd_2',
        soundTitle: 'Lofi Kitchen Vibes - Cooking Chill',
        soundAuthor: 'Ramen Lo-Fi Project',
        coverResName: '/assets/video_cover_food.jpg',
        videoUrl: '/assets/video_cover_food.jpg',
        likesCount: 1250000,
        commentsCount: 28900,
        sharesCount: 112000,
        savesCount: 94500,
        viewsCount: 4890000,
        durationSeconds: 18,
        category: 'food',
        hashtags: '#ramen #foodietok #cookingtips #chefsecret',
      },
      {
        id: '3',
        authorId: 'creator_cyber',
        caption: 'Building an interactive hologram table with WebGL & Depth Sensors in 48 hours! 🤖⚡ #creativecoding #tech #futurevibes #cyberpunk',
        soundId: 'snd_3',
        soundTitle: 'Synthwave Night Driver (80s Analog)',
        soundAuthor: 'Cyber Samurai',
        coverResName: '/assets/video_cover_tech.jpg',
        videoUrl: '/assets/video_cover_tech.jpg',
        likesCount: 620000,
        commentsCount: 8400,
        sharesCount: 43000,
        savesCount: 51200,
        viewsCount: 2450000,
        durationSeconds: 14,
        category: 'tech',
        hashtags: '#creativecoding #tech #futurevibes #cyberpunk',
      },
      {
        id: '4',
        authorId: 'user_elena',
        caption: 'Morning stretching routine before rehearsals 🩰✨ Discipline is choosing what you want most over what you want now. #ballet #routine #mindset',
        soundId: 'snd_4',
        soundTitle: 'Calm Piano Study Session',
        soundAuthor: 'Acoustic Soul',
        coverResName: '/assets/video_cover_nature.jpg',
        videoUrl: '/assets/video_cover_nature.jpg',
        likesCount: 410000,
        commentsCount: 6200,
        sharesCount: 18900,
        savesCount: 33400,
        viewsCount: 1850000,
        durationSeconds: 20,
        category: 'lifestyle',
        hashtags: '#ballet #routine #mindset #fyp',
      },
    ];

    for (const v of seedVideos) {
      await query(
        `INSERT INTO videos (
          id, author_id, caption, sound_id, sound_title, sound_author,
          cover_res_name, video_url, likes_count, comments_count, shares_count,
          saves_count, views_count, duration_seconds, category, hashtags, status
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, 'READY')
        ON CONFLICT (id) DO NOTHING`,
        [
          v.id,
          v.authorId,
          v.caption,
          v.soundId,
          v.soundTitle,
          v.soundAuthor,
          v.coverResName,
          v.videoUrl,
          v.likesCount,
          v.commentsCount,
          v.sharesCount,
          v.savesCount,
          v.viewsCount,
          v.durationSeconds,
          v.category,
          v.hashtags,
        ]
      );
    }

    // 4. Seed Comments
    const seedComments = [
      {
        id: 'c_1',
        videoId: '1',
        authorId: 'user_alex',
        content: 'That footwork transition at 0:06 is INSANE!! 🔥',
        likesCount: 342,
      },
      {
        id: 'c_2',
        videoId: '1',
        authorId: 'user_elena',
        content: 'Clean lines and perfect rhythm! Bravo Marcus! 👏',
        likesCount: 189,
      },
      {
        id: 'c_3',
        videoId: '2',
        authorId: 'user_alex',
        content: 'Making this TONIGHT! Does chicken stock substitute well?',
        likesCount: 89,
      },
    ];

    for (const c of seedComments) {
      await query(
        `INSERT INTO comments (id, video_id, author_id, content, likes_count, status)
         VALUES ($1, $2, $3, $4, $5, 'VISIBLE')
         ON CONFLICT (id) DO NOTHING`,
        [c.id, c.videoId, c.authorId, c.content, c.likesCount]
      );
    }

    // 5. Seed Follows
    await query(
      `INSERT INTO follows (follower_id, following_id)
       VALUES ('user_alex', 'user_elena'), ('user_alex', 'user_marcus')
       ON CONFLICT DO NOTHING`
    );

    // 6. Seed Direct Conversations
    const convId = 'conv_alex_elena';
    await query(
      `INSERT INTO direct_conversations (id, user_one_id, user_two_id, last_message_text)
       VALUES ($1, 'user_alex', 'user_elena', 'Hey Elena! Loved your new rehearsal sequence!')
       ON CONFLICT DO NOTHING`,
      [convId]
    );

    await query(
      `INSERT INTO direct_messages (id, conversation_id, sender_id, receiver_id, content, is_read)
       VALUES
       ('msg_1', $1, 'user_alex', 'user_elena', 'Hey Elena! Loved your new rehearsal sequence!', TRUE),
       ('msg_2', $1, 'user_elena', 'user_alex', 'Thank you Alex! Let us collaborate on the next video!', FALSE)
       ON CONFLICT DO NOTHING`,
      [convId]
    );

    logger.info('Database seeding completed successfully.');
  } catch (err) {
    logger.error('Error during database seeding', err);
  }
}
