import { EventEmitter } from 'events';

class AppEventBus extends EventEmitter {
  constructor() {
    super();
    this.setMaxListeners(200);
  }

  emitDirectMessage(receiverId: string, message: any) {
    this.emit(`dm:${receiverId}`, message);
  }

  emitNotification(recipientId: string, notification: any) {
    this.emit(`notif:${recipientId}`, notification);
  }

  emitVideoProcessed(videoId: string, status: string) {
    this.emit(`video:${videoId}`, { videoId, status });
  }
}

export const eventBus = new AppEventBus();
