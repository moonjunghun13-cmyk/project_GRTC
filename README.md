# Vue 3 + Vite

This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).

## 백엔드 연동 기준 (현재 연결 보류)

최신 기준은 [docs/backend-api.md](docs/backend-api.md), 변경점과 확인 사항은 [docs/backend-integration-plan.md](docs/backend-integration-plan.md)입니다. 이전 SESSION 계약은 최신 JWT 계약으로 대체됐습니다.

현재는 프론트 화면 제작 단계로 mock 미리보기와 `VITE_ENABLE_API=false`를 유지합니다. 서버 주소는 `.env.example`의 Vite 환경변수로 관리합니다. 기존 비활성 연동 코드와 인증 계약 테스트는 이전 명세 기반이므로, 최신 계약 반영 및 최종 연결 요청 전에는 활성화하지 마세요.
