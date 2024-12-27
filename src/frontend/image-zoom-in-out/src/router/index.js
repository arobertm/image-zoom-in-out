import { createRouter, createWebHistory } from 'vue-router';
import UploadView from '../views/UploadView.vue';
import SNMPStatsView from '../views/SNMPStatsView.vue';
import DownloadView from '../views/DownloadView.vue';

const routes = [
  { path: '/', redirect: '/upload' },
  { path: '/upload', name: 'upload', component: UploadView },
  { path: '/snmp', name: 'snmp', component: SNMPStatsView },
  { path: '/download', name: 'download', component: DownloadView },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
