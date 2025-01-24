<template>
  <div class="bg-black/30 backdrop-blur-md rounded-xl p-8">
    <h2 class="text-xl font-semibold mb-4 text-white">Download Processed Image</h2>
    
    <div v-if="notificationStore.downloadUrl" class="space-y-6">
      <div class="relative group max-w-2xl mx-auto">
        <div class="absolute -inset-0.5 bg-gradient-to-r from-green-600 to-green-500 
                    rounded-lg blur opacity-30 group-hover:opacity-50 transition-opacity"></div>
        <div class="relative bg-black/40 rounded-lg p-4">
          <img 
            :src="notificationStore.downloadUrl"
            alt="Processed image"
            class="w-full h-auto max-h-[500px] object-contain rounded-lg shadow-lg"
          />
        </div>
      </div>

      <div class="flex justify-center mt-4">
        <a 
          :href="notificationStore.downloadUrl"
          download
          class="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 
                 transition-all duration-200 transform hover:scale-105 active:scale-95
                 flex items-center space-x-2"
        >
          <span class="text-lg font-bold">&darr;</span>
          <span>Download Image</span>
        </a>
      </div>
    </div>

    <div v-else class="text-center py-12">
      <div class="mb-4 text-gray-400">
        No processed image available. Please upload an image first.
      </div>
      <router-link 
        to="/upload"
        class="inline-block px-6 py-2 text-green-500 hover:text-green-400 transition-colors"
      >
        Go to Upload
      </router-link>
    </div>
  </div>
</template>

<script>
import { useNotificationStore } from '../stores/notificationStore';

export default {
  name: 'DownloadView',
  setup() {
    const notificationStore = useNotificationStore();
    return { notificationStore };
  }
}
</script>