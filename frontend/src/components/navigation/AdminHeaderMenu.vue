<script setup>
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import {currentUser} from '../../stores/currentUser'
import {API_INTEGRATION_ENABLED} from '../../api/clients'
import {canAccessAdminPath} from '../../utils/adminAccess'
const route=useRoute()
const menus=[{label:'대시보드',path:'/dashboard',prefix:'admin-dashboard'},{label:'차량관리',path:'/dashboard/vehicles',prefix:'admin-vehicle'},{label:'배차관리',path:'/dashboard/dispatches',prefix:'admin-dispatch'},{label:'운행관리',path:'/dashboard/operations',prefix:'admin-operation'},{label:'민원관리',path:'/dashboard/complaints',prefix:'admin-complaint'},{label:'회원관리',path:'/dashboard/members',prefix:'admin-member'}]
const visibleMenus=computed(()=>menus.filter(m=>!API_INTEGRATION_ENABLED||canAccessAdminPath(currentUser.value,m.path)))
const active=m=>String(route.name||'').startsWith(m.prefix)
</script>
<template><nav class="admin-header-menu" aria-label="관리자 상단 메뉴"><RouterLink v-for="menu in visibleMenus" :key="menu.path" :to="menu.path" :class="{active:active(menu)}" :aria-current="active(menu)?'page':undefined">{{menu.label}}</RouterLink></nav></template>
<style scoped>
.admin-header-menu{display:flex;align-items:center;gap:8px;flex:1;min-width:0;height:100%}.admin-header-menu a{display:flex;align-items:center;height:38px;padding:0 16px;border-radius:6px;text-decoration:none;color:#475569;font-size:14px;font-weight:600;white-space:nowrap}.admin-header-menu a:hover{background:#F3F7FC;color:#006BA6}.admin-header-menu a.active{background:#006BA6;color:white;font-weight:700}.admin-header-menu a:focus-visible{outline:2px solid #006BA6;outline-offset:3px}@media(max-width:1200px){.admin-header-menu{display:none}}
</style>
