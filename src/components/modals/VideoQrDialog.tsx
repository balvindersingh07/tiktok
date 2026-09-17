import React from 'react';
import { X, QrCode, Copy, Download } from 'lucide-react';
import { VideoEntity } from '../../types';
import { useTikTok } from '../../context/TikTokContext';

interface VideoQrDialogProps {
  video: VideoEntity;
  onClose: () => void;
}

export const VideoQrDialog: React.FC<VideoQrDialogProps> = ({ video, onClose }) => {
  const { showToast } = useTikTok();

  const handleCopyLink = () => {
    navigator.clipboard?.writeText(window.location.href);
    showToast('Tashan clip code copied to clipboard!');
  };

  const handleSaveImage = () => {
    showToast('QR Code saved to camera roll!');
  };

  return (
    <div
      id="video-qr-dialog-overlay"
      className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4 max-w-md mx-auto"
      onClick={onClose}
    >
      <div
        id="video-qr-dialog-card"
        onClick={(e) => e.stopPropagation()}
        className="w-full max-w-sm bg-[#161324] border border-[#322C52] rounded-3xl p-6 flex flex-col items-center text-center shadow-2xl space-y-4"
      >
        <div className="w-full flex justify-end">
          <button onClick={onClose} className="text-white/60 hover:text-white p-1">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* QR Code Container with Rainbow Border */}
        <div className="p-3 rainbow-gradient rounded-3xl shadow-xl shadow-[#00F5D4]/20">
          <div className="bg-white p-4 rounded-2xl flex flex-col items-center justify-center">
            {/* High visual fidelity QR Pattern */}
            <div className="w-44 h-44 bg-white flex flex-col items-center justify-center relative">
              <QrCode className="w-40 h-40 text-black stroke-[1.5]" />
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div className="w-10 h-10 rounded-full bg-[#0C0A14] border-2 border-white flex items-center justify-center">
                  <div className="w-4 h-4 rounded-full rainbow-gradient" />
                </div>
              </div>
            </div>
            <span className="text-[11px] font-black tracking-widest text-black/80 uppercase mt-2">
              Tashan • Video Code
            </span>
          </div>
        </div>

        <div>
          <h3 className="text-sm font-bold text-white">{video.authorHandle}</h3>
          <p className="text-xs text-white/60 mt-0.5 line-clamp-1">{video.caption}</p>
        </div>

        <div className="flex gap-2 w-full pt-2">
          <button
            onClick={handleCopyLink}
            className="flex-1 py-2.5 rounded-xl bg-[#1F1B33] hover:bg-[#322C52] border border-[#322C52] text-white text-xs font-bold flex items-center justify-center gap-1.5 transition"
          >
            <Copy className="w-4 h-4 text-[#00F5D4]" />
            <span>Copy Link</span>
          </button>

          <button
            onClick={handleSaveImage}
            className="flex-1 py-2.5 rounded-xl rainbow-gradient text-white text-xs font-bold flex items-center justify-center gap-1.5 shadow transition"
          >
            <Download className="w-4 h-4" />
            <span>Save Code</span>
          </button>
        </div>
      </div>
    </div>
  );
};
