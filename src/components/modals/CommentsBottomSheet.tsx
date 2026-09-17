import React, { useState } from 'react';
import { X, Send, Heart, Trash2, Reply } from 'lucide-react';
import { useTikTok } from '../../context/TikTokContext';
import { CommentEntity, VideoEntity } from '../../types';

export function formatTimeAgo(timestampMs: number): string {
  const diff = Date.now() - timestampMs;
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (days > 0) return `${days}d`;
  if (hours > 0) return `${hours}h`;
  if (minutes > 0) return `${minutes}m`;
  return 'just now';
}

interface CommentsBottomSheetProps {
  video: VideoEntity;
  onClose: () => void;
}

export const CommentsBottomSheet: React.FC<CommentsBottomSheetProps> = ({ video, onClose }) => {
  const {
    getVideoComments,
    onAddComment,
    onLikeComment,
    onDeleteComment,
    currentUser,
  } = useTikTok();

  const [commentInput, setCommentInput] = useState<string>('');
  const [replyingTo, setReplyingTo] = useState<CommentEntity | null>(null);

  const videoComments = getVideoComments(video.id);
  const emojiChips = ['❤️', '🔥', '😂', '👏', '💀', '🤩', '🙌', '💯'];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (commentInput.trim()) {
      const text = replyingTo
        ? `@${replyingTo.authorHandle} ${commentInput.trim()}`
        : commentInput.trim();
      onAddComment(video.id, text, replyingTo ? replyingTo.id : null);
      setCommentInput('');
      setReplyingTo(null);
    }
  };

  const handleEmojiClick = (emoji: string) => {
    setCommentInput((prev) => prev + emoji);
  };

  return (
    <div
      id="comments-bottom-sheet-overlay"
      className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-end justify-center p-0 max-w-md mx-auto"
      onClick={onClose}
    >
      <div
        id="comments-sheet-container"
        onClick={(e) => e.stopPropagation()}
        className="w-full bg-[#161324] border-t border-[#322C52] rounded-t-3xl flex flex-col h-[75vh] max-h-[600px] overflow-hidden shadow-2xl"
      >
        {/* Drag handle & Header */}
        <div className="pt-3 pb-2 px-4 flex flex-col items-center border-b border-[#1F1B33]">
          <div className="w-10 h-1 rounded-full bg-white/20 mb-2" />
          <div className="w-full flex items-center justify-between">
            <div className="w-6" />
            <h3 className="text-sm font-bold text-white">
              {videoComments.length} {videoComments.length === 1 ? 'comment' : 'comments'}
            </h3>
            <button
              onClick={onClose}
              className="p-1 rounded-full text-white/60 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Quick Emoji Reaction Chips */}
        <div className="flex items-center gap-2 px-4 py-2 border-b border-[#1F1B33]/60 overflow-x-auto no-scrollbar">
          {emojiChips.map((emoji) => (
            <button
              key={emoji}
              onClick={() => handleEmojiClick(emoji)}
              className="text-base px-2 py-0.5 rounded-full bg-[#1F1B33] hover:bg-[#322C52] transition shrink-0"
            >
              {emoji}
            </button>
          ))}
        </div>

        {/* Comments Scrollable List */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4 no-scrollbar">
          {videoComments.length === 0 ? (
            <div className="text-center py-16 text-white/40 text-xs">
              Be the first to comment on this video! 💬
            </div>
          ) : (
            videoComments.map((comment) => {
              const isMine =
                comment.authorId === currentUser.userId ||
                comment.authorHandle === currentUser.handle;

              return (
                <div key={comment.id} className="flex items-start justify-between gap-3 group">
                  {/* Left: Avatar & Text */}
                  <div className="flex items-start gap-3 flex-1">
                    <img
                      src={
                        comment.authorAvatarUrl ||
                        'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80'
                      }
                      alt={comment.authorName}
                      className="w-8 h-8 rounded-full object-cover border border-[#322C52] shrink-0 mt-0.5"
                    />
                    <div className="flex-1 text-left">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-white/90">
                          {comment.authorHandle}
                        </span>
                        <span className="text-[10px] text-white/40">
                          {formatTimeAgo(comment.timestamp)}
                        </span>
                      </div>
                      <p className="text-xs text-white/90 mt-1 leading-relaxed break-words">
                        {comment.content}
                      </p>

                      {/* Reply & Delete actions */}
                      <div className="flex items-center gap-3 mt-1.5 text-[11px] text-white/50">
                        <button
                          onClick={() => setReplyingTo(comment)}
                          className="hover:text-[#00F5D4] flex items-center gap-1 transition"
                        >
                          <Reply className="w-3 h-3" />
                          <span>Reply</span>
                        </button>

                        {isMine && (
                          <button
                            onClick={() => onDeleteComment(comment)}
                            className="hover:text-red-400 flex items-center gap-1 transition"
                          >
                            <Trash2 className="w-3 h-3" />
                            <span>Delete</span>
                          </button>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Right: Like Comment */}
                  <button
                    onClick={() => onLikeComment(comment)}
                    className="flex flex-col items-center pt-1 text-white/60 hover:text-[#FF2A85] transition"
                  >
                    <Heart
                      className={`w-4 h-4 ${
                        comment.isLiked ? 'text-[#FF2A85] fill-[#FF2A85]' : ''
                      }`}
                    />
                    <span className="text-[10px] font-semibold mt-0.5">
                      {comment.likesCount > 0 ? comment.likesCount : ''}
                    </span>
                  </button>
                </div>
              );
            })
          )}
        </div>

        {/* Replying Banner */}
        {replyingTo && (
          <div className="px-4 py-1.5 bg-[#1F1B33] border-t border-[#322C52] flex items-center justify-between text-xs text-white/80">
            <span>
              Replying to <span className="font-bold text-[#00F5D4]">{replyingTo.authorHandle}</span>
            </span>
            <button
              onClick={() => setReplyingTo(null)}
              className="text-white/60 hover:text-white"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        )}

        {/* Input Bar */}
        <form
          onSubmit={handleSubmit}
          className="p-3 bg-[#0C0A14] border-t border-[#1F1B33] flex items-center gap-2"
        >
          <input
            type="text"
            value={commentInput}
            onChange={(e) => setCommentInput(e.target.value)}
            placeholder={
              replyingTo ? `Reply to ${replyingTo.authorHandle}...` : 'Add a thoughtful comment...'
            }
            className="flex-1 bg-[#161324] border border-[#322C52] rounded-full px-4 py-2 text-xs text-white placeholder:text-white/40 focus:outline-none focus:border-[#00F5D4]"
          />
          <button
            type="submit"
            disabled={!commentInput.trim()}
            className="w-9 h-9 rounded-full rainbow-gradient flex items-center justify-center text-white disabled:opacity-40 transition shadow-sm"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
      </div>
    </div>
  );
};
