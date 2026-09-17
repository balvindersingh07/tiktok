import React from 'react';
import { TikTokProvider, useTikTok } from './context/TikTokContext';
import { FeedScreen } from './components/feed/FeedScreen';
import { DiscoverScreen } from './components/discover/DiscoverScreen';
import { CreateVideoScreen } from './components/create/CreateVideoScreen';
import { InboxScreen } from './components/inbox/InboxScreen';
import { ProfileScreen } from './components/profile/ProfileScreen';
import { TikTokBottomNav } from './components/navigation/TikTokBottomNav';
import { CommentsBottomSheet } from './components/modals/CommentsBottomSheet';
import { ShareBottomSheet } from './components/modals/ShareBottomSheet';
import { SoundDetailSheet } from './components/modals/SoundDetailSheet';
import { VideoQrDialog } from './components/modals/VideoQrDialog';
import { LocalBackendConsoleSheet } from './components/modals/LocalBackendConsoleSheet';
import { TashanSettingsSheet } from './components/modals/TashanSettingsSheet';

const TikTokAppContent: React.FC = () => {
  const { currentTab, activeModal, modalVideo, closeModal, toastMessage } = useTikTok();

  return (
    <div className="min-h-screen w-full bg-[#07050B] flex items-center justify-center relative overflow-hidden font-sans">
      {/* Background ambient lighting for desktop view */}
      <div className="absolute -top-40 -left-40 w-96 h-96 rounded-full bg-[#00F5D4]/10 blur-[120px] pointer-events-none" />
      <div className="absolute -bottom-40 -right-40 w-96 h-96 rounded-full bg-[#FF2A85]/10 blur-[120px] pointer-events-none" />

      {/* Main Mobile App Frame */}
      <div
        id="tashan-phone-frame"
        className="w-full h-screen sm:h-[94vh] sm:max-h-[890px] sm:max-w-[430px] bg-[#0C0A14] sm:rounded-[36px] sm:border-[5px] sm:border-[#1F1B33] sm:shadow-[0_20px_60px_rgba(0,0,0,0.85)] flex flex-col relative overflow-hidden"
      >
        {/* Active Screen View */}
        <div className="flex-1 w-full h-full relative overflow-hidden">
          {currentTab === 'HOME' && <FeedScreen />}
          {currentTab === 'DISCOVER' && <DiscoverScreen />}
          {currentTab === 'CREATE' && <CreateVideoScreen />}
          {currentTab === 'INBOX' && <InboxScreen />}
          {currentTab === 'PROFILE' && <ProfileScreen />}
        </div>

        {/* Global Bottom Navigation (hidden during recording studio) */}
        {currentTab !== 'CREATE' && <TikTokBottomNav />}

        {/* Floating Toast Notification */}
        {toastMessage && (
          <div
            id="tashan-toast-notification"
            className="fixed sm:absolute top-16 left-1/2 -translate-x-1/2 z-50 px-4 py-2 rounded-full bg-[#161324]/95 border border-[#322C52] text-xs font-semibold text-white shadow-2xl backdrop-blur-md flex items-center gap-2 pointer-events-none transition-all duration-300 animate-in fade-in slide-in-from-top-4"
          >
            <span className="w-2 h-2 rounded-full rainbow-gradient" />
            <span>{toastMessage}</span>
          </div>
        )}

        {/* Bottom Sheets & Modals */}
        {activeModal === 'COMMENTS' && modalVideo && (
          <CommentsBottomSheet video={modalVideo} onClose={closeModal} />
        )}

        {activeModal === 'SHARE' && modalVideo && (
          <ShareBottomSheet video={modalVideo} onClose={closeModal} />
        )}

        {activeModal === 'SOUND_DETAIL' && modalVideo && (
          <SoundDetailSheet video={modalVideo} onClose={closeModal} />
        )}

        {activeModal === 'VIDEO_QR' && modalVideo && (
          <VideoQrDialog video={modalVideo} onClose={closeModal} />
        )}

        {activeModal === 'BACKEND_CONSOLE' && (
          <LocalBackendConsoleSheet onClose={closeModal} />
        )}

        {activeModal === 'SETTINGS' && (
          <TashanSettingsSheet onClose={closeModal} />
        )}
      </div>
    </div>
  );
};

export function App() {
  return (
    <TikTokProvider>
      <TikTokAppContent />
    </TikTokProvider>
  );
}

export default App;
