<template>
  <div class="bg-black/30 backdrop-blur-md rounded-xl p-8">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-semibold text-white">System Statistics</h2>
      <button 
        @click="refreshStats"
        :disabled="snmpStore.isLoading"
        class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 
               transition-all duration-200"
      >
        <span v-if="snmpStore.isLoading">Loading...</span>
        <span v-else>Refresh</span>
      </button>
    </div>

    <div v-if="snmpStore.error" class="mb-4 p-4 bg-red-500/20 text-red-400 rounded-lg">
      {{ snmpStore.error }}
    </div>

    <div class="overflow-x-auto">
      <table class="w-full">
        <thead class="bg-black/40">
          <tr>
            <th class="text-left p-4 text-white">Server ID</th>
            <th class="text-left p-4 text-white">OS Name</th>
            <th class="text-left p-4 text-white">CPU Usage</th>
            <th class="text-left p-4 text-white">RAM Usage</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="stat in snmpStore.latestStats" 
              :key="stat._id"
              class="border-t border-gray-800">
            <td class="p-4 text-gray-300">{{ stat.serverId }}</td>
            <td class="p-4 text-gray-300">{{ stat.osName }}</td>
            <td class="p-4 text-gray-300">
              <div class="flex items-center">
                <div class="w-full bg-gray-700 rounded-full h-2 mr-2">
                  <div class="bg-red-600 h-full rounded-full"
                       :style="{ width: stat.cpuUsage + '%' }">
                  </div>
                </div>
                <span>{{ stat.cpuUsage }}%</span>
              </div>
            </td>
            <td class="p-4 text-gray-300">
              <div class="flex items-center">
                <div class="w-full bg-gray-700 rounded-full h-2 mr-2">
                  <div class="bg-red-600 h-full rounded-full"
                       :style="{ width: stat.ramUsage + '%' }">
                  </div>
                </div>
                <span>{{ stat.ramUsage }}%</span>
              </div>
            </td>
          </tr>
          <tr v-if="snmpStore.latestStats.length === 0">
            <td colspan="4" class="p-4 text-center text-gray-500">
              No metrics data available
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="mt-4 text-right text-sm text-gray-400">
      Last updated: {{ snmpStore.lastUpdate || 'Never' }}
    </div>
  </div>
</template>

<script>
import { useSnmpStore } from '../stores/snmpStore';
import { onMounted, onUnmounted } from 'vue';

export default {
  name: 'SNMPStatsView',
  setup() {
    const snmpStore = useSnmpStore();
    let refreshInterval;

    const refreshStats = async () => {
      await snmpStore.fetchStats();
    };

    onMounted(() => {
      refreshStats();
      refreshInterval = setInterval(refreshStats, 30000);
    });

    onUnmounted(() => {
      if (refreshInterval) {
        clearInterval(refreshInterval);
      }
    });

    return {
      snmpStore,
      refreshStats
    };
  }
};
</script>