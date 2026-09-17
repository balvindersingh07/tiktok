package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TikTokDao {

    @Query("SELECT * FROM videos ORDER BY id ASC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE category = :category ORDER BY id ASC")
    fun getVideosByCategory(category: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isLiked = 1 ORDER BY timestamp DESC")
    fun getLikedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE authorHandle = :handle ORDER BY timestamp DESC")
    fun getUserVideos(handle: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET isLiked = :liked, likesCount = likesCount + :delta WHERE id = :id")
    suspend fun toggleLike(id: Long, liked: Boolean, delta: Int)

    @Query("UPDATE videos SET isBookmarked = :bookmarked, bookmarksCount = bookmarksCount + :delta WHERE id = :id")
    suspend fun toggleBookmark(id: Long, bookmarked: Boolean, delta: Int)

    @Query("UPDATE videos SET isFollowing = :following WHERE authorHandle = :authorHandle")
    suspend fun toggleFollow(authorHandle: String, following: Boolean)

    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY timestamp DESC")
    fun getCommentsForVideo(videoId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Query("UPDATE comments SET isLiked = :liked, likesCount = likesCount + :delta WHERE id = :commentId")
    suspend fun toggleCommentLike(commentId: Long, liked: Boolean, delta: Int)

    @Query("UPDATE videos SET commentsCount = commentsCount + 1 WHERE id = :videoId")
    suspend fun incrementCommentCount(videoId: Long)

    @Query("SELECT * FROM user_profile WHERE userId = 'me' LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun getVideoCount(): Int
}
