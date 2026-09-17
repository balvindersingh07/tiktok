import React, { useState } from 'react';
import {
  Settings,
  ArrowLeft,
  Edit,
  Share2,
  Lock,
  Bookmark,
  Heart,
  Grid,
  FolderDown,
  Trash2,
  UploadCloud,
  CheckCircle,
  Users,
  KeyRound,
  Shield,
  Server
} from 'lucide-react';
import { useTikTok } from '../../context/TikTokContext';
import { ProfileSubTab, DraftEntity } from '../../types';
import { formatCount } from '../feed/VideoFeedItem';

export const ProfileScreen: React.FC = () => {
  const {
    currentUser,
    viewingCreator,
    setViewingCreator,
    videos,
    likedVideos,
    bookmarkedVideos,
    drafts,
    onPublishDraft,
    onDeleteDraft,
    onToggleFollow,
    allAccounts,
    onSwitchAccount,
    onLogin,
    onSignup,
    onChangePin,
    onUpdateProfile,
    openModal,
    jumpToVideo,
    setActiveChatUser,
    setCurrentTab,
    showToast,
  } = useTikTok();

  const [selectedSubTab, setSelectedSubTab] = useState<ProfileSubTab>('MY_VIDEOS');

  // Dialogs
  const [showEditProfileDialog, setShowEditProfileDialog] = useState<boolean>(false);
  const [showAccountsDialog, setShowAccountsDialog] = useState<boolean>(false);
  const [showAuthDialog, setShowAuthDialog] = useState<'LOGIN' | 'SIGNUP' | null>(null);
  const [showChangePinDialog, setShowChangePinDialog] = useState<boolean>(false);

  // Edit Profile Form
  const [editName, setEditName] = useState<string>(currentUser.displayName);
  const [editHandle, setEditHandle] = useState<string>(currentUser.handle);
  const [editBio, setEditBio] = useState<string>(currentUser.bio);
  const [editIsPrivate, setEditIsPrivate] = useState<boolean>(currentUser.isPrivate);

  // Auth form states
  const [authHandle, setAuthHandle] = useState<string>('');
  const [authName, setAuthName] = useState<string>('');
  const [authPin, setAuthPin] = useState<string>('');
  const [oldPin, setOldPin] = useState<string>('');
  const [newPin, setNewPin] = useState<string>('');

  const activeProfile = viewingCreator || currentUser;
  const isViewingSelf = !viewingCreator || viewingCreator.userId === currentUser.userId;

  // Filter videos for this user
  const userVideos = videos.filter(
    (v) =>
      v.authorHandle.toLowerCase() === activeProfile.handle.toLowerCase() ||
      v.authorId === activeProfile.userId
  );

  const privateVideos = videos.filter(
    (v) => v.isPrivate && (v.authorHandle.toLowerCase() === activeProfile.handle.toLowerCase())
  );

  const displayVideos =
    selectedSubTab === 'MY_VIDEOS'
      ? userVideos.length > 0 ? userVideos : (isViewingSelf ? videos.slice(0, 3) : [])
      : selectedSubTab === 'LIKED_VIDEOS'
      ? likedVideos
      : selectedSubTab === 'BOOKMARKED'
      ? bookmarkedVideos
      : selectedSubTab === 'PRIVATE'
      ? privateVideos
      : [];

  const handleSaveProfile = (e: React.FormEvent) => {
    e.preventDefault();
    onUpdateProfile(editName, editHandle, editBio, currentUser.avatarUrl, editIsPrivate, true);
    setShowEditProfileDialog(false);
  };

  const handleLoginSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (onLogin(authHandle, authPin)) {
      setShowAuthDialog(null);
      setAuthHandle('');
      setAuthPin('');
    }
  };

  const handleSignupSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (onSignup(authName, authHandle, authPin)) {
      setShowAuthDialog(null);
      setAuthName('');
      setAuthHandle('');
      setAuthPin('');
    }
  };

  const handleChangePinSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (onChangePin(oldPin, newPin)) {
      setShowChangePinDialog(false);
      setOldPin('');
      setNewPin('');
    }
  };

  return (
    <div
      id="profile-screen"
      className="w-full h-screen bg-[#0C0A14] text-white flex flex-col pb-16 overflow-y-auto no-scrollbar max-w-md mx-auto"
    >
      {/* Profile Header Bar */}
      <header className="sticky top-0 z-30 bg-[#0C0A14]/95 backdrop-blur-md px-4 pt-3 pb-2 border-b border-[#1F1B33] flex items-center justify-between">
        <div className="flex items-center gap-2">
          {!isViewingSelf && (
            <button
              onClick={() => setViewingCreator(null)}
              className="p-1 rounded-full text-white/80 hover:text-white"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
          )}
          <span className="font-bold text-base text-white truncate max-w-[200px]">
            {activeProfile.handle}
          </span>
          {activeProfile.isVerified && (
            <CheckCircle className="w-4 h-4 text-[#00F5D4] shrink-0" />
          )}
        </div>

        {isViewingSelf && (
          <div className="flex items-center gap-1">
            {/* Switch Account */}
            <button
              id="profile-switch-account-btn"
              onClick={() => setShowAccountsDialog(true)}
              className="p-1.5 rounded-full text-white/70 hover:text-white hover:bg-[#161324] transition"
              title="Switch Accounts"
            >
              <Users className="w-5 h-5" />
            </button>

            {/* Local Backend Engine Console */}
            <button
              id="profile-backend-console-btn"
              onClick={() => openModal('BACKEND_CONSOLE')}
              className="p-1.5 rounded-full text-[#00F5D4] hover:bg-[#161324] transition"
              title="Backend Diagnostics Engine"
            >
              <Server className="w-5 h-5" />
            </button>

            {/* Settings */}
            <button
              id="profile-settings-btn"
              onClick={() => openModal('SETTINGS')}
              className="p-1.5 rounded-full text-white/70 hover:text-white hover:bg-[#161324] transition"
              title="Settings & Privacy"
            >
              <Settings className="w-5 h-5" />
            </button>
          </div>
        )}
      </header>

      {/* User Information & Avatar */}
      <section className="px-6 py-4 flex flex-col items-center text-center">
        <div className="relative mb-3">
          <div className="w-22 h-22 rounded-full p-[3px] rainbow-gradient shadow-xl shadow-[#FF2A85]/20">
            <img
              src={activeProfile.avatarUrl}
              alt={activeProfile.displayName}
              className="w-full h-full rounded-full object-cover border-2 border-[#0C0A14]"
            />
          </div>
          {activeProfile.isPrivate && (
            <div className="absolute bottom-0 right-0 w-6 h-6 rounded-full bg-[#161324] border border-[#322C52] flex items-center justify-center text-[#FFD600]">
              <Lock className="w-3.5 h-3.5" />
            </div>
          )}
        </div>

        <h2 className="text-lg font-extrabold text-white flex items-center justify-center gap-1.5">
          {activeProfile.displayName}
        </h2>
        <p className="text-xs text-white/60 font-medium mt-0.5">{activeProfile.handle}</p>

        {/* Following, Followers, Likes Stats */}
        <div className="flex items-center justify-center gap-8 my-4 py-2 border-y border-[#1F1B33]/80 w-full max-w-xs">
          <div className="flex flex-col items-center">
            <span className="font-extrabold text-base text-white">
              {formatCount(activeProfile.followingCount)}
            </span>
            <span className="text-[11px] text-white/50">Following</span>
          </div>

          <div className="flex flex-col items-center">
            <span className="font-extrabold text-base text-white">
              {formatCount(activeProfile.followersCount)}
            </span>
            <span className="text-[11px] text-white/50">Followers</span>
          </div>

          <div className="flex flex-col items-center">
            <span className="font-extrabold text-base text-white">
              {activeProfile.likesCount}
            </span>
            <span className="text-[11px] text-white/50">Likes</span>
          </div>
        </div>

        {/* Profile Action Buttons */}
        {isViewingSelf ? (
          <div className="flex items-center gap-2 w-full max-w-xs">
            <button
              id="edit-profile-btn"
              onClick={() => {
                setEditName(currentUser.displayName);
                setEditHandle(currentUser.handle);
                setEditBio(currentUser.bio);
                setEditIsPrivate(currentUser.isPrivate);
                setShowEditProfileDialog(true);
              }}
              className="flex-1 py-2 px-4 rounded-xl bg-[#161324] hover:bg-[#1F1B33] border border-[#322C52] text-white font-bold text-xs flex items-center justify-center gap-1.5 transition"
            >
              <Edit className="w-3.5 h-3.5" />
              <span>Edit profile</span>
            </button>

            <button
              id="share-profile-btn"
              onClick={() => {
                navigator.clipboard?.writeText(window.location.href);
                showToast('Profile link copied to clipboard!');
              }}
              className="py-2 px-3 rounded-xl bg-[#161324] hover:bg-[#1F1B33] border border-[#322C52] text-white transition"
              title="Share profile"
            >
              <Share2 className="w-4 h-4" />
            </button>
          </div>
        ) : (
          <div className="flex items-center gap-2 w-full max-w-xs">
            <button
              id="profile-follow-toggle-btn"
              onClick={() => onToggleFollow(activeProfile.handle)}
              className="flex-1 py-2.5 rounded-xl rainbow-gradient text-white font-bold text-xs shadow-md hover:scale-[1.02] active:scale-95 transition"
            >
              Follow
            </button>
            <button
              onClick={() => {
                setActiveChatUser(activeProfile.handle);
                setCurrentTab('INBOX');
              }}
              className="flex-1 py-2.5 rounded-xl bg-[#161324] hover:bg-[#1F1B33] border border-[#322C52] text-white font-bold text-xs transition"
            >
              Message
            </button>
          </div>
        )}

        {/* Bio Text */}
        <p className="text-xs text-white/80 mt-3 whitespace-pre-line max-w-xs leading-relaxed">
          {activeProfile.bio}
        </p>
      </section>

      {/* Profile Sub-Tabs */}
      <div className="sticky top-12 z-20 bg-[#0C0A14] border-b border-[#1F1B33] flex items-center justify-around">
        <button
          onClick={() => setSelectedSubTab('MY_VIDEOS')}
          className={`py-3 flex-1 flex justify-center border-b-2 transition ${
            selectedSubTab === 'MY_VIDEOS' ? 'border-[#00F5D4] text-white' : 'border-transparent text-white/40 hover:text-white'
          }`}
          title="Uploaded Videos"
        >
          <Grid className="w-5 h-5" />
        </button>

        <button
          onClick={() => setSelectedSubTab('LIKED_VIDEOS')}
          className={`py-3 flex-1 flex justify-center border-b-2 transition ${
            selectedSubTab === 'LIKED_VIDEOS' ? 'border-[#FF2A85] text-[#FF2A85]' : 'border-transparent text-white/40 hover:text-white'
          }`}
          title="Liked Videos"
        >
          <Heart className="w-5 h-5" />
        </button>

        <button
          onClick={() => setSelectedSubTab('BOOKMARKED')}
          className={`py-3 flex-1 flex justify-center border-b-2 transition ${
            selectedSubTab === 'BOOKMARKED' ? 'border-[#FFD600] text-[#FFD600]' : 'border-transparent text-white/40 hover:text-white'
          }`}
          title="Bookmarked"
        >
          <Bookmark className="w-5 h-5" />
        </button>

        {isViewingSelf && (
          <>
            <button
              onClick={() => setSelectedSubTab('PRIVATE')}
              className={`py-3 flex-1 flex justify-center border-b-2 transition ${
                selectedSubTab === 'PRIVATE' ? 'border-white text-white' : 'border-transparent text-white/40 hover:text-white'
              }`}
              title="Private Videos"
            >
              <Lock className="w-5 h-5" />
            </button>

            <button
              onClick={() => setSelectedSubTab('DRAFTS')}
              className={`py-3 flex-1 flex justify-center border-b-2 transition ${
                selectedSubTab === 'DRAFTS' ? 'border-[#00B0FF] text-[#00B0FF]' : 'border-transparent text-white/40 hover:text-white'
              }`}
              title="Drafts"
            >
              <FolderDown className="w-5 h-5" />
            </button>
          </>
        )}
      </div>

      {/* Tab Content Display */}
      <main className="flex-1 p-2">
        {selectedSubTab === 'DRAFTS' ? (
          <div className="space-y-2">
            {drafts.length === 0 ? (
              <div className="text-center py-16 text-white/50 text-xs">
                <FolderDown className="w-8 h-8 mx-auto text-white/20 mb-2" />
                No saved drafts yet. Record or upload in Create Studio to save drafts!
              </div>
            ) : (
              drafts.map((draft: DraftEntity) => (
                <div
                  key={draft.id}
                  className="flex items-center justify-between p-3 rounded-2xl bg-[#161324] border border-[#1F1B33]"
                >
                  <div className="flex items-center gap-3">
                    <img
                      src={draft.coverResName}
                      alt="Cover"
                      className="w-12 h-16 rounded-lg object-cover border border-[#322C52]"
                    />
                    <div>
                      <h4 className="text-xs font-bold text-white line-clamp-1">{draft.caption || 'Untitled Draft'}</h4>
                      <p className="text-[11px] text-white/50 mt-0.5">{draft.soundTitle}</p>
                      <span className="text-[10px] text-white/40 mt-1 block">
                        Saved {new Date(draft.updatedAt).toLocaleDateString()}
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => onPublishDraft(draft)}
                      className="px-3 py-1.5 rounded-lg rainbow-gradient text-white text-xs font-bold shadow hover:scale-105 transition flex items-center gap-1"
                    >
                      <UploadCloud className="w-3.5 h-3.5" />
                      <span>Post</span>
                    </button>
                    <button
                      onClick={() => onDeleteDraft(draft.id)}
                      className="p-1.5 rounded-lg bg-[#322C52]/40 text-red-400 hover:bg-red-950/40 transition"
                      title="Delete draft"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        ) : displayVideos.length === 0 ? (
          <div className="text-center py-16 text-white/50 text-xs">
            No videos in this section yet.
          </div>
        ) : (
          <div className="grid grid-cols-3 gap-1.5">
            {displayVideos.map((video) => (
              <div
                key={video.id}
                onClick={() => jumpToVideo(video.id)}
                className="group relative aspect-[9/16] rounded-lg overflow-hidden bg-[#161324] border border-[#1F1B33] cursor-pointer hover:border-[#00F5D4]"
              >
                <img
                  src={video.coverResName}
                  alt={video.caption}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent pointer-events-none" />
                <div className="absolute bottom-1 left-1 flex items-center gap-1 text-[10px] text-white font-semibold">
                  <Heart className="w-3 h-3 text-[#FF2A85] fill-[#FF2A85]" />
                  <span>{formatCount(video.likesCount)}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Edit Profile Dialog */}
      {showEditProfileDialog && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 max-w-md mx-auto">
          <form
            onSubmit={handleSaveProfile}
            className="w-full bg-[#161324] border border-[#322C52] rounded-3xl p-5 space-y-4"
          >
            <h3 className="text-base font-bold text-white">Edit Profile</h3>

            <div>
              <label className="text-xs text-white/60 block mb-1">Display Name</label>
              <input
                type="text"
                value={editName}
                onChange={(e) => setEditName(e.target.value)}
                className="w-full bg-[#0C0A14] border border-[#322C52] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
                required
              />
            </div>

            <div>
              <label className="text-xs text-white/60 block mb-1">Handle</label>
              <input
                type="text"
                value={editHandle}
                onChange={(e) => setEditHandle(e.target.value)}
                className="w-full bg-[#0C0A14] border border-[#322C52] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
                required
              />
            </div>

            <div>
              <label className="text-xs text-white/60 block mb-1">Bio</label>
              <textarea
                value={editBio}
                onChange={(e) => setEditBio(e.target.value)}
                rows={3}
                className="w-full bg-[#0C0A14] border border-[#322C52] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#00F5D4] resize-none"
              />
            </div>

            <div className="flex items-center justify-between py-2 border-t border-[#1F1B33]">
              <span className="text-xs text-white">Private Account</span>
              <input
                type="checkbox"
                checked={editIsPrivate}
                onChange={(e) => setEditIsPrivate(e.target.checked)}
                className="w-4 h-4 accent-[#FF2A85] rounded cursor-pointer"
              />
            </div>

            <div className="flex gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowEditProfileDialog(false)}
                className="flex-1 py-2 rounded-xl bg-[#1F1B33] text-white text-xs font-bold"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="flex-1 py-2 rounded-xl rainbow-gradient text-white text-xs font-bold"
              >
                Save
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Switch Account Modal */}
      {showAccountsDialog && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-end justify-center p-0 max-w-md mx-auto">
          <div className="w-full bg-[#161324] border-t border-[#322C52] rounded-t-3xl p-5 space-y-4 max-h-[80vh] overflow-y-auto no-scrollbar">
            <h3 className="text-base font-bold text-white">Switch Accounts</h3>

            <div className="space-y-2">
              {allAccounts.map((account) => (
                <div
                  key={account.userId}
                  onClick={() => {
                    onSwitchAccount(account.userId);
                    setShowAccountsDialog(false);
                  }}
                  className={`flex items-center justify-between p-3 rounded-2xl border transition cursor-pointer ${
                    account.userId === currentUser.userId
                      ? 'bg-[#1F1B33] border-[#00F5D4]'
                      : 'bg-[#0C0A14] border-[#1F1B33] hover:border-[#322C52]'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <img
                      src={account.avatarUrl}
                      alt={account.displayName}
                      className="w-10 h-10 rounded-full object-cover border border-[#322C52]"
                    />
                    <div>
                      <h4 className="text-xs font-bold text-white flex items-center gap-1">
                        {account.displayName}
                        {account.isVerified && <CheckCircle className="w-3.5 h-3.5 text-[#00F5D4]" />}
                      </h4>
                      <p className="text-[11px] text-white/50">{account.handle}</p>
                    </div>
                  </div>
                  {account.userId === currentUser.userId && (
                    <CheckCircle className="w-5 h-5 text-[#00F5D4]" />
                  )}
                </div>
              ))}
            </div>

            {/* Actions: Add Account & Change PIN */}
            <div className="pt-2 border-t border-[#1F1B33] space-y-2">
              <button
                onClick={() => {
                  setShowAccountsDialog(false);
                  setShowAuthDialog('SIGNUP');
                }}
                className="w-full py-2.5 rounded-xl bg-[#1F1B33] hover:bg-[#322C52] text-xs font-bold text-white flex items-center justify-center gap-2"
              >
                <Users className="w-4 h-4 text-[#00F5D4]" />
                <span>Add Existing / New Account</span>
              </button>

              <button
                onClick={() => {
                  setShowAccountsDialog(false);
                  setShowChangePinDialog(true);
                }}
                className="w-full py-2.5 rounded-xl bg-[#1F1B33] hover:bg-[#322C52] text-xs font-bold text-white flex items-center justify-center gap-2"
              >
                <KeyRound className="w-4 h-4 text-[#FFD600]" />
                <span>Change Account PIN</span>
              </button>

              <button
                onClick={() => setShowAccountsDialog(false)}
                className="w-full py-2 text-xs text-white/60 hover:text-white"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Auth Login / Signup Dialog */}
      {showAuthDialog && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 max-w-md mx-auto">
          <div className="w-full bg-[#161324] border border-[#322C52] rounded-3xl p-5 space-y-4">
            <h3 className="text-base font-bold text-white">
              {showAuthDialog === 'LOGIN' ? 'Login with PIN' : 'Create New Account'}
            </h3>

            {showAuthDialog === 'SIGNUP' && (
              <div>
                <label className="text-xs text-white/60 block mb-1">Full Name</label>
                <input
                  type="text"
                  value={authName}
                  onChange={(e) => setAuthName(e.target.value)}
                  placeholder="e.g. Maya Lin"
                  className="w-full bg-[#0C0A14] border border-[#322C52] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
                  required
                />
              </div>
            )}

            <div>
              <label className="text-xs text-white/60 block mb-1">Handle (@username)</label>
              <input
                type="text"
                value={authHandle}
                onChange={(e) => setAuthHandle(e.target.value)}
                placeholder="@username"
                className="w-full bg-[#0C0A14] border border-[#322C52] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
                required
              />
            </div>

            <div>
              <label className="text-xs text-white/60 block mb-1">Security PIN (4 digits)</label>
              <input
                type="password"
                maxLength={4}
                value={authPin}
                onChange={(e) => setAuthPin(e.target.value)}
                placeholder="1234"
                className="w-full bg-[#0C0A14] border border-[#322C52] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
                required
              />
            </div>

            <div className="flex gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowAuthDialog(null)}
                className="flex-1 py-2 rounded-xl bg-[#1F1B33] text-white text-xs font-bold"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={showAuthDialog === 'LOGIN' ? handleLoginSubmit : handleSignupSubmit}
                className="flex-1 py-2 rounded-xl rainbow-gradient text-white text-xs font-bold"
              >
                {showAuthDialog === 'LOGIN' ? 'Login' : 'Sign Up'}
              </button>
            </div>

            <p className="text-center text-[11px] text-white/50 pt-1">
              {showAuthDialog === 'LOGIN' ? (
                <span>
                  Don&apos;t have an account?{' '}
                  <button
                    onClick={() => setShowAuthDialog('SIGNUP')}
                    className="text-[#00F5D4] underline"
                  >
                    Sign up
                  </button>
                </span>
              ) : (
                <span>
                  Already registered?{' '}
                  <button
                    onClick={() => setShowAuthDialog('LOGIN')}
                    className="text-[#00F5D4] underline"
                  >
                    Login
                  </button>
                </span>
              )}
            </p>
          </div>
        </div>
      )}

      {/* Change PIN Dialog */}
      {showChangePinDialog && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 max-w-md mx-auto">
          <form
            onSubmit={handleChangePinSubmit}
            className="w-full bg-[#161324] border border-[#322C52] rounded-3xl p-5 space-y-4"
          >
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <Shield className="w-5 h-5 text-[#FFD600]" />
              Change Security PIN
            </h3>

            <div>
              <label className="text-xs text-white/60 block mb-1">Old PIN</label>
              <input
                type="password"
                maxLength={4}
                value={oldPin}
                onChange={(e) => setOldPin(e.target.value)}
                placeholder="Current 4-digit PIN"
                className="w-full bg-[#0C0A14] border border-[#322C52] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
                required
              />
            </div>

            <div>
              <label className="text-xs text-white/60 block mb-1">New PIN</label>
              <input
                type="password"
                maxLength={4}
                value={newPin}
                onChange={(e) => setNewPin(e.target.value)}
                placeholder="New 4-digit PIN"
                className="w-full bg-[#0C0A14] border border-[#322C52] rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-[#00F5D4]"
                required
              />
            </div>

            <div className="flex gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowChangePinDialog(false)}
                className="flex-1 py-2 rounded-xl bg-[#1F1B33] text-white text-xs font-bold"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="flex-1 py-2 rounded-xl bg-[#FFD600] text-black text-xs font-bold"
              >
                Update PIN
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
