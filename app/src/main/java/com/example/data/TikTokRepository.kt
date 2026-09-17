package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TikTokRepository(private val dao: TikTokDao) {

    val allVideos: Flow<List<VideoEntity>> = dao.getAllVideos()
    val likedVideos: Flow<List<VideoEntity>> = dao.getLikedVideos()
    val bookmarkedVideos: Flow<List<VideoEntity>> = dao.getBookmarkedVideos()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()

    fun getVideosByCategory(category: String): Flow<List<VideoEntity>> {
        return dao.getVideosByCategory(category)
    }

    fun getCommentsForVideo(videoId: Long): Flow<List<CommentEntity>> {
        return dao.getCommentsForVideo(videoId)
    }

    fun getUserVideos(handle: String): Flow<List<VideoEntity>> {
        return dao.getUserVideos(handle)
    }

    suspend fun toggleLike(videoId: Long, currentlyLiked: Boolean) {
        withContext(Dispatchers.IO) {
            val delta = if (currentlyLiked) -1 else 1
            dao.toggleLike(videoId, !currentlyLiked, delta)
        }
    }

    suspend fun toggleBookmark(videoId: Long, currentlyBookmarked: Boolean) {
        withContext(Dispatchers.IO) {
            val delta = if (currentlyBookmarked) -1 else 1
            dao.toggleBookmark(videoId, !currentlyBookmarked, delta)
        }
    }

    suspend fun toggleFollow(authorHandle: String, currentlyFollowing: Boolean) {
        withContext(Dispatchers.IO) {
            dao.toggleFollow(authorHandle, !currentlyFollowing)
        }
    }

    suspend fun addComment(videoId: Long, authorName: String, authorHandle: String, text: String): Long {
        return withContext(Dispatchers.IO) {
            val comment = CommentEntity(
                videoId = videoId,
                authorName = authorName,
                authorHandle = authorHandle,
                content = text,
                timestamp = System.currentTimeMillis()
            )
            val id = dao.insertComment(comment)
            dao.incrementCommentCount(videoId)
            id
        }
    }

    suspend fun toggleCommentLike(commentId: Long, currentlyLiked: Boolean) {
        withContext(Dispatchers.IO) {
            val delta = if (currentlyLiked) -1 else 1
            dao.toggleCommentLike(commentId, !currentlyLiked, delta)
        }
    }

    suspend fun postNewVideo(
        caption: String,
        soundTitle: String,
        soundAuthor: String,
        coverResName: String,
        category: String = "fyp"
    ): Long {
        return withContext(Dispatchers.IO) {
            val newVideo = VideoEntity(
                authorName = "Alex Rivera",
                authorHandle = "@alex_creative",
                caption = caption,
                soundTitle = soundTitle,
                soundAuthor = soundAuthor,
                coverResName = coverResName,
                likesCount = 1,
                commentsCount = 0,
                sharesCount = 0,
                bookmarksCount = 0,
                category = category,
                timestamp = System.currentTimeMillis()
            )
            dao.insertVideo(newVideo)
        }
    }

    suspend fun initializePreloadedData() {
        withContext(Dispatchers.IO) {
            if (dao.getVideoCount() == 0) {
                val seedVideos = listOf(
                    VideoEntity(
                        authorName = "Marcus Ray",
                        authorHandle = "@marcus_moves",
                        caption = "Neon night freestyle in downtown LA 🔥 Can't stop the groove! Rate the footwork 1-10 👇 #streetdance #fyp #freestyle #dancer",
                        soundTitle = "Bassdrop Groove - Marcus Beats",
                        soundAuthor = "Marcus Beats",
                        coverResName = "video_cover_dance",
                        likesCount = 842100,
                        commentsCount = 14200,
                        sharesCount = 49100,
                        bookmarksCount = 31200,
                        isLiked = false,
                        isBookmarked = false,
                        isFollowing = false,
                        category = "fyp"
                    ),
                    VideoEntity(
                        authorName = "Chef Kenji",
                        authorHandle = "@chef_ramen",
                        caption = "48-hour tonkotsu broth from scratch 🍜 Wait for that satisfying egg slice at the end! #ramen #foodtok #satisfying #recipe",
                        soundTitle = "Midnight Kitchen Lofi - ChillHop",
                        soundAuthor = "ChillHop Beats",
                        coverResName = "video_cover_food",
                        likesCount = 1250400,
                        commentsCount = 28900,
                        sharesCount = 112000,
                        bookmarksCount = 98400,
                        isLiked = true,
                        isBookmarked = true,
                        isFollowing = true,
                        category = "fyp"
                    ),
                    VideoEntity(
                        authorName = "Elena & Theo",
                        authorHandle = "@wanderlust_vibes",
                        caption = "Chasing golden hour in Bali 🌅 Tag someone you would run away with to this beach! #travel #sunset #vibes #wanderlust",
                        soundTitle = "Golden Horizon - Acoustic Drift",
                        soundAuthor = "Acoustic Drift",
                        coverResName = "video_cover_travel",
                        likesCount = 631000,
                        commentsCount = 8920,
                        sharesCount = 33400,
                        bookmarksCount = 41200,
                        isLiked = false,
                        isBookmarked = false,
                        isFollowing = false,
                        category = "fyp"
                    ),
                    VideoEntity(
                        authorName = "Alex Rivera",
                        authorHandle = "@alex_creative",
                        caption = "Testing the new 35mm f/1.4 lens in cinematic neon streets 🎥 Which color grade is your favorite? #cinematography #camera #creator #filmmaking",
                        soundTitle = "Cyber Neon Nights - Synthwave",
                        soundAuthor = "Synthwave Audio",
                        coverResName = "video_cover_dance",
                        likesCount = 29400,
                        commentsCount = 812,
                        sharesCount = 1240,
                        bookmarksCount = 2100,
                        isLiked = false,
                        isBookmarked = false,
                        isFollowing = false,
                        category = "following"
                    ),
                    VideoEntity(
                        authorName = "Chef Kenji",
                        authorHandle = "@chef_ramen",
                        caption = "Crispy pork belly crackling sound test 🔊 Put on your headphones! #asmr #satisfying #foodtok #cooking",
                        soundTitle = "Crispy Sizzle ASMR - Kenji Kitchen",
                        soundAuthor = "Kenji Kitchen",
                        coverResName = "video_cover_food",
                        likesCount = 482000,
                        commentsCount = 9410,
                        sharesCount = 21800,
                        bookmarksCount = 34500,
                        isLiked = false,
                        isBookmarked = false,
                        isFollowing = true,
                        category = "following"
                    )
                )
                dao.insertVideos(seedVideos)

                // Add comments for first video
                dao.insertComment(
                    CommentEntity(
                        videoId = 1,
                        authorName = "Jessica Taylor",
                        authorHandle = "@jessica_t",
                        content = "That reverse spin at 0:04 was completely unreal!! 🔥🔥🔥",
                        likesCount = 4210,
                        timestamp = System.currentTimeMillis() - 3600000
                    )
                )
                dao.insertComment(
                    CommentEntity(
                        videoId = 1,
                        authorName = "BeatMaster",
                        authorHandle = "@beat_pro",
                        content = "Track matching the rhythm so perfectly! Cleanest footwork on my fyp today 👏",
                        likesCount = 1890,
                        timestamp = System.currentTimeMillis() - 7200000
                    )
                )
                dao.insertComment(
                    CommentEntity(
                        videoId = 1,
                        authorName = "Liam Walker",
                        authorHandle = "@liam_walk",
                        content = "Bro broke the laws of physics on the freeze 🤯",
                        likesCount = 940,
                        timestamp = System.currentTimeMillis() - 14400000
                    )
                )

                // Add comments for second video
                dao.insertComment(
                    CommentEntity(
                        videoId = 2,
                        authorName = "FoodieGal",
                        authorHandle = "@foodie_queen",
                        content = "The egg yolk was perfection! Need the marinade recipe please!! 🤤",
                        likesCount = 6120,
                        timestamp = System.currentTimeMillis() - 1800000
                    )
                )

                // Seed user profile
                dao.insertUserProfile(UserProfileEntity())
            }
        }
    }
}
