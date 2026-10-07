<script setup>
import { ref, computed, onMounted } from 'vue'
import { members, loadMembers } from '../../../services/memberApi'
import { errorMessage } from '../../../api/clients'
import { profileImage } from '../../../utils/profileImage'
import { isAdminAccount } from '../../../utils/memberAccount'
import './members.css'
const error=ref(''),loading=ref(true),filter=ref('all')
const filteredMembers=computed(()=>members.filter(member=>filter.value==='all'||(filter.value==='admin'?isAdminAccount(member):!isAdminAccount(member))))
onMounted(async()=>{try{await loadMembers()}catch(e){error.value=errorMessage(e)}finally{loading.value=false}})
</script>
<template>
  <section class="members-page">
    <header class="members-heading"><h1>회원관리</h1><p>등록된 회원 정보를 조회하고 관리할 수 있습니다.</p></header>
    <div class="member-type-filter" role="group" aria-label="회원구분"><span>회원구분</span><button v-for="item in [{value:'all',label:'전체'},{value:'admin',label:'관리자'},{value:'citizen',label:'민원인'}]" :key="item.value" type="button" :aria-pressed="filter===item.value" @click="filter=item.value">{{item.label}}</button></div>
    <p v-if="error" role="alert">{{error}}</p><p v-if="loading" role="status">회원 목록을 불러오는 중입니다.</p>
    <div class="member-list-card"><table class="member-table"><caption class="visually-hidden">회원 목록</caption><thead><tr><th>이름</th><th>아이디</th><th>회원구분</th><th>회원정보</th></tr></thead><tbody><tr v-for="member in filteredMembers" :key="member.id"><td><span class="member-list-name"><img :src="profileImage(isAdminAccount(member)?'ADMIN':'USER')" alt="" />{{ member.name }}</span></td><td>{{ member.loginId }}</td><td><span class="member-type-badge" :class="{'administrator':isAdminAccount(member)}">{{isAdminAccount(member)?'관리자':'민원인'}}</span></td><td><RouterLink :to="'/dashboard/members/' + member.id" :aria-label="member.name + ' 회원정보 보기'">상세보기</RouterLink></td></tr><tr v-if="!loading&&!error&&!filteredMembers.length"><td colspan="4">조회된 회원이 없습니다.</td></tr></tbody></table></div>
  </section>
</template>
<style scoped>
.member-type-filter{display:flex;align-items:center;gap:8px;flex-wrap:wrap;margin-bottom:18px;color:#173554}.member-type-filter>span{font-size:16px;font-weight:700;margin-right:8px}.member-type-filter button{padding:8px 18px;border:1px solid #d3e3ed;border-radius:8px;background:white;color:#526b80;font:inherit;font-size:15px;cursor:pointer}.member-type-filter button[aria-pressed=true]{background:#e4f6fc;border-color:#90c9e2;color:#064b76;font-weight:700}.member-type-filter button:hover{border-color:#90c9e2}.member-type-badge{display:inline-block;padding:5px 12px;border-radius:16px;background:#ecf8fd;color:#39758d;font-size:14px;font-weight:700}.member-type-badge.administrator{background:#e9eff6;color:#173e5c}
</style>
