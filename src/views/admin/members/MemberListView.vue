<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { members, loadMembers, loadMember } from '../../../services/memberApi'
import { errorMessage } from '../../../api/clients'
import { X } from 'lucide-vue-next'
import { profileImage } from '../../../utils/profileImage'
import { isAdminAccount } from '../../../utils/memberAccount'
import './members.css'
import NumberPagination from '../../../components/common/NumberPagination.vue'
const selected=ref(null),detailLoading=ref(false),detailError=ref(''),imageFailed=ref(false),panelOpen=ref(false)
let detailSequence=0
async function showMember(id){const request=++detailSequence;panelOpen.value=true;selected.value=null;detailError.value='';detailLoading.value=true;imageFailed.value=false;try{const result=await loadMember(id);if(request===detailSequence)selected.value=result}catch(e){if(request===detailSequence)detailError.value=errorMessage(e)}finally{if(request===detailSequence)detailLoading.value=false}}
function closePanel(){detailSequence++;panelOpen.value=false;selected.value=null;detailLoading.value=false}
const joinedDate=value=>value?new Intl.DateTimeFormat('ko-KR',{year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date(value)):'미등록'
const error=ref(''),loading=ref(true),filter=ref('all')
const draftKeyword=ref(''),keyword=ref('')
const filteredMembers=computed(()=>members.filter(member=>{
  const matchesType=filter.value==='all'||(filter.value==='admin'?isAdminAccount(member):!isAdminAccount(member))
  const matchesKeyword=!keyword.value||[member.name,member.loginId].some(value=>String(value??'').toLocaleLowerCase().includes(keyword.value))
  return matchesType&&matchesKeyword
}))
function searchMembers(){keyword.value=draftKeyword.value.trim().toLocaleLowerCase();page.value=1}
const page=ref(1),pageSize=10
const totalPages=computed(()=>Math.max(1,Math.ceil(filteredMembers.value.length/pageSize)))
const pageMembers=computed(()=>filteredMembers.value.slice((page.value-1)*pageSize,page.value*pageSize))
const pages=computed(()=>{const start=Math.floor((page.value-1)/5)*5+1;return Array.from({length:Math.min(5,totalPages.value-start+1)},(_,i)=>start+i)})
watch(filter,()=>{page.value=1})
watch(totalPages,value=>{if(page.value>value)page.value=value})
onMounted(async()=>{try{await loadMembers()}catch(e){error.value=errorMessage(e)}finally{loading.value=false}})
</script>
<template>
  <section class="members-page member-list-page">
    <header class="members-heading"><h1>회원관리</h1><p>등록된 회원 정보를 조회하고 관리할 수 있습니다.</p></header>
    <section v-if="panelOpen" class="member-info-panel" aria-label="선택한 회원정보" :aria-busy="detailLoading">
      <button class="panel-close" type="button" aria-label="회원정보 패널 닫기" @click="closePanel"><X :size="20" /></button>
      <p v-if="detailLoading" role="status">회원정보를 불러오는 중입니다.</p><p v-else-if="detailError" role="alert">{{detailError}}</p>
      <template v-else-if="selected"><div class="panel-identity"><div class="panel-avatar"><img :src="!imageFailed && selected.profileImageUrl || profileImage(isAdminAccount(selected)?'ADMIN':'USER')" :alt="selected.name+' 프로필'" @error="imageFailed=true"/></div><div><span class="panel-kicker">회원정보</span><h2>{{selected.name||'미등록'}}</h2><p>{{selected.loginId||'미등록'}}</p></div></div>
      <dl class="panel-facts"><div><dt>이름</dt><dd>{{selected.name||'미등록'}}</dd></div><div><dt>아이디</dt><dd>{{selected.loginId||'미등록'}}</dd></div><div><dt>이메일</dt><dd>{{selected.email||'미등록'}}</dd></div><div><dt>연락처</dt><dd>{{selected.phone||'미등록'}}</dd></div><div><dt>회원 유형</dt><dd>{{selected.roleLabel||selected.role||'미등록'}}</dd></div><div><dt>가입일</dt><dd>{{joinedDate(selected.joinedAt)}}</dd></div><div><dt>계정 상태</dt><dd>{{selected.statusLabel||selected.status||'미등록'}}</dd></div><template v-if="isAdminAccount(selected)"><div><dt>소속 부서</dt><dd>{{selected.department||'미등록'}}</dd></div><div><dt>직급</dt><dd>{{selected.position||'미등록'}}</dd></div></template></dl></template>
    </section>
    <form class="member-search" @submit.prevent="searchMembers"><input v-model="draftKeyword" type="search" aria-label="회원 이름 또는 아이디 검색" placeholder="이름 또는 아이디 검색"/><button type="submit">검색</button></form>
    <div class="member-type-filter" role="group" aria-label="회원구분"><span>회원구분</span><button v-for="item in [{value:'all',label:'전체'},{value:'admin',label:'관리자'},{value:'citizen',label:'민원인'}]" :key="item.value" type="button" :aria-pressed="filter===item.value" @click="filter=item.value">{{item.label}}</button></div>
    <p v-if="error" role="alert">{{error}}</p><p v-if="loading" role="status">회원 목록을 불러오는 중입니다.</p>
    <div class="member-list-card"><table class="member-table"><caption class="visually-hidden">회원 목록</caption><thead><tr><th>이름</th><th>아이디</th><th>회원구분</th><th>회원정보</th></tr></thead><tbody><tr v-for="member in pageMembers" :key="member.id"><td><span class="member-list-name"><img :src="profileImage(isAdminAccount(member)?'ADMIN':'USER')" alt="" />{{ member.name }}</span></td><td>{{ member.loginId }}</td><td><span class="member-type-badge" :class="{'administrator':isAdminAccount(member)}">{{isAdminAccount(member)?'관리자':'민원인'}}</span></td><td><button type="button" class="member-view-button" :aria-label="member.name + ' 회원정보 보기'" :aria-pressed="panelOpen && selected?.id===member.id" @click="showMember(member.id)">보기</button></td></tr><tr v-if="!loading&&!error&&!filteredMembers.length"><td colspan="4">조회된 회원이 없습니다.</td></tr></tbody></table></div>
    <NumberPagination :page="page" :pages="pages" :total-pages="totalPages" @change="page=$event" />
  </section>
</template>
<style scoped>
.member-search{display:flex;gap:10px;flex-shrink:0;margin-bottom:12px}.member-search input{flex:1;min-width:0;height:40px;padding:0 14px;border:1px solid var(--grtc-border);border-radius:8px;background:#fff;color:var(--grtc-text);font:inherit;font-size:15px}.member-search button{height:40px;padding:0 22px;border:1px solid var(--grtc-primary);border-radius:8px;background:var(--grtc-primary);color:white;font:inherit;cursor:pointer}.member-search button:hover{background:var(--grtc-primary-hover)}
.member-type-filter{display:flex;align-items:center;gap:8px;flex-wrap:wrap;margin-bottom:18px;color:var(--grtc-heading)}.member-type-filter>span{font-size:16px;font-weight:700;margin-right:8px}.member-type-filter button{padding:8px 18px;border:1px solid #d3e3ed;border-radius:8px;background:white;color:var(--grtc-muted);font:inherit;font-size:15px;cursor:pointer}.member-type-filter button[aria-pressed=true]{background:#e4f6fc;border-color:var(--grtc-focus);color:var(--grtc-heading);font-weight:700}.member-type-filter button:hover{border-color:var(--grtc-focus)}.member-type-badge{display:inline-block;padding:5px 12px;border-radius:16px;background:#ecf8fd;color:#39758d;font-size:14px;font-weight:700}.member-type-badge.administrator{background:#e9eff6;color:var(--grtc-heading)}
</style>

<style>
@media (min-width: 1001px) and (min-height: 700px) {
  .management-shell .app-content:has(> .member-list-page) { padding: 16px 28px; }
  .member-list-page { height: 100%; min-height: 0; display: flex; flex-direction: column; gap: 12px; }
  .member-list-page .members-heading { margin-bottom: 0; flex-shrink: 0; }
  .member-list-page .members-heading h1 { margin-bottom: 6px; }
  .member-list-page .member-search { margin-bottom: 0; }
  .member-list-page .member-type-filter { margin-bottom: 0; flex-shrink: 0; }
  .member-list-page .member-list-card { flex: 1; min-height: 0; }
  .member-list-page .member-table { height: 100%; font-size: 16px; }
  .member-list-page .member-table th { padding: 8px 24px; height: 38px; }
  .member-list-page .member-table td { padding: 3px 24px; }
  .member-list-page .member-list-name img { width: 32px; height: 32px; }
  .member-list-page .number-pagination { flex-shrink: 0; }
}
@media (max-width: 1000px), (max-height: 699px) {
  .member-list-page .number-pagination { margin-top: 16px; }
}
</style>

<style scoped>
.member-info-panel{position:relative;display:grid;grid-template-columns:240px minmax(0,1fr);gap:28px;padding:24px 52px 24px 24px;background:white;border:1px solid #B9CDDD;border-top:3px solid #006BA6;border-radius:10px;flex-shrink:0;min-width:0}.panel-close{position:absolute;right:14px;top:14px;width:30px;height:30px;display:grid;place-items:center;background:white;border:0;color:#536879;cursor:pointer}.panel-identity{display:flex;align-items:center;gap:16px;min-width:0}.panel-avatar{width:72px;height:72px;border:1px solid #DFE8EF;border-radius:50%;display:grid;place-items:center;flex-shrink:0;color:#006BA6}.panel-avatar img{width:100%;height:100%;object-fit:cover;border-radius:50%}.panel-kicker{font-size:12px;color:#536879}.panel-identity h2{font-size:24px;margin:6px 0;color:#123B5D;overflow-wrap:anywhere}.panel-identity p{margin:0;color:#536879;font-size:14px;overflow-wrap:anywhere}.panel-facts{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:18px 24px;margin:0}.panel-facts div{min-width:0}.panel-facts dt{font-size:12px;color:#536879;margin-bottom:6px}.panel-facts dd{font-size:14px;font-weight:600;color:#263F52;margin:0;overflow-wrap:anywhere}.member-view-button{border:1px solid #B9CDDD;border-radius:5px;background:white;color:#006BA6;padding:5px 16px;font:inherit;font-size:13px;font-weight:600;cursor:pointer}.member-view-button:hover,.member-view-button[aria-pressed=true]{background:#006BA6;color:white}.panel-close:focus-visible,.member-view-button:focus-visible{outline:2px solid #006BA6;outline-offset:3px}.member-list-page:has(.member-info-panel){height:auto !important;min-height:100% !important}.member-list-page:has(.member-info-panel) .member-list-card{flex:none;overflow:auto}.member-list-page:has(.member-info-panel) .member-table{height:auto}.member-list-page:has(.member-info-panel) .member-table td{height:44px}@media(max-width:1100px){.member-info-panel{grid-template-columns:1fr}.panel-facts{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:600px){.member-info-panel{padding:20px;gap:20px}.panel-facts{grid-template-columns:1fr}}
</style>

<style scoped>
.member-list-card{scrollbar-gutter:auto !important}
.member-table th:nth-child(1),.member-table td:nth-child(1){width:28%}
.member-table th:nth-child(2),.member-table td:nth-child(2){width:28%}
.member-table th:nth-child(3),.member-table td:nth-child(3){width:26%}
.member-table th:nth-child(4),.member-table td:nth-child(4){width:18%;text-align:center !important}
.member-table th:last-child,.member-table td:last-child{padding-left:16px !important;padding-right:16px !important}
</style>

<style scoped>:global(.admin-ui .app-content .member-list-page .member-list-card){scrollbar-gutter:auto !important}</style>
