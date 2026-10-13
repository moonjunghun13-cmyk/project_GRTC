<script setup>
import {computed,ref} from 'vue'
import {useRouter} from 'vue-router'
import {withdrawComplaint} from '../../../services/complaintApi'
import {errorMessage} from '../../../api/clients'
import ComplaintContent from '../../../components/complaints/ComplaintContent.vue'
import ComplaintErrorNotice from '../../../components/complaints/ComplaintErrorNotice.vue'
import {useComplaintDetail} from '../../../composables/useComplaintDetail'
import {currentUser} from '../../../stores/currentUser'
import AccessNotice from '../../../components/common/AccessNotice.vue'
import {complaintDetailMode} from '../../../utils/complaintAccess'
const {complaint,loading,error,httpStatus}=useComplaintDetail(),dialog=ref(null),router=useRouter(),busy=ref(false),withdrawError=ref('')
const own=computed(()=>currentUser.value?.loginId!=='admin'&&String(complaint.value?.writerId)===String(currentUser.value?.id)),editable=computed(()=>own.value&&complaint.value?.editable&&complaint.value.status==='WAITING')
const detailMode=computed(()=>complaintDetailMode({complaint:complaint.value,user:currentUser.value,httpStatus:httpStatus.value}))
async function withdraw(){
 if(busy.value||!editable.value)return
 busy.value=true;withdrawError.value=''
 try{await withdrawComplaint(complaint.value.id);dialog.value?.close();await router.replace({name:'user-complaints'})}
 catch(e){withdrawError.value=e.response?errorMessage(e):e.message}
 finally{busy.value=false}
}
</script>
<template><p v-if="loading" role="status">불러오는 중입니다.</p><ComplaintErrorNotice v-else-if="detailMode==='deleted'" reason="withdrawn"/><AccessNotice v-else-if="detailMode==='ownership'" reason="ownership"/><ComplaintErrorNotice v-else-if="detailMode==='not-found'" :status="404"/><section v-else-if="!complaint"><p>{{error||'민원 정보를 찾을 수 없습니다.'}}</p><RouterLink to="/complaints">민원 목록</RouterLink></section><section v-else><ComplaintContent class="citizen-detail-content" :complaint="complaint"/><p v-if="own&&!editable">접수대기 상태의 본인 민원만 수정할 수 있습니다.</p><div v-if="editable" class="complaint-actions"><RouterLink :to="'/complaints/'+complaint.id+'/edit'">수정</RouterLink><button class="danger" @click="withdrawError='';dialog.showModal()">삭제</button></div><dialog ref="dialog" class="complaint-delete-dialog" @cancel="busy&&$event.preventDefault()"><h2>민원을 철회하시겠습니까?</h2><p>삭제 요청은 철회로 처리됩니다. 철회된 민원은 상세 내용을 확인할 수 없습니다.</p><p v-if="withdrawError" class="complaint-error" role="alert">{{withdrawError}}</p><div class="complaint-actions"><button class="secondary" :disabled="busy" @click="dialog.close()">취소</button><button class="danger" :disabled="busy" @click="withdraw">{{busy?'철회 중…':'철회'}}</button></div></dialog></section></template>


<style scoped>
.citizen-detail-content :deep(.complaint-submission){border:1px solid #D1E6F2;background:#FFFFFF}
.citizen-detail-content :deep(.detail-eyebrow){color:#526A7D;font-size:15px}
.citizen-detail-content :deep(.complaint-official-answer){margin-top:12px;padding:28px;border:1px solid #B4D5E9;border-top:4px solid #0078AE;border-left:1px solid #B4D5E9;border-radius:14px;background:#F0F8FD}
.citizen-detail-content :deep(.official-heading){padding-bottom:20px;margin-bottom:22px;border-bottom:1px solid #B4D5E9}
.citizen-detail-content :deep(.official-icon){width:48px;height:48px;background:#0078AE;color:#FFFFFF;border-radius:12px}
.citizen-detail-content :deep(.official-heading h3){font-size:24px;font-weight:800;color:#064B76}
.citizen-detail-content :deep(.official-heading p){color:#526A7D}
.citizen-detail-content :deep(.official-content){background:#FFFFFF;border-radius:10px;padding:22px;color:#123B5D;line-height:1.85}
.citizen-detail-content :deep(.official-empty){color:#526A7D}
.citizen-detail-content :deep(.complaint-answer-date){margin-top:18px;color:#526A7D}
@media(max-width:650px){.citizen-detail-content :deep(.complaint-official-answer){padding:20px}.citizen-detail-content :deep(.official-content){padding:16px}.citizen-detail-content :deep(.official-heading h3){font-size:22px}}
</style>
