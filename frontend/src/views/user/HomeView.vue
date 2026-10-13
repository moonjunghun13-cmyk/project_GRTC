<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { ArrowUpRight, ArrowRight, MessageSquareText, Megaphone, FolderOpen, PenLine, BookOpen, CircleHelp } from 'lucide-vue-next'
import AppHeader from '../../components/common/AppHeader.vue'
import { currentUser } from '../../stores/currentUser'
import UserRollingBanner from '../../components/common/UserRollingBanner.vue'
import writingCharacter from '../../assets/loud.png'
import questionCharacter from '../../assets/normal.png'
import homeBackground from '../../assets/background2.png'
const name=computed(()=>currentUser.value?.name||'회원')
const shortcuts=[
 {title:'민원목록',description:'작성한 민원과 답변을 확인해요',to:'/complaints',icon:MessageSquareText,color:'blue'},
 {title:'민원 작성',description:'더 나은 교통을 위한 의견을 남겨요',to:'/complaints/new',icon:PenLine,color:'green'},
 {title:'공지사항',description:'새로운 소식과 이용 안내',to:'/notices',icon:Megaphone,color:'sky'},
 {title:'자료실',description:'이용 안내와 필요한 자료',to:'/resources',icon:FolderOpen,color:'purple'},
]
</script>
<template>
 <div class="citizen-home" :style="{ '--home-background': `url(${homeBackground})` }">
  <AppHeader admin />
  <nav class="home-navigation" aria-label="사용자 주요 메뉴"><div><RouterLink to="/home" aria-current="page">홈</RouterLink><RouterLink to="/notices">공지사항</RouterLink><RouterLink to="/resources">자료실</RouterLink><RouterLink to="/complaints">민원목록</RouterLink><RouterLink to="/complaints/guide">민원 작성 가이드</RouterLink><RouterLink to="/complaints/faq">자주 묻는 질문</RouterLink></div></nav>
  <main>
   <UserRollingBanner />
   <div class="home-content">
    <div class="welcome-line"><div><span class="welcome-dot" aria-hidden="true"></span><p><strong>{{name}}님, 반갑습니다.</strong> 오늘은 무엇을 도와드릴까요?</p></div><RouterLink to="/complaints">민원 확인하기 <ArrowUpRight :size="16" /></RouterLink></div>
    <section class="shortcut-grid" aria-label="빠른 메뉴"><RouterLink v-for="item in shortcuts" :key="item.to" :to="item.to" class="shortcut-card" :class="item.color"><span class="shortcut-icon"><component :is="item.icon" :size="27" :stroke-width="1.7" /></span><div><h2>{{item.title}}</h2><p>{{item.description}}</p></div><ArrowUpRight class="shortcut-arrow" :size="20" /></RouterLink></section>
    <section class="help-section" aria-labelledby="help-title"><div class="section-heading"><div><span>처음 이용하시나요?</span><h2 id="help-title">쉽게 알아보는 민원 서비스</h2></div><p>작성부터 답변 확인까지, 차근차근 안내해 드려요.</p></div><div class="help-grid">
     <RouterLink to="/complaints/guide" class="home-help-card guide-card"><div class="help-copy"><span class="help-tag"><BookOpen :size="15" /> 작성 가이드</span><h3>어떤 의견을<br/>어떻게 남기면 좋을까요?</h3><p>민원 유형과 작성 예시, 첨부파일 등록 방법을 알아보세요.</p><span class="help-action">민원 작성 가이드 <ArrowRight :size="17" /></span></div><img :src="writingCharacter" alt="민원 작성을 돕는 광주교통공사 캐릭터" /></RouterLink>
     <RouterLink to="/complaints/faq" class="home-help-card faq-card"><div class="help-copy"><span class="help-tag"><CircleHelp :size="15" /> 자주 묻는 질문</span><h3>궁금한 점이 있다면,<br/>여기서 먼저 확인해요.</h3><p>회원정보, 민원 작성, 답변 확인에 대한 질문을 모았어요.</p><span class="help-action">자주 묻는 질문 <ArrowRight :size="17" /></span></div><img :src="questionCharacter" alt="질문을 안내하는 광주교통공사 캐릭터" /></RouterLink>
    </div></section>
   </div>
  </main>
  <footer class="home-footer"><span>광주교통공사</span><p>시민의 목소리와 함께, 더 편리하고 안전한 교통을 만들어 갑니다.</p><RouterLink to="/complaints/new">민원 작성 <ArrowUpRight :size="14" /></RouterLink></footer>
 </div>
