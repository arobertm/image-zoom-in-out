<template>
  <div class="bg-black/30 backdrop-blur-md rounded-xl p-8">
    <h2 class="text-xl font-semibold mb-4 text-white">Download Processed Image</h2>
    
    <div v-if="downloadUrl" class="space-y-6">
      <p class="text-gray-300">
        Your processed BMP image is ready for download.
      </p>
      
      <div class="relative group">
        <div class="absolute -inset-0.5 bg-gradient-to-r from-red-600 to-red-500 
                    rounded-lg blur opacity-30 group-hover:opacity-50 transition-opacity"></div>
        <div class="relative bg-black/40 rounded-lg p-4">
          <img 
            :src="downloadUrl" 
            alt="Processed image preview" 
            class="w-full rounded-lg shadow-lg"
          />
        </div>
      </div>

      <button
        @click="downloadImage"
        class="w-full bg-red-600 text-white px-6 py-3 rounded-lg
               hover:bg-red-700 transition-all duration-200
               transform hover:scale-105 active:scale-95
               flex items-center justify-center space-x-2"
      >
        <span class="text-lg">↓</span>
        <span>Download Image</span>
      </button>
    </div>

    <div v-else class="text-center py-12">
      <div class="mb-6">
        <div class="w-16 h-16 mx-auto border-2 border-red-500 rounded-full
                    flex items-center justify-center">
          <span class="text-2xl text-red-500">?</span>
        </div>
      </div>
      <p class="text-gray-400 mb-4">No processed image available for download.</p>
      <router-link 
        to="/upload"
        class="inline-block px-6 py-2 text-red-500 hover:text-red-400
               transition-colors border border-red-500 rounded-lg
               hover:border-red-400"
      >
        Upload an image first
      </router-link>
    </div>
  </div>
</template>

<script>
import { useImageStore } from '../stores/imageStore'
import { computed } from 'vue'

export default {
  name: 'DownloadView',
  setup() {
    const imageStore = useImageStore()
    
    const downloadUrl = computed(() => imageStore.downloadUrl)

    const downloadImage = () => {
      if (downloadUrl.value) {
        const a = document.createElement('a')
        a.href = downloadUrl.value
        a.download = 'processed_image.bmp'
        a.click()
      }
    }

    return {
      downloadUrl,
      downloadImage
    }
  }
}
</script>

<style scoped>
@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.animate-shimmer {
  animation: shimmer 2s infinite;
}
</style>