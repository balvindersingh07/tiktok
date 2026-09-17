import React from 'react';
import {
  X,
  Repeat,
  Share,
  Copy,
  Download,
  Zap,
  Film,
  QrCode,
  Check
} from 'lucide-react';
import { VideoEntity } from '../../types';
import { useTikTok } from '../../context/TikTokContext';

interface ShareBottomSheetProps {
  video: VideoEntity;
  onClose: () => void;
}

export const ShareBottomSheet: React.FC<ShareBottomSheetProps> = ({ video, onClose }) => {
  const {
    onRepostVideo,
    onShareVideo,
    onStartDuet,
    onStartStitch,
    openModal,
    showToast,
  } = useTikTok();

  const quickFriends = [
    { name: 'Sarah D.', handle: '@sarah_d', avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80' },
    { name: 'Marco C.', handle: '@chef_marco', avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80' },
    { name: 'Liam W.', handle: '@liam_w', avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80' },
    { name: 'Emma K.', handle: '@emma_k', avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80' },
    { name: 'Dave R.', handle: '@dave_r', avatarUrl: 'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=150&q=80' },
  ];

  const handleCopyLink = () => {
    navigator.clipboard?.writeText(window.location.href);
    onShareVideo(video);
    showToast('Video link copied to clipboard!');
    onClose();
  };

  const handleRepost = () => {
    onRepostVideo(video);
    onClose();
  };

  const handleSystemShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: video.caption,
          text: `Check out this clip by ${video.authorHandle} on Tashan!`,
          url: window.location.href,
        });
        onShareVideo(video);
      } catch {}
    } else {
      handleCopyLink();
    }
  };

  const handleSaveVideo = () => {
    onShareVideo(video);
    showToast('Saved video clip to local gallery!');
    onClose();
  };

  const handleDuet = () => {
    onStartDuet(video);
    onClose();
  };

  const handleStitch = () => {
    onStartStitch(video);
    onClose();
  };

  const handleQrCode = () => {
    onClose();
    openModal('VIDEO_QR', video);
  };

  const handleSendToFriend = (friendHandle: string) => {
    showToast(`Shared video with ${friendHandle}`);
    onShareVideo(video);
    onClose();
  };

  return (
    <div
      id="share-bottom-sheet-overlay"
      className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-end justify-center p-0 max-w-md mx-auto"
      onClick={onClose}
    >
      <div
        id="share-sheet-container"
        onClick={(e) => e.stopPropagation()}
        className="w-full bg-[#161324] border-t border-[#322C52] rounded-t-3xl p-5 space-y-4 shadow-2xl"
      >
        {/* Header */}
        <div className="flex items-center justify-between">
          <div className="w-6" />
          <h3 className="text-sm font-bold text-white">Send to</h3>
          <button onClick={onClose} className="p-1 rounded-full text-white/60 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Quick Send to Friends */}
        <div className="flex gap-4 overflow-x-auto no-scrollbar py-2">
          {quickFriends.map((f) => (
            <div
              key={f.handle}
              onClick={() => handleSendToFriend(f.handle)}
              className="flex flex-col items-center cursor-pointer group shrink-0"
            >
              <div className="w-13 h-13 rounded-full p-[2px] bg-[#1F1B33] border border-[#322C52] group-hover:border-[#00F5D4] transition">
                <img src={f.avatarUrl} alt={f.name} className="w-full h-full rounded-full object-cover" />
              </div>
              <span className="text-[11px] text-white/80 mt-1 max-w-[54px] truncate text-center">
                {f.name}
              </span>
            </div>
          ))}
        </div>

        {/* Action Row */}
        <div className="pt-2 border-t border-[#1F1B33]">
          <h4 className="text-xs font-semibold text-white/50 mb-3 uppercase tracking-wider">
            Share Action
          </h4>
          <div className="flex gap-4 overflow-x-auto no-scrollbar pb-2">
            {/* Repost */}
            <button
              onClick={handleRepost}
              className="flex flex-col items-center gap-1.5 shrink-0 group"
            >
              <div className={`w-12 h-12 rounded-full flex items-center justify-center transition ${video.isReposted ? 'bg-[#FF2A85] text-white' : 'bg-[#1F1B33] text-[#FF2A85] border border-[#322C52]'}`}>
                <Repeat className="w-5 h-5" />
              </div>
              <span className="text-[11px] text-white/80">{video.isReposted ? 'Reposted' : 'Repost'}</span>
            </button>

            {/* System Share */}
            <button
              onClick={handleSystemShare}
              className="flex flex-col items-center gap-1.5 shrink-0 group"
            >
              <div className="w-12 h-12 rounded-full bg-[#1F1B33] text-[#00B0FF] border border-[#322C52] flex items-center justify-center group-hover:scale-105 transition">
                <Share className="w-5 h-5" />
              </div>
              <span className="text-[11px] text-white/80">Share...</span>
            </button>

            {/* Copy Link */}
            <button
              onClick={handleCopyLink}
              className="flex flex-col items-center gap-1.5 shrink-0 group"
            >
              <div className="w-12 h-12 rounded-full bg-[#1F1B33] text-[#00F5D4] border border-[#322C52] flex items-center justify-center group-hover:scale-105 transition">
                <Copy className="w-5 h-5" />
              </div>
              <span className="text-[11px] text-white/80">Copy Link</span>
            </button>

            {/* Save Video */}
            <button
              onClick={handleSaveVideo}
              className="flex flex-col items-center gap-1.5 shrink-0 group"
            >
              <div className="w-12 h-12 rounded-full bg-[#1F1B33] text-[#00E676] border border-[#322C52] flex items-center justify-center group-hover:scale-105 transition">
                <Download className="w-5 h-5" />
              </div>
              <span className="text-[11px] text-white/80">Save Video</span>
            </button>

            {/* Duet */}
            <button
              onClick={handleDuet}
              className="flex flex-col items-center gap-1.5 shrink-0 group"
            >
              <div className="w-12 h-12 rounded-full bg-[#1F1B33] text-[#7C4DFF] border border-[#322C52] flex items-center justify-center group-hover:scale-105 transition">
                <Zap className="w-5 h-5" />
              </div>
              <span className="text-[11px] text-white/80">Duet</span>
            </button>

            {/* Stitch */}
            <button
              onClick={handleStitch}
              className="flex flex-col items-center gap-1.5 shrink-0 group"
            >
              <div className="w-12 h-12 rounded-full bg-[#1F1B33] text-[#FF6D00] border border-[#322C52] flex items-center justify-center group-hover:scale-105 transition">
                <Film className="w-5 h-5" />
              </div>
              <span className="text-[11px] text-white/80">Stitch</span>
            </button>

            {/* QR Code */}
            <button
              onClick={handleQrCode}
              className="flex flex-col items-center gap-1.5 shrink-0 group"
            >
              <div className="w-12 h-12 rounded-full bg-[#1F1B33] text-[#FFD600] border border-[#322C52] flex items-center justify-center group-hover:scale-105 transition">
                <QrCode className="w-5 h-5" />
              </div>
              <span className="text-[11px] text-white/80">QR Code</span>
            </button>
          </div>
        </div>

        {/* Cancel Button */}
        <button
          onClick={onClose}
          className="w-full py-3 rounded-2xl bg-[#1F1B33] text-white text-xs font-bold hover:bg-[#322C52] transition"
        >
          Cancel
        </button>
      </div>
    </div>
  );
};
