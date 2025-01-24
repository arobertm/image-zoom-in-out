import { defineStore } from 'pinia';
import { useRouter } from 'vue-router';

export const useNotificationStore = defineStore('notificationStore', {
  state: () => ({
    wsConnected: false,
    notification: null,
    downloadUrl: null
  }),

  actions: {
    setupWebSocket() {
      const ws = new WebSocket('ws://localhost:7010/ws');
      const router = useRouter();

      ws.onopen = () => {
        console.log('Connected to WebSocket');
        this.wsConnected = true;
      };

      ws.onmessage = (event) => {
        console.log('WebSocket message received:', event.data);
        try {
          const data = JSON.parse(event.data);
          if (data.type === 'imageProcessed') {
            this.downloadUrl = `http://localhost:3000/api/images/${data.imageId}`;
            this.notification = {
              message: 'Image processing completed successfully!',
              type: 'success'
            };

            setTimeout(() => {
              router.push('/download');
            }, 1000);


            setTimeout(() => {
              this.clearNotification();
            }, 5000);
          }
        } catch (error) {
          console.error('Error processing WebSocket message:', error);
        }
      };

      ws.onclose = () => {
        console.log('WebSocket disconnected');
        this.wsConnected = false;
        setTimeout(() => this.setupWebSocket(), 5000);
      };

      ws.onerror = (error) => {
        console.error('WebSocket error:', error);
      };
    },

    clearNotification() {
      this.notification = null;
    }
  }
});