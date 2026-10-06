<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logoutCurrentUser } from '../../services/logout'
import logo from '../../assets/logo.png'
import headerLandscape from '../../assets/back.png'
import HeaderRouteMenu from '../navigation/HeaderRouteMenu.vue'
import { currentUser } from '../../stores/currentUser'
defineProps({ title: { type: String, default: '광주교통공사' }, admin: Boolean })
const router = useRouter()
const route = useRoute()
const adminComplaint = computed(() => currentUser.value?.role === 'ADMIN' && String(route.name || '').startsWith('admin-complaint'))
const headerBoundary = computed(() => currentUser.value?.role === 'ADMIN' && ['admin-dashboard', 'admin-vehicle', 'admin-dispatch', 'admin-operation', 'admin-member'].some(prefix => String(route.name || '').startsWith(prefix)))
const logoutBusy = ref(false)
const logoutError = ref('')
async function logout() {
  if (logoutBusy.value) return
  logoutBusy.value = true
  try { logoutCurrentUser(); await router.replace('/') }
  catch (error) { logoutError.value = error.message }
  finally { logoutBusy.value = false }
}
</script>
<template><header class="app-header" :class="{ 'admin-header': admin, 'header-boundary': headerBoundary, 'complaint-header': adminComplaint, 'landscape-header': admin && currentUser?.role === 'ADMIN' }"><span v-if="admin && currentUser?.role === 'ADMIN'" class="header-landscape" aria-hidden="true"><img :src="headerLandscape" alt="" /></span><RouterLink to="/"><img :src="logo" alt="광주교통공사" /></RouterLink><span v-if="!admin">{{ title }}</span><HeaderRouteMenu v-if="admin && currentUser?.role === 'ADMIN'" /><div class="header-session"><span v-if="logoutError" role="alert">{{ logoutError }}</span><button type="button" :disabled="logoutBusy" @click="logout">로그아웃</button></div></header></template>

<style scoped>
.app-header.admin-header { height: 70px; min-height: 70px; flex-shrink: 0; padding: 0 28px 0 0; flex-wrap: nowrap; border-bottom: 1px solid #e9eef1; }
.app-header.landscape-header { position: relative; isolation: isolate; background: white; }
.header-landscape { position: absolute; inset: 0; overflow: hidden; pointer-events: none; z-index: -1; container-type: inline-size; }
.app-header .header-landscape img { position: absolute; left: 0; top: calc(100% - 38.65cqw); display: block; width: 100%; max-width: none; height: auto; opacity: 0.5; }
.app-header.header-boundary { box-sizing: border-box; position: relative; z-index: 10; border-bottom: 1px solid #DCE6ED; box-shadow: 0 3px 8px rgba(23, 61, 88, 0.06); background: white; }
.app-header.complaint-header { border-bottom-color: transparent; box-shadow: none; }
.admin-header > a:first-of-type { width: var(--sidebar-width, 265px); height: 100%; flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.app-header.admin-header > a:first-of-type img { width: 76%; max-width: 220px; height: auto; display: block; }
.admin-header > a:last-child { color: #064b76; font-weight: 700; }
.app-header.admin-header { gap: 24px; }
.header-session { margin-left: auto; display: flex; align-items: center; gap: 12px; }
.header-session button, .header-session a { color: #064b76; font: inherit; font-weight: 700; }
.header-session button { border: 1px solid #c7dce8; background: #ffffff; padding: 9px 18px; border-radius: 8px; cursor: pointer; transition: background-color 180ms ease-out; }
.header-session button:hover {background:#ffffff;border-color:#8fbad4;}
.header-session button:focus-visible {outline:3px solid #64b3e0;outline-offset:2px;}
.header-session [role="alert"] { color: #c52f35; font-size: 13px; }
</style>











