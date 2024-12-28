<template>
  <div class="bg-black/30 backdrop-blur-md rounded-xl p-8">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-semibold text-white">System Statistics</h2>
      <button 
        @click="refreshStats"
        class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 
               transition-all duration-200 transform hover:scale-105 active:scale-95
               flex items-center space-x-2"
        :disabled="isLoading"
      >
        <span v-if="isLoading" class="animate-spin">⟳</span>
        <span v-else>↻</span>
        <span>Refresh</span>
      </button>
    </div>

    <div class="overflow-hidden rounded-lg">
      <table class="w-full border-collapse">
        <thead class="bg-black/40">
          <tr>
            <th class="px-4 py-3 text-left text-white border-b border-gray-700">OS Name</th>
            <th class="px-4 py-3 text-left text-white border-b border-gray-700">CPU Usage</th>
            <th class="px-4 py-3 text-left text-white border-b border-gray-700">RAM Usage</th>
            <th class="px-4 py-3 text-left text-white border-b border-gray-700">Status</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="(node, index) in stats" 
            :key="index"
            class="transition-colors hover:bg-white/5"
          >
            <td class="px-4 py-3 text-gray-300 border-b border-gray-700">
              {{ node.osName }}
            </td>
            <td class="px-4 py-3 text-gray-300 border-b border-gray-700">
              <div class="flex items-center space-x-2">
                <div class="w-full bg-gray-700 rounded-full h-2 overflow-hidden">
                  <div 
                    class="bg-red-600 h-full rounded-full relative"
                    :style="{ width: `${node.cpuUsage}%` }"
                  >
                    <div class="absolute top-0 left-0 w-full h-full 
                              bg-gradient-to-r from-transparent via-white/20 to-transparent
                              animate-shimmer"></div>
                  </div>
                </div>
                <span class="text-sm">{{ node.cpuUsage }}%</span>
              </div>
            </td>
            <td class="px-4 py-3 text-gray-300 border-b border-gray-700">
              <div class="flex items-center space-x-2">
                <div class="w-full bg-gray-700 rounded-full h-2 overflow-hidden">
                  <div 
                    class="bg-red-600 h-full rounded-full relative"
                    :style="{ width: `${node.ramUsagePercent}%` }"
                  >
                    <div class="absolute top-0 left-0 w-full h-full 
                              bg-gradient-to-r from-transparent via-white/20 to-transparent
                              animate-shimmer"></div>
                  </div>
                </div>
                <span class="text-sm">{{ node.ramUsage }}</span>
              </div>
            </td>
            <td class="px-4 py-3 text-gray-300 border-b border-gray-700">
              <span 
                class="px-2 py-1 rounded-full text-xs font-medium"
                :class="node.status === 'active' ? 
                        'bg-green-500/20 text-green-400' : 
                        'bg-red-500/20 text-red-400'"
              >
                {{ node.status }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="mt-4 text-sm text-gray-400 text-right">
      Last updated: {{ lastUpdateTime }}
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { useSnmpStore } from '../stores/snmpStore'

export default {
  name: 'SNMPStatsView',
  setup() {
    const snmpStore = useSnmpStore()
    const isLoading = ref(false)
    const updateInterval = ref(null)
    const lastUpdateTime = ref(null)

    const refreshStats = async () => {
      isLoading.value = true
      try {
        await snmpStore.fetchStats()
        lastUpdateTime.value = new Date().toLocaleTimeString()
      } finally {
        isLoading.value = false
      }
    }

    onMounted(() => {
      refreshStats()
      updateInterval.value = setInterval(refreshStats, 30000)
    })

    onUnmounted(() => {
      if (updateInterval.value) {
        clearInterval(updateInterval.value)
      }
    })

    return {
      stats: snmpStore.stats,
      isLoading,
      lastUpdateTime,
      refreshStats
    }
  }
}
</script>