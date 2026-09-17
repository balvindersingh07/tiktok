import React, { useState, useRef, useEffect } from 'react';
import { Heart, MessageCircle, Bookmark, Share2, Disc, Play, Volume2, VolumeX, Plus, Check } from 'lucide-react';
import { VideoEntity } from '../../types';
import { useTikTok } from '../../context/TikTokContext';

interface VideoFeedItemProps {
  video: VideoEntity;
  isActive: boolean;
}

export const formatCount = (count: number): string => {
  if (count >= 1000000) {
    return (count / 1000000).toFixed(1) + 'M';
  }
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'K';
  }
  return count.toString();
};

export const VideoFeedItem: React.FC<VideoFeedItemProps> = ({ video, isActive }) => {
  const {
    onLikeVideo,
    onBookmarkVideo,
    onToggleFollow,
    openModal,
    isMuted,
    toggleMute,
    setViewingCreator,
    setCurrentTab,
    allAccounts,
    onRecordVideoView,
  } = useTikTok();

  const [isPlaying, setIsPlaying] = useState<boolean>(true);
  const [progress, setProgress] = useState<number>(0);
  const [hearts, setHearts] = useState<{ id: number; x: number; y: number }[]>([]);
  const videoRef = useRef<HTMLVideoElement>(null);
  const lastTapRef = useRef<number>(0);
  const progressIntervalRef = useRef<number | null>(null);
  const hasRecordedRef = useRef<string | number | null>(null);

  // Sync video play/pause with active state
  useEffect(() => {
    if (!videoRef.current) return;
    if (isActive && isPlaying) {
      videoRef.current.play().catch(() => {});
    } else {
      videoRef.current.pause();
    }
  }, [isActive, isPlaying]);

  // Sync mute state with video element
  useEffect(() => {
    if (videoRef.current) {
      videoRef.current.muted = isMuted;
    }
  }, [isMuted]);

  // Record view on active video
  useEffect(() => {
    if (isActive) {
      setIsPlaying(true);
      if (hasRecordedRef.current !== video.id) {
        hasRecordedRef.current = video.id;
        onRecordVideoView(video.id, 15000);
      }
    } else {
      setIsPlaying(false);
      setProgress(0);
      hasRecordedRef.current = null;
    }
  }, [isActive, video.id, onRecordVideoView]);

  // Simulate video playback progress bar (15s duration)
  useEffect(() => {
    if (isActive && isPlaying) {
      progressIntervalRef.current = window.setInterval(() => {
        setProgress((prev) => {
          if (prev >= 100) return 0;
          return prev + 100 / (15 * 10); // 100ms ticks for 15s
        });
      }, 100);
    } else {
      if (progressIntervalRef.current !== null) {
        clearInterval(progressIntervalRef.current);
        progressIntervalRef.current = null;
      }
    }
    return () => {
      if (progressIntervalRef.current !== null) {
        clearInterval(progressIntervalRef.current);
        progressIntervalRef.current = null;
      }
    };
  }, [isActive, isPlaying]);

  // Tap & Double Tap Handler
  const handleContainerClick = (e: React.MouseEvent<HTMLDivElement>) => {
    const now = Date.now();
    const DOUBLE_TAP_DELAY = 300;

    if (now - lastTapRef.current < DOUBLE_TAP_DELAY) {
      // Double tap -> Like
      const rect = e.currentTarget.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      setHearts((prev) => [...prev, { id: now, x, y }]);
      setTimeout(() => {
        setHearts((prev) => prev.filter((h) => h.id !== now));
      }, 1000);

      if (!video.isLiked) {
        onLikeVideo(video);
      }
    } else {
      // Single tap -> Play / Pause toggle
      setIsPlaying((prev) => !prev);
    }

    lastTapRef.current = now;
  };

  const handleCreatorClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    const creator = allAccounts.find(
      (a) => a.handle.toLowerCase() === video.authorHandle.toLowerCase()
    ) || {
      userId: video.authorId,
      displayName: video.authorName,
      handle: video.authorHandle,
      passwordPin: '1234',
      bio: `Official creator profile for ${video.authorName} 🚀`,
      followingCount: 120,
      followersCount: 54000,
      likesCount: formatCount(video.likesCount * 3),
      avatarUrl: video.authorAvatarUrl || '/assets/tiktok_icon.jpg',
      isVerified: true,
      isPrivate: false,
      allowComments: true,
      allowDuet: true,
      allowStitch: true,
      allowDownloads: true,
      createdAt: Date.now() - 1000 * 60 * 60 * 24 * 30,
    };

    setViewingCreator(creator);
    setCurrentTab('PROFILE');
  };

  const coverUrl = video.coverResName?.startsWith('http') || video.coverResName?.startsWith('/')
    ? video.coverResName
    : `/assets/${video.coverResName}.jpg`;

  const avatarUrl = video.authorAvatarUrl && video.authorAvatarUrl.length > 5
    ? video.authorAvatarUrl
    : 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80';

  return (
    <div
      id={`video-feed-item-${video.id}`}
      className="relative w-full h-full bg-[#0C0A14] overflow-hidden select-none flex flex-col justify-end"
      onClick={handleContainerClick}
    >
      {/* Background Visual (Video Player / Cover Fallback) */}
      <div className="absolute inset-0 z-0">
        {video.videoUrl ? (
          <video
            ref={videoRef}
            src={video.videoUrl}
            poster={coverUrl}
            loop
            playsInline
            muted={isMuted}
            preload="metadata"
            className="w-full h-full object-cover"
          />
        ) : (
          <img
            src={coverUrl}
            alt={video.caption}
            className={`w-full h-full object-cover transition-transform duration-700 ${
              isPlaying ? 'scale-105' : 'scale-100 filter brightness-90'
            }`}
          />
        )}
        {/* Subtle Dark Gradients for contrast */}
        <div className="absolute inset-0 bg-gradient-to-b from-black/40 via-transparent to-black/85 pointer-events-none" />
        <div className="absolute inset-x-0 bottom-0 h-72 bg-gradient-to-t from-black/95 via-black/50 to-transparent pointer-events-none" />
      </div>

      {/* Floating Double Tap Hearts */}
      {hearts.map((h) => (
        <div
          key={h.id}
          className="absolute z-30 pointer-events-none animate-float-heart"
          style={{ left: `${h.x}px`, top: `${h.y}px` }}
        >
          <Heart className="w-24 h-24 text-[#FF2A85] fill-[#FF2A85] drop-shadow-[0_4px_12px_rgba(255,42,133,0.8)]" />
        </div>
      ))}

      {/* Big Play Button Overlay when Paused */}
      {!isPlaying && (
        <div className="absolute inset-0 z-20 flex items-center justify-center pointer-events-none">
          <div className="w-16 h-16 rounded-full bg-black/60 backdrop-blur-sm flex items-center justify-center border border-white/20">
            <Play className="w-8 h-8 text-white fill-white ml-1 opacity-90" />
          </div>
        </div>
      )}

      {/* Top Right Controls (Mute / Sound Toggle) */}
      <div className="absolute top-14 right-4 z-20 flex flex-col items-end gap-2">
        <button
          id={`video-mute-btn-${video.id}`}
          onClick={(e) => {
            e.stopPropagation();
            toggleMute();
          }}
          className="w-10 h-10 rounded-full bg-black/40 backdrop-blur-md flex items-center justify-center border border-white/15 text-white hover:bg-black/60 active:scale-90 transition"
          aria-label={isMuted ? 'Unmute video sound' : 'Mute video sound'}
        >
          {isMuted ? <VolumeX className="w-5 h-5 text-red-400" /> : <Volume2 className="w-5 h-5 text-white" />}
        </button>
      </div>

      {/* Content Layout: Bottom details + Right Actions */}
      <div className="relative z-20 w-full px-4 pb-20 flex items-end justify-between pointer-events-none">
        {/* Left Bottom: Caption, Creator info, Sound tag */}
        <div className="flex-1 mr-4 pointer-events-auto max-w-[76%] text-left">
          {/* Creator handle */}
          <div className="flex items-center gap-2 mb-2 cursor-pointer" onClick={handleCreatorClick}>
            <span className="font-bold text-[16px] text-white hover:underline drop-shadow-md">
              {video.authorName}
            </span>
            <span className="text-[13px] text-white/80 font-normal">
              {video.authorHandle}
            </span>
          </div>

          {/* Caption & Hashtags */}
          <p className="text-[14px] leading-snug text-white/95 drop-shadow-md mb-3 line-clamp-3">
            {video.caption.split(' ').map((word, index) => {
              if (word.startsWith('#')) {
                return (
                  <span key={index} className="font-semibold text-[#00F5D4] hover:underline cursor-pointer mr-1">
                    {word}{' '}
                  </span>
                );
              }
              return word + ' ';
            })}
          </p>

          {/* Duet / Stitch Tag if present */}
          {(video.duetWithVideoId || video.stitchWithVideoId) && (
            <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-black/60 border border-[#00F5D4]/40 text-[#00F5D4] text-xs font-semibold mb-2">
              <span>{video.duetWithVideoId ? '⚡ DUET' : '🎬 STITCH'}</span>
            </div>
          )}

          {/* Sound Marquee Ticker */}
          <div
            id={`sound-ticker-${video.id}`}
            onClick={(e) => {
              e.stopPropagation();
              openModal('SOUND_DETAIL', video);
            }}
            className="flex items-center gap-2 bg-black/40 backdrop-blur-sm border border-white/10 px-3 py-1.5 rounded-full w-fit max-w-[280px] cursor-pointer hover:bg-black/60 transition group"
          >
            <Disc className="w-3.5 h-3.5 text-[#00F5D4] animate-spin-slow shrink-0" />
            <div className="overflow-hidden whitespace-nowrap text-xs text-white/90 font-medium">
              <span className="inline-block group-hover:text-[#00F5D4] transition-colors">
                {video.soundTitle} - {video.soundAuthor}
              </span>
            </div>
          </div>
        </div>

        {/* Right Side Action Column */}
        <div className="flex flex-col items-center gap-4.5 pointer-events-auto pb-1">
          {/* Creator Avatar with Follow '+' button */}
          <div className="relative cursor-pointer group" onClick={handleCreatorClick}>
            <div className="w-12 h-12 rounded-full p-[2px] rainbow-gradient shadow-md">
              <img
                src={avatarUrl}
                alt={video.authorName}
                className="w-full h-full rounded-full object-cover border border-[#0C0A14]"
              />
            </div>
            {!video.isFollowing && (
              <button
                id={`follow-creator-${video.id}`}
                onClick={(e) => {
                  e.stopPropagation();
                  onToggleFollow(video.authorHandle);
                }}
                className="absolute -bottom-1.5 left-1/2 -translate-x-1/2 w-5 h-5 rounded-full bg-[#FF2A54] text-white flex items-center justify-center border-2 border-[#0C0A14] hover:scale-110 active:scale-95 transition"
                aria-label={`Follow ${video.authorName}`}
              >
                <Plus className="w-3 h-3 stroke-[3]" />
              </button>
            )}
            {video.isFollowing && (
              <div className="absolute -bottom-1.5 left-1/2 -translate-x-1/2 w-4 h-4 rounded-full bg-[#00E676] text-white flex items-center justify-center border border-[#0C0A14]">
                <Check className="w-2.5 h-2.5 stroke-[3]" />
              </div>
            )}
          </div>

          {/* Like Button */}
          <button
            id={`like-btn-${video.id}`}
            onClick={(e) => {
              e.stopPropagation();
              onLikeVideo(video);
            }}
            className="flex flex-col items-center group active:scale-75 transition-transform"
            aria-label="Like video"
          >
            <div className="w-10 h-10 rounded-full flex items-center justify-center transition-colors">
              <Heart
                className={`w-8 h-8 drop-shadow-md transition-all ${
                  video.isLiked
                    ? 'text-[#FF2A85] fill-[#FF2A85] scale-110'
                    : 'text-white hover:text-white/80'
                }`}
              />
            </div>
            <span className="text-[12px] font-semibold text-white drop-shadow mt-0.5">
              {formatCount(video.likesCount)}
            </span>
          </button>

          {/* Comments Button */}
          <button
            id={`comment-btn-${video.id}`}
            onClick={(e) => {
              e.stopPropagation();
              openModal('COMMENTS', video);
            }}
            className="flex flex-col items-center group active:scale-75 transition-transform"
            aria-label="View comments"
          >
            <div className="w-10 h-10 rounded-full flex items-center justify-center">
              <MessageCircle className="w-8 h-8 text-white fill-white/10 drop-shadow-md" />
            </div>
            <span className="text-[12px] font-semibold text-white drop-shadow mt-0.5">
              {formatCount(video.commentsCount)}
            </span>
          </button>

          {/* Bookmark Button */}
          <button
            id={`bookmark-btn-${video.id}`}
            onClick={(e) => {
              e.stopPropagation();
              onBookmarkVideo(video);
            }}
            className="flex flex-col items-center group active:scale-75 transition-transform"
            aria-label="Bookmark video"
          >
            <div className="w-10 h-10 rounded-full flex items-center justify-center">
              <Bookmark
                className={`w-8 h-8 drop-shadow-md transition-all ${
                  video.isBookmarked
                    ? 'text-[#FFD600] fill-[#FFD600] scale-110'
                    : 'text-white fill-white/10 hover:text-white/80'
                }`}
              />
            </div>
            <span className="text-[12px] font-semibold text-white drop-shadow mt-0.5">
              {formatCount(video.bookmarksCount)}
            </span>
          </button>

          {/* Share Button */}
          <button
            id={`share-btn-${video.id}`}
            onClick={(e) => {
              e.stopPropagation();
              openModal('SHARE', video);
            }}
            className="flex flex-col items-center group active:scale-75 transition-transform"
            aria-label="Share video"
          >
            <div className="w-10 h-10 rounded-full flex items-center justify-center">
              <Share2 className="w-8 h-8 text-white drop-shadow-md" />
            </div>
            <span className="text-[12px] font-semibold text-white drop-shadow mt-0.5">
              {formatCount(video.sharesCount)}
            </span>
          </button>

          {/* Spinning Vinyl Record (Sound details) */}
          <div
            id={`vinyl-disc-${video.id}`}
            onClick={(e) => {
              e.stopPropagation();
              openModal('SOUND_DETAIL', video);
            }}
            className="mt-2 relative cursor-pointer active:scale-90 transition-transform"
          >
            <div
              className={`w-11 h-11 rounded-full bg-[#161324] border-[3px] border-[#2A2440] p-1 shadow-lg flex items-center justify-center ${
                isPlaying ? 'animate-spin-slow' : ''
              }`}
            >
              <div className="w-5 h-5 rounded-full rainbow-gradient flex items-center justify-center">
                <Disc className="w-3 h-3 text-white" />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Progress Scrubber Bar */}
      <div className="absolute bottom-[60px] left-0 right-0 h-1 bg-white/20 z-30 overflow-hidden">
        <div
          className="h-full bg-gradient-to-r from-[#00F5D4] via-[#00B0FF] to-[#FF2A85] transition-all duration-100 ease-linear"
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  );
};
