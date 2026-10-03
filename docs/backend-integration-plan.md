# 최신 API 기준 비교 및 최종 연동 계획

갱신일: 2026-10-02. 기준 문서: [backend-api.md](backend-api.md).
이번 변경은 문서만 갱신한다. 디자인, 레이아웃, 라우팅, mock, 검증, API 비활성화 설정은 유지한다. 아직 전달받지 않은 화면은 제작하지 않는다.

## 이전 기준에서 대체된 규칙

| 항목 | 이전 기준 | 최신 기준 |
|---|---|---|
| 인증 | SESSION 쿠키, SPRING_SESSION | 두 서버 모두 Authorization: Bearer {accessToken}; Refresh는 HttpOnly refreshToken 쿠키 |
| 토큰 기간 | 해당 없음 | Access 30분, Refresh 14일 |
| 일반 경로 | /api | /api/v1 |
| 일반 성공 응답 | 직접 객체 | { success, data, error } |
| 일반 목록 | page 1부터 | page 0부터, 기본 size 20, 최대 100 |
| 민원 경로·성공·페이지 | /api, 직접 객체, page 1부터 | 동일 유지; 기본 size 10 |
| 실패 응답 | { code, message }, A001/A002 | { success:false, data:null, error:{ code, message, details } } |
| 로그인 이동 | redirectPath | data.member.redirectPath: ADMIN /dashboard, USER /complaints |
| 회원·차량·배차 정보 수정 | 일부 PUT | 최신 각 엔드포인트의 PATCH, 전달한 항목만 수정 |
| 민원 수정·답변 | PUT | PUT 유지 |
| 본인 프로필 이미지 | 미명시 | main PUT /api/v1/members/me/profile-image, multipart file |
| 민원 파일 제한 | 미확정 | jpg/png/gif/pdf, 파일당 10MB 이하, 민원당 최대 5개 |

main: http://localhost:8081, dashboard: http://localhost:8082. 로그인·재발급은 main에서 수행한다. 두 인스턴스 모두 withCredentials:true를 유지한다. 일반 API는 /api/v1 뒤에 URI를 붙이고, 민원 /api 경로는 그대로 사용한다. 단일 전역 접두사·성공 응답 해제·페이지 번호 변환을 적용하지 않는다. 파일 조회·다운로드의 바이너리 응답도 JSON envelope로 처리하지 않는다.

일반 날짜는 ISO-8601 및 명세의 오프셋을 따르며, 민원 날짜·시각은 오프셋 없는 문자열이다. 의미를 확인하지 않고 시간대를 일괄 변환하지 않는다. PATCH 외 민원 PUT, 프로필 이미지 PUT, 답변 PUT 등 개별 메서드를 보존한다.

## 현재 코드와 비교한 차이

확인 대상은 현재 frontend 소스다. 오래된 로컬 백엔드 DTO를 새 계약보다 우선하지 않는다.

