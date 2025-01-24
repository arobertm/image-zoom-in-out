import { defineStore } from 'pinia';

export const useSnmpStore = defineStore('snmpStore', {
  state: () => ({
    stats: [],
    isLoading: false,
    lastUpdate: null,
    error: null
  }),

  actions: {
    async fetchStats() {
      this.isLoading = true;
      this.error = null;
      
      try {
        console.log('Fetching metrics...');
        const response = await fetch('http://localhost:3000/api/metrics', {
          method: 'GET',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          },
          mode: 'cors'
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log('Received metrics:', data);
        
        this.stats = Array.isArray(data) ? data : [];
        this.lastUpdate = new Date().toLocaleTimeString();
        this.error = null;
      } catch (error) {
        console.error('Failed to fetch metrics:', error);
        this.error = 'Failed to fetch metrics. Please try again.';
        this.stats = [];
      } finally {
        this.isLoading = false;
      }
    }
  },

  getters: {
    latestStats: (state) => {
      if (!state.stats || state.stats.length === 0) return [];

      return [...state.stats]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 2)
        .map(stat => ({
          ...stat,
          cpuUsage: Number(stat.cpuUsage),
          ramUsage: Number(stat.ramUsage)
        }));
    }
  }
});