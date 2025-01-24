import { defineStore } from 'pinia';

export const useImageStore = defineStore('imageStore', {
  state: () => ({
    uploadedImage: null,
    downloadUrl: null,
    isProcessing: false,
    error: null,
    uploadProgress: 0
  }),

  actions: {
    setUploadedImage(file) {
      this.uploadedImage = file;
    },

    setDownloadUrl(url) {
      this.downloadUrl = url;
    },

    async uploadImage(file, zoomLevel) {
      this.isProcessing = true;
      this.error = null;
      this.uploadProgress = 0;

      try {
        const formData = new FormData();
        formData.append('image', file);
        formData.append('zoomLevel', zoomLevel);

        const response = await fetch('http://localhost:7010/api/image/zoom', {
          method: 'POST',
          body: formData
        });

        if (!response.ok) {
          throw new Error(`Upload failed: ${response.statusText}`);
        }

        const result = await response.json();
        console.log('Upload result:', result);

        if (result.imageId) {
          this.imageId = result.imageId;
        }

        return result;
      } catch (error) {
        console.error('Upload error:', error);
        this.error = error.message;
        throw error;
      } finally {
        this.isProcessing = false;
      }
    },

    resetState() {
      this.uploadedImage = null;
      this.downloadUrl = null;
      this.isProcessing = false;
      this.error = null;
      this.uploadProgress = 0;
    }
  },

  getters: {
    isUploading: (state) => state.isProcessing,
    hasError: (state) => state.error !== null,
    getProgress: (state) => state.uploadProgress
  }
});