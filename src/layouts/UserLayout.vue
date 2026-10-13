<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { currentUser } from '../stores/currentUser'
import ComplaintBanner from '../components/complaints/ComplaintBanner.vue'
import AppHeader from '../components/common/AppHeader.vue'
import UserSidebar from '../components/navigation/UserSidebar.vue'
import '../styles/management-layout.css'
const route = useRoute()
const helpPage=computed(()=>['user-complaints','user-faq','user-complaint-guide','user-complaint-create','user-complaint-edit','user-notices','user-resources'].includes(route.name))
const sidebarOpen=ref(true)
</script>
<template><div class="app-layout management-shell"><AppHeader title="민원목록" admin /><div class="app-body" :class="{'help-sidebar-layout':helpPage,'help-sidebar-closed':helpPage&&!sidebarOpen,'citizen-list-layout':['user-complaints','user-notices','user-resources'].includes(route.name)}"><UserSidebar :user="currentUser" :inert="helpPage&&!sidebarOpen"/><button v-if="helpPage" type="button" class="help-sidebar-toggle" :aria-expanded="sidebarOpen" aria-label="사이드바 접기 또는 펼치기" @click="sidebarOpen=!sidebarOpen">{{sidebarOpen?'☰ 메뉴 접기':'☰ 메뉴 펼치기'}}</button><main class="app-content complaint-content-shell"><ComplaintBanner title="민원목록" v-if="!['user-faq','user-complaint-guide','user-complaint-create','user-member-info','menu-access-denied','user-notices','user-resources','user-complaints'].includes(route.name)" /><div class="complaint-page-body" :class="{ 'list-page-body': route.name === 'user-complaints', 'access-page-body': route.name === 'forbidden', 'write-page-body': route.name === 'user-complaint-create' }"><RouterView /></div></main></div></div></template>

<style scoped>
.complaint-content-shell { display: flex; flex-direction: column; }
.complaint-content-shell > :first-child { flex-shrink: 0; }
.access-page-body { flex: 1; min-height: min-content; display: flex; flex-direction: column; }
.access-page-body :deep(.access-notice) { flex: 1; }
.write-page-body { flex: 1; min-height: 0; display: flex; flex-direction: column; padding: 24px 36px; }
.write-page-body :deep(.complaint-write-page) { flex: 1; }
@media (max-width:1000px) { .write-page-body { flex: none; padding: 24px 20px; } }
</style>


