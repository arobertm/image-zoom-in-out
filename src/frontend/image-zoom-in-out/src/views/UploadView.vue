<template>
  <div class="bg-white shadow-lg rounded-lg p-6 max-w-md mx-auto">
    <h2 class="text-xl font-semibold mb-4 text-gray-800">Upload BMP Image</h2>
    <input 
      type="file" 
      @change="onFileSelected" 
      accept=".bmp" 
      class="block w-full text-sm text-gray-600 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-600 file:text-white hover:file:bg-blue-700"
    />
    <button 
      @click="uploadImage" 
      :disabled="!file" 
      class="mt-4 bg-blue-600 text-white px-6 py-2 rounded-lg disabled:opacity-50"
    >
      Upload
    </button>
  </div>
</template>

<script>
import { useImageStore } from '../stores/imageStore';

export default {
  data() {
    return {
      file: null,
    };
  },
  methods: {
    onFileSelected(event) {
      this.file = event.target.files[0];
    },
    async uploadImage() {
      const imageStore = useImageStore();
      const formData = new FormData();
      formData.append('image', this.file);

      try {
        const response = await fetch('/api/upload', {
          method: 'POST',
          body: formData,
        });

        if (response.ok) {
          const result = await response.json();
          imageStore.setDownloadUrl(result.downloadUrl);
          this.$router.push('/download');
        } else {
          alert('Failed to upload image.');
        }
      } catch (error) {
        console.error('Upload Error:', error);
      }
    },
  },
};
</script>
