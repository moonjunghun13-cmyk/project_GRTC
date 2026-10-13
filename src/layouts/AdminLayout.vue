<script setup>
import { currentUser } from '../stores/currentUser'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import ComplaintBanner from '../components/complaints/ComplaintBanner.vue'

import AdminSidebar from '../components/navigation/AdminSidebar.vue'
import '../styles/management-layout.css'
import '../styles/admin-ui.css'
const route = useRoute()
const complaints = computed(() => /^\/(?:dashboard|admin)\/complaints(?:\/|$)/.test(route.path))
const sidebarOpen = ref(false)
const collapsible = computed(() => (/^\/(?:dashboard|admin)(?:\/(?:vehicles|dispatches|operations|members|complaints|notices|resources)(?:\/.*)?)?\/?$/.test(route.path)))
function closeSidebar(event) { if (event.key === 'Escape' && sidebarOpen.value) { sidebarOpen.value = false; document.querySelector('.sidebar-control button')?.focus() } }
</script>
<template><div class="app-layout management-shell admin-ui" :class="{ 'dashboard-layout': route.name === 'admin-dashboard' }"><div class="app-body" :class="{ 'collapsible-body': collapsible, 'sidebar-expanded': collapsible && sidebarOpen }" @keydown="closeSidebar"><AdminSidebar id="admin-side-navigation" :user="currentUser" :collapsed="collapsible && !sidebarOpen" :collapsible="collapsible" @toggle="sidebarOpen = !sidebarOpen" /><main class="app-content" :class="{ 'complaint-content-shell': complaints, 'dashboard-content-shell': route.name === 'admin-dashboard', 'fleet-content-shell': ['admin-vehicles','admin-dispatches'].includes(route.name), 'compact-when-collapsed': ['admin-vehicles','admin-dispatches','admin-members'].includes(route.name), 'operations-content-shell': route.name === 'admin-operations' }"><ComplaintBanner v-if="route.name === 'admin-complaints'" /><div v-if="complaints" class="complaint-page-body" :class="{ 'list-page-body': route.name === 'admin-complaints', 'write-page-body': route.name === 'admin-complaint-create' }"><RouterView /></div><RouterView v-else /></main></div></div></template>

<style scoped>
.management-shell .app-body.collapsible-body{position:relative;grid-template-columns:72px minmax(0,1fr);transition:grid-template-columns .3s ease}
.management-shell .app-body.collapsible-body.sidebar-expanded{grid-template-columns:var(--sidebar-width) minmax(0,1fr)}
.collapsible-body > :deep(.admin-sidebar){width:72px;min-width:0;grid-column:1;grid-row:1;transform:none;visibility:visible;transition:width .3s ease}
.collapsible-body.sidebar-expanded > :deep(.admin-sidebar){width:var(--sidebar-width);transform:none;visibility:visible}
.collapsible-body>.app-content{grid-column:2;grid-row:1}
.sidebar-toggle{position:absolute;right:16px;bottom:10px;z-index:20;display:inline-flex;align-items:center;gap:8px;min-height:36px;padding:0 14px;border:1px solid #CAD6DE;border-radius:8px;background:#fff;color:#006BA6;font:inherit;font-size:13px;cursor:pointer}
.sidebar-toggle:focus-visible{outline:2px solid #006BA6;outline-offset:3px}
@media(prefers-reduced-motion:reduce){.management-shell .app-body.collapsible-body,.collapsible-body > :deep(.admin-sidebar){transition:none}}
</style>
