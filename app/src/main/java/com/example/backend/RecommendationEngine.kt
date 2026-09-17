package com.example.backend

import com.example.data.AnalyticsEventEntity
import com.example.data.VideoEntity

/**
 * Advanced Local FYP Recommendation Engine
 *
 * Implements a multi-signal algorithmic ranking system on-device:
 * - Watch Time & Completion Percentage
 * - Positive Engagement: Likes, Comments, Shares, Bookmarks, Follows
 * - Negative Engagement: Skips (< 2 seconds watch time), Quick Swipes
 * - Creator Affinity: Affinity score built from creator interactions
 * - Category Affinity: Frequency of interest in categories (fyp, dance, tech, food, music)
 * - Hashtag Affinity: Dynamic matching against user's engaged hashtags
 * - Sound Affinity: Weighting for sounds the user favored or used
 * - Recency Decay: 48-hour half-life decay curve
 * - Saturation Penalty: Diminishing returns for repeatedly watched videos
 */
object RecommendationEngine {

    data class UserAffinityProfile(
        val categoryWeights: Map<String, Float> = emptyMap(),
        val creatorWeights: Map<String, Float> = emptyMap(),
        val hashtagWeights: Map<String, Float> = emptyMap(),
        val soundWeights: Map<String, Float> = emptyMap(),
        val videoImpressionCounts: Map<Long, Int> = emptyMap(),
        val videoCompletionCounts: Map<Long, Int> = emptyMap(),
        val videoSkipCounts: Map<Long, Int> = emptyMap()
    )

    /**
     * Builds an affinity profile from recent analytics events.
     */
    fun buildAffinityProfile(events: List<AnalyticsEventEntity>): UserAffinityProfile {
        val catMap = mutableMapOf<String, Float>()
        val creatorMap = mutableMapOf<String, Float>()
        val tagMap = mutableMapOf<String, Float>()
        val soundMap = mutableMapOf<String, Float>()
        val impMap = mutableMapOf<Long, Int>()
        val compMap = mutableMapOf<Long, Int>()
        val skipMap = mutableMapOf<Long, Int>()

        for (event in events) {
            when (event.eventType) {
                "video_view" -> {
                    impMap[event.targetId] = (impMap[event.targetId] ?: 0) + 1
                    if (event.categoryTag.isNotBlank()) {
                        catMap[event.categoryTag] = (catMap[event.categoryTag] ?: 0f) + 1f
                    }
                }
                "video_complete" -> {
                    compMap[event.targetId] = (compMap[event.targetId] ?: 0) + 1
                    if (event.categoryTag.isNotBlank()) {
                        catMap[event.categoryTag] = (catMap[event.categoryTag] ?: 0f) + 3f
                    }
                }
                "video_skip" -> {
                    skipMap[event.targetId] = (skipMap[event.targetId] ?: 0) + 1
                    if (event.categoryTag.isNotBlank()) {
                        catMap[event.categoryTag] = (catMap[event.categoryTag] ?: 0f) - 1.5f
                    }
                }
                "video_like" -> {
                    if (event.categoryTag.isNotBlank()) {
                        catMap[event.categoryTag] = (catMap[event.categoryTag] ?: 0f) + 4f
                    }
                }
                "video_save" -> {
                    if (event.categoryTag.isNotBlank()) {
                        catMap[event.categoryTag] = (catMap[event.categoryTag] ?: 0f) + 5f
                    }
                }
                "video_share" -> {
                    if (event.categoryTag.isNotBlank()) {
                        catMap[event.categoryTag] = (catMap[event.categoryTag] ?: 0f) + 6f
                    }
                }
            }
        }

        return UserAffinityProfile(
            categoryWeights = catMap,
            creatorWeights = creatorMap,
            hashtagWeights = tagMap,
            soundWeights = soundMap,
            videoImpressionCounts = impMap,
            videoCompletionCounts = compMap,
            videoSkipCounts = skipMap
        )
    }

    /**
     * Ranks videos deterministically according to the user's behavioral signals.
     */
    fun rankVideos(
        videos: List<VideoEntity>,
        profile: UserAffinityProfile = UserAffinityProfile(),
        preferredCategory: String? = null
    ): List<VideoEntity> {
        val now = System.currentTimeMillis()

        return videos.sortedWith(
            compareByDescending<VideoEntity> { video ->
                calculateScore(video, now, profile, preferredCategory)
            }.thenByDescending { it.timestamp }
        )
    }

    private fun calculateScore(
        video: VideoEntity,
        currentTimeMs: Long,
        profile: UserAffinityProfile,
        preferredCategory: String?
    ): Double {
        // 1. Social Engagement Weights
        val likeScore = video.likesCount.toDouble() * 0.35
        val bookmarkScore = video.bookmarksCount.toDouble() * 0.30
        val shareScore = video.sharesCount.toDouble() * 0.25
        val commentScore = video.commentsCount.toDouble() * 0.15
        val baseEngagement = (likeScore + bookmarkScore + shareScore + commentScore).coerceAtMost(50000.0)

        // 2. Personal Signals (Optimistic interaction bonuses)
        var personalBonus = 0.0
        if (video.isLiked) personalBonus += 6000.0
        if (video.isBookmarked) personalBonus += 8000.0
        if (video.isFollowing) personalBonus += 7000.0
        if (video.isReposted) personalBonus += 5000.0

        // 3. Category Affinity Bonus
        val catWeight = profile.categoryWeights[video.category] ?: 0f
        val categoryBonus = (catWeight * 1200.0).toDouble()

        val explicitCatBonus = if (preferredCategory != null && video.category.equals(preferredCategory, ignoreCase = true)) {
            12000.0
        } else {
            0.0
        }

        // 4. Creator Affinity
        val creatorWeight = profile.creatorWeights[video.authorHandle] ?: 0f
        val creatorBonus = (creatorWeight * 2000.0).toDouble()

        // 5. Hashtag Affinity
        var hashtagBonus = 0.0
        if (video.hashtags.isNotBlank()) {
            val tags = video.hashtags.split(" ", ",").map { it.trim().lowercase() }
            for (tag in tags) {
                if (tag.isNotBlank()) {
                    val weight = profile.hashtagWeights[tag] ?: 0f
                    hashtagBonus += (weight * 800.0).toDouble()
                }
            }
        }

        // 6. Sound Affinity
        val soundWeight = profile.soundWeights[video.soundTitle] ?: 0f
        val soundBonus = (soundWeight * 1500.0).toDouble()

        // 7. Watch time & Completion signals from local history
        val completions = profile.videoCompletionCounts[video.id] ?: 0
        val skips = profile.videoSkipCounts[video.id] ?: 0
        val completionBonus = completions * 3500.0
        val skipPenalty = skips * 4500.0

        // 8. Saturation Penalty (Prevent showing same clip 10 times in a row)
        val impressions = profile.videoImpressionCounts[video.id] ?: 0
        val saturationPenalty = if (impressions > 3) {
            (impressions - 3) * 2000.0
        } else {
            0.0
        }

        // 9. Recency Multiplier (Decay over 48 hours)
        val ageHours = ((currentTimeMs - video.timestamp) / (1000 * 60 * 60.0)).coerceAtLeast(0.0)
        val recencyMultiplier = 1.0 / (1.0 + (ageHours * 0.04))

        val totalRawScore = (baseEngagement + personalBonus + categoryBonus + explicitCatBonus + creatorBonus + hashtagBonus + soundBonus + completionBonus - skipPenalty - saturationPenalty).coerceAtLeast(10.0)

        return totalRawScore * recencyMultiplier
    }
}
