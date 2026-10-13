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
<template><p v-if="loading" role="status">불러오는 중입니다.</p><ComplaintErrorNotice v-if="withdrawn" reason="withdrawn" admin/><section v-else-if="complaint" class="admin-complaint-workspace"><div class="citizen-column"><ComplaintContent :complaint="complaint" admin :show-answer="false"/></div><div class="answer-column"><form class="complaint-card answer-form" @submit.prevent="save"><div class="answer-editor-heading"><span class="answer-kicker">관리자 전용</span><h3>관리자 답변</h3><p>처리상태를 선택하고 민원인에게 전달할 답변을 작성하세요.</p></div><section v-if="complaint.answer" class="saved-answer-preview"><h4>등록된 답변</h4><p>{{complaint.answer.content}}</p><div><span>답변자 {{complaint.answer.answeredByName||'—'}}</span><span>답변일 {{complaint.answer.answeredAt?.replace('T',' ').slice(0,16)||'—'}}</span></div></section><label>답변 처리상태<select v-model="status"><option v-for="o in answerStatuses" :key="o.value" :value="o.value">{{o.label}}</option></select></label><label>답변 내용<textarea v-model="content" rows="4"/></label><div class="complaint-actions"><RouterLink class="secondary" :to="{name:'admin-complaints'}">이전</RouterLink><button v-if="!saved" type="submit" :disabled="busy || !status">{{busy?'저장 중…':complaint.answer?'답변 저장':'답변 등록'}}</button></div><p role="status">{{message}}</p></form></div></section><section v-else><p>{{error || '민원 정보를 찾을 수 없습니다.'}}</p><RouterLink to="/dashboard/complaints">목록으로 돌아가기</RouterLink></section></template>


