<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import backgroundUrl from '../../assets/background.jpg'
import logoUrl from '../../assets/logo.png'

const backgroundImage = ref(null)
const started = ref(false)
const reducedMotion = ref(window.matchMedia('(prefers-reduced-motion: reduce)').matches)
const showOverlay = ref(!reducedMotion.value)
const buttonReady = ref(reducedMotion.value)
let motionPreference
let disposed = false
let loading = false

async function startEntrance() {
  if (started.value || loading || disposed) return
  const image = backgroundImage.value
  if (!image?.complete || !image.naturalWidth) return
  loading = true
  try { await image.decode() } catch { /* A successfully loaded image can still be displayed. */ }
  if (disposed) return
  started.value = true
}
function updateMotionPreference() {
  reducedMotion.value = motionPreference.matches
  if (reducedMotion.value) {
    showOverlay.value = false
    buttonReady.value = true
  }
}
function guardLogin(event, navigate) {
  if (!buttonReady.value) {
    event.preventDefault()
    return
  }
  navigate(event)
}
onMounted(() => {
  motionPreference = window.matchMedia('(prefers-reduced-motion: reduce)')
  updateMotionPreference()
  motionPreference.addEventListener('change', updateMotionPreference)
  startEntrance()
})
onUnmounted(() => {
  disposed = true
  motionPreference?.removeEventListener('change', updateMotionPreference)
})
</script>

<template>
  <main class="intro" :class="{ 'intro-started': started, 'intro-reduced': reducedMotion, 'intro-button-ready': buttonReady }" aria-labelledby="intro-title">
    <img ref="backgroundImage" class="intro-background" :src="backgroundUrl" alt="" @load="startEntrance" />
    <div v-if="showOverlay" class="intro-overlay" aria-hidden="true" @animationend.self="showOverlay = false"></div>
    <img class="intro-logo" :src="logoUrl" alt="광주교통공사" />
    <h1 id="intro-title" class="intro-title">
      <span>시민과 함께 달리는</span>
      <strong>광주 교통공사</strong>
    </h1>
    <p class="intro-description">
      안전하고 편리한 도시철도 운영으로<br />
      시민의 더 나은 일상을 만들어갑니다.
    </p>
    <RouterLink v-slot="{ href, navigate }" to="/login" custom>
    <a class="intro-login" :href="href" :inert="!buttonReady" :tabindex="buttonReady ? 0 : -1" :aria-disabled="!buttonReady" @click="guardLogin($event, navigate)" @animationend.self="buttonReady = true">
      <span>로그인</span>
      <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M4 12h16m-6-6 6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" /></svg>
    </a>
    </RouterLink>
  </main>
</template>



<style scoped>
.intro {
  font-family: 'Noto Sans KR', 'Malgun Gothic', sans-serif;
  --scale: min(0.0520833333vw, 0.0925925926svh);
  position: relative;
  width: 100%;
  min-height: 100svh;
  overflow: hidden;
  isolation: isolate;
}

.intro-background {
  position: absolute;
  inset: 0;
  z-index: -1;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: right bottom;
}

.intro-logo {
  position: absolute;
  left: calc(90 * var(--scale));
  top: calc(60 * var(--scale));
  width: calc(340 * var(--scale));
  height: auto;
}

.intro-title {
  position: absolute;
  left: calc(155 * var(--scale));
  top: calc(185 * var(--scale));
  margin: 0;
  font-weight: 800;
  letter-spacing: -0.04em;
  line-height: 1.15;
  white-space: nowrap;
}

.intro-title span {
  display: block;
  color: #3298db;
  font-size: calc(88 * var(--scale));
}

.intro-title strong {
  display: block;
  color: #123d78;
  font-size: calc(100 * var(--scale));
  font-weight: 900;
}

.intro-description {
  position: absolute;
  left: calc(160 * var(--scale));
  top: calc(420 * var(--scale));
  margin: 0;
  color: #68727c;
  font-size: calc(30 * var(--scale));
  line-height: 1.65;
  letter-spacing: -0.035em;
}