<style scoped>.help-sidebar-layout{position:relative;transition:grid-template-columns .3s ease}.help-sidebar-layout.help-sidebar-closed{grid-template-columns:0 minmax(0,1fr)}.help-sidebar-layout>:deep(.admin-sidebar){grid-column:1;grid-row:1;min-width:0;transition:transform .3s ease,visibility .3s}.help-sidebar-closed>:deep(.admin-sidebar){transform:translateX(-100%);visibility:hidden}.help-sidebar-layout>.app-content{grid-column:2;grid-row:1}.help-sidebar-toggle{position:absolute;top:12px;right:16px;z-index:20;min-height:34px;border:1px solid #ADCFE4;border-radius:7px;background:white;color:#064B76;padding:0 12px;font:inherit;font-size:13px;cursor:pointer}.help-sidebar-layout .complaint-page-body{padding-top:54px}.help-sidebar-toggle:focus-visible{outline:2px solid #0078AE;outline-offset:2px}@media(prefers-reduced-motion:reduce){.help-sidebar-layout,.help-sidebar-layout>:deep(.admin-sidebar){transition:none}}@media(max-width:700px){.help-sidebar-layout.help-sidebar-closed{grid-template-rows:0 minmax(0,1fr)}.help-sidebar-layout>:deep(.admin-sidebar){grid-column:1;grid-row:1}.help-sidebar-layout>.app-content{grid-column:1;grid-row:2}.help-sidebar-layout .help-sidebar-toggle{top:8px}.help-sidebar-closed .complaint-page-body{padding-top:54px}}</style>

<style scoped>
.help-sidebar-layout .help-sidebar-toggle{left:var(--sidebar-width,265px);right:auto;top:18px;transform:translateX(-50%);transition:left .3s ease;box-shadow:0 2px 5px rgba(23,61,88,.08);font-size:0;width:30px;min-height:38px;padding:0;border-radius:7px}
.help-sidebar-layout .help-sidebar-toggle::before{content:'‹';font-size:24px;line-height:1}
.help-sidebar-layout.help-sidebar-closed .help-sidebar-toggle{left:14px;transform:none}
.help-sidebar-layout.help-sidebar-closed .help-sidebar-toggle::before{content:'›'}
.help-sidebar-layout .complaint-page-body{padding-top:32px}
@media(max-width:700px){.help-sidebar-layout .help-sidebar-toggle{left:auto;right:8px;top:8px;transform:none}.help-sidebar-layout.help-sidebar-closed .help-sidebar-toggle{left:8px;right:auto}.help-sidebar-layout.help-sidebar-closed .complaint-page-body{padding-top:52px}}
@media(prefers-reduced-motion:reduce){.help-sidebar-layout .help-sidebar-toggle{transition:none}}
</style>

<style scoped>.list-page-body{flex:1;min-height:0;display:flex;flex-direction:column}</style>
<style scoped>
@media(min-width:1001px) and (min-height:700px){
.complaint-page-body:has(.information-board){flex:1;min-height:0;display:flex;flex-direction:column;box-sizing:border-box}
.complaint-content-shell:has(.information-board){overflow:visible;min-height:0}
}
</style>
<style scoped>.list-page-body{padding:32px;box-sizing:border-box}@media(max-width:600px){.list-page-body{padding:16px}}</style>
<style scoped>
.citizen-list-layout{--citizen-list-width:1400px;--citizen-list-padding:32px}
.citizen-list-layout .complaint-page-body{padding:32px var(--citizen-list-padding);box-sizing:border-box}
.citizen-list-layout .complaint-page-body :deep(.citizen-list-page),.citizen-list-layout .complaint-page-body :deep(.information-board){width:100%;max-width:var(--citizen-list-width);margin-inline:auto;box-sizing:border-box}
.citizen-list-layout .complaint-page-body :deep(.list-search),.citizen-list-layout .complaint-page-body :deep(.board-search),.citizen-list-layout .complaint-page-body :deep(.list-table-wrap),.citizen-list-layout .complaint-page-body :deep(.board-list){width:100%;box-sizing:border-box}
@media(max-width:600px){.citizen-list-layout{--citizen-list-padding:16px}.citizen-list-layout .complaint-page-body{padding-top:16px;padding-bottom:16px}}
</style><style scoped>
/* Keep viewport capacity for pagination, while the visible frame ends at the last row. */
.citizen-list-layout .complaint-page-body :deep(.list-table-wrap){border:0;border-radius:0;background:transparent;box-shadow:none}
.citizen-list-layout .complaint-page-body :deep(.list-table-wrap table){background:white;border:1px solid #D5E4EF;border-radius:14px;border-collapse:separate;border-spacing:0;overflow:hidden}
.citizen-list-layout .complaint-page-body :deep(.board-list){border:0;border-radius:0;background:transparent;box-shadow:none}
.citizen-list-layout .complaint-page-body :deep(.board-table-scroll){border-radius:0;background:transparent}
.citizen-list-layout .complaint-page-body :deep(.board-table-scroll table){background:white;border:1px solid #D5E4EF;border-radius:14px;border-collapse:separate;border-spacing:0;overflow:hidden}
.citizen-list-layout .complaint-page-body :deep(.board-list .list-heading){background:white;border:1px solid #D5E4EF;border-bottom:0;border-radius:14px 14px 0 0}
.citizen-list-layout .complaint-page-body :deep(.board-list .board-table-scroll table){border-radius:0 0 14px 14px}
</style>