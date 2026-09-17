import React from 'react';
import { Home, Compass, MessageSquare, User, Plus } from 'lucide-react';
import { useTikTok } from '../../context/TikTokContext';
import { MainTab } from '../../types';

export const TikTokBottomNav: React.FC = () => {
  const { currentTab, setCurrentTab, unreadNotificationCount } = useTikTok();

  const handleTabClick = (tab: MainTab) => {
    setCurrentTab(tab);
  };

  return (
    <nav
      id="bottom-navigation-bar"
      className="fixed bottom-0 left-0 right-0 z-40 h-[60px] bg-[#0C0A14]/95 backdrop-blur-md border-t border-[#1F1B33] flex items-center justify-around px-2 max-w-md mx-auto"
    >
      {/* Home */}
      <button
        id="nav-tab-home"
        onClick={() => handleTabClick('HOME')}
        className={`flex flex-col items-center justify-center flex-1 py-1 transition-colors ${
          currentTab === 'HOME' ? 'text-white' : 'text-[#8E8B9E] hover:text-white'
        }`}
      >
        <Home className={`w-6 h-6 ${currentTab === 'HOME' ? 'stroke-[2.5]' : 'stroke-2'}`} />
        <span className="text-[10px] font-medium mt-0.5">Home</span>
      </button>

      {/* Discover */}
      <button
        id="nav-tab-discover"
        onClick={() => handleTabClick('DISCOVER')}
        className={`flex flex-col items-center justify-center flex-1 py-1 transition-colors ${
          currentTab === 'DISCOVER' ? 'text-white' : 'text-[#8E8B9E] hover:text-white'
        }`}
      >
        <Compass className={`w-6 h-6 ${currentTab === 'DISCOVER' ? 'stroke-[2.5]' : 'stroke-2'}`} />
        <span className="text-[10px] font-medium mt-0.5">Discover</span>
      </button>

      {/* Create Button (Custom Tashan Rainbow Add Button) */}
      <div className="flex-1 flex justify-center items-center">
        <button
          id="nav-tab-create"
          onClick={() => handleTabClick('CREATE')}
          aria-label="Create new video"
          className="relative group p-0.5 rounded-xl bg-gradient-to-r from-[#00F5D4] via-[#FFD600] to-[#FF2A85] shadow-lg shadow-[#FF2A85]/20 hover:scale-105 active:scale-95 transition-transform"
        >
          <div className="bg-[#0C0A14] group-hover:bg-[#161324] px-3.5 py-1 rounded-[10px] flex items-center justify-center">
            <Plus className="w-5 h-5 text-white stroke-[2.5]" />
          </div>
        </button>
      </div>

      {/* Inbox */}
      <button
        id="nav-tab-inbox"
        onClick={() => handleTabClick('INBOX')}
        className={`relative flex flex-col items-center justify-center flex-1 py-1 transition-colors ${
          currentTab === 'INBOX' ? 'text-white' : 'text-[#8E8B9E] hover:text-white'
        }`}
      >
        <div className="relative">
          <MessageSquare className={`w-6 h-6 ${currentTab === 'INBOX' ? 'stroke-[2.5]' : 'stroke-2'}`} />
          {unreadNotificationCount > 0 && (
            <span
              id="inbox-unread-badge"
              className="absolute -top-1 -right-1.5 min-w-[16px] h-4 px-1 bg-[#FF2A54] text-white text-[10px] font-bold rounded-full flex items-center justify-center border-2 border-[#0C0A14]"
            >
              {unreadNotificationCount > 9 ? '9+' : unreadNotificationCount}
            </span>
          )}
        </div>
        <span className="text-[10px] font-medium mt-0.5">Inbox</span>
      </button>

      {/* Profile */}
      <button
        id="nav-tab-profile"
        onClick={() => handleTabClick('PROFILE')}
        className={`flex flex-col items-center justify-center flex-1 py-1 transition-colors ${
          currentTab === 'PROFILE' ? 'text-white' : 'text-[#8E8B9E] hover:text-white'
        }`}
      >
        <User className={`w-6 h-6 ${currentTab === 'PROFILE' ? 'stroke-[2.5]' : 'stroke-2'}`} />
        <span className="text-[10px] font-medium mt-0.5">Profile</span>
      </button>
    </nav>
  );
};
