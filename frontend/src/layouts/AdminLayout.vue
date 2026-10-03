<script setup>
import { currentUser } from '../stores/currentUser'
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ComplaintBanner from '../components/complaints/ComplaintBanner.vue'
import AppHeader from '../components/common/AppHeader.vue'
import AdminSidebar from '../components/navigation/AdminSidebar.vue'
import '../styles/management-layout.css'
const route = useRoute()
const complaints = computed(() => /^\/(?:dashboard|admin)\/complaints(?:\/|$)/.test(route.path))
</script>
<template><div class="app-layout management-shell" :class="{ 'dashboard-layout': route.name === 'admin-dashboard' }"><AppHeader title="관리자" admin /><div class="app-body"><AdminSidebar :user="currentUser" /><main class="app-content" :class="{ 'complaint-content-shell': complaints, 'dashboard-content-shell': route.name === 'admin-dashboard', 'fleet-content-shell': ['admin-vehicles','admin-dispatches'].includes(route.name) }"><ComplaintBanner v-if="complaints && route.name !== 'admin-complaint-create'" /><div v-if="complaints" class="complaint-page-body" :class="{ 'list-page-body': route.name === 'admin-complaints', 'write-page-body': route.name === 'admin-complaint-create' }"><RouterView /></div><RouterView v-else /></main></div></div></template>

<style scoped>
.complaint-content-shell {display:flex;flex-direction:column;}
.complaint-content-shell > :first-child {flex-shrink:0;}
.write-page-body {flex:1;min-height:0;display:flex;flex-direction:column;padding:24px 36px;}
.write-page-body :deep(.complaint-write-page){flex:1;}
.management-shell.dashboard-layout { height: 100dvh; }
.management-shell .dashboard-content-shell { padding: 16px; background: #edf7fc; }
.management-shell .fleet-content-shell {padding:24px 28px;}
@media(min-width:1101px) and (max-height:850px){.management-shell .fleet-content-shell{padding:16px 24px;}}
</style>
