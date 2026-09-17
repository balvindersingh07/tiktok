package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VideoEntity::class, CommentEntity::class, UserProfileEntity::class],
    version = 1,
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
                    "tiktok_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
