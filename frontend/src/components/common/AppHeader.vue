<script setup>
import {homeRoute} from '../../composables/useHomeRoute'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logoutCurrentUser } from '../../services/logout'
import logo from '../../assets/logo.png'
import userHeaderTrain from '../../assets/train3.png'
import headerLandscape from '../../assets/back.png'
import AdminHeaderMenu from '../navigation/AdminHeaderMenu.vue'

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
  try { await logoutCurrentUser(); await router.replace('/') }
  catch (error) { logoutError.value = error.message }
  finally { logoutBusy.value = false }
}
</script>
<template><header class="app-header" :class="{ 'admin-header': admin, 'header-boundary': headerBoundary, 'complaint-header': adminComplaint, 'citizen-landscape-header': admin && currentUser?.role === 'USER' }"><span v-if="admin && currentUser?.role === 'USER'" class="header-landscape" aria-hidden="true"><img :src="headerLandscape" alt="" /></span><RouterLink :to="homeRoute"><img :src="logo" alt="광주교통공사" /></RouterLink><span v-if="!admin">{{ title }}</span><div v-if="admin && currentUser?.role === 'USER'" class="user-header-train-track" aria-hidden="true"><span class="user-header-bridge"></span><img :src="userHeaderTrain" alt="" /></div><AdminHeaderMenu v-if="admin && currentUser?.role === 'ADMIN'" /><div class="header-session"><span v-if="logoutError" role="alert">{{ logoutError }}</span><button type="button" :disabled="logoutBusy" @click="logout">로그아웃</button></div></header></template>

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














<style scoped>.user-header-train-track{position:relative;flex:1;min-width:0;height:100%;overflow:hidden;pointer-events:none}.user-header-train-track img{position:absolute;top:50%;height:28px;width:auto;max-width:none;transform:translateY(-50%);animation:user-header-train-pass 12s linear infinite}@keyframes user-header-train-pass{from{left:-80px}to{left:100%}}@media(prefers-reduced-motion:reduce){.user-header-train-track img{animation:none;left:50%;transform:translate(-50%,-50%)}}</style>

<style scoped>.citizen-landscape-header{position:relative;isolation:isolate;background:white}.user-header-train-track{overflow:hidden}.user-header-train-track::before{content:"";position:absolute;inset:0;background:linear-gradient(0deg,rgba(255,255,255,.85),rgba(255,255,255,.2));pointer-events:none}.user-header-bridge{position:absolute;left:0;right:0;top:44px;height:4px;background:#CBD5DF;border-top:1.5px solid #A4ADB5;box-sizing:border-box}.user-header-bridge::after{content:"";position:absolute;left:0;right:0;top:4px;height:7px;background:repeating-linear-gradient(to right,transparent 0,transparent 96px,#DDE5EC 96px,#DDE5EC 100px)}.user-header-train-track img{top:16px;transform:none;height:28px;clip-path:inset(0 0 2px 0)}@media(prefers-reduced-motion:reduce){.user-header-train-track img{transform:translateX(-50%)}}</style>
