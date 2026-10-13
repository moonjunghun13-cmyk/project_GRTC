<script setup>
import { computed, ref } from 'vue'
import { LayoutDashboard, TrainFront, CalendarClock, MessageSquare, Users, LogOut, Megaphone, FolderOpen, PanelLeftClose, PanelLeftOpen } from 'lucide-vue-next'
import {API_INTEGRATION_ENABLED} from '../../api/clients'
import {canAccessAdminPath} from '../../utils/adminAccess'
import { currentUser } from '../../stores/currentUser'
import { useRoute, useRouter } from 'vue-router'
import logo from '../../assets/logo.png'
import {homeRoute} from '../../composables/useHomeRoute'
import {logoutCurrentUser} from '../../services/logout'
import SidebarProfile from './SidebarProfile.vue'
import character from '../../assets/ch.png'
import complaintCharacter from '../../assets/ch2.png'
import menuTrain from '../../assets/train2.png'
const props = defineProps({ role: { type: String, default: 'admin' }, user: { type: Object, default: null }, collapsed: Boolean, collapsible: Boolean })
const emit = defineEmits(['toggle'])
const route = useRoute()
const router=useRouter(),logoutBusy=ref(false),logoutError=ref('')
async function logout(){if(logoutBusy.value)return;logoutBusy.value=true;logoutError.value='';try{await logoutCurrentUser();await router.replace('/')}catch(e){logoutError.value=e.message}finally{logoutBusy.value=false}}
const menus = [
  { label: '대시보드', path: '/dashboard', icon: LayoutDashboard },
  { label: '차량관리', path: '/dashboard/vehicles', icon: TrainFront },
  { label: '배차관리', path: '/dashboard/dispatches', icon: CalendarClock },
  { label: '민원관리', path: '/dashboard/complaints', icon: MessageSquare },
  // Frontend-only boards: authenticated administrators can preview these screens.
  { label: '공지사항', path: '/dashboard/notices', icon: Megaphone, frontendBoard: true },
  { label: '자료실', path: '/dashboard/resources', icon: FolderOpen, frontendBoard: true },
]
const complaintPage = computed(() => /^\/(?:dashboard\/complaints|admin\/complaints|complaints|user\/complaints)(?:\/|$)/.test(route.path))
const identity = computed(()=>props.user || currentUser.value)
const accountRole = computed(()=>identity.value?.role === 'ADMIN' ? 'admin' : 'user')
const visibleMenus = computed(() => accountRole.value === 'admin' ? menus.filter(menu=>menu.frontendBoard||(identity.value?.adminType==='VEHICLE_MANAGER'&&menu.path==='/dashboard/dispatches')||!API_INTEGRATION_ENABLED||canAccessAdminPath(identity.value,menu.path)) : [{ label: '민원목록', path: '/complaints', icon: MessageSquare }])
const canManageMembers=computed(()=>accountRole.value==='admin'&&(!API_INTEGRATION_ENABLED||canAccessAdminPath(identity.value,'/dashboard/members')))
const currentPath = computed(() => route.path.replace(/^\/admin(?=\/|$)/, '/dashboard').replace(/^\/user\/complaints(?=\/|$)/, '/complaints'))
const active = path => accountRole.value === 'user' && path === '/complaints' && ['user-faq','user-complaint-guide'].includes(route.name) ? false : accountRole.value === 'user' && route.name === 'forbidden' ? path === '/complaints' : path === '/dashboard' ? currentPath.value === path : currentPath.value === path || currentPath.value.startsWith(path + '/')
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
  <aside class="admin-sidebar" :class="{ 'complaint-sidebar': accountRole === 'user' || complaintPage, 'sidebar-compact': collapsed }">
    <RouterLink v-if="accountRole === 'admin'" :to="homeRoute" class="sidebar-brand" aria-label="광주교통공사 홈"><img :src="logo" alt="광주교통공사" /></RouterLink>
    <div v-if="accountRole === 'admin' && collapsible" class="sidebar-control"><span v-if="!collapsed">관리 메뉴</span><button type="button" :aria-label="collapsed ? '사이드바 펼치기' : '사이드바 접기'" :aria-expanded="!collapsed" aria-controls="admin-menu-navigation" @click="emit('toggle')"><component :is="collapsed ? PanelLeftOpen : PanelLeftClose" :size="20" aria-hidden="true" /></button></div>
    <nav id="admin-menu-navigation" class="admin-menu" :aria-label="accountRole === 'admin' ? '관리자 메뉴' : '사용자 메뉴'"><RouterLink v-if="accountRole === 'user'" to="/notices" :class="{selected:route.name==='user-notices'}" :aria-current="route.name==='user-notices'?'page':undefined">공지사항</RouterLink><RouterLink v-if="accountRole === 'user'" to="/resources" :class="{selected:route.name==='user-resources'}" :aria-current="route.name==='user-resources'?'page':undefined">자료실</RouterLink><div v-if="accountRole === 'user'" class="citizen-menu-group"><RouterLink to="/complaints" class="citizen-menu-parent" :class="{selected:currentPath.startsWith('/complaints')}" :aria-current="route.name==='user-complaints'?'page':undefined"><span>민원목록</span></RouterLink><div class="citizen-submenu"><RouterLink to="/complaints/guide" :aria-current="route.name==='user-complaint-guide'?'page':undefined">민원 작성 가이드</RouterLink><RouterLink to="/complaints/faq" :aria-current="route.name==='user-faq'?'page':undefined">자주 묻는 질문</RouterLink></div></div><RouterLink v-for="menu in accountRole === 'admin' ? visibleMenus : []" :key="menu.path" :to="menu.path" custom v-slot="{ href, navigate }"><a :href="href" :title="collapsed ? menu.label : undefined" :aria-label="menu.label" @click="selectMenu($event, navigate, menu.path)" :class="{ selected: active(menu.path), 'menu-clicked': clickedPath === menu.path }" :aria-current="active(menu.path) ? 'page' : undefined"><span :key="'background-'+menu.path+'-'+(clickedPath === menu.path ? effectSequence : 0)" class="menu-background" aria-hidden="true"></span><component v-if="accountRole === 'admin'" :is="menu.icon" class="menu-icon" :size="20" :stroke-width="1.8" aria-hidden="true" /><span class="menu-label">{{ menu.label }}</span><span v-if="accountRole === 'user' && clickedPath === menu.path" :key="'train-'+menu.path+'-'+effectSequence" class="menu-train-layer" aria-hidden="true"><span class="menu-train-motion" @animationend="finishEffect($event, menu.path, effectSequence)"><img :src="menuTrain" alt="" /></span></span></a></RouterLink></nav>
    <div class="admin-sidebar-bottom"><img v-if="accountRole === 'user'" class="admin-character" :src="accountRole === 'user' || complaintPage ? complaintCharacter : character" alt="광주교통공사 캐릭터" /><SidebarProfile admin :role="accountRole" :label="accountRole === 'admin' ? '관리자' : '사용자'" :user="identity" /><div class="sidebar-session"><RouterLink v-if="canManageMembers" to="/dashboard/members" class="sidebar-members-link" :class="{selected:active('/dashboard/members')}" :title="collapsed?'회원관리':undefined" aria-label="회원관리" :aria-current="active('/dashboard/members')?'page':undefined"><Users :size="18" :stroke-width="1.8" aria-hidden="true" /><span v-if="!collapsed">회원관리</span></RouterLink><p v-if="logoutError" role="alert">{{logoutError}}</p><button type="button" :disabled="logoutBusy" @click="logout" :title="collapsed?'로그아웃':undefined" aria-label="로그아웃"><LogOut v-if="accountRole==='admin'" :size="18" :stroke-width="1.8" aria-hidden="true" /><span v-else aria-hidden="true">↪</span><span v-if="!collapsed">로그아웃</span></button></div></div>
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









