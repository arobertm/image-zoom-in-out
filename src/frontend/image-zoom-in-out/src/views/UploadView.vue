<template>
  <div class="bg-black/30 backdrop-blur-md rounded-xl p-8">
    <h2 class="text-xl font-semibold mb-4 text-white">Upload BMP Image</h2>
    <div class="space-y-4">
      <!-- File Input -->
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
      
      <!-- Preview and Controls -->
      <div v-if="selectedImage" class="space-y-4">
        <div class="relative overflow-hidden rounded-lg bg-black/20 p-4">
          <img
            :src="imagePreview"
            alt="Preview"
            class="w-full h-auto rounded-lg"
          />
          
          <!-- Zoom Controls -->
          <div class="flex justify-center mt-4 space-x-4">
            <button
              v-for="action in zoomActions"
              :key="action.type"
              @click="handleZoom(action.type)"
              :disabled="isProcessing"
              class="px-6 py-2 bg-red-600 text-white rounded-lg
                     hover:bg-red-700 transition-all duration-200
                     disabled:opacity-50 disabled:cursor-not-allowed
                     transform hover:scale-105 active:scale-95
                     flex items-center space-x-2"
            >
              <span>{{ action.icon }}</span>
              <span>{{ action.label }}</span>
            </button>
          </div>
        </div>

        <!-- Processing Status -->
        <div v-if="isProcessing" class="flex justify-center">
          <div class="flex items-center space-x-2 text-white/80">
            <div class="animate-spin h-5 w-5 border-2 border-red-500 rounded-full border-t-transparent"></div>
            <span>Processing zoom operation...</span>
          </div>
        </div>

        <!-- Upload Button -->
        <button
          @click="uploadImage"
          :disabled="isProcessing"
          class="w-full bg-red-600 text-white px-6 py-3 rounded-lg
                 hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed
                 transition-all duration-200 transform hover:scale-105"
        >
          {{ isProcessing ? 'Processing...' : 'Upload Image' }}
        </button>

        <!-- Progress Bar -->
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

    const zoomActions = [
      { type: 'out', label: 'Zoom Out', icon: '−' },
      { type: 'in', label: 'Zoom In', icon: '+' }
    ]

    const onFileSelected = (event) => {
      const file = event.target.files[0]
      if (file) {
        selectedImage.value = file
        imagePreview.value = URL.createObjectURL(file)
        uploadProgress.value = 0
      }
    }

    const handleZoom = async (direction) => {
      if (!selectedImage.value || isProcessing.value) return
      
      isProcessing.value = true
      const formData = new FormData()
      formData.append('image', selectedImage.value)
      formData.append('operation', direction === 'in' ? 'zoom_in' : 'zoom_out')

      try {
        const response = await fetch('/api/process-zoom', {
          method: 'POST',
          body: formData
        })

        if (!response.ok) throw new Error('Zoom operation failed')

        const blob = await response.blob()
        selectedImage.value = new File([blob], 'processed.bmp', { type: 'image/bmp' })
        imagePreview.value = URL.createObjectURL(blob)
      } catch (error) {
        console.error('Zoom Error:', error)
        alert('Failed to process zoom operation')
      } finally {
        isProcessing.value = false
      }
    }

    const uploadImage = async () => {
      if (!selectedImage.value || isProcessing.value) return
      isProcessing.value = true
      uploadProgress.value = 0

      const formData = new FormData()
      formData.append('image', selectedImage.value)

      try {
        const response = await fetch('/api/upload', {
          method: 'POST',
          body: formData,
          onUploadProgress: (progressEvent) => {
            uploadProgress.value = (progressEvent.loaded / progressEvent.total) * 100
          }
        })

        if (!response.ok) throw new Error('Upload failed')

        const result = await response.json()
        imageStore.setDownloadUrl(result.downloadUrl)
        router.push('/download')
      } catch (error) {
        console.error('Upload Error:', error)
        alert('Failed to upload image')
      } finally {
        isProcessing.value = false
      }
    }

    return {
      selectedImage,
      imagePreview,
      isProcessing,
      uploadProgress,
      zoomActions,
      onFileSelected,
      handleZoom,
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