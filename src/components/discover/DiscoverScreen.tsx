import React, { useState, useMemo } from 'react';
import { Search, X, QrCode, Play, Flame, Music, Hash, UserCheck } from 'lucide-react';
import { useTikTok } from '../../context/TikTokContext';
import { formatCount } from '../feed/VideoFeedItem';

interface TrendingTag {
  name: string;
  views: string;
  description: string;
}

export const DiscoverScreen: React.FC = () => {
  const {
    videos,
    jumpToVideo,
    searchQuery,
    setSearchQuery,
    searchHistory,
    onAddSearchQuery,
    onClearSearchHistory,
    allAccounts,
    sounds,
    openModal,
    setViewingCreator,
    setCurrentTab,
  } = useTikTok();

  const [activeFilter, setActiveFilter] = useState<'ALL' | 'VIDEOS' | 'USERS' | 'SOUNDS'>('ALL');

  const trendingTags: TrendingTag[] = [
    { name: 'StreetDance', views: '8.4B', description: 'Trending breakbeats & street styles' },
    { name: 'FoodieTashan', views: '14.2B', description: 'Viral culinary recipes & reviews' },
    { name: 'TravelVibes', views: '5.1B', description: 'Hidden paradises & wanderlust' },
    { name: 'DailyComedy', views: '12.8B', description: 'Hilarious sketches & pets' },
    { name: 'TechRevealed', views: '3.2B', description: 'Futuristic hardware unboxings' },
  ];

  const filteredVideos = useMemo(() => {
    if (!searchQuery.trim()) return videos;
    const q = searchQuery.toLowerCase();
    return videos.filter(
      (v) =>
        v.caption.toLowerCase().includes(q) ||
        v.authorName.toLowerCase().includes(q) ||
        v.authorHandle.toLowerCase().includes(q) ||
        v.soundTitle.toLowerCase().includes(q) ||
        v.hashtags.toLowerCase().includes(q)
    );
  }, [videos, searchQuery]);

  const filteredUsers = useMemo(() => {
    if (!searchQuery.trim()) return allAccounts;
    const q = searchQuery.toLowerCase();
    return allAccounts.filter(
      (u) =>
        u.displayName.toLowerCase().includes(q) ||
        u.handle.toLowerCase().includes(q) ||
        u.bio.toLowerCase().includes(q)
    );
  }, [allAccounts, searchQuery]);

  const filteredSounds = useMemo(() => {
    if (!searchQuery.trim()) return sounds;
    const q = searchQuery.toLowerCase();
    return sounds.filter(
      (s) => s.title.toLowerCase().includes(q) || s.author.toLowerCase().includes(q)
    );
  }, [sounds, searchQuery]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      onAddSearchQuery(searchQuery.trim());
    }
  };

  const handleTagClick = (tagName: string) => {
    setSearchQuery(`#${tagName}`);
    onAddSearchQuery(`#${tagName}`);
  };

  return (
    <div
      id="discover-screen"
      className="w-full h-screen bg-[#0C0A14] text-white flex flex-col pb-16 overflow-y-auto no-scrollbar max-w-md mx-auto"
    >
      {/* Top Search Bar */}
      <header className="sticky top-0 z-30 bg-[#0C0A14]/95 backdrop-blur-md px-4 pt-3 pb-2 border-b border-[#1F1B33]">
        <form onSubmit={handleSearchSubmit} className="flex items-center gap-2">
          <div className="flex-1 relative flex items-center bg-[#161324] rounded-full border border-[#322C52] px-3.5 py-2">
            <Search className="w-4 h-4 text-white/50 shrink-0 mr-2" />
            <input
              id="discover-search-input"
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search creators, sounds, #tags"
              className="w-full bg-transparent text-sm text-white placeholder:text-white/40 focus:outline-none"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery('')}
                className="text-white/50 hover:text-white ml-1 p-0.5"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </div>
          <button
            type="button"
            onClick={() => openModal('VIDEO_QR', videos[0])}
            className="w-9 h-9 rounded-full bg-[#161324] border border-[#322C52] flex items-center justify-center text-white hover:text-[#00F5D4] transition shrink-0"
            title="Scan QR Code"
          >
            <QrCode className="w-4 h-4" />
          </button>
        </form>

        {/* Filter Pills */}
        <div className="flex items-center gap-2 mt-2.5 overflow-x-auto no-scrollbar pb-1">
          {(['ALL', 'VIDEOS', 'USERS', 'SOUNDS'] as const).map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveFilter(tab)}
              className={`px-3 py-1 rounded-full text-xs font-semibold whitespace-nowrap transition-colors ${
                activeFilter === tab
                  ? 'bg-gradient-to-r from-[#00F5D4] to-[#FF2A85] text-white shadow-sm'
                  : 'bg-[#161324] text-white/60 hover:text-white border border-[#1F1B33]'
              }`}
            >
              {tab.charAt(0) + tab.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 px-3 py-3 space-y-4">
        {/* Trending Hashtags Carousel */}
        {!searchQuery && (
          <section id="trending-hashtags-section">
            <div className="flex items-center gap-1.5 mb-2 px-1">
              <Flame className="w-4 h-4 text-[#FF2A54]" />
              <h2 className="text-xs font-bold uppercase tracking-wider text-white/70">
                Trending on Tashan
              </h2>
            </div>
            <div className="flex gap-2 overflow-x-auto no-scrollbar pb-1">
              {trendingTags.map((tag) => (
                <div
                  key={tag.name}
                  onClick={() => handleTagClick(tag.name)}
                  className="flex-shrink-0 bg-[#161324] hover:bg-[#1F1B33] border border-[#322C52] rounded-xl p-2.5 w-44 cursor-pointer transition"
                >
                  <div className="flex items-center gap-1 text-[#00F5D4] font-bold text-sm">
                    <Hash className="w-3.5 h-3.5" />
                    <span>{tag.name}</span>
                  </div>
                  <p className="text-[11px] text-white/50 truncate mt-0.5">{tag.description}</p>
                  <span className="inline-block mt-2 text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[#322C52]/60 text-white/80">
                    {tag.views} views
                  </span>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Search History Chips if query is empty and history exists */}
        {!searchQuery && searchHistory.length > 0 && (
          <section id="search-history-section" className="px-1">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-white/60">Recent Searches</span>
              <button
                onClick={onClearSearchHistory}
                className="text-[11px] text-[#FF2A85] hover:underline"
              >
                Clear
              </button>
            </div>
            <div className="flex flex-wrap gap-1.5">
              {searchHistory.map((query, i) => (
                <button
                  key={i}
                  onClick={() => setSearchQuery(query)}
                  className="px-2.5 py-1 rounded-full bg-[#161324] border border-[#1F1B33] text-xs text-white/80 hover:border-[#00F5D4] transition"
                >
                  {query}
                </button>
              ))}
            </div>
          </section>
        )}

        {/* Users results if USERS or ALL active and matching query */}
        {(activeFilter === 'USERS' || (activeFilter === 'ALL' && searchQuery)) && (
          <section id="matching-creators-section" className="px-1">
            <h3 className="text-xs font-bold uppercase tracking-wider text-white/60 mb-2">
              Creators ({filteredUsers.length})
            </h3>
            <div className="space-y-2">
              {filteredUsers.slice(0, 4).map((user) => (
                <div
                  key={user.userId}
                  onClick={() => {
                    setViewingCreator(user);
                    setCurrentTab('PROFILE');
                  }}
                  className="flex items-center justify-between p-2.5 rounded-xl bg-[#161324] border border-[#1F1B33] cursor-pointer hover:border-[#322C52] transition"
                >
                  <div className="flex items-center gap-3">
                    <img
                      src={user.avatarUrl}
                      alt={user.displayName}
                      className="w-10 h-10 rounded-full object-cover border border-[#322C52]"
                    />
                    <div>
                      <h4 className="text-sm font-bold text-white flex items-center gap-1">
                        {user.displayName}
                        {user.isVerified && <UserCheck className="w-3.5 h-3.5 text-[#00F5D4]" />}
                      </h4>
                      <p className="text-xs text-white/50">{user.handle}</p>
                    </div>
                  </div>
                  <span className="text-xs text-white/60 font-medium">
                    {formatCount(user.followersCount)} followers
                  </span>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Sounds results if SOUNDS active */}
        {(activeFilter === 'SOUNDS' || (activeFilter === 'ALL' && searchQuery && filteredSounds.length > 0)) && (
          <section id="matching-sounds-section" className="px-1">
            <h3 className="text-xs font-bold uppercase tracking-wider text-white/60 mb-2">
              Sounds ({filteredSounds.length})
            </h3>
            <div className="space-y-2">
              {filteredSounds.slice(0, 3).map((sound) => (
                <div
                  key={sound.id}
                  onClick={() => openModal('SOUND_DETAIL', videos.find((v) => v.soundTitle === sound.title) || videos[0])}
                  className="flex items-center justify-between p-2.5 rounded-xl bg-[#161324] border border-[#1F1B33] cursor-pointer hover:border-[#00F5D4]/40 transition"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-full bg-[#1F1B33] flex items-center justify-center text-[#00F5D4]">
                      <Music className="w-4 h-4" />
                    </div>
                    <div>
                      <h4 className="text-sm font-bold text-white line-clamp-1">{sound.title}</h4>
                      <p className="text-xs text-white/50">{sound.author}</p>
                    </div>
                  </div>
                  <span className="text-xs text-[#00F5D4] font-semibold">
                    {formatCount(sound.usageCount)} uses
                  </span>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Videos Grid */}
        {(activeFilter === 'VIDEOS' || activeFilter === 'ALL') && (
          <section id="videos-bento-grid">
            <h3 className="text-xs font-bold uppercase tracking-wider text-white/60 mb-2 px-1">
              Trending Videos ({filteredVideos.length})
            </h3>
            {filteredVideos.length === 0 ? (
              <div className="text-center py-12 text-white/50 text-sm">
                No videos found matching &quot;{searchQuery}&quot;
              </div>
            ) : (
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                {filteredVideos.map((video) => (
                  <div
                    key={video.id}
                    id={`discover-video-card-${video.id}`}
                    onClick={() => jumpToVideo(video.id)}
                    className="group relative aspect-[9/16] rounded-xl overflow-hidden bg-[#161324] border border-[#1F1B33] hover:border-[#00F5D4]/60 cursor-pointer transition-all hover:scale-[1.02]"
                  >
                    {/* Cover image */}
                    <img
                      src={video.coverResName}
                      alt={video.caption}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/20 to-transparent pointer-events-none" />

                    {/* Play count badge */}
                    <div className="absolute top-2 left-2 flex items-center gap-1 bg-black/50 backdrop-blur-sm px-2 py-0.5 rounded-md text-[11px] font-semibold text-white/90">
                      <Play className="w-3 h-3 fill-white/80" />
                      <span>{formatCount(video.viewsCount)}</span>
                    </div>

                    {/* Bottom Author & Caption snippet */}
                    <div className="absolute bottom-2 left-2 right-2 text-left pointer-events-none">
                      <p className="text-xs font-bold text-white line-clamp-2 drop-shadow">
                        {video.caption}
                      </p>
                      <span className="text-[11px] text-white/70 block mt-1">
                        {video.authorHandle}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
};
