<template>
  <div class="bg-black/30 backdrop-blur-md rounded-xl p-8">
    <h2 class="text-xl font-semibold mb-4 text-white">Upload BMP Image</h2>
    <div class="space-y-4">
      <div class="relative group">
        <input
          type="file"
          @change="onFileSelected"
          accept=".bmp"
          class="block w-full text-sm text-gray-300
                 file:mr-4 file:py-2 file:px-4
                 file:rounded-lg file:border-0
                 file:text-sm file:font-semibold
                 file:bg-red-600 file:text-white
                 hover:file:bg-red-700
                 file:transition-colors
                 focus:outline-none"
        />
      </div>
      
      <div v-if="selectedImage" class="space-y-4">
        <!-- Image Preview -->
        <div class="relative overflow-hidden rounded-lg bg-black/20 p-4">
          <img
            :src="imagePreview"
            alt="Preview"
            class="w-full max-h-[400px] object-contain rounded-lg"
          />
        </div>

        <div class="flex flex-col space-y-2">
          <label class="text-white">Zoom Level: {{ selectedZoom }}x</label>
          <select 
            v-model="selectedZoom"
            class="w-full p-2 rounded-lg bg-gray-800 text-white border border-gray-700"
          >
            <option v-for="level in zoomLevels" :key="level" :value="level">
              {{ level }}x
            </option>
          </select>
        </div>


        <div v-if="isProcessing" class="flex justify-center">
          <div class="flex items-center space-x-2 text-white/80">
            <div class="animate-spin h-5 w-5 border-2 border-red-500 rounded-full border-t-transparent"></div>
            <span>Processing...</span>
          </div>
        </div>

        <button
          @click="uploadImage"
          :disabled="isProcessing"
          class="w-full bg-red-600 text-white px-6 py-3 rounded-lg
                 hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed
                 transition-all duration-200 transform hover:scale-105"
        >
          {{ isProcessing ? 'Processing...' : 'Upload Image' }}
        </button>

        <div v-if="uploadProgress > 0" 
             class="relative w-full h-2 bg-gray-700 rounded-full overflow-hidden">
          <div class="absolute top-0 left-0 h-full bg-red-600 transition-all duration-200"
               :style="{ width: `${uploadProgress}%` }">
            <div class="absolute top-0 left-0 w-full h-full 
                        bg-gradient-to-r from-transparent via-white/20 to-transparent
                        animate-shimmer"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useImageStore } from '../stores/imageStore'

export default {
  name: 'UploadView',
  setup() {
    const router = useRouter()
    const imageStore = useImageStore()
    const selectedImage = ref(null)
    const imagePreview = ref('')
    const isProcessing = ref(false)
    const uploadProgress = ref(0)
    const selectedZoom = ref(1.0)

    const zoomLevels = [0.1, 0.2, 0.4, 0.6, 1.0, 2.0, 4.0, 6.0, 8.0, 16.0]

    const onFileSelected = (event) => {
      const file = event.target.files[0]
      if (file) {
        selectedImage.value = file
        imagePreview.value = URL.createObjectURL(file)
        uploadProgress.value = 0
      }
    }

    const uploadImage = async () => {
      if (!selectedImage.value || isProcessing.value) return;
      
      try {
        const result = await imageStore.uploadImage(
          selectedImage.value, 
          selectedZoom.value
        );
        
        console.log('Upload successful:', result);
      } catch (error) {
        console.error('Upload failed:', error);
        alert('Failed to upload image: ' + error.message);
      }
    };

    return {
      selectedImage,
      imagePreview,
      isProcessing,
      uploadProgress,
      selectedZoom,
      zoomLevels,
      onFileSelected,
      uploadImage
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