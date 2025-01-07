<template>
  <div class="min-h-screen bg-gradient-to-b from-gray-900 via-gray-800 to-gray-900">
    <!-- Animated Korean text banner -->
    <div class="fixed top-0 w-full bg-red-600 overflow-hidden py-1 z-50">
      <div class="animate-marquee whitespace-nowrap">
        <span v-for="i in 3" :key="i" class="mx-4 text-white">
          이미지 크기 조정-이미지 크기 조정
        </span>
      </div>
    </div>

    <!-- Main navigation -->
    <nav class="fixed top-8 left-0 w-full bg-black/70 backdrop-blur-sm text-white shadow-lg z-40">
      <div class="container mx-auto flex justify-between items-center p-4">
        <div class="flex items-center space-x-4">
          <h1 class="text-4xl font-bold">
            <span class="text-red-500">_+</span>
            <span class="text-white">image</span>
          </h1>
          <div class="text-sm">
            <div>DISTRIBUTED APPS DEV AND CLOUD</div>
            <div class="flex items-center">
              <span class="text-gray-400">ZOOM</span>
              <span class="text-red-500 mx-1">IN</span>
              <span class="text-gray-400">-OUT</span>
            </div>
          </div>
        </div>
        
        <div class="space-x-6">
          <router-link 
            v-for="route in routes" 
            :key="route.path"
            :to="route.path"
            class="hover:text-red-500 transition-colors px-4 py-2 rounded-lg hover:bg-white/10"
            active-class="text-red-500"
          >
            {{ route.name }}
          </router-link>
        </div>
      </div>
    </nav>

    <!-- Background pattern -->
    <div class="fixed inset-0 z-0 opacity-5">
      <div class="absolute inset-0 grid grid-cols-8 gap-4 p-8">
        <div 
          v-for="i in 64" 
          :key="i"
          class="bg-white rounded-full w-4 h-4"
          :class="'animate-pulse-' + (i % 4)"
        />
      </div>
    </div>

    <!-- Main content -->
    <main class="container mx-auto pt-32 p-4 relative z-10">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- Bottom banner -->
    <div class="fixed bottom-0 w-full bg-black/70 backdrop-blur-sm text-white py-2">
      <div class="container mx-auto text-center text-sm">
        ALEXANDRU ROBERT-MIHAI | Docker 컨테이너로 만든
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      routes: [
        { path: '/upload', name: 'Upload' },
        { path: '/snmp', name: 'SNMP Stats' },
        { path: '/download', name: 'Download' }
      ]
    }
  }
}
</script>

<style>
@keyframes marquee {
  0% { transform: translateX(100%); }
  100% { transform: translateX(-100%); }
}

.animate-marquee {
  display: inline-block;
  animation: marquee 20s linear infinite;
}

@keyframes pulse-0 {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.1; }
}

@keyframes pulse-1 {
  0%, 100% { opacity: 0.25; }
  50% { opacity: 0.05; }
}

@keyframes pulse-2 {
  0%, 100% { opacity: 0.2; }
  50% { opacity: 0.1; }
}

@keyframes pulse-3 {
  0%, 100% { opacity: 0.15; }
  50% { opacity: 0.05; }
}

.animate-pulse-0 { animation: pulse-0 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }
.animate-pulse-1 { animation: pulse-1 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }
.animate-pulse-2 { animation: pulse-2 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }
.animate-pulse-3 { animation: pulse-3 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>