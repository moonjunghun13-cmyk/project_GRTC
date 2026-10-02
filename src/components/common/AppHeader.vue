<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '../../stores/auth'
import logo from '../../assets/logo.png'
defineProps({ title: { type: String, default: '광주교통공사' }, admin: Boolean })
const router = useRouter()
const logoutBusy = ref(false)
const logoutError = ref('')
async function logout() {
  if (logoutBusy.value) return
  logoutBusy.value = true
  try { await auth.logout(); await router.push('/login') }
  catch (error) { logoutError.value = auth.errorMessage(error) }
  finally { logoutBusy.value = false }
}
</script>
<template><header class="app-header" :class="{ 'admin-header': admin }"><RouterLink to="/"><img :src="logo" alt="광주교통공사" /></RouterLink><span v-if="!admin">{{ title }}</span><div class="header-session"><span v-if="logoutError" role="alert">{{ logoutError }}</span><button v-if="auth.state.user" type="button" :disabled="logoutBusy" @click="logout">로그아웃</button><RouterLink v-else to="/login">로그인</RouterLink></div></header></template>

<style scoped>
.app-header.admin-header { height: 70px; min-height: 70px; flex-shrink: 0; padding: 0 28px 0 0; flex-wrap: nowrap; border-bottom: 1px solid #e9eef1; }
.admin-header > a:first-child { width: var(--sidebar-width, 265px); height: 100%; flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.app-header.admin-header img { width: 76%; max-width: 220px; height: auto; display: block; }
.admin-header > a:last-child { color: #064b76; font-weight: 700; }
.header-session { margin-left: auto; display: flex; align-items: center; gap: 12px; }
.header-session button, .header-session a { color: #064b76; font: inherit; font-weight: 700; }
.header-session button { border: 0; background: none; cursor: pointer; }
.header-session [role="alert"] { color: #c52f35; font-size: 13px; }
</style>
