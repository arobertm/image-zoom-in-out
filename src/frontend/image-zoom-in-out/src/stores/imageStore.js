import { defineStore } from 'pinia';

export const useImageStore = defineStore('imageStore', {
  state: () => ({
    uploadedImage: null,
    downloadUrl: null,
  }),
  actions: {
    setUploadedImage(file) {
      this.uploadedImage = file;
    },
    setDownloadUrl(url) {
      this.downloadUrl = url;
    },
  },
});
