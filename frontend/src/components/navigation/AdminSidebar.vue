<script setup>
import { computed, ref } from 'vue'
import {API_INTEGRATION_ENABLED} from '../../api/clients'
import {canAccessAdminPath} from '../../utils/adminAccess'
import { currentUser } from '../../stores/currentUser'
import { useRoute } from 'vue-router'
import SidebarProfile from './SidebarProfile.vue'
import character from '../../assets/ch.png'
import complaintCharacter from '../../assets/ch2.png'
import menuTrain from '../../assets/train2.png'
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
const visibleMenus = computed(() => accountRole.value === 'admin' ? menus.filter(menu=>!API_INTEGRATION_ENABLED||canAccessAdminPath(identity.value,menu.path)) : [{ label: '민원관리', path: '/complaints' }])
const currentPath = computed(() => route.path.replace(/^\/admin(?=\/|$)/, '/dashboard').replace(/^\/user\/complaints(?=\/|$)/, '/complaints'))
const active = path => accountRole.value === 'user' && route.name === 'forbidden' ? path === '/complaints' : path === '/dashboard' ? currentPath.value === path : currentPath.value === path || currentPath.value.startsWith(path + '/')
const clickedPath = ref(null)
const effectSequence = ref(0)
function selectMenu(event, navigate, path) {
  if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
    navigate(event)
    return
  }
  clickedPath.value = path
  effectSequence.value += 1
  navigate(event)
}
function finishEffect(event, path, sequence) {
  if (event.animationName.includes('menu-train') && clickedPath.value === path && effectSequence.value === sequence) clickedPath.value = null
}</script>
<template>
  <aside class="admin-sidebar" :class="{ 'complaint-sidebar': accountRole === 'user' || complaintPage }">
    <nav class="admin-menu" :aria-label="accountRole === 'admin' ? '관리자 메뉴' : '사용자 메뉴'"><RouterLink v-for="menu in visibleMenus" :key="menu.path" :to="menu.path" custom v-slot="{ href, navigate }"><a :href="href" @click="selectMenu($event, navigate, menu.path)" :class="{ selected: active(menu.path), 'menu-clicked': clickedPath === menu.path }" :aria-current="active(menu.path) ? 'page' : undefined"><span :key="'background-'+menu.path+'-'+(clickedPath === menu.path ? effectSequence : 0)" class="menu-background" aria-hidden="true"></span><span class="menu-label">{{ menu.label }}</span><span v-if="clickedPath === menu.path" :key="'train-'+menu.path+'-'+effectSequence" class="menu-train-layer" aria-hidden="true"><span class="menu-train-motion" @animationend="finishEffect($event, menu.path, effectSequence)"><img :src="menuTrain" alt="" /></span></span></a></RouterLink></nav>
    <div class="admin-sidebar-bottom"><img class="admin-character" :src="accountRole === 'user' || complaintPage ? complaintCharacter : character" alt="광주교통공사 캐릭터" /><SidebarProfile admin :role="accountRole" :label="accountRole === 'admin' ? '관리자' : '사용자'" :user="identity" /></div>
  </aside>

</template>
<style scoped>
.admin-sidebar { display: flex; flex-direction: column; min-height: 0; overflow-y: auto; background-color: #ecf8fd; }
.complaint-sidebar { background-color: #e4f6fc; }
.admin-menu { padding: 24px 0 16px; flex-shrink: 0; }
.admin-menu a { position: relative; display: flex; align-items: center; justify-content: center; min-height: 54px; margin-right: 30px; color: #5b6063; font-size: 22px; font-weight: 700; text-decoration: none; border-radius: 0 12px 12px 0; transition: background-color 180ms ease-out, color 180ms ease-out, transform 180ms ease-out; }
.admin-menu a::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 8px; background: #0078ae; opacity: 0; transition: opacity 200ms ease-out; }
.admin-menu a:not(.selected):hover { background: #f0f2f4; transform: scale(1.015); }
.admin-menu a.selected { color: #064b76; font-weight: 800; }
.menu-background::after { content: ''; position: absolute; inset: 0; background: linear-gradient(100deg, transparent 25%, rgb(255 255 255 / 45%) 50%, transparent 75%); transform: translateX(-100%); opacity: 0; }
.menu-clicked .menu-background::after { animation: menu-shine 550ms ease-out; }
@keyframes menu-shine { 0% { transform: translateX(-100%); opacity: 0; } 15% { opacity: 1; } 85% { opacity: 1; } 100% { transform: translateX(100%); opacity: 0; } }
.menu-label { position: relative; z-index: 2; }
.menu-background { position: absolute; inset: 0; border-radius: inherit; overflow: hidden; pointer-events: none; }
.menu-background::before { content: ''; position: absolute; inset: 0; background: linear-gradient(105deg, #e4f6fc, #ccefff 50%, #dff4fc); opacity: 0; }
.selected .menu-background::before { opacity: 1; }
.menu-clicked.selected .menu-background::before { animation: menu-appear 250ms ease-out; }
@keyframes menu-appear { from { opacity: 0; } to { opacity: 1; } }
.menu-train-layer { position: absolute; inset: 0; border-radius: inherit; overflow: hidden; pointer-events: none; z-index: 2; }
.menu-train-motion { position: absolute; left: 0; right: 0; bottom: 0; height: 10px; animation: menu-train 1400ms ease-in-out both; }
.menu-train-motion img { display: block; width: 56px; height: 10px; object-fit: contain; }
@keyframes menu-train { 0% { transform: translateX(-56px); opacity: 0; } 12% { opacity: 1; } 80% { opacity: 1; } 100% { transform: translateX(100%); opacity: 0; } }
.admin-menu a.selected::before { opacity: 1; z-index: 3; }
.admin-menu a:focus-visible { outline: 2px solid #0078ae; outline-offset: -2px; }
.admin-sidebar-bottom { margin-top: auto; flex-shrink: 0; }
.complaint-sidebar .admin-sidebar-bottom { background: linear-gradient(to top, #f3fafd 86px, transparent 86px); }
.admin-character { display: block; width: 100%; height: auto; }
@media (prefers-reduced-motion: reduce) { .admin-menu a, .admin-menu a::before { transition: none; } .admin-menu a:not(.selected):hover { transform: none; } .menu-background::before, .menu-background::after { animation: none !important; } .menu-train-layer { display: none; } .menu-train-motion { animation: none; } }
</style>








