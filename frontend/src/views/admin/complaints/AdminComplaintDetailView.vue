<script setup>
import {computed,ref,watch,onMounted} from 'vue'
import {isWithdrawnComplaint,withdrawnComplaintStatuses} from '../../../utils/complaintAccess'
import ComplaintErrorNotice from '../../../components/complaints/ComplaintErrorNotice.vue'
import ComplaintContent from '../../../components/complaints/ComplaintContent.vue'
import {useComplaintDetail} from '../../../composables/useComplaintDetail'
import {saveAnswer,complaintOptions} from '../../../services/complaintApi'
import {errorMessage} from '../../../api/clients'
const {complaint,loading,error}=useComplaintDetail(true),answerStatuses=ref([]),status=ref(''),content=ref(''),message=ref(''),busy=ref(false),saved=ref(false)
onMounted(async()=>{try{const o=await complaintOptions(true);answerStatuses.value=o.statuses.filter(s=>s.code!=='WAITING'&&!withdrawnComplaintStatuses.has(s.code)).map(s=>({value:s.code,label:s.label}));status.value=answerStatuses.value.some(s=>s.value===complaint.value?.status)?complaint.value.status:answerStatuses.value[0]?.value||''}catch(e){message.value=errorMessage(e)}})
watch(complaint,c=>{saved.value=false;content.value=c?.answer?.content||'';status.value=answerStatuses.value.some(s=>s.value===c?.status)?c.status:answerStatuses.value[0]?.value||''})
watch([status,content],()=>{saved.value=false},{flush:'sync'})
async function save(){if(busy.value||withdrawn.value||!answerStatuses.value.some(o=>o.value===status.value))return;if(!content.value.trim()){message.value='답변 내용을 입력해 주세요.';return}busy.value=true;try{complaint.value=await saveAnswer(complaint.value.id,status.value,content.value.trim());saved.value=true;message.value='답변이 저장되었습니다.'}catch(e){message.value=errorMessage(e)}finally{busy.value=false}}
const withdrawn=computed(()=>isWithdrawnComplaint(complaint.value))
</script>
<template><p v-if="loading" role="status">불러오는 중입니다.</p><ComplaintErrorNotice v-if="withdrawn" reason="withdrawn" admin/><section v-else-if="complaint"><ComplaintContent :complaint="complaint" admin/><form class="complaint-card answer-form" @submit.prevent="save"><div class="answer-editor-heading"><h3>{{complaint.answer?'답변 수정':'답변 작성'}}</h3><p>처리상태를 선택하고 민원인에게 전달할 답변을 작성하세요.</p></div><label>답변 처리상태<select v-model="status"><option v-for="o in answerStatuses" :key="o.value" :value="o.value">{{o.label}}</option></select></label><label>답변 내용<textarea v-model="content" rows="4"/></label><div class="complaint-actions"><RouterLink class="secondary" :to="{name:'admin-complaints'}">이전</RouterLink><button v-if="!saved" type="submit" :disabled="busy || !status">{{busy?'저장 중…':complaint.answer?'답변 저장':'답변 등록'}}</button></div><p role="status">{{message}}</p></form></section><section v-else><p>{{error || '민원 정보를 찾을 수 없습니다.'}}</p><RouterLink to="/dashboard/complaints">목록으로 돌아가기</RouterLink></section></template>


<style scoped>
.answer-form{border-top:3px solid #0879ac;gap:18px;margin-top:16px}
.answer-editor-heading{padding-bottom:16px;border-bottom:1px solid #e0eaf1}
.answer-editor-heading h3{margin:0 0 6px}
.answer-editor-heading p{margin:0;font-size:14px;color:#728796}
.answer-form label{font-size:16px;gap:10px}
.answer-form select{max-width:280px;background:#fff;min-height:44px}
.answer-form textarea{min-height:150px;resize:vertical;line-height:1.7;background:#fff}
.answer-form .complaint-actions{margin-top:0}
.answer-form>p{margin:0}
</style>