.intro-login {
  position: absolute;
  left: calc(175 * var(--scale));
  top: calc(750 * var(--scale));
  display: flex;
  align-items: center;
  justify-content: center;
  width: calc(240 * var(--scale));
  height: calc(64 * var(--scale));
  border-radius: calc(16 * var(--scale));
  background: linear-gradient(135deg, #225b9b, #123d78);
  color: #fff;
  font-size: calc(24 * var(--scale));
  gap: calc(18 * var(--scale));
  box-shadow: 0 8px 22px rgb(18 61 120 / 22%);
  font-weight: 700;
  text-decoration: none;
  transition: transform 200ms ease, box-shadow 200ms ease;
}

.intro-login:hover { transform: translateY(-3px); box-shadow: 0 12px 28px rgb(18 61 120 / 32%); }
.intro-login:active { transform: translateY(0); box-shadow: 0 4px 12px rgb(18 61 120 / 24%); }
.intro-login:focus-visible { outline: 3px solid #3298db; outline-offset: 5px; }

/* Portrait screens keep the full character group visible at its original ratio. */
@media (max-aspect-ratio: 4/3) {
  .intro-background {
    object-fit: contain;
    object-position: right bottom;
    background: #f7fbfd;
  }

  .intro-logo { left: 6vw; top: 6svh; width: clamp(170px, 34vw, 280px); }
  .intro-title { left: 8vw; top: 20svh; }
  .intro-title span { font-size: clamp(26px, 5.8vw, 64px); }
  .intro-title strong { font-size: clamp(32px, 7vw, 76px); }
  .intro-description { left: 8vw; top: 38svh; font-size: clamp(15px, 2.6vw, 25px); }
  .intro-login {
    left: 8vw;
    top: 53svh;
    width: clamp(160px, 29vw, 250px);
    height: clamp(48px, 8vw, 68px);
    border-radius: 12px;
    font-size: clamp(18px, 3vw, 26px);
  }
}



.intro-overlay {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: #fff;
  opacity: 1;
  pointer-events: none;
}
.intro-logo, .intro-title, .intro-description, .intro-login { z-index: 1; opacity: 0; transform: translateY(12px); }
.intro-started .intro-overlay { animation: intro-reveal .75s ease-out both; }
.intro-started .intro-logo { animation: intro-content .5s cubic-bezier(.22, 1, .36, 1) .35s both; }
.intro-started .intro-title { animation: intro-content .5s cubic-bezier(.22, 1, .36, 1) .55s both; }
.intro-started .intro-description { animation: intro-content .5s cubic-bezier(.22, 1, .36, 1) .65s both; }
.intro-started:not(.intro-button-ready) .intro-login { animation: intro-content .5s cubic-bezier(.22, 1, .36, 1) .85s both; }
.intro:not(.intro-button-ready) .intro-login { pointer-events: none; transition: none; }
.intro-button-ready .intro-login { opacity: 1; transform: translateY(0); }
.intro-button-ready .intro-login:hover { transform: translateY(-3px); }
.intro-button-ready .intro-login:active { transform: translateY(0); }
.intro-login svg { width: 18px; height: 18px; flex-shrink: 0; }
@keyframes intro-reveal { from { opacity: 1; } to { opacity: 0; } }
@keyframes intro-content { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: translateY(0); } }
.intro-reduced .intro-logo, .intro-reduced .intro-title, .intro-reduced .intro-description, .intro-reduced .intro-login { animation: none; opacity: 1; transform: none; }
@media (prefers-reduced-motion: reduce) {
  .intro-overlay { display: none; animation: none; }
  .intro .intro-logo, .intro .intro-title, .intro .intro-description, .intro .intro-login { animation: none; opacity: 1; transform: none; }
  .intro .intro-login { transition: none; }
  .intro .intro-login:hover, .intro .intro-login:active { transform: none; }
}
</style>
