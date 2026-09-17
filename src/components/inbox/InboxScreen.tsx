import React, { useState } from 'react';
import {
  CheckCheck,
  Send,
  ArrowLeft,
  Heart,
  MessageCircle,
  UserPlus,
  Bell,
  Sparkles
} from 'lucide-react';
import { useTikTok } from '../../context/TikTokContext';
import { formatTimeAgo } from '../modals/CommentsBottomSheet';

interface ActiveFriend {
  name: string;
  handle: string;
  isLive: boolean;
  avatarUrl: string;
}

export const InboxScreen: React.FC = () => {
  const {
    notifications,
    onMarkAllNotificationsRead,
    chatMessages,
    activeChatUser,
    setActiveChatUser,
    onSendMessage,
    jumpToVideo,
    currentUser,
  } = useTikTok();

  const [selectedFilter, setSelectedFilter] = useState<'All' | 'Likes' | 'Comments' | 'Followers'>('All');
  const [inputText, setInputText] = useState<string>('');

  const activeFriends: ActiveFriend[] = [
    {
      name: 'Marcus',
      handle: '@marcus_moves',
      isLive: true,
      avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80',
    },
    {
      name: 'Chef Kenji',
      handle: '@chef_ramen',
      isLive: false,
      avatarUrl: 'https://images.unsplash.com/photo-1556157382-97eda2d62296?auto=format&fit=crop&w=300&q=80',
    },
    {
      name: 'Elena',
      handle: '@elena_dance',
      isLive: false,
      avatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=300&q=80',
    },
    {
      name: 'Maya',
      handle: '@maya_traveler',
      isLive: false,
      avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80',
    },
    {
      name: 'Jordan',
      handle: '@jordan_skates',
      isLive: false,
      avatarUrl: 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=300&q=80',
    },
  ];

  const filteredNotifications = notifications.filter((n) => {
    if (selectedFilter === 'Likes') return n.type === 'like';
    if (selectedFilter === 'Comments') return n.type === 'comment';
    if (selectedFilter === 'Followers') return n.type === 'follow';
    return true;
  });

  const handleSendChat = (e: React.FormEvent) => {
    e.preventDefault();
    if (inputText.trim()) {
      onSendMessage(inputText);
      setInputText('');
    }
  };

  const currentChatUserMessages = chatMessages.filter(
    (m) =>
      (m.senderHandle === activeChatUser && m.receiverHandle === currentUser.handle) ||
      (m.senderHandle === currentUser.handle && m.receiverHandle === activeChatUser)
  );

  // If chat is open with a user
  if (activeChatUser) {
    return (
      <div
        id="direct-message-chat-view"
        className="w-full h-screen bg-[#0C0A14] text-white flex flex-col justify-between max-w-md mx-auto"
      >
        {/* Chat Header */}
        <header className="p-4 border-b border-[#1F1B33] bg-[#161324] flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setActiveChatUser(null)}
              className="p-1 rounded-full text-white/70 hover:text-white hover:bg-white/10"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div>
              <h3 className="text-sm font-bold text-white">{activeChatUser}</h3>
              <span className="text-[11px] text-[#00E676] flex items-center gap-1">
                <span className="w-1.5 h-1.5 rounded-full bg-[#00E676]" /> Active now
              </span>
            </div>
          </div>
        </header>

        {/* Message Bubble List */}
        <div className="flex-1 overflow-y-auto p-4 space-y-3 no-scrollbar">
          {currentChatUserMessages.length === 0 ? (
            <div className="text-center py-10 text-white/50 text-xs">
              Wave or say hi to start the conversation! 👋
            </div>
          ) : (
            currentChatUserMessages.map((msg) => {
              const isMine = msg.senderHandle === currentUser.handle;
              return (
                <div
                  key={msg.id}
                  className={`flex flex-col ${isMine ? 'items-end' : 'items-start'}`}
                >
                  <div
                    className={`max-w-[75%] px-3.5 py-2 rounded-2xl text-xs font-medium ${
                      isMine
                        ? 'bg-gradient-to-r from-[#00F5D4] to-[#00B0FF] text-black font-semibold rounded-br-none'
                        : 'bg-[#1F1B33] text-white rounded-bl-none border border-[#322C52]'
                    }`}
                  >
                    {msg.messageText}
                  </div>
                  <span className="text-[9px] text-white/40 mt-1 px-1">
                    {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>
              );
            })
          )}
        </div>

        {/* Chat Input */}
        <form onSubmit={handleSendChat} className="p-3 bg-[#161324] border-t border-[#1F1B33] flex items-center gap-2">
          <input
            type="text"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            placeholder="Send a message..."
            className="flex-1 bg-[#0C0A14] border border-[#322C52] rounded-full px-4 py-2 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
          />
          <button
            type="submit"
            disabled={!inputText.trim()}
            className="w-8 h-8 rounded-full rainbow-gradient flex items-center justify-center text-white disabled:opacity-40 transition"
          >
            <Send className="w-3.5 h-3.5" />
          </button>
        </form>
      </div>
    );
  }

  return (
    <div
      id="inbox-screen"
      className="w-full h-screen bg-[#0C0A14] text-white flex flex-col pb-16 overflow-y-auto no-scrollbar max-w-md mx-auto"
    >
      {/* Inbox Header */}
      <header className="sticky top-0 z-30 bg-[#0C0A14]/95 backdrop-blur-md px-4 pt-3 pb-2 border-b border-[#1F1B33] flex items-center justify-between">
        <h1 className="text-xl font-extrabold text-white tracking-tight">Inbox</h1>
        <button
          id="inbox-mark-all-read-btn"
          onClick={onMarkAllNotificationsRead}
          className="flex items-center gap-1 text-xs font-semibold text-[#00F5D4] hover:underline"
        >
          <CheckCheck className="w-4 h-4" />
          <span>Mark all read</span>
        </button>
      </header>

      {/* Active Friends Row */}
      <section className="px-4 py-3 border-b border-[#1F1B33]/60">
        <h2 className="text-xs font-bold uppercase tracking-wider text-white/50 mb-2">
          Direct Messages & Friends
        </h2>
        <div className="flex gap-4 overflow-x-auto no-scrollbar pb-1">
          {activeFriends.map((friend) => (
            <div
              key={friend.handle}
              onClick={() => setActiveChatUser(friend.handle)}
              className="flex flex-col items-center cursor-pointer group shrink-0"
            >
              <div className="relative">
                <div className={`w-14 h-14 rounded-full p-[2px] ${friend.isLive ? 'rainbow-gradient animate-pulse' : 'bg-[#1F1B33] border border-[#322C52]'}`}>
                  <img
                    src={friend.avatarUrl}
                    alt={friend.name}
                    className="w-full h-full rounded-full object-cover"
                  />
                </div>
                {friend.isLive ? (
                  <span className="absolute -bottom-1 left-1/2 -translate-x-1/2 px-1.5 py-0.2 rounded-full bg-[#FF2A54] text-[9px] font-black text-white uppercase tracking-tighter">
                    LIVE
                  </span>
                ) : (
                  <span className="absolute bottom-0 right-0 w-3.5 h-3.5 rounded-full bg-[#00E676] border-2 border-[#0C0A14]" />
                )}
              </div>
              <span className="text-[11px] font-medium text-white/80 mt-1 max-w-[56px] truncate text-center">
                {friend.name}
              </span>
            </div>
          ))}
        </div>
      </section>

      {/* Filter Tabs */}
      <section className="px-4 pt-3">
        <div className="flex gap-2 overflow-x-auto no-scrollbar">
          {(['All', 'Likes', 'Comments', 'Followers'] as const).map((filter) => (
            <button
              key={filter}
              onClick={() => setSelectedFilter(filter)}
              className={`px-3 py-1 rounded-full text-xs font-semibold whitespace-nowrap transition-colors ${
                selectedFilter === filter
                  ? 'bg-white text-black font-bold'
                  : 'bg-[#161324] text-white/60 hover:text-white border border-[#1F1B33]'
              }`}
            >
              {filter}
            </button>
          ))}
        </div>
      </section>

      {/* Notifications List */}
      <main className="flex-1 px-4 py-3 space-y-2">
        {filteredNotifications.length === 0 ? (
          <div className="text-center py-16 text-white/50 text-sm">
            <Bell className="w-8 h-8 mx-auto text-white/20 mb-2" />
            No notifications in this filter yet.
          </div>
        ) : (
          filteredNotifications.map((n) => {
            const icon =
              n.type === 'like' ? (
                <Heart className="w-3 h-3 text-[#FF2A85] fill-[#FF2A85]" />
              ) : n.type === 'comment' ? (
                <MessageCircle className="w-3 h-3 text-[#00F5D4] fill-[#00F5D4]" />
              ) : n.type === 'follow' ? (
                <UserPlus className="w-3 h-3 text-[#00B0FF]" />
              ) : (
                <Sparkles className="w-3 h-3 text-[#FFD600]" />
              );

            return (
              <div
                key={n.id}
                onClick={() => {
                  if (n.targetVideoId) {
                    jumpToVideo(n.targetVideoId);
                  }
                }}
                className={`flex items-center justify-between p-3 rounded-2xl transition cursor-pointer ${
                  n.isRead ? 'bg-[#161324]/60 hover:bg-[#161324]' : 'bg-[#1F1B33] border border-[#00F5D4]/20'
                }`}
              >
                <div className="flex items-center gap-3">
                  <div className="relative">
                    <div className="w-10 h-10 rounded-full bg-[#322C52] flex items-center justify-center font-bold text-xs text-white">
                      {n.actorName.charAt(0)}
                    </div>
                    <div className="absolute -bottom-1 -right-1 w-5 h-5 rounded-full bg-[#0C0A14] border border-[#1F1B33] flex items-center justify-center">
                      {icon}
                    </div>
                  </div>
                  <div>
                    <p className="text-xs text-white">
                      <span className="font-bold text-white mr-1">{n.actorName}</span>
                      <span className="text-white/70">{n.actionText}</span>
                    </p>
                    <span className="text-[10px] text-white/40 mt-0.5 block">
                      {formatTimeAgo(n.timestamp)}
                    </span>
                  </div>
                </div>

                {!n.isRead && (
                  <span className="w-2 h-2 rounded-full bg-[#FF2A54] shrink-0" />
                )}
              </div>
            );
          })
        )}
      </main>
    </div>
  );
};
