<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import ComplaintContent from '../../../components/complaints/ComplaintContent.vue'
import { findComplaint } from '../../../mocks/complaints'
import { answerStatuses } from '../../../services/complaintOptions'
const route=useRoute(), complaint=computed(()=>findComplaint(route.params.id))
const status=ref('RECEIVED'),content=ref(''),message=ref('')
watch(()=>route.params.id,()=>{status.value=answerStatuses.some(o=>o.value===complaint.value?.status)?complaint.value.status:'RECEIVED';content.value=complaint.value?.answer||'';message.value=''}, {immediate:true})
function save(){if(!content.value.trim()){message.value='답변 내용을 입력해 주세요.';return}complaint.value.status=status.value;complaint.value.statusLabel=answerStatuses.find(o=>o.value===status.value).label;complaint.value.answer=content.value.trim();message.value='답변이 저장되었습니다.'}
</script>
<template><section v-if="complaint"><ComplaintContent :complaint="complaint"/><form class="mock-answer" @submit.prevent="save"><label>답변 처리상태<select v-model="status"><option v-for="o in answerStatuses" :key="o.value" :value="o.value">{{o.label}}</option></select></label><label>답변 내용<textarea v-model="content" rows="4"/></label><button type="submit">답변 저장</button><p role="status">{{message}}</p></form><RouterLink to="/dashboard/complaints">목록으로 돌아가기</RouterLink></section><section v-else><p>민원 정보를 찾을 수 없습니다.</p><RouterLink to="/dashboard/complaints">목록으로 돌아가기</RouterLink></section></template>
<style scoped>.mock-answer{display:grid;gap:12px;margin:24px 0}.mock-answer label{display:grid;gap:8px}.mock-answer :is(select,textarea){padding:10px;border:1px solid #c7dce8;border-radius:8px;font:inherit}.mock-answer button{justify-self:start;padding:10px 20px;border:0;border-radius:8px;background:#065782;color:white;font:inherit}</style>

