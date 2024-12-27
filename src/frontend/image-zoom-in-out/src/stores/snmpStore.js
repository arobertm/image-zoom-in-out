import { defineStore } from 'pinia';

export const useSnmpStore = defineStore('snmpStore', {
  state: () => ({
    stats: [],
  }),
  actions: {
    async fetchStats() {
      try {
        const response = await fetch('/api/snmp');
        this.stats = await response.json();
      } catch (error) {
        console.error('Error fetching SNMP stats:', error);
      }
    },
  },
});