</template>
<style scoped>
.citizen-home{min-height:100svh;background:#fff;color:#123D58;font-family:var(--font-family,'SUIT',sans-serif);display:flex;flex-direction:column}.citizen-home main{flex:1}.home-navigation{background:#fff;border-bottom:1px solid #DFECF3}.home-navigation>div{max-width:1440px;margin:auto;display:flex;align-items:center;gap:32px;padding:0 36px;min-height:62px;box-sizing:border-box}.home-navigation a{color:#526D7D;text-decoration:none;font-size:14px;font-weight:600;padding:20px 0;white-space:nowrap}.home-navigation a[aria-current=page]{color:#0078AE;border-bottom:3px solid #0078AE}.home-navigation a:hover{color:#0078AE}.home-navigation .profile-link{margin-left:auto;display:flex;align-items:center;gap:6px}.home-content{max-width:1440px;margin:auto;padding:0 36px 42px;box-sizing:border-box}.welcome-line{display:flex;justify-content:space-between;align-items:center;gap:16px;padding:26px 0}.welcome-line>div{display:flex;align-items:center;gap:10px}.welcome-dot{width:8px;height:8px;background:#83AF38;border-radius:50%;flex-shrink:0}.welcome-line p{margin:0;color:#627F91;font-size:14px}.welcome-line strong{color:#164C6A;margin-right:8px}.welcome-line>a{display:flex;align-items:center;gap:6px;text-decoration:none;font-size:13px;color:#0078AE;font-weight:700;white-space:nowrap}.shortcut-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:16px}.shortcut-card{display:flex;align-items:center;position:relative;gap:14px;border:1px solid #D8E7F0;border-radius:16px;padding:24px 20px;text-decoration:none;color:inherit;transition:transform .2s,border-color .2s,box-shadow .2s;background:white}.shortcut-card:hover{transform:translateY(-3px);border-color:#91C4DF;box-shadow:0 8px 20px rgba(0,94,143,.07)}.shortcut-icon{width:50px;height:50px;border-radius:14px;display:grid;place-items:center;flex-shrink:0;background:#EAF5FC;color:#0078AE}.green .shortcut-icon{background:#EFF6E6;color:#578A22}.sky .shortcut-icon{background:#EAF8FB;color:#158CA8}.purple .shortcut-icon{background:#F1EEFB;color:#7662B5}.shortcut-card h2{margin:0 0 8px;font-size:19px;font-weight:800}.shortcut-card p{margin:0;font-size:12px;color:#6B8391;line-height:1.5}.shortcut-arrow{position:absolute;right:14px;top:14px;color:#88A7BA}.help-section{margin-top:36px}.section-heading{display:flex;justify-content:space-between;align-items:flex-end;gap:16px;margin-bottom:18px}.section-heading span{font-size:12px;color:#0078AE;font-weight:700}.section-heading h2{margin:7px 0 0;font-size:23px;font-weight:800}.section-heading>p{margin:0;font-size:13px;color:#7890A0}.help-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px}.home-help-card{display:flex;align-items:center;justify-content:space-between;min-height:232px;border-radius:20px;padding:26px 30px;text-decoration:none;color:inherit;position:relative;isolation:isolate;border:1px solid #DAEAF3;box-sizing:border-box;background:#F2F9FD;transition:border-color .2s}.faq-card{background:#F5F9F0;border-color:#E4EDD9}.home-help-card:hover{border-color:#91C4DF}.help-copy{max-width:70%;position:relative;z-index:1}.help-tag{display:inline-flex;align-items:center;gap:6px;color:#4B7890;font-size:12px;font-weight:700}.home-help-card h3{font-size:23px;font-weight:800;line-height:1.4;letter-spacing:-.6px;margin:12px 0}.home-help-card p{font-size:13px;color:#658192;line-height:1.7;margin:0 0 18px;max-width:300px}.help-action{display:inline-flex;align-items:center;gap:16px;color:#0078AE;font-size:13px;font-weight:700}.home-help-card img{width:190px;max-width:32%;height:190px;object-fit:contain;align-self:flex-end;transition:transform .3s;transform-origin:center bottom}.home-help-card:hover img{transform:scale(1.05)}.home-footer{border-top:1px solid #E2ECF2;display:flex;align-items:center;justify-content:center;gap:20px;padding:20px 36px;color:#7790A0;font-size:12px;background:#FAFCFE}.home-footer>span{color:#41687F;font-weight:800}.home-footer p{margin:0}.home-footer a{color:#0078AE;text-decoration:none;display:flex;align-items:center;gap:4px}.citizen-home a:focus-visible{outline:3px solid #62B5D4;outline-offset:4px}

@media(max-width:1150px){.home-navigation>div{gap:22px}.shortcut-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.home-help-card{padding:24px}.home-help-card h3{font-size:20px}.home-help-card img{width:140px;height:170px}.help-copy{max-width:70%}.section-heading>p{display:none}}
@media(max-width:700px){.home-navigation>div{padding:0 20px;gap:22px;overflow-x:auto;min-height:52px}.home-navigation a{padding:16px 0;font-size:13px}.home-navigation .profile-link{margin-left:0}.home-content{padding:0 20px 30px}.welcome-line{align-items:flex-start;padding:22px 0}.welcome-line p{line-height:1.8}.welcome-line strong{display:block}.welcome-line>a{font-size:12px}.shortcut-grid{gap:10px}.shortcut-card{padding:22px 14px;align-items:flex-start;flex-direction:column;gap:12px}.shortcut-card h2{font-size:17px}.shortcut-card p{font-size:12px}.shortcut-icon{width:42px;height:42px}.help-grid{grid-template-columns:1fr}.home-help-card{min-height:230px;padding:24px 20px}.home-help-card img{max-width:30%;height:165px}.help-copy{max-width:70%}.home-footer{padding:20px;flex-wrap:wrap;gap:10px}.home-footer p{width:100%;text-align:center;line-height:1.6;order:3}.section-heading h2{font-size:21px}.citizen-home :deep(.app-header.admin-header){padding-right:12px;gap:8px}.citizen-home :deep(.app-header.admin-header>a:first-of-type){width:190px}.citizen-home :deep(.header-session button){padding:8px 12px;font-size:13px}}
@media(prefers-reduced-motion:reduce){.shortcut-card,.home-help-card,.home-help-card img{transition:none}.shortcut-card:hover,.home-help-card:hover img{transform:none}}
</style>
<style scoped>
.citizen-home{position:relative;isolation:isolate}
.citizen-home::before{content:'';position:absolute;inset:0;z-index:-1;pointer-events:none;background-image:var(--home-background);background-position:center top;background-size:cover;background-repeat:no-repeat;opacity:1}
</style><style scoped>
/* Keep text legible over the illustrated page background. */
.welcome-line,.section-heading{position:relative;isolation:isolate}
.welcome-line::before,.section-heading::before{content:'';position:absolute;inset:0 -14px;z-index:-1;background:rgba(255,255,255,.94);border-radius:12px;pointer-events:none}
.section-heading::before{inset:-10px -14px}
.welcome-line p,.section-heading>p{color:#456579}
.welcome-line strong,.section-heading h2{color:#123D58}
.guide-card .help-copy{max-width:58%;flex:1;min-width:0}
.guide-card img{width:280px;max-width:42%;height:220px;align-self:center}
@media(max-width:1150px){.guide-card img{width:230px;height:205px;max-width:42%}}
@media(max-width:700px){.guide-card{flex-wrap:wrap}.guide-card .help-copy{max-width:100%;flex-basis:100%}.guide-card img{width:260px;max-width:90%;height:145px;margin:8px auto 0}.welcome-line::before,.section-heading::before{inset-inline:-8px}}
</style><style scoped>
/* Align all visible panels to the same content edges. */
.welcome-line::before,.section-heading::before{content:none}
.welcome-line,.section-heading{box-sizing:border-box;width:100%;background:rgba(255,255,255,.96);border-radius:14px;padding:18px 20px}
.welcome-line{margin:12px 0 18px}
.section-heading{margin-bottom:18px}
.help-grid{align-items:stretch}
.home-help-card{height:100%;min-height:276px}
@media(max-width:700px){.welcome-line,.section-heading{padding:16px}.home-help-card{height:auto;min-height:250px}}
</style><style scoped>
.shortcut-card,.home-help-card{box-shadow:0 5px 16px rgba(20,62,87,.12),0 1px 3px rgba(20,62,87,.05)}
.shortcut-card:hover{box-shadow:0 9px 24px rgba(20,62,87,.17)}
.welcome-line,.section-heading{box-shadow:0 4px 14px rgba(20,62,87,.10)}
.citizen-home :deep(.banner-window){box-shadow:0 8px 26px rgba(20,62,87,.16)}
</style><style scoped>
.faq-card .help-copy{max-width:58%;flex:1;min-width:0}
.faq-card img{width:280px;max-width:42%;height:220px;align-self:center}
@media(max-width:1150px){.faq-card img{width:230px;height:205px;max-width:42%}}
@media(max-width:700px){.faq-card{flex-wrap:wrap}.faq-card .help-copy{max-width:100%;flex-basis:100%}.faq-card img{width:260px;max-width:90%;height:145px;margin:8px auto 0}}
</style>