| 현재 파일/화면 | 확인한 상태 | 최종 연동 시 작업 또는 확인 |
|---|---|---|
| src/api/clients.js | 두 서버 주소·withCredentials·비활성 요청 차단; 이전 message/code, A001/A002 처리 | 요청 헤더와 error.code/message/details 처리 변경. TOKEN_EXPIRED와 LOGIN_FAILED를 같은 401로 일괄 처리하지 않음 |
| src/api/auth.js | /api/auth/login 및 signup; signup에 nickname/email, passwordConfirm 누락 | /api/v1/auth/*로 변경. 로그인 {loginId,password}; signup 항목은 아래 확인 후 매핑 |
| src/stores/auth.js | /api/auth/me 직접 객체, nickname 기반 profile, 세션 복원 방식 | /api/v1/auth/me 응답 예시 확인 후 JWT 복원과 loginId 매핑. 본인 요약 응답 형태를 추측하지 않음 |
| src/views/auth/LoginView.vue | 이전 로그인 응답 경로 처리 | axios response.data의 data.member.redirectPath를 읽고 허용된 앱 내부 경로로 이동 |
| src/api/pagination.js | 모든 목록에 1 기반 page를 가정 | 일반/민원 어댑터 분리; UI 페이지 표시와 서버 번호를 해당 API별로 명시적으로 매핑 |
| tests/auth.test.mjs | 이전 경로·DTO·오류 응답을 테스트 | 최종 연동 단계에 최신 계약 테스트로 교체. 현재 테스트는 새 계약의 검증 근거가 아님 |
| 회원가입 화면 | 이름/아이디/이메일/비밀번호/확인/14세 체크 및 이메일 필수 검증 | 이메일 삭제·미전송을 이번 작업에서 결정하지 않음 |
| 민원 작성 화면 | type/title/content/민원인, category 선택 추가됨; 사용자 ID 기반 mock 등록 | 선택 코드 저장 및 필터 연결; 실제 연동 시 options.categories 공급 |
| src/mocks/complaintForm.js | simple/suggestion/report/dissatisfaction UI mock 값 | API의 SIMPLE/SUGGESTION/REPORT/COMPLAINT와 구분; options 응답 구조 확인 후 매핑 |
| 민원 작성 첨부 | image/* 또는 PDF, 임의 개수·용량 제한 없이 로컬 선택 | 최종 연동 때 명세의 형식/10MB/5개 제한 적용. 이번에는 mock 기능 유지 |
| 관리자 회원 상세 | username/rank/photo 등 UI mock, 로컬 이미지 미리보기 | loginId/position 등 DTO 매핑 확인; 조회 대상 회원 이미지 업로드 API 없음 |
| 대시보드 | null 상태, 독립 complainPeriod/answerPeriod 선택 | API data 구조 확인 후 카드에 연결. 임의 수치 생성 금지 |

## 백엔드 담당자 확인 항목

1. **회원가입 이메일:** 새 signup은 name, loginId, password, passwordConfirm, over14이며 이메일이 없다. 이메일 입력·필수 검증을 유지해야 하는가? 이메일을 가입 시 저장하려면 어떤 필드·엔드포인트를 사용하는가? 문서의 DUPLICATE_EMAIL은 어느 요청에서 발생하는가? 현재 이메일을 삭제하거나 다른 API로 임의 전송하지 않는다.
2. **민원 category (후속 보완으로 해결):** 작성·수정에 별도 분류 선택을 제공하고 선택 코드를 저장한다. 실제 공급은 options.categories, 전송은 multipart 일반 필드 category다. 구체적인 options 항목 JSON 구조만 추가 확인한다.
3. **타 회원 프로필:** 관리자 회원 상세에서 조회 중인 회원의 이미지 변경은 로컬 미리보기다. 타 회원 이미지 변경을 지원하는가? 지원한다면 dashboard 엔드포인트·권한·multipart 파트·응답이 필요하다. /members/me/profile-image는 로그인한 본인 전용이며 타 회원에게 대신 사용하지 않는다.
4. /auth/me, /auth/reissue, check-id, signup, 회원 목록·상세·options, 대시보드·차량·배차·운행, 민원 options·상세·첨부의 실제 JSON 예시와 각 필드 자료형/null 허용 여부가 필요하다. 로그인 member 예시는 /auth/me 응답 전체 형식을 보장하지 않는다.
5. 관리자 회원 PATCH의 허용 요청 필드·필수 조건, department/position의 코드 또는 표시 문자열 여부를 확인한다. UI mock rank를 임의로 서버 position 값으로 확정하지 않는다.
6. 민원 content가 HTML을 허용하는지 또는 일반 텍스트인지, 최대 글자 수(현재 5,000은 UI 설정), multipart files/deleteFileIds의 반복 키·직렬화 방식과 관리자 등록 파트 규칙을 확인한다.
7. Refresh 쿠키의 Path/Domain/SameSite/Secure, 프론트 origin 및 두 서버 CORS 허용 설정, 재발급/로그아웃 응답과 쿠키 삭제 범위를 확인한다. 토큰 보관 위치는 이번 문서 갱신에서 임의 확정하지 않는다.

## 최종 연결 요청 후 진행할 작업

- mainApi/dashboardApi 서버 환경변수와 withCredentials 유지. Access 토큰 헤더를 양쪽에 적용하고 Refresh 토큰은 JavaScript로 읽지 않는다.
- 로그인 성공 envelope, data.accessToken 및 data.member, redirectPath 검증; 재발급/새로고침 복원/로그아웃 흐름 구현. TOKEN_EXPIRED 재발급은 중복 요청을 합치고 재시도 횟수를 제한해 무한 루프를 방지한다. LOGIN_FAILED는 폼 오류를 유지한다. UNAUTHORIZED 또는 재발급 실패 시 로그인 유도, FORBIDDEN은 기존 제한 화면 사용. 계정 정지/탈퇴·항목별 INVALID_INPUT은 별도 처리한다.
- 일반 JSON 성공/실패와 민원 원본 성공/공통 실패를 각각 해석한다. 두 페이징 규칙을 독립적으로 테스트한다.
- 최신 메서드·요청 필드만 적용하고 확인되지 않은 DTO는 추측하지 않는다. 민원 파일은 FormData, boundary는 브라우저가 설정하게 한다. WAITING일 때만 사용자 수정·삭제를 허용한다.
- 서버 데이터와 mock을 분리해 단계적으로 연결하고 관리자/사용자 권한, 본인 민원 접근, 필드 오류, 파일 제한, 세션 대신 토큰 재발급, 두 서버 인증과 CORS를 실제 서버에서 검증한다.
- API 호출 차단을 해제하는 작업은 사용자의 최종 연결 요청 시에만 수행한다. 이번 작업에서는 토큰 발급·저장·재발급·API 호출을 추가하지 않는다.

## 후속 보완 적용 결과 (2026-10-02)

- 분류 선택·저장·목록 필터를 mock으로 연결. 유형과 분류는 별도 필드이며 임의 기본 분류 없음.
- options 공급을 src/services/complaintOptions.js로 분리. mock value/label은 UI 어댑터 형식이며 실제 응답 DTO를 확정하지 않는다.
- 목록·상세는 statusLabel 우선; 코드 기반 색상 유지. 관리자 답변은 WAITING 제외.
- 대시보드 mock 상태 순서·명칭을 최신 기준으로 통일하고 모든 수치 null 유지. 서버 byStatus 연결 시 label·순서를 유지하며 수치 필드는 별도 확인.
- 직전 화면 수정에서 답변 선택에 접수대기를 포함했던 동작은 이 보완으로 대체한다.
