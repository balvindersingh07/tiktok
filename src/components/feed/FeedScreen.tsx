import React, { useEffect, useRef } from 'react';
import { Search, Radio, ChevronUp, ChevronDown } from 'lucide-react';
import { useTikTok } from '../../context/TikTokContext';
import { VideoFeedItem } from './VideoFeedItem';

export const FeedScreen: React.FC = () => {
  const {
    displayedFeedVideos,
    activeVideoIndex,
    setActiveVideoIndex,
    feedCategory,
    setFeedCategory,
    setCurrentTab,
    showToast,
  } = useTikTok();

  const containerRef = useRef<HTMLDivElement>(null);

  // Keyboard navigation for feed (ArrowUp / ArrowDown)
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setActiveVideoIndex((prev) => Math.min(displayedFeedVideos.length - 1, prev + 1));
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        setActiveVideoIndex((prev) => Math.max(0, prev - 1));
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [displayedFeedVideos.length, setActiveVideoIndex]);

  const handleNext = () => {
    if (activeVideoIndex < displayedFeedVideos.length - 1) {
      setActiveVideoIndex(activeVideoIndex + 1);
    } else {
      showToast('Reached end of feed! Returning to top');
      setActiveVideoIndex(0);
    }
  };

  const handlePrev = () => {
    if (activeVideoIndex > 0) {
      setActiveVideoIndex(activeVideoIndex - 1);
    }
  };

  const activeVideo = displayedFeedVideos[activeVideoIndex] || displayedFeedVideos[0];

  return (
    <div
      id="feed-screen-container"
      ref={containerRef}
      className="relative w-full h-screen bg-[#0C0A14] overflow-hidden flex flex-col items-center justify-center max-w-md mx-auto"
    >
      {/* Top Header Bar */}
      <header
        id="feed-top-header"
        className="absolute top-0 left-0 right-0 z-30 pt-3 pb-2 px-4 flex items-center justify-between pointer-events-auto bg-gradient-to-b from-black/70 to-transparent"
      >
        {/* LIVE Stream Button */}
        <button
          id="feed-live-btn"
          onClick={() => showToast('LIVE Studio: No broadcasts active in your region')}
          className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-black/40 backdrop-blur-md border border-white/15 text-xs font-semibold text-white/90 hover:bg-black/60 transition"
        >
          <span className="w-2 h-2 rounded-full bg-[#FF2A54] animate-pulse" />
          <Radio className="w-3.5 h-3.5 text-[#FF2A54]" />
          <span>LIVE</span>
        </button>

        {/* Following & For You Tabs */}
        <div className="flex items-center gap-5">
          <button
            id="feed-tab-following"
            onClick={() => setFeedCategory('FOLLOWING')}
            className={`text-[16px] font-bold tracking-tight transition-all relative py-1 ${
              feedCategory === 'FOLLOWING' ? 'text-white' : 'text-white/60 hover:text-white/80'
            }`}
          >
            Following
            {feedCategory === 'FOLLOWING' && (
              <span className="absolute bottom-0 left-1/2 -translate-x-1/2 w-5 h-0.5 bg-[#00F5D4] rounded-full shadow-[0_0_8px_#00F5D4]" />
            )}
          </button>

          <span className="text-white/30 text-sm">|</span>

          <button
            id="feed-tab-foryou"
            onClick={() => setFeedCategory('FOR_YOU')}
            className={`text-[16px] font-bold tracking-tight transition-all relative py-1 ${
              feedCategory === 'FOR_YOU' ? 'text-white' : 'text-white/60 hover:text-white/80'
            }`}
          >
            For You
            {feedCategory === 'FOR_YOU' && (
              <span className="absolute bottom-0 left-1/2 -translate-x-1/2 w-6 h-0.5 bg-gradient-to-r from-[#00F5D4] to-[#FF2A85] rounded-full shadow-[0_0_8px_#FF2A85]" />
            )}
          </button>
        </div>

        {/* Search shortcut */}
        <button
          id="feed-search-btn"
          onClick={() => setCurrentTab('DISCOVER')}
          className="w-8 h-8 rounded-full flex items-center justify-center text-white hover:text-[#00F5D4] transition"
          aria-label="Search videos"
        >
          <Search className="w-5 h-5" />
        </button>
      </header>

      {/* Main Video Viewport */}
      <div className="w-full h-full relative">
        {activeVideo ? (
          <VideoFeedItem video={activeVideo} isActive={true} />
        ) : (
          <div className="w-full h-full flex flex-col items-center justify-center text-center p-6 bg-[#0C0A14]">
            <p className="text-white/70 text-base mb-4">No videos in Following feed yet.</p>
            <button
              onClick={() => setFeedCategory('FOR_YOU')}
              className="px-5 py-2.5 rounded-full rainbow-gradient text-white font-bold text-sm shadow-lg shadow-[#FF2A85]/20"
            >
              Explore For You Feed
            </button>
          </div>
        )}
      </div>

      {/* Desktop Quick Next / Prev Float Controls */}
      <div className="hidden sm:flex absolute right-2 top-1/2 -translate-y-1/2 z-30 flex-col gap-2">
        <button
          onClick={handlePrev}
          disabled={activeVideoIndex === 0}
          className="w-9 h-9 rounded-full bg-black/40 backdrop-blur-md border border-white/10 text-white flex items-center justify-center hover:bg-black/70 disabled:opacity-30 transition"
          title="Previous Video (Arrow Up)"
        >
          <ChevronUp className="w-5 h-5" />
        </button>
        <button
          onClick={handleNext}
          className="w-9 h-9 rounded-full bg-black/40 backdrop-blur-md border border-white/10 text-white flex items-center justify-center hover:bg-black/70 transition"
          title="Next Video (Arrow Down)"
        >
          <ChevronDown className="w-5 h-5" />
        </button>
      </div>
    </div>
  );
};
