<script setup>
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import lock from '../../assets/lock.png'
import ComplaintBanner from '../../components/complaints/ComplaintBanner.vue'
import {currentUser} from '../../stores/currentUser'
const route=useRoute()
const menus={vehicles:['차량관리','안전한 차량 운영을 위한 관리 공간입니다.'],dispatches:['배차관리','정확한 배차와 운행 일정을 관리합니다.'],operations:['운행관리','안전하고 원활한 운행을 관리합니다.'],complaints:['민원관리','시민의 소중한 의견에 귀 기울입니다.'],members:['회원관리','회원 정보를 안전하게 관리합니다.']}
const menu=computed(()=>menus[route.query.menu]||menus.complaints)
</script>
<template><section class="menu-denied"><ComplaintBanner :title="menu[0]" :description="menu[1]"/><article class="denied-card"><img :src="lock" alt=""/><span>열람 제한</span><h2>접근할 수 없는 페이지입니다.</h2><p>현재 계정에는 이 페이지를 열람할 권한이 없습니다.<br/>이용 가능한 메뉴로 이동해 주세요.</p><RouterLink :to="currentUser?.role==='ADMIN'?'/dashboard':'/complaints'">홈으로</RouterLink></article></section></template>
<style scoped>.menu-denied{margin:-24px -28px;color:#064b76}.denied-card{margin:28px; padding:28px;border:1px solid #dce7ee;border-radius:18px;background:#fff;display:flex;flex-direction:column;align-items:center;text-align:center;gap:12px}.denied-card img{width:min(460px,85%);height:230px;object-fit:contain}.denied-card span{padding:7px 18px;border-radius:20px;background:#e9f5fc;font-weight:700}.denied-card h2{margin:0;font-size:30px}.denied-card p{margin:0;color:#718394;line-height:1.6}.denied-card a{padding:12px 26px;background:#064b76;color:white;text-decoration:none;border-radius:9px;font-weight:700}.denied-card a:focus-visible{outline:3px solid #3298db;outline-offset:3px}@media(max-width:700px){.menu-denied{margin:0}.denied-card{margin:18px}.denied-card h2{font-size:24px}}</style>
