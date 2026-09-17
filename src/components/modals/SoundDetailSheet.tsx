import React, { useState } from 'react';
import { X, Play, Pause, Bookmark, Video, Disc } from 'lucide-react';
import { VideoEntity, SoundEntity } from '../../types';
import { useTikTok } from '../../context/TikTokContext';
import { soundSynth } from '../../utils/audioSynth';
import { formatCount } from '../feed/VideoFeedItem';

interface SoundDetailSheetProps {
  video: VideoEntity;
  onClose: () => void;
}

export const SoundDetailSheet: React.FC<SoundDetailSheetProps> = ({ video, onClose }) => {
  const {
    sounds,
    videos,
    setSelectedSoundForCreation,
    setCurrentTab,
    jumpToVideo,
    showToast,
  } = useTikTok();

  const [isPlayingPreview, setIsPlayingPreview] = useState<boolean>(false);
  const [isFavorited, setIsFavorited] = useState<boolean>(false);

  // Find matching sound entity
  const sound: SoundEntity =
    sounds.find((s) => s.title.toLowerCase() === video.soundTitle.toLowerCase()) || {
      id: 999,
      title: video.soundTitle,
      author: video.soundAuthor,
      durationSeconds: 15,
      usageCount: 245000,
      isFavorite: false,
      category: 'Synthwave Beat',
    };

  // Videos sharing this sound
  const matchingVideos = videos.filter(
    (v) => v.soundTitle.toLowerCase() === sound.title.toLowerCase()
  );

  const togglePreview = () => {
    if (isPlayingPreview) {
      soundSynth.stop();
      setIsPlayingPreview(false);
    } else {
      soundSynth.playSound(sound.title);
      setIsPlayingPreview(true);
      setTimeout(() => {
        setIsPlayingPreview(false);
      }, 5000);
    }
  };

  const handleUseSound = () => {
    setSelectedSoundForCreation(sound);
    onClose();
    setCurrentTab('CREATE');
    showToast(`Using sound: "${sound.title}"`);
  };

  return (
    <div
      id="sound-detail-sheet-overlay"
      className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-end justify-center p-0 max-w-md mx-auto"
      onClick={onClose}
    >
      <div
        id="sound-detail-sheet-container"
        onClick={(e) => e.stopPropagation()}
        className="w-full bg-[#161324] border-t border-[#322C52] rounded-t-3xl p-5 max-h-[85vh] overflow-y-auto no-scrollbar shadow-2xl space-y-4"
      >
        {/* Header with Close */}
        <div className="flex items-center justify-between">
          <div className="w-6" />
          <h3 className="text-sm font-bold text-white uppercase tracking-wider">Original Sound</h3>
          <button onClick={onClose} className="p-1 rounded-full text-white/60 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Vinyl & Sound Meta */}
        <div className="flex items-center gap-4 py-2">
          <div
            onClick={togglePreview}
            className="relative cursor-pointer group"
          >
            <div
              className={`w-20 h-20 rounded-full bg-[#0C0A14] border-4 border-[#322C52] p-1 flex items-center justify-center ${
                isPlayingPreview ? 'animate-spin-slow shadow-lg shadow-[#00F5D4]/40' : ''
              }`}
            >
              <div className="w-9 h-9 rounded-full rainbow-gradient flex items-center justify-center">
                <Disc className="w-5 h-5 text-white" />
              </div>
            </div>
            <div className="absolute inset-0 flex items-center justify-center bg-black/30 rounded-full group-hover:bg-black/50 transition">
              {isPlayingPreview ? (
                <Pause className="w-6 h-6 text-white" />
              ) : (
                <Play className="w-6 h-6 text-white fill-white ml-0.5" />
              )}
            </div>
          </div>

          <div className="flex-1">
            <h2 className="text-base font-extrabold text-white line-clamp-1">{sound.title}</h2>
            <p className="text-xs text-white/60 mt-0.5 font-medium">{sound.author}</p>
            <p className="text-[11px] text-[#00F5D4] font-semibold mt-1">
              {formatCount(sound.usageCount)} videos
            </p>
          </div>
        </div>

        {/* Action Buttons: Add to Favorites & Use this Sound */}
        <div className="flex items-center gap-2 pt-2">
          <button
            onClick={() => {
              setIsFavorited((prev) => !prev);
              showToast(isFavorited ? 'Removed sound from favorites' : 'Added sound to favorites');
            }}
            className={`p-3 rounded-2xl border transition flex items-center justify-center ${
              isFavorited
                ? 'bg-[#FFD600]/20 border-[#FFD600] text-[#FFD600]'
                : 'bg-[#1F1B33] border-[#322C52] text-white hover:border-white/40'
            }`}
            title="Add sound to favorites"
          >
            <Bookmark className={`w-5 h-5 ${isFavorited ? 'fill-[#FFD600]' : ''}`} />
          </button>

          <button
            id="sound-use-sound-cta-btn"
            onClick={handleUseSound}
            className="flex-1 py-3 px-4 rounded-2xl rainbow-gradient text-white font-bold text-xs flex items-center justify-center gap-2 shadow-lg shadow-[#FF2A85]/20 hover:scale-[1.02] active:scale-95 transition"
          >
            <Video className="w-4 h-4" />
            <span>Use this sound</span>
          </button>
        </div>

        {/* Video Grid using this sound */}
        <div className="pt-3 border-t border-[#1F1B33]">
          <h4 className="text-xs font-semibold text-white/60 mb-3 uppercase tracking-wider">
            Popular videos with this sound
          </h4>
          <div className="grid grid-cols-3 gap-2">
            {(matchingVideos.length > 0 ? matchingVideos : videos.slice(0, 3)).map((v) => (
              <div
                key={v.id}
                onClick={() => {
                  onClose();
                  jumpToVideo(v.id);
                }}
                className="relative aspect-[9/16] rounded-xl overflow-hidden bg-black cursor-pointer hover:border border-[#00F5D4] transition"
              >
                <img src={v.coverResName} alt="Video" className="w-full h-full object-cover" />
                <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent pointer-events-none" />
                <span className="absolute bottom-1 left-1.5 text-[10px] font-bold text-white">
                  {formatCount(v.viewsCount)}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
