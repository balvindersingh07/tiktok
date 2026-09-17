import React, { useState, useRef, useEffect } from 'react';
import {
  X,
  Music,
  RotateCcw,
  Sparkles,
  Zap,
  Clock,
  Gauge,
  Upload,
  Check,
  Film,
  Lock,
  MessageCircle,
  FolderDown
} from 'lucide-react';
import { useTikTok } from '../../context/TikTokContext';
import { SoundEntity } from '../../types';

export const CreateVideoScreen: React.FC = () => {
  const {
    setCurrentTab,
    sounds,
    selectedSoundForCreation,
    setSelectedSoundForCreation,
    duetSourceVideo,
    stitchSourceVideo,
    onSaveDraft,
    onPublishVideo,
    showToast,
  } = useTikTok();

  // Studio states
  const [isRecording, setIsRecording] = useState<boolean>(false);
  const [recordingProgress, setRecordingProgress] = useState<number>(0);
  const [selectedDuration, setSelectedDuration] = useState<'15s' | '60s'>('15s');
  const [selectedSpeed, setSelectedSpeed] = useState<'0.5x' | '1x' | '2x' | '3x'>('1x');
  const [beautyEnabled, setBeautyEnabled] = useState<boolean>(true);
  const [flashEnabled, setFlashEnabled] = useState<boolean>(false);
  const [isFrontCamera, setIsFrontCamera] = useState<boolean>(true);
  const [selectedEffect, setSelectedEffect] = useState<'Natural' | 'Cyber Neon' | 'Golden Hour' | 'Vibrant Glow' | 'Emerald'>('Cyber Neon');

  // Sound picker modal state
  const [showSoundPicker, setShowSoundPicker] = useState<boolean>(false);

  // Publish / Review sheet state
  const [showPublishSheet, setShowPublishSheet] = useState<boolean>(false);
  const [caption, setCaption] = useState<string>(() => {
    if (duetSourceVideo) return `#duet with ${duetSourceVideo.authorHandle} 🔥`;
    if (stitchSourceVideo) return `#stitch with ${stitchSourceVideo.authorHandle} 🎬`;
    return 'Just created this new vibe! 🔥 What do you think? #creator #fyp #viral';
  });
  const [selectedCover, setSelectedCover] = useState<string>('/assets/video_cover_dance.jpg');
  const [isPrivate, setIsPrivate] = useState<boolean>(false);
  const [allowComments, setAllowComments] = useState<boolean>(true);

  // Video element / camera stream
  const videoRef = useRef<HTMLVideoElement>(null);
  const mediaStreamRef = useRef<MediaStream | null>(null);
  const [hasCameraPermission, setHasCameraPermission] = useState<boolean>(false);
  const recordingTimerRef = useRef<number | null>(null);

  // Start webcam if allowed, otherwise show simulated camera view
  useEffect(() => {
    let mounted = true;
    async function initCamera() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: isFrontCamera ? 'user' : 'environment' },
          audio: false,
        });
        if (mounted) {
          mediaStreamRef.current = stream;
          if (videoRef.current) {
            videoRef.current.srcObject = stream;
            videoRef.current.play().catch(() => {});
          }
          setHasCameraPermission(true);
        }
      } catch {
        if (mounted) {
          setHasCameraPermission(false);
        }
      }
    }

    initCamera();

    return () => {
      mounted = false;
      if (mediaStreamRef.current) {
        mediaStreamRef.current.getTracks().forEach((track) => track.stop());
      }
    };
  }, [isFrontCamera]);

  // Recording timer simulation
  useEffect(() => {
    const maxSec = selectedDuration === '15s' ? 15 : 60;
    if (isRecording) {
      const intervalMs = 100;
      recordingTimerRef.current = window.setInterval(() => {
        setRecordingProgress((prev) => {
          const next = prev + (100 / (maxSec * 10));
          if (next >= 100) {
            setIsRecording(false);
            setShowPublishSheet(true);
            return 100;
          }
          return next;
        });
      }, intervalMs);
    } else {
      if (recordingTimerRef.current) {
        clearInterval(recordingTimerRef.current);
      }
    }

    return () => {
      if (recordingTimerRef.current) {
        clearInterval(recordingTimerRef.current);
      }
    };
  }, [isRecording, selectedDuration]);

  const toggleRecording = () => {
    if (isRecording) {
      setIsRecording(false);
      setShowPublishSheet(true);
    } else {
      setRecordingProgress(0);
      setIsRecording(true);
    }
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const url = URL.createObjectURL(file);
      setSelectedCover(url);
      setShowPublishSheet(true);
      showToast(`Imported ${file.name}`);
    }
  };

  const effectFilterClasses = {
    Natural: 'contrast-100 brightness-100',
    'Cyber Neon': 'contrast-125 saturate-150 hue-rotate-15',
    'Golden Hour': 'sepia-25 brightness-105 saturate-125',
    'Vibrant Glow': 'saturate-200 contrast-110',
    Emerald: 'hue-rotate-60 saturate-120',
  };

  const handlePublish = () => {
    const soundTitle = selectedSoundForCreation?.title || 'Original Sound - Tashan Creator';
    const soundAuthor = selectedSoundForCreation?.author || 'Tashan Original';
    onPublishVideo(caption, soundTitle, soundAuthor, selectedCover, '', isPrivate, allowComments);
  };

  const handleSaveDraft = () => {
    const soundTitle = selectedSoundForCreation?.title || 'Original Sound - Tashan Creator';
    const soundAuthor = selectedSoundForCreation?.author || 'Tashan Original';
    onSaveDraft(caption, soundTitle, soundAuthor, selectedCover, '');
  };

  return (
    <div
      id="create-video-screen"
      className="relative w-full h-screen bg-[#0C0A14] text-white flex flex-col justify-between overflow-hidden max-w-md mx-auto select-none"
    >
      {/* Background Viewfinder: Live Camera or Studio Canvas */}
      <div className="absolute inset-0 z-0 bg-gradient-to-b from-[#161324] via-[#0C0A14] to-[#1F1B33]">
        {hasCameraPermission ? (
          <video
            ref={videoRef}
            autoPlay
            playsInline
            muted
            className={`w-full h-full object-cover transition-all ${
              isFrontCamera ? 'scale-x-[-1]' : ''
            } ${effectFilterClasses[selectedEffect]}`}
          />
        ) : (
          <div className="w-full h-full relative overflow-hidden flex items-center justify-center">
            {/* High-tech creative simulated studio viewfinder */}
            <div
              className={`w-full h-full bg-cover bg-center transition-all duration-700 ${
                isRecording ? 'scale-105' : 'scale-100'
              } ${effectFilterClasses[selectedEffect]}`}
              style={{
                backgroundImage: `url(${selectedCover})`,
              }}
            />
            {/* Viewfinder Grid & Focus Marks */}
            <div className="absolute inset-0 pointer-events-none border-[1px] border-white/10 m-6 rounded-2xl flex items-center justify-center">
              <div className="w-24 h-24 border border-white/30 rounded-lg flex items-center justify-center">
                <div className="w-2 h-2 rounded-full bg-[#00F5D4]" />
              </div>
            </div>
          </div>
        )}
        <div className="absolute inset-0 bg-gradient-to-b from-black/60 via-transparent to-black/80 pointer-events-none" />
      </div>

      {/* Top Header Bar */}
      <header className="relative z-20 pt-4 px-4 flex items-center justify-between">
        {/* Close Button */}
        <button
          id="create-close-btn"
          onClick={() => setCurrentTab('HOME')}
          className="w-10 h-10 rounded-full bg-black/40 backdrop-blur-md flex items-center justify-center text-white hover:bg-black/60 transition"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Sound Selector Banner */}
        <button
          id="create-sound-picker-btn"
          onClick={() => setShowSoundPicker(true)}
          className="flex items-center gap-2 bg-black/50 backdrop-blur-md border border-[#00F5D4]/40 px-3.5 py-1.5 rounded-full text-xs font-semibold text-white hover:border-[#00F5D4] transition"
        >
          <Music className="w-3.5 h-3.5 text-[#00F5D4]" />
          <span className="max-w-[140px] truncate">
            {selectedSoundForCreation?.title || 'Add sound'}
          </span>
        </button>

        {/* Placeholder spacer */}
        <div className="w-10" />
      </header>

      {/* Duet or Stitch Banner */}
      {(duetSourceVideo || stitchSourceVideo) && (
        <div className="relative z-20 mx-4 mt-2 px-3 py-1.5 rounded-lg bg-[#161324]/80 backdrop-blur-md border border-[#00F5D4]/50 flex items-center justify-between text-xs">
          <span className="font-semibold text-[#00F5D4]">
            {duetSourceVideo ? `⚡ Dueting with ${duetSourceVideo.authorHandle}` : `🎬 Stitching with ${stitchSourceVideo?.authorHandle}`}
          </span>
          <button
            onClick={() => {
              showToast('Cancelled collaboration mode');
            }}
            className="text-white/60 hover:text-white"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      )}

      {/* Right Side Camera Tool Controls */}
      <div className="absolute right-4 top-20 z-20 flex flex-col items-center gap-4 bg-black/30 backdrop-blur-md p-2 rounded-2xl border border-white/10">
        <button
          id="create-flip-camera-btn"
          onClick={() => setIsFrontCamera((prev) => !prev)}
          className="flex flex-col items-center text-white hover:text-[#00F5D4] transition"
        >
          <RotateCcw className="w-5 h-5" />
          <span className="text-[9px] mt-0.5 font-medium">Flip</span>
        </button>

        <button
          id="create-speed-btn"
          onClick={() => {
            const speeds: ('0.5x' | '1x' | '2x' | '3x')[] = ['0.5x', '1x', '2x', '3x'];
            const nextIdx = (speeds.indexOf(selectedSpeed) + 1) % speeds.length;
            setSelectedSpeed(speeds[nextIdx]);
          }}
          className="flex flex-col items-center text-white hover:text-[#00F5D4] transition"
        >
          <Gauge className="w-5 h-5" />
          <span className="text-[9px] mt-0.5 font-medium">{selectedSpeed}</span>
        </button>

        <button
          id="create-beauty-btn"
          onClick={() => setBeautyEnabled((prev) => !prev)}
          className={`flex flex-col items-center transition ${beautyEnabled ? 'text-[#FF2A85]' : 'text-white'}`}
        >
          <Sparkles className="w-5 h-5" />
          <span className="text-[9px] mt-0.5 font-medium">Beauty</span>
        </button>

        <button
          id="create-flash-btn"
          onClick={() => setFlashEnabled((prev) => !prev)}
          className={`flex flex-col items-center transition ${flashEnabled ? 'text-[#FFD600]' : 'text-white'}`}
        >
          <Zap className="w-5 h-5" />
          <span className="text-[9px] mt-0.5 font-medium">Flash</span>
        </button>

        <button
          id="create-duration-btn"
          onClick={() => setSelectedDuration((prev) => (prev === '15s' ? '60s' : '15s'))}
          className="flex flex-col items-center text-white hover:text-[#00F5D4] transition"
        >
          <Clock className="w-5 h-5" />
          <span className="text-[9px] mt-0.5 font-medium">{selectedDuration}</span>
        </button>
      </div>

      {/* Center Effect Filter Selection Bar */}
      <div className="relative z-20 px-4 py-2 flex items-center justify-center gap-2 overflow-x-auto no-scrollbar">
        {(['Natural', 'Cyber Neon', 'Golden Hour', 'Vibrant Glow', 'Emerald'] as const).map((effect) => (
          <button
            key={effect}
            onClick={() => setSelectedEffect(effect)}
            className={`px-3 py-1 rounded-full text-xs font-semibold whitespace-nowrap transition-colors ${
              selectedEffect === effect
                ? 'bg-gradient-to-r from-[#00F5D4] to-[#FF2A85] text-white shadow-md'
                : 'bg-black/40 backdrop-blur-md text-white/70 border border-white/10 hover:text-white'
            }`}
          >
            {effect}
          </button>
        ))}
      </div>

      {/* Bottom Recording Section */}
      <footer className="relative z-20 pb-8 px-8 flex items-center justify-around">
        {/* Device Media Import Button */}
        <label className="flex flex-col items-center cursor-pointer group">
          <input
            id="create-upload-input"
            type="file"
            accept="image/*,video/*"
            onChange={handleFileUpload}
            className="hidden"
          />
          <div className="w-11 h-11 rounded-xl bg-black/40 backdrop-blur-md border border-white/20 flex items-center justify-center group-hover:border-[#00F5D4] transition">
            <Upload className="w-5 h-5 text-white" />
          </div>
          <span className="text-[10px] text-white/80 font-medium mt-1">Upload</span>
        </label>

        {/* Big Progressive Record Button */}
        <div className="relative flex items-center justify-center">
          {/* Progress Ring */}
          <svg className="w-24 h-24 transform -rotate-90 pointer-events-none">
            <circle
              cx="48"
              cy="48"
              r="42"
              className="stroke-white/20"
              strokeWidth="4"
              fill="transparent"
            />
            <circle
              cx="48"
              cy="48"
              r="42"
              className="stroke-[#FF2A54] transition-all duration-100 ease-linear"
              strokeWidth="4"
              fill="transparent"
              strokeDasharray="264"
              strokeDashoffset={264 - (264 * recordingProgress) / 100}
              strokeLinecap="round"
            />
          </svg>

          {/* Inner Shutter Button */}
          <button
            id="create-record-btn"
            onClick={toggleRecording}
            className={`absolute w-18 h-18 rounded-full flex items-center justify-center transition-all ${
              isRecording
                ? 'bg-[#FF2A54] scale-90 rounded-2xl'
                : 'rainbow-gradient hover:scale-105 active:scale-95 shadow-xl shadow-[#FF2A85]/30'
            }`}
            aria-label={isRecording ? 'Stop recording' : 'Start recording'}
          >
            <div className={`transition-all ${isRecording ? 'w-6 h-6 bg-white rounded-md' : 'w-7 h-7 bg-white rounded-full'}`} />
          </button>
        </div>

        {/* Quick Review / Publish Trigger */}
        <button
          onClick={() => setShowPublishSheet(true)}
          className="flex flex-col items-center group"
        >
          <div className="w-11 h-11 rounded-xl bg-black/40 backdrop-blur-md border border-white/20 flex items-center justify-center group-hover:border-[#FF2A85] transition">
            <Film className="w-5 h-5 text-white" />
          </div>
          <span className="text-[10px] text-white/80 font-medium mt-1">Review</span>
        </button>
      </footer>

      {/* Sound Picker Modal */}
      {showSoundPicker && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-end justify-center p-0 max-w-md mx-auto">
          <div className="w-full bg-[#161324] border-t border-[#322C52] rounded-t-3xl p-5 max-h-[75vh] overflow-y-auto no-scrollbar">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Music className="w-5 h-5 text-[#00F5D4]" />
                Select Audio Track
              </h3>
              <button onClick={() => setShowSoundPicker(false)} className="text-white/60 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-2">
              {sounds.map((sound) => (
                <div
                  key={sound.id}
                  onClick={() => {
                    setSelectedSoundForCreation(sound);
                    setShowSoundPicker(false);
                    showToast(`Selected "${sound.title}"`);
                  }}
                  className={`flex items-center justify-between p-3 rounded-xl border transition cursor-pointer ${
                    selectedSoundForCreation?.id === sound.id
                      ? 'bg-[#1F1B33] border-[#00F5D4]'
                      : 'bg-[#0C0A14] border-[#1F1B33] hover:border-[#322C52]'
                  }`}
                >
                  <div>
                    <h4 className="text-sm font-bold text-white">{sound.title}</h4>
                    <p className="text-xs text-white/50">{sound.author} • {sound.durationSeconds}s</p>
                  </div>
                  {selectedSoundForCreation?.id === sound.id && (
                    <Check className="w-5 h-5 text-[#00F5D4]" />
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Publish & Review Bottom Sheet */}
      {showPublishSheet && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-end justify-center p-0 max-w-md mx-auto">
          <div className="w-full bg-[#161324] border-t border-[#322C52] rounded-t-3xl p-5 max-h-[85vh] overflow-y-auto no-scrollbar">
            <div className="flex items-center justify-between pb-3 border-b border-[#1F1B33]">
              <h3 className="text-base font-bold text-white">Post Video</h3>
              <button onClick={() => setShowPublishSheet(false)} className="text-white/60 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Video preview thumbnail & Caption Input */}
            <div className="flex gap-4 my-4">
              <div className="w-24 h-32 rounded-xl overflow-hidden bg-black shrink-0 border border-[#322C52] relative">
                <img src={selectedCover} alt="Cover" className="w-full h-full object-cover" />
                <span className="absolute bottom-1 right-1 bg-black/60 px-1 rounded text-[9px] font-bold text-white">
                  15s
                </span>
              </div>
              <div className="flex-1 flex flex-col justify-between">
                <textarea
                  id="publish-caption-input"
                  value={caption}
                  onChange={(e) => setCaption(e.target.value)}
                  placeholder="Describe your video, add hashtags..."
                  rows={4}
                  className="w-full bg-[#0C0A14] rounded-xl border border-[#322C52] p-2.5 text-xs text-white placeholder:text-white/40 focus:outline-none focus:border-[#00F5D4] resize-none"
                />
                <div className="flex gap-1.5 mt-1">
                  {['#fyp', '#viral', '#trending', '#dance'].map((tag) => (
                    <button
                      key={tag}
                      onClick={() => setCaption((prev) => `${prev} ${tag}`)}
                      className="text-[10px] px-2 py-0.5 rounded-full bg-[#1F1B33] text-[#00F5D4] font-semibold"
                    >
                      {tag}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Privacy & Comments Options */}
            <div className="space-y-3 pt-2 pb-4">
              <div className="flex items-center justify-between p-3 rounded-xl bg-[#0C0A14] border border-[#1F1B33]">
                <div className="flex items-center gap-2.5">
                  <Lock className="w-4 h-4 text-white/70" />
                  <span className="text-xs font-semibold text-white">Private Video</span>
                </div>
                <input
                  type="checkbox"
                  checked={isPrivate}
                  onChange={(e) => setIsPrivate(e.target.checked)}
                  className="w-4 h-4 accent-[#FF2A85] rounded cursor-pointer"
                />
              </div>

              <div className="flex items-center justify-between p-3 rounded-xl bg-[#0C0A14] border border-[#1F1B33]">
                <div className="flex items-center gap-2.5">
                  <MessageCircle className="w-4 h-4 text-white/70" />
                  <span className="text-xs font-semibold text-white">Allow Comments</span>
                </div>
                <input
                  type="checkbox"
                  checked={allowComments}
                  onChange={(e) => setAllowComments(e.target.checked)}
                  className="w-4 h-4 accent-[#00F5D4] rounded cursor-pointer"
                />
              </div>
            </div>

            {/* Action Buttons: Save Draft & Post */}
            <div className="flex gap-3 pt-2">
              <button
                id="publish-save-draft-btn"
                onClick={handleSaveDraft}
                className="flex-1 py-3 rounded-xl bg-[#1F1B33] hover:bg-[#322C52] border border-[#322C52] text-white font-bold text-xs flex items-center justify-center gap-2 transition"
              >
                <FolderDown className="w-4 h-4 text-white/70" />
                <span>Save Draft</span>
              </button>

              <button
                id="publish-post-video-btn"
                onClick={handlePublish}
                className="flex-[2] py-3 rounded-xl rainbow-gradient text-white font-bold text-xs shadow-lg shadow-[#FF2A85]/20 hover:scale-[1.02] active:scale-95 transition"
              >
                Post Video
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
