<script setup>
import { useRoute } from 'vue-router'
import { currentUser } from '../stores/currentUser'
import ComplaintBanner from '../components/complaints/ComplaintBanner.vue'
import AppHeader from '../components/common/AppHeader.vue'
import UserSidebar from '../components/navigation/UserSidebar.vue'
import '../styles/management-layout.css'
const route = useRoute()
</script>
<template><div class="app-layout management-shell"><AppHeader title="민원관리" admin /><div class="app-body"><UserSidebar :user="currentUser" /><main class="app-content complaint-content-shell"><ComplaintBanner v-if="!['user-complaint-create','user-member-info','menu-access-denied'].includes(route.name)" /><div class="complaint-page-body" :class="{ 'list-page-body': route.name === 'user-complaints', 'access-page-body': route.name === 'forbidden', 'write-page-body': route.name === 'user-complaint-create' }"><RouterView /></div></main></div></div></template>

<style scoped>
.complaint-content-shell { display: flex; flex-direction: column; }
.complaint-content-shell > :first-child { flex-shrink: 0; }
.access-page-body { flex: 1; min-height: min-content; display: flex; flex-direction: column; }
.access-page-body :deep(.access-notice) { flex: 1; }
.write-page-body { flex: 1; min-height: 0; display: flex; flex-direction: column; padding: 24px 36px; }
.write-page-body :deep(.complaint-write-page) { flex: 1; }
@media (max-width:1000px) { .write-page-body { flex: none; padding: 24px 20px; } }
</style>

