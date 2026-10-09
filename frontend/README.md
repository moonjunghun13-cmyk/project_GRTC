# Vue 3 + Vite

This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).

## 백엔드 연동 기준

최신 기준은 [../docs/backend-api.md](../docs/backend-api.md)입니다. 백엔드는 통합 서버 하나(`http://localhost:8081`)입니다.

- **연동 완료:** 회원가입 · 로그인 · 내 정보(`/api/v1/auth/me`) · 로그아웃 · Access 토큰 자동 재발급(401 `TOKEN_EXPIRED` → `/api/v1/auth/reissue`)
  - Access 토큰은 `Authorization: Bearer` 헤더로 자동 첨부됩니다. (`src/api/clients.js`)
  - 응답 본문의 `data` 는 `unwrap(response)` 로 꺼냅니다.
- **아직 mock:** 대시보드, 차량, 배차, 운행, 민원, 회원관리 화면

`.env` 의 `VITE_ENABLE_API=true` 로 바꾸면 실제 서버와 연결됩니다. (`false` 면 mock 미리보기)
