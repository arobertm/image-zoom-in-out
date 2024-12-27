<template>
  <div class="bg-white shadow-lg rounded-lg p-6 max-w-4xl mx-auto">
    <h2 class="text-xl font-semibold mb-4 text-gray-800">System Statistics</h2>
    <table class="table-auto w-full border-collapse border border-gray-300">
      <thead class="bg-gray-100">
        <tr>
          <th class="border border-gray-300 px-4 py-2 text-left">OS Name</th>
          <th class="border border-gray-300 px-4 py-2 text-left">CPU Usage</th>
          <th class="border border-gray-300 px-4 py-2 text-left">RAM Usage</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(node, index) in stats" :key="index" class="odd:bg-white even:bg-gray-50">
          <td class="border border-gray-300 px-4 py-2">{{ node.osName }}</td>
          <td class="border border-gray-300 px-4 py-2">{{ node.cpuUsage }}</td>
          <td class="border border-gray-300 px-4 py-2">{{ node.ramUsage }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { useSnmpStore } from '../stores/snmpStore';

export default {
  computed: {
    stats() {
      const snmpStore = useSnmpStore();
      return snmpStore.stats;
    },
  },
  async created() {
    const snmpStore = useSnmpStore();
    await snmpStore.fetchStats();
  },
};
</script>