<style scoped>
.answer-form{border-top:3px solid var(--grtc-primary);gap:18px;margin-top:16px}
.answer-editor-heading{padding-bottom:16px;border-bottom:1px solid var(--grtc-border-soft)}
.answer-editor-heading h3{margin:0 0 6px}
.answer-editor-heading p{margin:0;font-size:14px;color:var(--grtc-muted)}
.answer-form label{font-size:16px;gap:10px}
.answer-form select{max-width:280px;background:#fff;min-height:44px}
.answer-form textarea{min-height:150px;resize:vertical;line-height:1.7;background:#fff}
.answer-form .complaint-actions{margin-top:0}
.answer-form>p{margin:0}
</style>
<style scoped>.admin-complaint-workspace{display:grid;grid-template-columns:minmax(0,1.15fr) minmax(0,1fr);gap:24px;align-items:start}.citizen-column,.answer-column{min-width:0}.answer-column{padding-top:86px}.answer-form{margin-top:0;padding:26px;border-top:4px solid #006BA6 !important}.answer-editor-heading h3{font-size:24px;color:#006BA6;margin:8px 0}.answer-kicker{display:inline-block;font-size:12px;font-weight:700;color:#006BA6}.answer-form textarea{min-height:280px;box-sizing:border-box;width:100%}.answer-form select{width:100%;max-width:none}.saved-answer-preview{padding:18px;border:1px solid #B9CDDD;border-radius:8px;background:#F3F7FB}.saved-answer-preview h4{margin:0 0 12px;font-size:15px;color:#123B5D}.saved-answer-preview p{margin:0;white-space:pre-wrap;overflow-wrap:anywhere;line-height:1.7;color:#263F52}.saved-answer-preview>div{display:flex;gap:12px;flex-wrap:wrap;margin-top:14px;font-size:12px;color:#536879}.citizen-column :deep(.complaint-facts){grid-template-columns:repeat(2,minmax(0,1fr))}.citizen-column :deep(.complaint-detail-heading){flex-wrap:wrap}.answer-form label{font-weight:600}.answer-form .complaint-actions{justify-content:flex-end}@media(max-width:1100px){.admin-complaint-workspace{grid-template-columns:minmax(0,1fr)}.answer-column{padding-top:0}.answer-form textarea{min-height:220px}}
</style>

<style scoped>
.citizen-column :deep(.complaint-facts){grid-template-columns:repeat(3,minmax(0,1fr));gap:18px 20px;padding:18px 0;margin:0;border-top:1px solid #DFE8EF;border-bottom:1px solid #DFE8EF;border-radius:0}
.citizen-column :deep(.complaint-facts>div){min-width:0}
.citizen-column :deep(.complaint-facts dt){margin-bottom:6px}
.citizen-column :deep(.complaint-facts dd){margin:0;overflow-wrap:anywhere;font-size:14px;line-height:1.5}
@media(max-width:650px){.citizen-column :deep(.complaint-facts){grid-template-columns:repeat(2,minmax(0,1fr))}}
</style>

<style scoped>
.citizen-column :deep(.complaint-facts){display:flex;flex-wrap:nowrap;gap:14px;overflow-x:auto}
.citizen-column :deep(.complaint-facts>div){flex:0 0 auto;min-width:0}
.citizen-column :deep(.complaint-facts dd){white-space:nowrap;font-size:13px}
</style>

<style scoped>
.citizen-column :deep(.complaint-facts){display:grid;grid-template-columns:1.4fr .9fr 1.7fr .7fr .9fr .9fr;gap:12px;width:100%;box-sizing:border-box;align-items:start}
.citizen-column :deep(.complaint-facts>div){min-width:0;text-align:left}
.citizen-column :deep(.complaint-facts dt){font-size:12px;line-height:1.4;margin:0 0 8px}
.citizen-column :deep(.complaint-facts dd){font-size:13px;line-height:1.5;white-space:normal;overflow-wrap:anywhere;margin:0}
@media(max-width:1350px) and (min-width:1101px){.admin-complaint-workspace{grid-template-columns:minmax(0,1.35fr) minmax(0,1fr);gap:20px}.citizen-column :deep(.complaint-facts){gap:8px}}
</style>

<style scoped>
@media(min-width:1101px){
.admin-complaint-workspace{height:calc(100dvh - 48px);min-height:0;align-items:stretch}
.citizen-column{height:100%;min-height:0;overflow:auto}
.citizen-column :deep(.complaint-detail){min-height:100%;display:flex;flex-direction:column}
.citizen-column :deep(.complaint-detail-heading){flex-shrink:0}
.citizen-column :deep(.complaint-submission){flex:1}
.citizen-column :deep(.complaint-attachment-card){flex-shrink:0}
.answer-column{height:100%;min-height:0;box-sizing:border-box;padding-top:86px;display:flex;flex-direction:column}
.answer-form{flex:1;min-height:0;overflow:auto;display:flex;flex-direction:column;gap:16px}
.answer-form>label:last-of-type{flex:1;min-height:170px;display:flex;flex-direction:column}
.answer-form textarea{flex:1;min-height:150px;resize:none}
.saved-answer-preview{max-height:180px;overflow:auto;flex-shrink:0}
.answer-editor-heading,.answer-form .complaint-actions,.answer-form>p{flex-shrink:0}
}
@media(min-width:1101px) and (max-width:1400px){.admin-complaint-workspace{height:calc(100dvh - 40px)}}
</style>

<style scoped>
@media(min-width:1101px){
.citizen-column{overflow:visible}
.citizen-column :deep(.complaint-detail){height:100%;min-height:0}
.citizen-column :deep(.complaint-submission){display:flex;flex-direction:column;min-height:0}
.citizen-column :deep(.submission-heading),.citizen-column :deep(.submission-title),.citizen-column :deep(.complaint-facts){flex-shrink:0}
.citizen-column :deep(.submission-body){flex:1;min-height:0;overflow:auto;overscroll-behavior:contain;padding-right:8px}
.answer-form textarea{overflow:auto;overscroll-behavior:contain}
.saved-answer-preview{overscroll-behavior:contain}
}
</style>
