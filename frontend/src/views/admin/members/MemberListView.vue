<script setup>
import { listMembers } from '../../../services/mockMembers'
import { profileImage } from '../../../utils/profileImage'
import './members.css'
const members = listMembers()
</script>
<template>
  <section class="members-page">
    <header class="members-heading"><h1>회원관리</h1><p>등록된 회원 정보를 조회하고 관리할 수 있습니다.</p></header>
    <div class="member-list-card"><table class="member-table"><caption class="visually-hidden">회원 목록</caption><thead><tr><th>이름</th><th>아이디</th><th>소속 부서</th><th>직급</th><th>회원정보</th></tr></thead><tbody><tr v-for="member in members" :key="member.id"><td><span class="member-list-name"><img :src="profileImage(member.role)" alt="" />{{ member.name }}</span></td><td>{{ member.username }}</td><td>{{ member.department }}</td><td>{{ member.rank }}</td><td><RouterLink :to="'/dashboard/members/' + member.id" :aria-label="member.name + ' 회원정보 보기'">상세보기</RouterLink></td></tr></tbody></table></div>
  </section>
</template>
