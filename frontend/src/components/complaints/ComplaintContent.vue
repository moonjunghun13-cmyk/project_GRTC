<script setup>
import '../../styles/complaint-detail.css'
import StatusBadge from '../common/StatusBadge.vue'
import AttachmentList from './AttachmentList.vue'
defineProps({complaint:{type:Object,required:true},admin:Boolean})
const date=value=>value?.replace('T',' ').slice(0,19)||'—'
</script>
<template>
 <article class="complaint-detail">
  <header class="complaint-detail-heading">
   <div><h2>민원 상세</h2><p>민원 내용과 처리 현황을 확인하세요.</p></div>
   <RouterLink class="complaint-back" :to="admin?'/dashboard/complaints':'/complaints'">← 민원 목록</RouterLink>
  </header>
  <section class="complaint-card complaint-submission">
   <div class="submission-heading"><span class="detail-eyebrow">접수된 민원</span><StatusBadge :status="complaint.status" :label="complaint.statusLabel"/></div>
   <h3 class="submission-title">{{complaint.title}}</h3>
   <dl class="complaint-facts">
    <div><dt>민원번호</dt><dd>{{complaint.complainNo}}</dd></div>
    <div><dt>작성자</dt><dd>{{complaint.writerName}}</dd></div>
    <div><dt>등록일</dt><dd>{{date(complaint.createdAt)}}</dd></div>
    <div><dt>민원유형</dt><dd>{{complaint.typeLabel}}</dd></div>
    <div><dt>분류</dt><dd>{{complaint.categoryLabel}}</dd></div>
    <div><dt>처리상태</dt><dd>{{complaint.statusLabel}}</dd></div>
   </dl>
   <div class="submission-body"><h4>민원 내용</h4><p>{{complaint.content}}</p></div>
  </section>
  <section class="complaint-card complaint-attachment-card"><AttachmentList :attachments="complaint.attachments||[]"/></section>
  <section class="complaint-card complaint-official-answer">
   <div class="official-heading"><span class="official-icon" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M20 11.5a7.5 7.5 0 0 1-7.5 7.5H5l-3 3V11.5A7.5 7.5 0 0 1 9.5 4H13a7 7 0 0 1 7 7.5Z"/><path d="m8 11 3 3 5-6"/></svg></span><div><h3>관리자 답변</h3><p>민원에 대한 담당자의 안내입니다.</p></div></div>
   <p class="official-content" :class="{'official-empty':!complaint.answer?.content}">{{complaint.answer?.content||'아직 등록된 답변이 없습니다.'}}</p>
   <div v-if="complaint.answer" class="complaint-answer-date"><span>답변자 <strong>{{complaint.answer.answeredByName||'—'}}</strong></span><span>답변일 {{date(complaint.answer.answeredAt)}}</span></div>
  </section>
 </article>
</template>
<style scoped>
.complaint-detail{gap:16px}
.complaint-detail-heading h2{margin:0 0 8px;font-size:30px}
.complaint-back{display:inline-flex;align-items:center;min-height:40px;padding:0 16px;border:1px solid #cbdde8;border-radius:9px;background:#fff;font-weight:700;white-space:nowrap}
.complaint-back:hover{background:#f0f8fd}
.submission-heading{display:flex;justify-content:space-between;align-items:center;gap:12px;margin-bottom:14px}
.detail-eyebrow{color:#607d90;font-size:14px;font-weight:700}
.complaint-submission .submission-title{font-size:26px;line-height:1.45;margin:0 0 22px;color:#173e5c;overflow-wrap:anywhere}
.complaint-facts{padding:18px 20px;background:#f6f9fc;border-radius:10px;gap:16px 24px}
.complaint-facts dt{font-size:13px;margin-bottom:5px}
.complaint-facts dd{font-size:15px;color:#354b5c}
.submission-body{margin-top:24px}
.submission-body h4{margin:0 0 14px;font-size:17px;color:#064b76}
.submission-body p,.official-content{white-space:pre-wrap;overflow-wrap:anywhere;margin:0;font-size:17px;line-height:1.85;color:#354b5c}
.complaint-official-answer{padding:24px;background:#f0f8fd;border-left:4px solid #0879ac}
.official-heading{display:flex;align-items:center;gap:12px;padding-bottom:18px;margin-bottom:20px;border-bottom:1px solid #cfe2ee}
.official-icon{width:42px;height:42px;border-radius:12px;background:#dceefa;color:#0879ac;display:grid;place-items:center;flex-shrink:0}
.official-icon svg{width:24px;height:24px}
.official-heading h3{margin:0 0 5px;font-size:20px;border:0;padding:0}
.official-heading p{margin:0;font-size:14px;color:#607d90}
.official-empty{color:#728796}
.complaint-answer-date{display:flex;flex-wrap:wrap;gap:10px 24px;font-size:14px}
.complaint-answer-date strong{margin-left:6px;font-weight:600}
@media(max-width:900px){.complaint-official-answer{padding:18px}.complaint-submission .submission-title{font-size:23px}.complaint-facts{padding:16px}.complaint-detail-heading{flex-wrap:wrap}}
@media(max-width:550px){.complaint-detail-heading h2{font-size:26px}.complaint-facts{grid-template-columns:repeat(2,minmax(0,1fr))}.submission-body p,.official-content{font-size:16px}.complaint-back{font-size:14px}}
</style>