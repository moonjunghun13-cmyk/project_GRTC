<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import lock from '../../assets/lock.png'
import { safePreviousPage } from '../../stores/restrictedNavigation'
import { currentUser } from '../../stores/currentUser'
import { findComplaint, canReadComplaint } from '../../mocks/complaints'
const props = defineProps({ reason: {type:String,default:'admin'} })
const router = useRouter()
const copy = computed(() => props.reason === 'ownership' ? {
 badge:'열람 제한', title:'본인이 작성한 민원만 열람할 수 있습니다',
 lines:['다른 사용자가 작성한 민원은 열람할 수 없습니다.','민원 목록으로 이동해주세요.'],
} : {badge:'관리자 전용', title:'관리자 페이지이므로 열람이 불가합니다',
 lines:['이 페이지는 관리자 권한이 있는 계정만 이용할 수 있습니다.','이용가능한 페이지로 이동해주세요.']})
function previous() {
 let target = safePreviousPage.value
 const resolved = router.resolve(target)
 const user = currentUser.value
 if (!target.startsWith('/') || target.startsWith('//') || !resolved.matched.length || resolved.name === 'forbidden' || (resolved.meta.role === 'ADMIN' && user.role !== 'ADMIN') || (['user-complaint-detail','user-complaint-edit'].includes(resolved.name) && !canReadComplaint(user,findComplaint(resolved.params.id)))) target='/complaints'
 safePreviousPage.value='/complaints'
 void router.replace(target)
}
</script>
<template><section class="access-notice" aria-labelledby="access-title"><div class="access-copy"><img :src="lock" alt="잠금 장치와 광주교통공사 캐릭터" /><span class="access-badge">{{ copy.badge }}</span><h2 id="access-title">{{ copy.title }}</h2><p>{{ copy.lines[0] }}<br />{{ copy.lines[1] }}</p></div><div class="access-actions"><button type="button" @click="previous">이전 페이지</button></div></section></template>
<style scoped>
.access-notice { min-height: 100%; display: flex; flex-direction: column; gap: 8px; justify-content: center; color: #064b76; }
.access-copy { flex: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; gap: 10px; }
.access-copy img { width: min(660px, 85%); max-height: min(320px, 30dvh); object-fit: contain; height: auto; }
.access-badge { padding: 7px 18px; border-radius: 20px; background: #e9f5fc; color: #087bb4; font-size: 16px; font-weight: 700; }
.access-copy h2 { margin: 0; font-size: 38px; font-weight: 900; line-height: 1.35; }
.access-copy p { margin: 0; color: #7b858e; font-size: 20px; line-height: 1.5; }
.access-actions { display: flex; justify-content: flex-end; margin-top: 0; }
.access-actions button { min-height: 48px; padding: 0 28px; border: 0; border-radius: 10px; background: #086b9f; color: white; font: inherit; font-size: 17px; font-weight: 700; cursor: pointer; transition: background-color 200ms ease-out; }
.access-actions button:hover { background: #064b76; }
.access-actions button:focus-visible { outline: 3px solid #3298db; outline-offset: 3px; }
@media (min-width:1001px) and (max-height:850px) {
 .access-copy { gap: 8px; }
 .access-copy img { width: min(620px,85%); max-height: 235px; }
 .access-copy h2 { font-size: 36px; }
 .access-copy p { font-size: 19px; }
 .access-badge { padding: 5px 16px; }
 .access-actions { margin-top: -48px; }
 .access-copy p { padding-inline: 150px; }
}
@media (max-width:1000px) {
 .access-copy img { width: min(620px,100%); max-height: none; }
 .access-copy h2 { font-size: 30px; }
 .access-copy p { font-size: 18px; }
 .access-actions { margin-top: 12px; }
}
</style>
