<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import {isWithdrawnComplaint} from '../../utils/complaintAccess'
import StatusBadge from '../common/StatusBadge.vue'
import { useComplaintList } from '../../composables/useComplaintList'
import { complaintTypes,complaintLabel } from '../../mocks/complaintForm'
import { complaintNotice } from '../../mocks/complaints'
import { getComplaintOptions } from '../../services/complaintOptions'
const {statuses:complaintStatuses}=getComplaintOptions()
const props=defineProps({admin:Boolean,autoPageSize:Boolean,pageSize:{type:Number,default:10}})
const {draft,page,size,rows,pages,totalPages,selected,allSelected,search,move,selectAll,error}=useComplaintList({pageSize:props.pageSize})
// `mine` is supplied by the real list API; never infer ownership from status/name.
const detail=complaint=>!isWithdrawnComplaint(complaint)&&!props.admin&&complaint.mine===false
 ? {name:'forbidden',query:{reason:'ownership'}}
 : (props.admin?'/dashboard/complaints/':'/complaints/')+complaint.id
const tableViewport=ref(null)
let listObserver,measureFrame
function fitRows(){
 const viewport=tableViewport.value
 if(!props.autoPageSize||!viewport)return
 const headerHeight=viewport.querySelector('thead')?.getBoundingClientRect().height||44
 const rowHeight=Math.max(1,viewport.querySelector('tbody tr:not(:has(.empty-result))')?.getBoundingClientRect().height||48)
 const capacity=Math.max(1,Math.floor((viewport.clientHeight-headerHeight)/rowHeight))
 if(size.value!==capacity){const firstItem=(page.value-1)*size.value;size.value=capacity;page.value=Math.floor(firstItem/capacity)+1}
}
watch(tableViewport,viewport=>{
 listObserver?.disconnect();cancelAnimationFrame(measureFrame)
 if(!props.autoPageSize||!viewport)return
 listObserver=new ResizeObserver(()=>{cancelAnimationFrame(measureFrame);measureFrame=requestAnimationFrame(fitRows)})
 listObserver.observe(viewport)
},{flush:'post'})
onBeforeUnmount(()=>{listObserver?.disconnect();cancelAnimationFrame(measureFrame)})
</script>
<template><section class="complaint-list" :class="{'has-notice':!!complaintNotice}">
<slot name="summary" :draft="draft" :search="search"/><form class="list-search" @submit.prevent="search"><input v-model="draft.keyword" aria-label="검색어" placeholder="민원 제목, 내용, 민원인 검색"/><select v-model="draft.status" aria-label="처리상태"><option value="">전체 상태</option><option v-for="o in complaintStatuses" :value="o.value" :key="o.value">{{o.label}}</option></select><button type="submit" class="search-button">검색</button><RouterLink v-if="!admin" class="register-button" :to="admin?'/dashboard/complaints/new':'/complaints/new'">+ 민원 등록</RouterLink></form>
<p v-if="error" role="alert">{{error}}</p><p v-if="complaintNotice" class="list-notice" role="status">{{complaintNotice}}</p>
<div ref="tableViewport" class="list-table-wrap"><table><colgroup><col style="width:56px"/><col style="width:112px"/><col style="width:90px"/><col/><col style="width:130px"/><col style="width:118px"/><col style="width:112px"/></colgroup><thead><tr><th>번호</th><th>접수일자</th><th>민원유형</th><th class="title-cell">제목</th><th>민원인</th><th>처리상태</th><th>관리</th></tr></thead><tbody><tr v-for="(c, index) in rows" :key="c.id"><td>{{(page - 1) * size + index + 1}}</td><td>{{c.receivedAt?.replaceAll('-','.')||'—'}}</td><td><span class="type-badge" :data-type="c.type">{{c.typeLabel || complaintLabel(complaintTypes,c.type)}}</span></td><td class="title-cell"><RouterLink :to="detail(c)">{{c.title}}</RouterLink></td><td>{{c.authorName}}</td><td><StatusBadge :status="c.status" :label="c.statusLabel"/></td><td><RouterLink class="detail-link" :to="detail(c)"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>상세보기</RouterLink></td></tr><tr v-if="!rows.length"><td colspan="7" class="empty-result">검색 결과가 없습니다.</td></tr></tbody></table></div>
<nav class="list-pagination" aria-label="민원 페이지 이동"><button :disabled="page===1" @click="move(page-1)">이전</button><button v-for="p in pages" :key="p" :class="{active:page===p}" :aria-current="page===p?'page':undefined" @click="move(p)">{{p}}</button><button :disabled="page===totalPages" @click="move(page+1)">다음</button></nav>
</section></template>
<style scoped>
.complaint-list{display:flex;flex-direction:column;gap:16px;min-height:0;color:var(--grtc-text)}.list-search{display:flex;gap:12px;flex-shrink:0}.list-search input{flex:1;min-width:140px}.list-search :is(input,select,button),.register-button{height:42px;border:1px solid var(--grtc-border);border-radius:8px;padding:0 14px;font:inherit;font-size:15px;background:white;color:var(--grtc-text)}.list-search select{width:140px}.list-search .search-button,.register-button{display:flex;align-items:center;justify-content:center;background:var(--grtc-primary);color:white;border-color:var(--grtc-primary);text-decoration:none;white-space:nowrap;font-weight:700}.register-button{background:var(--grtc-primary)}.list-table-wrap{min-height:0;overflow-x:auto}table{width:100%;table-layout:fixed;border-collapse:collapse;min-width:860px;font-size:15px}th{height:44px;background:var(--grtc-surface);color:var(--grtc-heading);font-size:16px;font-weight:700}td{height:48px;border-bottom:1px solid var(--grtc-border-soft);text-align:center;padding:4px 8px}tbody tr:hover{background:var(--grtc-surface)}th.title-cell{text-align:center}td.title-cell{text-align:center}td.title-cell a{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}a{color:var(--grtc-primary);text-decoration:none}.detail-link{display:inline-flex;align-items:center;gap:5px;font-size:14px;white-space:nowrap}.detail-link svg{width:18px;height:18px;fill:none;stroke:currentColor;stroke-width:1.6}input[type=checkbox]{width:16px;height:16px;accent-color:var(--grtc-primary)}.type-badge{display:inline-block;border-radius:8px;padding:4px 10px;background:#edf5ff;color:#3876a3;font-size:14px}.type-badge[data-type=SUGGESTION]{background:#f2effb;color:#7b62aa}.type-badge[data-type=REPORT]{background:#fff5e8;color:#9f742d}.type-badge[data-type=COMPLAINT]{background:#fceff1;color:#ab5969}.list-pagination{display:flex;justify-content:center;gap:7px;margin-top:auto}.list-pagination button{height:34px;min-width:34px;border:1px solid var(--grtc-border);background:white;border-radius:6px;color:var(--grtc-text);cursor:pointer}.list-pagination button.active{background:var(--grtc-primary);border-color:var(--grtc-primary);color:white}.list-pagination button:disabled{opacity:.45;cursor:default}.empty-result{height:200px;color:#7e909d}.list-notice{margin:0;color:#277c53;font-size:14px}.complaint-list :is(a,button,input,select):focus-visible{outline:3px solid var(--grtc-focus);outline-offset:2px}
@media(min-width:1101px) and (max-height:850px){.complaint-list{gap:10px}td{height:35px;padding:2px 6px}th{height:36px}.list-pagination button{height:30px}.has-notice td{height:32px}.list-search{gap:8px}.list-search select{width:120px}}
@media(max-width:1100px){.list-search{flex-wrap:wrap}.list-search input{flex-basis:100%}}
</style>

