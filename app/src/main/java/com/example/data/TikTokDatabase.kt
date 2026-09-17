package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VideoEntity::class,
        CommentEntity::class,
        UserProfileEntity::class,
        NotificationEntity::class,
        SoundEntity::class,
        AnalyticsEventEntity::class,
        ActiveSessionEntity::class,
        DraftEntity::class,
        FollowEntity::class,
        DirectMessageEntity::class,
        SearchHistoryEntity::class,
        BlockedUserEntity::class,
        MutedUserEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class TikTokDatabase : RoomDatabase() {
    abstract fun tikTokDao(): TikTokDao

    companion object {
        @Volatile
        private var INSTANCE: TikTokDatabase? = null

        fun getDatabase(context: Context): TikTokDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TikTokDatabase::class.java,
                    "tiktok_local_backend.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
