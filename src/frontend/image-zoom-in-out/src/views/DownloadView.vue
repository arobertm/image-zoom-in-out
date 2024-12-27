<template>
  <div class="bg-white shadow-lg rounded-lg p-6 max-w-md mx-auto">
    <h2 class="text-xl font-semibold mb-4 text-gray-800">Download Processed Image</h2>
    <p class="text-gray-600 mb-4">Your processed BMP image is ready for download.</p>
    <button 
      @click="downloadImage" 
      class="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700"
    >
      Download
    </button>
  </div>
</template>

<script>
import { useImageStore } from '../stores/imageStore';

export default {
  computed: {
    downloadUrl() {
      const imageStore = useImageStore();
      return imageStore.downloadUrl;
    },
  },
  methods: {
    downloadImage() {
      if (this.downloadUrl) {
        const a = document.createElement('a');
        a.href = this.downloadUrl;
        a.download = 'processed_image.bmp';
        a.click();
      } else {
        alert('No image available for download.');
      }
    },
  },
};
</script>
