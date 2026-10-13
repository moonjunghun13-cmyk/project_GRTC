<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { ChevronLeft, ChevronRight, Pause, Play } from 'lucide-vue-next'
import ba1 from '../../assets/ba1.png'
import ba2 from '../../assets/ba2.png'
import ba3 from '../../assets/ba3.png'
import ba4 from '../../assets/ba4.png'

import ba6 from '../../assets/ba6.png'
const banners=[ba1,ba2,ba3,ba4,ba6]
const destinations=['/resources','/complaints','/complaints/guide','/notices','/notices']
const destinationLabels=['자료실','민원목록','민원 작성 가이드','공지사항','공지사항']
const index=ref(0),direction=ref('next'),paused=ref(false),hovered=ref(false),focused=ref(false),hidden=ref(false),reducedMotion=ref(false)
const running=computed(()=>!paused.value&&!hovered.value&&!focused.value&&!hidden.value&&!reducedMotion.value)
let timer,motion
function advance(step){direction.value=step<0?'previous':'next';index.value=(index.value+step+banners.length)%banners.length}
function select(target){direction.value=target<index.value?'previous':'next';index.value=target}
function schedule(){clearInterval(timer);if(running.value)timer=setInterval(()=>advance(1),5000)}
function visibility(){hidden.value=document.hidden}
function motionChanged(){reducedMotion.value=motion.matches}
function focusOut(event){if(!event.currentTarget.contains(event.relatedTarget))focused.value=false}
watch(running,schedule)
onMounted(()=>{motion=window.matchMedia('(prefers-reduced-motion: reduce)');motionChanged();visibility();motion.addEventListener('change',motionChanged);document.addEventListener('visibilitychange',visibility);banners.forEach(src=>{const img=new Image();img.src=src});schedule()})
onBeforeUnmount(()=>{clearInterval(timer);motion?.removeEventListener('change',motionChanged);document.removeEventListener('visibilitychange',visibility)})
</script>
<template>
 <section class="rolling-banner" aria-label="광주교통공사 안내 배너" aria-roledescription="캐러셀" @mouseenter="hovered=true" @mouseleave="hovered=false" @focusin="focused=true" @focusout="focusOut">
  <div class="banner-stage" :class="direction">
   <div class="banner-window" :aria-live="running?'off':'polite'">
    <Transition name="banner-slide"><RouterLink :key="index" class="banner-link" :to="destinations[index]" :aria-label="`${destinationLabels[index]}로 이동`"><img class="banner-image" :class="{ 'edge-filled': index===2 }" :src="banners[index]" :alt="`광주교통공사 서비스 안내 배너 ${index+1}`" width="1980" height="650" /></RouterLink></Transition>
   </div>
   <button class="banner-arrow previous-arrow" type="button" aria-label="이전 배너" @click="advance(-1)"><ChevronLeft :size="26" :stroke-width="1.7" /></button>
   <button class="banner-arrow next-arrow" type="button" aria-label="다음 배너" @click="advance(1)"><ChevronRight :size="26" :stroke-width="1.7" /></button>
  </div>
  <div class="banner-controls"><div class="banner-dots" aria-label="배너 선택"><button v-for="(_,i) in banners" :key="i" type="button" :class="{active:index===i}" :aria-label="`${i+1}번 배너 보기`" :aria-pressed="index===i" @click="select(i)"></button></div><span class="banner-count"><strong>{{index+1}}</strong> / {{banners.length}}</span><button class="banner-play" type="button" :aria-label="paused?'배너 자동 재생':'배너 자동 재생 일시정지'" :aria-pressed="paused" :disabled="reducedMotion" @click="paused=!paused"><component :is="paused?Play:Pause" :size="15" /></button></div>
 </section>
</template>
<style scoped>
.rolling-banner{max-width:1440px;margin:26px auto 0;padding:0 36px;box-sizing:border-box;width:100%}.banner-stage{position:relative}.banner-window{position:relative;aspect-ratio:1980/650;border-radius:20px;overflow:hidden;background:#EAF5FC;isolation:isolate}.banner-link{position:absolute;inset:0;display:block}.banner-link:focus-visible{outline:3px solid #0078AE;outline-offset:-4px;z-index:1}.banner-image{position:absolute;inset:0;width:100%;height:100%;object-fit:cover;object-position:center;display:block;max-width:none}.banner-arrow{position:absolute;top:50%;transform:translateY(-50%);width:44px;height:44px;border-radius:50%;border:1px solid #D4E5EF;background:rgba(255,255,255,.94);color:#086B9D;display:grid;place-items:center;cursor:pointer;z-index:2;box-shadow:0 3px 12px rgba(0,76,115,.09);transition:background .2s}.previous-arrow{left:-22px}.next-arrow{right:-22px}.banner-arrow:hover{background:#0078AE;color:white}.banner-controls{display:flex;align-items:center;justify-content:center;gap:18px;height:42px}.banner-dots{display:flex;align-items:center;gap:8px}.banner-dots button{width:8px;height:8px;border:0;border-radius:8px;background:#C9DDE9;padding:0;cursor:pointer;transition:width .3s,background .3s}.banner-dots button.active{width:26px;background:#0078AE}.banner-count{color:#7992A2;font-size:12px;min-width:32px}.banner-count strong{color:#064B76}.banner-play{border:0;background:transparent;color:#456D84;display:grid;place-items:center;width:28px;height:28px;border-radius:50%;cursor:pointer}.banner-play:hover{background:#EAF5FC}.banner-play:disabled{opacity:.45;cursor:default}.rolling-banner button:focus-visible{outline:3px solid #62B5D4;outline-offset:4px}.banner-slide-enter-active,.banner-slide-leave-active{transition:transform .65s cubic-bezier(.22,.61,.36,1)}.next .banner-slide-enter-from,.previous .banner-slide-leave-to{transform:translateX(100%)}.next .banner-slide-leave-to,.previous .banner-slide-enter-from{transform:translateX(-100%)}
@media(max-width:700px){.rolling-banner{margin-top:18px;padding:0 24px}.banner-window{border-radius:12px}.banner-arrow{width:32px;height:32px}.previous-arrow{left:-16px}.next-arrow{right:-16px}.banner-arrow svg{width:20px;height:20px}.banner-controls{gap:10px;height:40px}.banner-dots{gap:7px}}
@media(prefers-reduced-motion:reduce){.banner-slide-enter-active,.banner-slide-leave-active,.banner-dots button,.banner-arrow{transition:none}}
</style>

<style scoped>.banner-image.edge-filled{transform:scale(1.008)}</style>
