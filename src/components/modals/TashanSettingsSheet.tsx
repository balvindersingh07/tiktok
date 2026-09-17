import React from 'react';
import {
  X,
  Lock,
  MessageCircle,
  Zap,
  Download,
  Wifi,
  HardDrive,
  Info,
  LogOut,
  ShieldCheck,
  Film
} from 'lucide-react';
import { useTikTok } from '../../context/TikTokContext';

interface TashanSettingsSheetProps {
  onClose: () => void;
}

export const TashanSettingsSheet: React.FC<TashanSettingsSheetProps> = ({ onClose }) => {
  const {
    currentUser,
    onUpdateProfile,
    showToast,
  } = useTikTok();

  const handleTogglePrivate = () => {
    const nextVal = !currentUser.isPrivate;
    onUpdateProfile(
      currentUser.displayName,
      currentUser.handle,
      currentUser.bio,
      currentUser.avatarUrl,
      nextVal,
      currentUser.allowComments
    );
    showToast(nextVal ? 'Account set to Private' : 'Account set to Public');
  };

  const handleToggleComments = () => {
    const nextVal = !currentUser.allowComments;
    onUpdateProfile(
      currentUser.displayName,
      currentUser.handle,
      currentUser.bio,
      currentUser.avatarUrl,
      currentUser.isPrivate,
      nextVal
    );
    showToast(nextVal ? 'Comments enabled' : 'Comments disabled');
  };

  return (
    <div
      id="settings-sheet-overlay"
      className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-end justify-center p-0 max-w-md mx-auto"
      onClick={onClose}
    >
      <div
        id="settings-sheet-container"
        onClick={(e) => e.stopPropagation()}
        className="w-full bg-[#161324] border-t border-[#322C52] rounded-t-3xl p-5 max-h-[85vh] overflow-y-auto no-scrollbar space-y-5 shadow-2xl"
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-2 border-b border-[#1F1B33]">
          <h3 className="text-sm font-bold text-white uppercase tracking-wider">
            Settings and Privacy
          </h3>
          <button onClick={onClose} className="text-white/60 hover:text-white p-1">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Account & Privacy Section */}
        <div className="space-y-3">
          <h4 className="text-xs font-semibold text-white/50 uppercase tracking-wider">
            Privacy & Safety
          </h4>

          {/* Private Account */}
          <div className="flex items-center justify-between p-3 rounded-2xl bg-[#0C0A14] border border-[#1F1B33]">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-[#1F1B33] flex items-center justify-center text-[#FFD600]">
                <Lock className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-bold text-white">Private Account</p>
                <p className="text-[11px] text-white/40">Only approved followers can view videos</p>
              </div>
            </div>
            <input
              type="checkbox"
              checked={currentUser.isPrivate}
              onChange={handleTogglePrivate}
              className="w-4 h-4 accent-[#00F5D4] rounded cursor-pointer"
            />
          </div>

          {/* Allow Comments */}
          <div className="flex items-center justify-between p-3 rounded-2xl bg-[#0C0A14] border border-[#1F1B33]">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-[#1F1B33] flex items-center justify-center text-[#00F5D4]">
                <MessageCircle className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-bold text-white">Allow Comments</p>
                <p className="text-[11px] text-white/40">Choose who can comment on your posts</p>
              </div>
            </div>
            <input
              type="checkbox"
              checked={currentUser.allowComments}
              onChange={handleToggleComments}
              className="w-4 h-4 accent-[#00F5D4] rounded cursor-pointer"
            />
          </div>

          {/* Duet & Stitch */}
          <div className="flex items-center justify-between p-3 rounded-2xl bg-[#0C0A14] border border-[#1F1B33]">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-[#1F1B33] flex items-center justify-center text-[#7C4DFF]">
                <Zap className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-bold text-white">Allow Duet & Stitch</p>
                <p className="text-[11px] text-white/40">Allow creators to remix your videos</p>
              </div>
            </div>
            <span className="text-xs text-[#00E676] font-semibold">Everyone</span>
          </div>

          {/* Video Downloads */}
          <div className="flex items-center justify-between p-3 rounded-2xl bg-[#0C0A14] border border-[#1F1B33]">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-[#1F1B33] flex items-center justify-center text-[#00E676]">
                <Download className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-bold text-white">Video Downloads</p>
                <p className="text-[11px] text-white/40">Allow viewers to download your clips</p>
              </div>
            </div>
            <span className="text-xs text-[#00E676] font-semibold">On</span>
          </div>
        </div>

        {/* Content & Playback Section */}
        <div className="space-y-3">
          <h4 className="text-xs font-semibold text-white/50 uppercase tracking-wider">
            Content & Playback
          </h4>

          <div className="flex items-center justify-between p-3 rounded-2xl bg-[#0C0A14] border border-[#1F1B33]">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-[#1F1B33] flex items-center justify-center text-[#00B0FF]">
                <Wifi className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-bold text-white">Data Saver</p>
                <p className="text-[11px] text-white/40">Lower streaming bitrate on mobile networks</p>
              </div>
            </div>
            <span className="text-xs text-white/60">Off</span>
          </div>

          <div className="flex items-center justify-between p-3 rounded-2xl bg-[#0C0A14] border border-[#1F1B33]">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-[#1F1B33] flex items-center justify-center text-[#FF2A85]">
                <Film className="w-4 h-4" />
              </div>
              <div>
                <p className="text-xs font-bold text-white">Autoplay Next Video</p>
                <p className="text-[11px] text-white/40">Advance feed after video loops once</p>
              </div>
            </div>
            <span className="text-xs text-[#00F5D4] font-semibold">Enabled</span>
          </div>
        </div>

        {/* Cache & App Info */}
        <div className="space-y-2 pt-2 border-t border-[#1F1B33]">
          <button
            onClick={() => showToast('Freed 48.2 MB of cached media')}
            className="w-full p-3 rounded-2xl bg-[#0C0A14] border border-[#1F1B33] hover:border-[#322C52] flex items-center justify-between text-left transition"
          >
            <div className="flex items-center gap-3">
              <HardDrive className="w-4 h-4 text-white/60" />
              <div>
                <span className="text-xs font-bold text-white block">Free Up Space</span>
                <span className="text-[11px] text-white/40">Clear temporary caches and drafts</span>
              </div>
            </div>
            <span className="text-xs text-[#00F5D4] font-semibold">48.2 MB</span>
          </button>

          <div className="p-3 rounded-2xl bg-[#0C0A14] border border-[#1F1B33] flex items-center justify-between">
            <div className="flex items-center gap-3">
              <ShieldCheck className="w-4 h-4 text-[#00F5D4]" />
              <div>
                <span className="text-xs font-bold text-white block">Community Guidelines</span>
                <span className="text-[11px] text-white/40">Safety, copyright & creator terms</span>
              </div>
            </div>
            <Info className="w-4 h-4 text-white/40" />
          </div>
        </div>

        {/* App Version & Logout */}
        <div className="pt-2 text-center space-y-3">
          <p className="text-[11px] text-white/40">
            Tashan Rainbow Edition • v2.4.0 (Web Build)
          </p>

          <button
            onClick={() => {
              showToast('Logged out of session');
              onClose();
            }}
            className="w-full py-2.5 rounded-xl bg-red-950/30 hover:bg-red-950/60 border border-red-900/40 text-red-400 text-xs font-bold flex items-center justify-center gap-2 transition"
          >
            <LogOut className="w-4 h-4" />
            <span>Log Out</span>
          </button>
        </div>
      </div>
    </div>
  );
};
