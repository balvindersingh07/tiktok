import { videoRepository } from '../repositories/video.repository.js';

export class FeedService {
  async getForYouFeed(options: {
    viewerId?: string;
    category?: string;
    cursor?: string;
    limit?: number;
  }) {
    return videoRepository.findFeedVideos(options);
  }

  async getFollowingFeed(options: {
    viewerId: string;
    cursor?: string;
    limit?: number;
  }) {
    return videoRepository.findFollowingVideos(options);
  }
}

export const feedService = new FeedService();
