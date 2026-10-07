<script setup>
import {homeRoute} from '../composables/useHomeRoute'
import { useRoute } from 'vue-router'
const route = useRoute()

import loginImage from '../assets/login.png'
import logoImage from '../assets/logo.png'
</script>

<template>
  <div class="authentication-layout">
    <aside class="authentication-visual" aria-label="광주 도시철도">
      <img :src="loginImage" alt="노을 진 광주 도심을 달리는 열차와 광주교통공사 캐릭터" />
    </aside>
    <main class="authentication-main" :class="{ 'is-signup': route.name === 'signup' }">
      <RouterLink class="authentication-logo" :to="homeRoute"><img :src="logoImage" alt="광주교통공사" /></RouterLink>
      <div class="authentication-content"><RouterView /></div>
    </main>
  </div>
</template>

<style scoped>
.authentication-layout { display: grid; grid-template-columns: 50% 50%; min-height: 100svh; background: #fff; font-family: var(--font-family); }
.authentication-visual { height: 100svh; position: sticky; top: 0; overflow: hidden; }
.authentication-visual > img { width: 100%; height: 100%; display: block; object-fit: cover; object-position: left center; }
.authentication-main { min-width: 0; position: relative; display: flex; flex-direction: column; align-items: center; padding: 150px 0 60px; }
.authentication-logo { position: absolute; top: 48px; right: 7.5%; width: min(34%, 300px); }
.authentication-logo img { width: 100%; height: auto; display: block; }
.authentication-main:not(.is-signup) :deep(.authentication-screen) { width: 85%; margin: auto 0; color: #222e3b; }
.authentication-main:not(.is-signup) :deep(.authentication-heading) { text-align: center; margin-bottom: 48px; }
.authentication-main:not(.is-signup) :deep(h1) { margin: 0 0 18px; color: #123d78; font-size: clamp(32px, 2.5vw, 48px); font-weight: 800; }
.authentication-main:not(.is-signup) :deep(.authentication-description) { margin: 0; color: #737b85; font-weight: 500; font-size: clamp(16px, 1.25vw, 24px); line-height: 1.65; }
.authentication-main:not(.is-signup) :deep(.authentication-form) { display: flex; flex-direction: column; gap: 26px; }
.authentication-main:not(.is-signup) :deep(.authentication-field) { display: flex; flex-direction: column; gap: 12px; }
.authentication-main:not(.is-signup) :deep(.authentication-field label) { font-size: clamp(16px, 1.1vw, 21px); font-weight: 800; }
.authentication-main:not(.is-signup) :deep(.authentication-field input) { width: 100%; min-width: 0; height: clamp(52px, 4vw, 72px); padding: 0 20px; border: 1px solid #d4dae1; border-radius: 8px; background: #fff; color: #222e3b; font: inherit; font-size: clamp(16px, 1.05vw, 20px); font-weight: 500; }
.authentication-main:not(.is-signup) :deep(input::placeholder) { color: #a0a6ad; }
.authentication-main:not(.is-signup) :deep(.authentication-signup-link) { align-self: flex-end; margin-top: -8px; color: #1954a2; font-weight: 700; font-size: 16px; text-underline-offset: 4px; }
.authentication-main:not(.is-signup) :deep(.authentication-submit) { width: 100%; min-height: clamp(56px, 4.2vw, 76px); border: 0; border-radius: 8px; background: #1954a2; color: #fff; font: inherit; font-size: clamp(19px, 1.4vw, 26px); font-weight: 700; cursor: pointer; transition: background-color 200ms ease; }
.authentication-main:not(.is-signup) :deep(.authentication-submit:hover) { background: #123d78; }
.authentication-main:not(.is-signup) :deep(.authentication-age) { display: flex; align-items: center; gap: 10px; font-size: 17px; cursor: pointer; }
.authentication-main:not(.is-signup) :deep(.authentication-age input) { width: 20px; height: 20px; margin: 0; accent-color: #1954a2; }
.authentication-main:not(.is-signup) :deep(.authentication-footer) { text-align: center; margin: 26px 0 0; color: #737b85; font-size: 17px; }
.authentication-main:not(.is-signup) :deep(.authentication-footer a) { color: #1954a2; font-weight: 700; margin-left: 10px; text-underline-offset: 4px; }
.authentication-main:not(.is-signup) :deep(.authentication-message) { color: #687582; text-align: center; margin: 18px 0 0; }
.authentication-main:not(.is-signup) :deep(input:focus-visible), .authentication-main:not(.is-signup) :deep(button:focus-visible), .authentication-main:not(.is-signup) :deep(a:focus-visible), .authentication-logo:focus-visible { outline: 3px solid #3298db; outline-offset: 4px; }
@media (max-width: 1000px) and (min-width: 761px) {
  .authentication-main:not(.is-signup) { padding-top: 110px; }
  .authentication-logo { top: 32px; width: 55%; }
  .authentication-main:not(.is-signup) :deep(.authentication-form) { gap: 20px; }
}
@media (max-width: 760px) {
  .authentication-layout { grid-template-columns: 1fr; }
  .authentication-visual { position: relative; height: clamp(200px, 35svh, 320px); }
  .authentication-visual > img { object-position: left 62%; }
  .authentication-main:not(.is-signup) { padding: 100px 0 40px; }
  .authentication-logo { top: 28px; width: 210px; max-width: 60%; }
  .authentication-main:not(.is-signup) :deep(.authentication-screen) { width: 85%; }
  .authentication-main:not(.is-signup) :deep(.authentication-heading) { margin-bottom: 30px; }
  .authentication-main:not(.is-signup) :deep(.authentication-form) { gap: 20px; }
}

.authentication-content { display: contents; }
.authentication-main.is-signup {
  display: flex;
  flex-direction: column;
  container-type: inline-size;
  padding: 24px 40px;
  box-sizing: border-box;
  gap: 16px;
}
.is-signup .authentication-logo {
  position: static;
  top: auto;
  right: auto;
  transform: none;
  align-self: flex-end;
  width: 240px;
  max-width: 100%;
  flex-shrink: 0;
}
.is-signup .authentication-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  width: 100%;
}
.authentication-main.is-signup :deep(.authentication-screen) {
  width: min(calc(82cqw + 65.6px), 100%);
  max-width: 760px;
  margin: auto;
  flex-shrink: 0;
  color: #222e3b;
}
.authentication-main.is-signup :deep(.authentication-heading) { text-align: center; margin-bottom: 22px; }
.authentication-main.is-signup :deep(h1) { margin: 0 0 8px; color: #123d78; font-size: 44px; font-weight: 800; line-height: 1.2; }
.authentication-main.is-signup :deep(.authentication-description) { margin: 0; color: #737b85; font-size: 17px; font-weight: 500; line-height: 1.5; }
.authentication-main.is-signup :deep(.authentication-form) { display: flex; flex-direction: column; gap: 12px; }
.authentication-main.is-signup :deep(.authentication-field) { display: flex; flex-direction: column; gap: 6px; }
.authentication-main.is-signup :deep(.authentication-field label) { font-size: 18px; font-weight: 700; line-height: 1.3; }
.authentication-main.is-signup :deep(.authentication-field input) { width: 100%; min-width: 0; height: 52px; padding: 0 18px; border: 1px solid #d4dae1; border-radius: 12px; background: #fff; color: #222e3b; font: inherit; font-size: 17px; font-weight: 500; }
.authentication-main.is-signup :deep(input::placeholder) { color: #a0a6ad; }
.authentication-main.is-signup :deep(.authentication-field input[aria-invalid="true"]) { border-color: #c52f35; }
.authentication-main.is-signup :deep(.authentication-age) { display: flex; align-items: center; gap: 10px; font-size: 16px; line-height: 22px; cursor: pointer; }
.authentication-main.is-signup :deep(.authentication-age input) { width: 20px; height: 20px; margin: 0; accent-color: #1954a2; }
.authentication-main.is-signup :deep(.authentication-submit) { width: 100%; height: 54px; min-height: 54px; border: 0; border-radius: 12px; background: #1954a2; color: #fff; font: inherit; font-size: 19px; font-weight: 800; cursor: pointer; transition: background-color 200ms ease; }
.authentication-main.is-signup :deep(.authentication-submit:hover) { background: #123d78; }
.authentication-main.is-signup :deep(.authentication-footer) { margin: 12px 0 0; text-align: center; color: #737b85; font-size: 16px; line-height: 1.5; }
.authentication-main.is-signup :deep(.authentication-footer a) { margin-left: 10px; color: #1954a2; font-weight: 700; text-underline-offset: 4px; }
.authentication-main.is-signup :deep(.authentication-message) { margin: 8px 0 0; color: #687582; font-size: 14px; text-align: center; }
.authentication-main.is-signup :deep(input:focus-visible), .authentication-main.is-signup :deep(button:focus-visible), .authentication-main.is-signup :deep(a:focus-visible) { outline: 3px solid #3298db; outline-offset: 4px; }
@media (max-height: 850px) {
  .authentication-main.is-signup { padding-block: 20px; gap: 4px; }
  .authentication-main.is-signup :deep(.authentication-heading) { margin-bottom: 10px; }
  .authentication-main.is-signup :deep(.authentication-form) { gap: 6px; }
  .authentication-main.is-signup :deep(.authentication-field) { gap: 4px; }
  .authentication-main.is-signup :deep(.authentication-field label) { line-height: 1.2; }
}
@media (max-width: 760px) { .authentication-main.is-signup { padding: 24px 20px; } }

@media (prefers-reduced-motion: reduce) { .authentication-main :deep(.authentication-submit) { transition: none; } }
</style>