<style scoped>.citizen-menu-group{position:relative;margin-right:20px}.citizen-menu-group .citizen-menu-parent{margin-right:0;gap:12px}.citizen-submenu{display:none;padding:8px 10px;background:#F4FBFF;border-radius:0 0 10px 10px}.citizen-menu-group:hover .citizen-submenu,.citizen-menu-group:focus-within .citizen-submenu{display:block}.citizen-submenu a{min-height:40px;font-size:16px;margin:0;border-radius:6px;justify-content:flex-start;padding:0 14px;font-weight:600}.citizen-submenu a[aria-current=page]{color:#0078AE;background:#DDF2FC}.citizen-submenu a::before{content:none}@media(hover:none){.citizen-menu-group:focus-within .citizen-submenu{display:block}}
</style>

<style scoped>.sidebar-session{padding:0 12px 14px}.sidebar-session button{display:flex;align-items:center;justify-content:center;gap:8px;width:100%;height:40px;border:1px solid #ADCFE4;border-radius:8px;background:white;color:#064B76;font:inherit;font-weight:700;cursor:pointer}.sidebar-session p{color:#BA3550;font-size:12px}.sidebar-session button:focus-visible{outline:2px solid #0078AE;outline-offset:2px}</style>
<style scoped>
/* Citizen menu labels align to the center of the full sidebar. */
.admin-sidebar:has(.citizen-menu-group) .admin-menu a{margin-right:0;justify-content:center;text-align:center}
.admin-sidebar:has(.citizen-menu-group) .citizen-menu-group{margin-right:0}
.admin-sidebar:has(.citizen-menu-group) .citizen-submenu a{justify-content:center}
</style><style scoped>
.admin-sidebar:has(.citizen-menu-group) .admin-menu a.selected{background:#CCEAF8;color:#064B76;font-weight:800}
.admin-sidebar:has(.citizen-menu-group) .admin-menu a.selected::before{background:#0078AE}
</style>
<style scoped>
.sidebar-members-link{display:flex;align-items:center;justify-content:center;gap:8px;width:100%;height:40px;box-sizing:border-box;margin-bottom:8px;border:1px solid #ADCFE4;border-radius:8px;background:white;color:#064B76;font:inherit;font-weight:700;text-decoration:none}
.sidebar-members-link.selected{background:#006BA6;color:white;border-color:#006BA6}
.sidebar-members-link:hover:not(.selected){background:#F3F7FB}
.sidebar-members-link:focus-visible{outline:2px solid #0078AE;outline-offset:2px}
</style>
