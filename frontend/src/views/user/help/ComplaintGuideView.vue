<script setup>
import {complaintTypeGuides} from '../../../data/citizenHelp'
import character from '../../../assets/minone.png'
import loud from '../../../assets/loud.png'
import normal from '../../../assets/normal.png'
import mad from '../../../assets/mad.png'
import star2 from '../../../assets/star2.png'
const typeCharacters={'단순':normal,'건의':loud,'제보':star2,'불만':mad}
import './help.css'
</script>
<template><section class="citizen-help"><header class="help-hero" aria-label="민원 작성 가이드"><img :src="character" alt="민원 작성 가이드" /></header><section aria-labelledby="type-heading"><h2 id="type-heading">어떤 유형의 민원인가요?</h2><div class="guide-types"><article v-for="item in complaintTypeGuides" :key="item.name" class="help-card guide-type-card" :data-type="item.name"><div class="guide-type-intro"><div><span class="guide-type-label">민원 유형</span><h3>{{item.name}}</h3><p>{{item.description}}</p></div><div class="guide-type-character"><img :src="typeCharacters[item.name]" alt=""/></div></div><div class="guide-example"><strong>작성 예시</strong><p>{{item.example}}</p></div></article></div></section><section class="help-card" aria-labelledby="steps-heading"><h2 id="steps-heading">민원 작성 절차</h2><ol class="guide-steps"><li><h3>로그인 후 민원 등록 열기</h3><p>민원 목록의 민원 등록 버튼을 선택합니다. 민원인 이름은 로그인한 회원 정보로 표시됩니다.</p></li><li><h3>민원유형과 분류 선택</h3><p>작성 화면의 실제 선택 목록에서 민원유형과 분류를 고릅니다.</p></li><li><h3>제목과 내용 작성</h3><p>핵심이 드러나는 제목과 함께 발생 장소·시간·상황·요청 사항을 적어 주세요. 내용은 5,000자 이내로 입력할 수 있습니다.</p></li><li><h3>첨부파일 확인 후 등록</h3><p>파일 선택으로 이미지 또는 PDF를 추가합니다. 여러 파일을 선택할 수 있으며 파일 이름 옆 제거 버튼으로 등록 전 제외할 수 있습니다. 서버 등록이 성공하면 민원 목록으로 이동합니다.</p></li></ol></section><div class="guide-types"><section class="help-card"><h2>수정·삭제 안내</h2><p>접수대기 상태이며 서버에서 수정 가능으로 내려준 본인 민원만 수정·삭제 버튼을 이용할 수 있습니다. 수정 화면에서는 기존 파일 삭제와 새 파일 추가가 가능합니다.</p><p>삭제 요청은 철회로 처리됩니다. 철회된 민원은 목록에 남지만 상세 내용은 볼 수 없습니다.</p></section><section class="help-card"><h2>답변 확인 방법</h2><p>민원 목록에서 본인 글의 상세보기를 선택하고 관리자 답변 영역을 확인하세요. 답변이 없으면 빈 상태 안내가 표시됩니다.</p><p>상세 화면을 다시 열거나 새로고침하면 최신 답변과 처리상태를 조회합니다.</p></section></div><footer class="guide-action"><p>준비가 되셨나요? 소중한 의견을 들려주세요.</p><RouterLink class="help-primary" :to="{name:'user-complaint-create'}">민원 작성하러 가기 →</RouterLink><RouterLink :to="{name:'user-faq'}">자주 묻는 질문 보기</RouterLink></footer></section></template>

<style scoped>
.guide-type-card{position:relative;border-top:3px solid #64B3E0 !important}
.guide-type-intro{display:flex;align-items:center;justify-content:space-between;gap:22px;min-height:150px}
.guide-type-intro>div:first-child{flex:1;min-width:0}
.guide-type-label{display:block;font-size:12px;color:#526A7D;font-weight:600;margin-bottom:8px}
.guide-type-intro h3{font-size:24px;margin-bottom:12px}
.guide-type-character{display:grid;place-items:center;width:140px;height:150px;flex-shrink:0;background:radial-gradient(ellipse,#EAF7FF 0%,#F7FCFF 50%,transparent 72%)}
.guide-type-character img{display:block;width:120px;height:140px;object-fit:contain}
.guide-type-card[data-type=건의]{border-top-color:#66B995 !important}
.guide-type-card[data-type=제보]{border-top-color:#608FB7 !important}
.guide-type-card[data-type=불만]{border-top-color:#DCA0B4 !important}
.guide-type-card[data-type=건의] .guide-type-character{background:radial-gradient(ellipse,#EAF7EF 0%,#FAFDFA 50%,transparent 72%)}
.guide-type-card[data-type=불만] .guide-type-character{background:radial-gradient(ellipse,#FCEEF3 0%,#FFF9FB 50%,transparent 72%)}
@media(max-width:700px){.guide-type-intro{gap:12px;min-height:130px}.guide-type-character{width:96px;height:120px}.guide-type-character img{width:90px;height:112px}.guide-type-intro h3{font-size:22px}}
</style>

<style scoped>
.guide-type-card{transition:transform .25s ease,box-shadow .25s ease;transform-origin:center}
.guide-type-character img{transition:transform .3s ease;transform-origin:center bottom}
@media(hover:hover) and (pointer:fine){
.guide-type-card:hover{transform:scale(1.025);z-index:1;box-shadow:0 8px 22px rgba(23,61,88,.1)}
.guide-type-card:hover .guide-type-character img{animation:guide-character-pulse 2.4s ease-in-out infinite}
}
@keyframes guide-character-pulse{0%,100%{transform:scale(1.08)}50%{transform:scale(1.16)}}
@media(prefers-reduced-motion:reduce){.guide-type-card,.guide-type-character img{transition:none}.guide-type-card:hover{transform:none}.guide-type-card:hover .guide-type-character img{animation:none}}
</style>

<style scoped>.citizen-help .help-hero .guide-happy-hero{width:240px;height:190px;object-fit:contain}@media(max-width:700px){.citizen-help .help-hero .guide-happy-hero{width:160px;height:135px}}</style>
