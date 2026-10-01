# 광주교통공사 백엔드 API 안내 (Vue 연동용)

백엔드는 두 개의 서버로 나뉩니다. 두 서버는 같은 PostgreSQL DB(`project`)를 씁니다.

| 서버 | 폴더 | 포트 | 담당 |
|---|---|---|---|
| main | `main` | 8081 | 인트로, 회원가입, 로그인, 사용자 화면(민원관리, 내 정보) |
| dashboard | `dashboard` | 8082 | 관리자 화면(대시보드, 차량, 배차, 운행, 민원, 회원관리) |

## 로그인 흐름 (같은 로그인 화면 사용)

1. 로그인 화면에서 `POST http://localhost:8081/api/auth/login` 을 호출합니다. 본문은 `{ loginId, password }` 입니다.
2. 응답의 `redirectPath` 로 화면을 이동합니다.
   - 관리자(ADMIN) 계정이면 `/dashboard` 입니다. 이후 관리자 화면은 8082 서버를 호출합니다.
   - 일반회원(USER)이면 `/complaints` 입니다. 이후 사용자 화면은 8081 서버를 호출합니다.
3. 로그인 세션은 DB(`SPRING_SESSION` 테이블)에 저장됩니다. 그래서 8081에서 로그인한 `SESSION` 쿠키로 8082도 인증됩니다.
   - axios 는 두 서버 모두 `withCredentials: true` 로 호출해야 합니다.
4. 개발용 관리자 계정은 `admin` / `admin1234!` 입니다. `app.seed.enabled=true` 일 때 자동으로 생성됩니다.

## 공통 오류 응답

모든 오류는 `{ "code": "...", "message": "..." }` 형식입니다.

- **401 `A002`:** 로그인하지 않은 상태입니다. 로그인 화면으로 이동시킵니다.
- **403 `A001`:** 일반회원이 관리자 API 를 호출한 경우입니다. "관리자 페이지이므로 열람이 불가합니다" 화면을 보여줍니다.
- 목록 API 의 `page` 는 1부터 시작합니다. 응답은 `{ content, page, size, totalElements, totalPages }` 입니다.

## main 서버 (8081)

| 기능 | 메서드 / 주소 |
|---|---|
| 로그인 / 로그아웃 | `POST /api/auth/login`, `POST /api/auth/logout` |
| 회원가입 / 아이디 중복확인 | `POST /api/auth/signup`, `GET /api/auth/check-id?loginId=` |
| 로그인한 사용자 (사이드바 하단) | `GET /api/auth/me` |
| 민원관리 목록 | `GET /api/complaints?keyword=&category=&type=&status=&page=1&size=10` |
| 민원 선택 목록 (유형/분류/상태) | `GET /api/complaints/options` |
| 글확인 (본인 민원만) | `GET /api/complaints/{id}` |
| 글쓰기 (multipart) | `POST /api/complaints` (type, category, title, content, files) |
| 수정 (multipart, 접수대기만) | `PUT /api/complaints/{id}` (+ deleteFileIds) |
| 삭제 (접수대기만) | `DELETE /api/complaints/{id}` |
| 첨부 다운로드 | `GET /api/complaints/{id}/attachments/{attachmentId}` |
| 내 정보 | `GET /api/members/me`, `PUT /api/members/me`, `GET /api/members/options` |

## dashboard 서버 (8082, 관리자 전용)

| 화면 | 메서드 / 주소 |
|---|---|
| 대시보드 | `GET /api/admin/dashboard?complainPeriod=THIS_MONTH&answerPeriod=THIS_MONTH` |
| 기간 선택 목록 | `GET /api/admin/dashboard/periods` |
| 차량관리 | `GET/POST /api/admin/vehicles`, `GET/PUT/DELETE /api/admin/vehicles/{id}` |
| 배차관리 | `GET/POST /api/admin/dispatches`, `GET /api/admin/dispatches/options`, `GET/PUT /api/admin/dispatches/{id}`, `PATCH /api/admin/dispatches/{id}/cancel` |
| 운행관리 (노선도) | `GET /api/admin/operations` |
| 민원관리 목록 / 상세 | `GET /api/admin/complaints`, `GET /api/admin/complaints/{id}` |
| 민원 등록 (multipart) | `POST /api/admin/complaints` |
| 답변 등록 | `PUT /api/admin/complaints/{id}/answer` (`{ status: RECEIVED/TRANSFERRED/ANSWERED, content }`) |
| 민원 첨부 다운로드 | `GET /api/admin/complaints/{id}/attachments/{attachmentId}` |
| 회원관리 | `GET /api/admin/members`, `GET/PUT /api/admin/members/{id}`, `PATCH /api/admin/members/{id}/status`, `PATCH /api/admin/members/{id}/role`, `GET /api/admin/members/options` |
| 관리자 본인 정보 | `GET /api/admin/me`, `PUT /api/admin/me` |

대시보드의 기간 값은 `TODAY`, `THIS_WEEK`, `THIS_MONTH`, `THIS_YEAR`, `ALL` 중 하나입니다.
