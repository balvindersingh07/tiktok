/**
 * Tashan API Client - Real Remote Backend Network Layer
 * Communicates with the live Hono PostgreSQL backend
 */

const API_BASE = '/api';

export function getAuthToken(): string | null {
  return localStorage.getItem('tashan_auth_token');
}

export function setAuthToken(token: string | null) {
  if (token) {
    localStorage.setItem('tashan_auth_token', token);
  } else {
    localStorage.removeItem('tashan_auth_token');
  }
}

async function request<T = any>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getAuthToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  const contentType = res.headers.get('content-type') || '';
  let data: any;
  if (contentType.includes('application/json')) {
    data = await res.json();
  } else {
    data = await res.text();
  }

  if (!res.ok) {
    const message = data?.message || data?.error || `Request failed with status ${res.status}`;
    throw new Error(message);
  }

  return data;
}

export const api = {
  // Authentication & Current User
  auth: {
    async login(handleOrEmail: string, passwordPin: string) {
      const res = await request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ handleOrEmail, passwordPin }),
      });
      if (res.token) {
        setAuthToken(res.token);
      }
      return res;
    },

    async signup(payload: {
      handle: string;
      email: string;
      passwordPin: string;
      displayName?: string;
      bio?: string;
    }) {
      const res = await request('/auth/signup', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      if (res.token) {
        setAuthToken(res.token);
      }
      return res;
    },

    async getMe() {
      return request('/auth/me');
    },

    async changePin(oldPin: string, newPin: string) {
      return request('/auth/change-pin', {
        method: 'POST',
        body: JSON.stringify({ oldPin, newPin }),
      });
    },

    async logout() {
      try {
        await request('/auth/logout', { method: 'POST' });
      } catch (err) {
        // Ignore logout error
      } finally {
        setAuthToken(null);
      }
    },
  },

  // Feed & Recommendations
  feed: {
    async getForYou(category?: string, cursor?: string) {
      const params = new URLSearchParams();
      if (category && category !== 'fyp') params.set('category', category);
      if (cursor) params.set('cursor', cursor);
      params.set('limit', '20');
      return request(`/feed/for-you?${params.toString()}`);
    },

    async getFollowing(cursor?: string) {
      const params = new URLSearchParams();
      if (cursor) params.set('cursor', cursor);
      params.set('limit', '20');
      return request(`/feed/following?${params.toString()}`);
    },
  },

  // Videos & Social Interactions
  videos: {
    async getAll(category?: string, authorId?: string) {
      const params = new URLSearchParams();
      if (category) params.set('category', category);
      if (authorId) params.set('authorId', authorId);
      return request(`/videos?${params.toString()}`);
    },

    async getById(id: string | number) {
      return request(`/videos/${id}`);
    },

    async publish(videoData: {
      caption: string;
      soundTitle?: string;
      soundAuthor?: string;
      soundId?: string;
      coverResName?: string;
      videoPath?: string;
      category?: string;
      hashtags?: string;
      durationSeconds?: number;
      isPrivate?: boolean;
      allowComments?: boolean;
      allowDuet?: boolean;
      allowStitch?: boolean;
      duetWithVideoId?: string;
      stitchWithVideoId?: string;
    }) {
      return request('/videos', {
        method: 'POST',
        body: JSON.stringify(videoData),
      });
    },

    async upload(formData: FormData) {
      const token = getAuthToken();
      const headers: Record<string, string> = {};
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      const res = await fetch(`${API_BASE}/videos/upload`, {
        method: 'POST',
        headers,
        body: formData,
      });
      if (!res.ok) {
        throw new Error('Video upload failed');
      }
      return res.json();
    },

    async createUploadSession(filename: string, contentType: string, sizeBytes: number) {
      return request<{
        success: boolean;
        sessionId: string;
        uploadUrl: string;
        method: string;
        headers: Record<string, string>;
        key: string;
        sessionToken: string;
        publicUrl: string;
        expiresIn: number;
      }>('/videos/upload-session', {
        method: 'POST',
        body: JSON.stringify({ filename, contentType, sizeBytes }),
      });
    },

    async directUploadToStorage(uploadUrl: string, method: string, data: Blob | ArrayBuffer, headers?: Record<string, string>) {
      const res = await fetch(uploadUrl, {
        method: method || 'PUT',
        headers: headers || { 'Content-Type': 'video/mp4' },
        body: data,
      });
      if (!res.ok) {
        throw new Error(`Direct storage upload failed with status ${res.status}`);
      }
      return res.json().catch(() => ({ success: true }));
    },

    async confirmUpload(payload: {
      key: string;
      sessionId?: string;
      caption?: string;
      soundTitle?: string;
      soundAuthor?: string;
      soundId?: string | null;
      category?: string;
      hashtags?: string;
      isPrivate?: boolean;
      allowComments?: boolean;
    }) {
      return request<{
        success: boolean;
        videoId: string;
        jobId: string;
        status: string;
        video: any;
      }>('/videos/confirm-upload', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
    },

    async getProcessingStatus(jobId: string) {
      return request<{
        success: boolean;
        jobId: string;
        videoId: string;
        status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
        attempts: number;
        errorMessage?: string;
        video: {
          id: string;
          status: string;
          videoUrl: string;
          thumbnailUrl: string;
          durationSeconds: number;
          width?: number;
          height?: number;
        };
      }>(`/videos/processing-status/${jobId}`);
    },

    async toggleLike(videoId: string | number) {
      return request(`/videos/${videoId}/like`, { method: 'POST' });
    },

    async toggleSave(videoId: string | number) {
      return request(`/videos/${videoId}/save`, { method: 'POST' });
    },

    async toggleRepost(videoId: string | number) {
      return request(`/videos/${videoId}/repost`, { method: 'POST' });
    },

    async recordView(videoId: string | number, durationMs: number = 15000, completionPercent: number = 1) {
      return request(`/videos/${videoId}/view`, {
        method: 'POST',
        body: JSON.stringify({ durationMs, completionPercent }),
      }).catch(() => {});
    },

    async share(videoId: string | number) {
      return request(`/videos/${videoId}/share`, { method: 'POST' });
    },

    async deleteVideo(videoId: string | number) {
      return request(`/videos/${videoId}`, { method: 'DELETE' });
    },

    // Video Comments
    async getComments(videoId: string | number) {
      return request(`/videos/${videoId}/comments`);
    },

    async addComment(videoId: string | number, content: string, parentCommentId?: string) {
      return request(`/videos/${videoId}/comments`, {
        method: 'POST',
        body: JSON.stringify({ content, parentCommentId }),
      });
    },

    async toggleCommentLike(commentId: string | number) {
      return request(`/videos/comments/${commentId}/like`, { method: 'POST' });
    },

    async deleteComment(commentId: string | number) {
      return request(`/videos/comments/${commentId}`, { method: 'DELETE' }).catch(() => {});
    },
  },

  // Users & Profiles
  users: {
    async getAll() {
      return request('/users');
    },

    async getByHandle(handle: string) {
      return request(`/users/${encodeURIComponent(handle)}`);
    },

    async updateProfile(data: {
      displayName?: string;
      bio?: string;
      avatarUrl?: string;
      isPrivate?: boolean;
      allowComments?: boolean;
      allowDuet?: boolean;
      allowStitch?: boolean;
      allowDownloads?: boolean;
      allowDirectMessages?: string;
      showLikedVideos?: boolean;
      showFollowingList?: boolean;
    }) {
      return request('/users/profile', {
        method: 'PUT',
        body: JSON.stringify(data),
      });
    },

    async toggleFollow(handle: string) {
      return request(`/users/${encodeURIComponent(handle)}/follow`, {
        method: 'POST',
      });
    },

    async toggleBlock(handle: string) {
      return request(`/users/${encodeURIComponent(handle)}/block`, {
        method: 'POST',
      });
    },

    async toggleMute(handle: string) {
      return request(`/users/${encodeURIComponent(handle)}/mute`, {
        method: 'POST',
      });
    },

    async getUserVideos(handle: string) {
      return request(`/users/${encodeURIComponent(handle)}/videos`);
    },

    async getBlocked() {
      return request('/users/blocked');
    },

    async getMuted() {
      return request('/users/muted');
    },

    async registerDeviceToken(token: string, platform: 'android' | 'ios' | 'web' = 'android') {
      return request('/users/device-token', {
        method: 'POST',
        body: JSON.stringify({ token, platform }),
      }).catch(() => {});
    },
  },

  // Sounds & Audio
  sounds: {
    async getAll(category?: string) {
      const params = category ? `?category=${category}` : '';
      return request(`/sounds${params}`);
    },

    async getById(id: string) {
      return request(`/sounds/${id}`);
    },

    async toggleFavorite(id: string) {
      return request(`/sounds/${id}/favorite`, { method: 'POST' });
    },
  },

  // Notifications
  notifications: {
    async getAll() {
      return request('/notifications');
    },

    async getUnreadCount() {
      return request('/notifications/unread-count');
    },

    async markAllRead() {
      return request('/notifications/read-all', { method: 'POST' });
    },

    async markRead(id: string) {
      return request(`/notifications/${id}/read`, { method: 'POST' });
    },

    async getPreferences() {
      return request('/notifications/preferences');
    },

    async updatePreferences(data: {
      likesEnabled?: boolean;
      commentsEnabled?: boolean;
      followsEnabled?: boolean;
      mentionsEnabled?: boolean;
      dmsEnabled?: boolean;
    }) {
      return request('/notifications/preferences', {
        method: 'PUT',
        body: JSON.stringify(data),
      });
    },
  },

  // Direct Messages
  messages: {
    async getConversations() {
      return request('/messages/conversations');
    },

    async getMessages(conversationId: string) {
      return request(`/messages/conversations/${conversationId}/messages`);
    },

    async send(receiverId: string, content: string) {
      return request('/messages/send', {
        method: 'POST',
        body: JSON.stringify({ receiverId, content }),
      });
    },

    /**
     * Connect to real-time SSE stream for instant DMs and notifications
     */
    connectStream(handlers: {
      onDm?: (dm: any) => void;
      onNotification?: (notif: any) => void;
      onConnected?: () => void;
    }): () => void {
      const token = getAuthToken();
      if (!token) return () => {};

      // EventSource doesn't accept headers natively in browser, but we can pass token or use fetch stream
      const controller = new AbortController();

      fetch(`${API_BASE}/messages/stream`, {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: 'text/event-stream',
        },
        signal: controller.signal,
      }).then(async (response) => {
        if (!response.ok || !response.body) return;
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n\n');
          buffer = lines.pop() || '';

          for (const block of lines) {
            const parts = block.split('\n');
            let event = 'message';
            let dataStr = '';
            for (const line of parts) {
              if (line.startsWith('event: ')) {
                event = line.substring(7).trim();
              } else if (line.startsWith('data: ')) {
                dataStr = line.substring(6).trim();
              }
            }

            if (!dataStr) continue;

            try {
              const parsed = JSON.parse(dataStr);
              if (event === 'dm' && handlers.onDm) {
                handlers.onDm(parsed);
              } else if (event === 'notification' && handlers.onNotification) {
                handlers.onNotification(parsed);
              } else if (event === 'connected' && handlers.onConnected) {
                handlers.onConnected();
              }
            } catch (e) {}
          }
        }
      }).catch(() => {});

      return () => {
        controller.abort();
      };
    },
  },

  // Search & Discover
  search: {
    async query(q: string) {
      return request(`/search?q=${encodeURIComponent(q)}`);
    },

    async getTrending() {
      return request('/search/trending');
    },
  },

  // Reports
  reports: {
    async submit(data: {
      targetType: 'video' | 'comment' | 'user';
      targetId: string;
      reason: string;
      details?: string;
    }) {
      return request('/reports', {
        method: 'POST',
        body: JSON.stringify(data),
      });
    },
  },

  // Drafts
  drafts: {
    async getAll() {
      return request('/drafts');
    },

    async save(draft: {
      caption: string;
      soundTitle?: string;
      soundAuthor?: string;
      coverResName?: string;
      durationSeconds?: number;
    }) {
      return request('/drafts', {
        method: 'POST',
        body: JSON.stringify(draft),
      });
    },

    async delete(id: string) {
      return request(`/drafts/${id}`, { method: 'DELETE' });
    },
  },

  // Analytics
  analytics: {
    async logEvent(event: {
      eventType: string;
      targetId?: string;
      targetType?: string;
      categoryTag?: string;
      watchDurationMs?: number;
      completionPercent?: number;
      metadata?: Record<string, any>;
    }) {
      return request('/analytics/event', {
        method: 'POST',
        body: JSON.stringify(event),
      }).catch(() => {});
    },

    async getCreatorMetrics() {
      return request('/analytics/creator');
    },
  },

  async health() {
    return request('/health');
  },
};
