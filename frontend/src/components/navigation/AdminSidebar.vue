<script setup>
import { computed } from 'vue'
import { currentUser } from '../../stores/currentUser'
import { useRoute } from 'vue-router'
import SidebarProfile from './SidebarProfile.vue'
import character from '../../assets/ch.png'
import complaintCharacter from '../../assets/ch2.png'
const props = defineProps({ role: { type: String, default: 'admin' }, user: { type: Object, default: null } })
const route = useRoute()
const menus = [
  { label: '대시보드', path: '/dashboard' },
  { label: '차량관리', path: '/dashboard/vehicles' },
  { label: '배차관리', path: '/dashboard/dispatches' },
  { label: '운행관리', path: '/dashboard/operations' },
  { label: '민원관리', path: '/dashboard/complaints' },
  { label: '회원관리', path: '/dashboard/members' },
]
const complaintPage = computed(() => /^\/(?:dashboard\/complaints|admin\/complaints|complaints|user\/complaints)(?:\/|$)/.test(route.path))
const identity = computed(()=>props.user || currentUser.value)
const accountRole = computed(()=>identity.value?.role === 'ADMIN' ? 'admin' : 'user')
const visibleMenus = computed(() => accountRole.value === 'admin' ? menus : [{ label: '민원관리', path: '/complaints' }])
const currentPath = computed(() => route.path.replace(/^\/admin(?=\/|$)/, '/dashboard').replace(/^\/user\/complaints(?=\/|$)/, '/complaints'))
const active = path => accountRole.value === 'user' && route.name === 'forbidden' ? path === '/complaints' : path === '/dashboard' ? currentPath.value === path : currentPath.value === path || currentPath.value.startsWith(path + '/')
</script>
<template>
  <aside class="admin-sidebar" :class="{ 'complaint-sidebar': accountRole === 'user' || complaintPage }">
    <nav class="admin-menu" :aria-label="accountRole === 'admin' ? '관리자 메뉴' : '사용자 메뉴'"><RouterLink v-for="menu in visibleMenus" :key="menu.path" :to="menu.path" custom v-slot="{ href, navigate }"><a :href="href" @click="navigate" :class="{ selected: active(menu.path) }" :aria-current="active(menu.path) ? 'page' : undefined">{{ menu.label }}</a></RouterLink></nav>
    <div class="admin-sidebar-bottom"><img class="admin-character" :src="accountRole === 'user' || complaintPage ? complaintCharacter : character" alt="광주교통공사 캐릭터" /><SidebarProfile admin :role="accountRole" :label="accountRole === 'admin' ? '관리자' : '사용자'" :user="identity" /></div>
  </aside>

</template>
<style scoped>
.admin-sidebar { display: flex; flex-direction: column; min-height: 0; overflow-y: auto; background-color: #ecf8fd; }
.complaint-sidebar { background-color: #e4f6fc; }
.admin-menu { padding: 24px 0 16px; flex-shrink: 0; }
.admin-menu a { position: relative; display: flex; align-items: center; justify-content: center; min-height: 54px; margin-right: 30px; color: #5b6063; font-size: 22px; font-weight: 700; text-decoration: none; border-radius: 0 12px 12px 0; transition: background-color 200ms ease-out, color 200ms ease-out; }
.admin-menu a::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 8px; background: #0078ae; opacity: 0; transition: opacity 200ms ease-out; }
.admin-menu a:not(.selected):hover { background: #f0f2f4; }
.admin-menu a.selected { background: #ccefff; color: #064b76; }
.admin-menu a.selected::before { opacity: 1; }
.admin-menu a:focus-visible { outline: 2px solid #0078ae; outline-offset: -2px; }
.admin-sidebar-bottom { margin-top: auto; flex-shrink: 0; }
.complaint-sidebar .admin-sidebar-bottom { background: linear-gradient(to top, #f3fafd 86px, transparent 86px); }
.admin-character { display: block; width: 100%; height: auto; }
@media (prefers-reduced-motion: reduce) { .admin-menu a, .admin-menu a::before { transition: none; } }
</style>

