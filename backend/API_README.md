# API 명세서

광주교통공사 백엔드 API 명세서입니다. 이 문서가 기준이며, 코드는 이 문서에 맞춰져 있습니다.

### 3-0. 서버 구성

백엔드는 **서버 하나**(`main` 폴더, `http://localhost:8081`)입니다. 예전에는 main(8081)과 dashboard(8082) 두 서버였지만 하나로 합쳤습니다.

| 구분 | 패키지 | 주소 | 담당 |
| --- | --- | --- | --- |
| 공통·일반 사용자 | `com.grtc.main.login`, `member`, `qna` | `/api/v1/auth/**`, `/api/v1/members/**`, `/api/complaints/**` | 회원가입, 로그인, 토큰, 내 정보, 민원 작성·조회 |
| 관리자 | `com.grtc.main.admin.*` | `/api/v1/admin/**`, `/api/admin/complaints/**` | 대시보드, 차량, 배차, 운행, 민원 처리, 회원 관리 |

- 로그인으로 받은 Access 토큰 하나로 모든 API 를 호출합니다.
- 관리자 API 는 `ADMIN` 권한 확인 + `AdminAccessFilter`(DB 의 최신 권한·상태 재확인)로 이중 확인합니다.
- 아래 3-3 API 목록의 **[main]**, **[dashboard]** 표시는 예전 구분입니다. 지금은 모두 8081 서버입니다.

### 3-1. 공통 규칙

- **Base URL**: `/api/v1`
- **인증**: `Authorization: Bearer {accessToken}` 헤더. Access 토큰은 30분, Refresh 토큰은 14일이며 Refresh 토큰은 HttpOnly 쿠키(`refreshToken`)로 전달합니다.
  - Refresh 쿠키가 오가려면 프론트(axios)는 `withCredentials: true` 로 호출해야 합니다.
  - Access 토큰이 만료되면 `401 TOKEN_EXPIRED` 가 내려옵니다. 이때 `POST /auth/reissue` 로 새 토큰을 받아 다시 요청합니다.
- **Content-Type**: `application/json` (파일 업로드는 `multipart/form-data`)
- **날짜 형식**: ISO-8601 (`2026-10-15`, `2026-10-15T05:30:00+09:00`, 시각만 있는 값은 `05:30:00`)
- **페이징**: `?page=0&size=20&sort=createdAt,desc` (page 는 0부터, size 기본 20·최대 100)
  - `sort` 는 `필드,방향` 형식입니다. 방향을 빼면 `asc` 이고, 보내지 않으면 API 마다 정해진 기본 정렬을 씁니다.
  - 정렬할 수 없는 필드를 보내면 `400 INVALID_INPUT` 입니다.
- **수정(PATCH)**: 보낸 항목만 바뀝니다. 보내지 않은 항목은 그대로 둡니다.
- **권한 표기**: 전체(비로그인 포함), 회원(로그인한 모든 사용자), 관리자(ADMIN)
  - 회원 역할은 관리자(`ADMIN`)와 일반회원(`USER`) 두 가지입니다.
  - 일반회원이 관리자 API 를 호출하면 `403 FORBIDDEN` 이 내려옵니다. (프론트는 "관리자 페이지이므로 열람이 불가합니다" 화면 표시)

**성공 응답**

```json
{
  "success": true,
  "data": { },
  "error": null
}
```

**페이징 응답의 data**

```json
{
  "content": [ ],
  "page": 0,
  "size": 20,
  "totalElements": 135,
  "totalPages": 7
}
```

**실패 응답**

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "SCHEDULE_CONFLICT",
    "message": "해당 편성은 같은 시간대에 이미 배정되어 있습니다.",
    "details": [ ]
  }
}
```

입력값 검증 실패(`INVALID_INPUT`)일 때는 `details` 에 항목별 내용이 들어갑니다.

```json
"details": [
  { "field": "loginId", "message": "필수 입력란입니다." }
]
```

### 3-2. 에러 코드

| HTTP | 코드 | 설명 |
| --- | --- | --- |
| 400 | INVALID_INPUT | 요청값 검증 실패 |
| 400 | PASSWORD_MISMATCH | 비밀번호와 비밀번호 확인이 다름 |
| 400 | CURRENT_PASSWORD_MISMATCH | 현재 비밀번호가 틀림(비밀번호 변경) |
| 400 | CANNOT_MODIFY_SELF | 관리자가 본인의 역할·상태를 변경하려 함 |
| 400 | INVALID_DISPATCH_TIME | 도착시간이 출발시간보다 빠르거나 같음 |
| 400 | DISPATCH_CANCELLED | 취소된 배차를 수정하려 함 |
| 400 | INVALID_ANSWER_STATUS | 민원 답변 처리상태가 답변중·답변완료·이관안내가 아님 |
| 400 | FILE_TOO_MANY | 첨부파일 개수 초과(민원 1건당 5개) |
| 400 | FILE_INVALID_NAME | 첨부파일 이름이 올바르지 않음 |
| 401 | UNAUTHORIZED | 인증 정보 없음(토큰 없음·잘못됨, Refresh 토큰 만료) |
| 401 | TOKEN_EXPIRED | Access 토큰 만료(재발급 필요) |
| 401 | LOGIN_FAILED | 아이디 또는 비밀번호 불일치 |
| 403 | FORBIDDEN | 권한 없음 |
| 403 | ACCOUNT_SUSPENDED | 이용이 정지된 계정 |
| 403 | ACCOUNT_WITHDRAWN | 탈퇴한 계정 |
| 403 | COMPLAIN_NOT_OWNER | 본인이 작성하지 않은 민원 수정·삭제 시도 |
| 404 | RESOURCE_NOT_FOUND | 대상 없음 |
| 405 | METHOD_NOT_ALLOWED | 허용되지 않은 요청 방식 |
| 409 | DUPLICATE_LOGIN_ID | 이미 사용 중인 아이디 |
| 409 | DUPLICATE_EMAIL | 이미 가입된 이메일 |
| 409 | DUPLICATE_ACCOUNT | 같은 아이디·이메일로 동시에 가입 요청이 들어옴(다시 시도) |
| 409 | DUPLICATE_VEHICLE_NO | 이미 등록된 차량번호 |
| 409 | VEHICLE_IN_USE | 배차·운행 이력이 있는 차량 삭제 시도 |
| 409 | SCHEDULE_CONFLICT | 편성 시간 중복 배정 |
| 409 | TRAINSET_NOT_AVAILABLE | 정비·운행정지 편성 배차 시도 |
| 409 | COMPLAIN_NOT_MODIFIABLE | 접수대기가 아닌 민원 수정·삭제 시도 |
| 409 | COMPLAIN_WITHDRAWN | 민원인이 철회한 민원에 답변 등록 시도 |
| 413 | FILE_TOO_LARGE | 파일 크기 초과(파일당 10MB) |
| 415 | UNSUPPORTED_FILE_TYPE | 허용되지 않는 파일 형식 |
| 500 | INTERNAL_ERROR | 서버 오류 |

### 3-3. API 목록

URI 는 모두 Base URL(`/api/v1`) 뒤에 붙습니다. 예) `POST http://localhost:8081/api/v1/auth/login`

#### ① 인증 (Auth) — [main]

| Method | URI | 설명 | 권한 |
| --- | --- | --- | --- |
| POST | /auth/signup | 회원가입(name, loginId, email, password, passwordConfirm, over14) | 전체 |
| GET | /auth/check-id | 아이디 사용 가능 여부(loginId) | 전체 |
| POST | /auth/login | 로그인, 토큰 발급(loginId, password) | 전체 |
| POST | /auth/reissue | Access 토큰 재발급(쿠키의 Refresh 토큰 사용) | 전체 |
| POST | /auth/logout | 로그아웃, Refresh 토큰 삭제 | 회원 |
| GET | /auth/me | 로그인한 회원 요약 정보(사이드바 하단, 메뉴 표시용) | 회원 |

로그인 응답의 `data`:

```json
{
  "accessToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 1800,
  "member": {
    "id": 1,
    "loginId": "admin",
    "name": "관리자",
    "role": "ADMIN",
    "roleLabel": "관리자",
    "redirectPath": "/dashboard"
  }
}
```

- `redirectPath` 는 로그인 직후 이동할 화면입니다. 관리자는 `/dashboard`, 일반회원은 `/complaints` 입니다.
- 개발용 관리자 계정은 `admin` / `admin1234!` 입니다. `app.seed.enabled=true` 일 때 자동으로 생성됩니다.

#### ② 회원 (Member)

**[main]** 로그인한 회원 본인

| Method | URI | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | /members/me | 내 정보 조회 | 회원 |
| PATCH | /members/me | 내 정보 수정(name, email, phone, department, position) | 회원 |
| PUT | /members/me/profile-image | 프로필 이미지 변경(multipart, 파트 이름 file, 썸네일 생성) | 회원 |
| PATCH | /members/me/password | 비밀번호 변경(currentPassword, newPassword, newPasswordConfirm) | 회원 |
| GET | /members/options | 소속 부서·직급 선택 목록 | 회원 |
| GET | /files/profile/{fileName} | 프로필 이미지 파일 조회(이미지 그대로 응답) | 전체 |

- 프로필 이미지는 jpg, png, gif 만 가능하고 파일당 10MB 이하입니다. 썸네일은 160px 정사각형 jpg 로 만들어집니다.
- 회원 정보 응답의 `profileImageUrl`, `profileThumbnailUrl` 은 main 서버 기준 경로입니다. 예) `http://localhost:8081` + `/api/v1/files/profile/xxxx_thumb.jpg`
- 비밀번호를 바꾸면 발급돼 있던 Refresh 토큰이 모두 지워집니다. 다시 로그인해야 합니다.

**[dashboard]** 회원관리

| Method | URI | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | /admin/members | 회원 목록(keyword, role, status 필터, 페이징) | 관리자 |
| GET | /admin/members/options | 소속 부서·직급 선택 목록 | 관리자 |
| GET | /admin/members/{memberId} | 회원 상세 | 관리자 |
| PATCH | /admin/members/{memberId} | 회원 정보 수정 | 관리자 |
| PATCH | /admin/members/{memberId}/role | 역할 변경(role: ADMIN, USER) | 관리자 |
| PATCH | /admin/members/{memberId}/status | 계정 상태 변경(status: NORMAL, SUSPENDED, WITHDRAWN) | 관리자 |
| GET | /admin/me | 로그인한 관리자 본인 정보 | 관리자 |
| PATCH | /admin/me | 관리자 본인 정보 수정 | 관리자 |

- 목록의 연락처는 `010-****-1234` 처럼 가려서 내려갑니다. 상세에서는 전체가 보입니다.
- 목록에도 소속 부서(`department`)와 직급(`position`)이 내려갑니다. 값이 없으면 `null` 입니다.
- 목록 기본 정렬은 가입 순서(`id,asc`)입니다. 정렬 가능 필드: id, name, loginId, email, role, status, createdAt

#### ③ 차량 (Vehicle) — [dashboard]

| Method | URI | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | /admin/vehicles | 차량 목록(keyword, status 필터, 페이징) | 관리자 |
| GET | /admin/vehicles/summary | 요약 카드(전체·운행중·대기·정비·운행정지 수, 가동률) | 관리자 |
| GET | /admin/vehicles/{vehicleId} | 차량 상세 | 관리자 |
| POST | /admin/vehicles | 차량 등록(vehicleNo, status, lastInspectionDate) | 관리자 |
| PATCH | /admin/vehicles/{vehicleId} | 차량 정보 수정 | 관리자 |
| DELETE | /admin/vehicles/{vehicleId} | 차량 삭제(배차·운행 이력이 없을 때만) | 관리자 |

- 상태(status): RUNNING(운행중), STANDBY(대기), MAINTENANCE(정비), STOPPED(운행정지)
- 목록 기본 정렬은 차량번호(`vehicleNo,asc`)입니다. 정렬 가능 필드: id, vehicleNo, status, lastInspectionDate

#### ④ 배차 (Dispatch) — [dashboard]

| Method | URI | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | /admin/dispatches | 배차 목록(keyword, date, vehicleId, driver, status 필터, 페이징) | 관리자 |
| GET | /admin/dispatches/summary | 요약 카드(전체·완료·대기·변경·취소 건수). 목록과 같은 필터 사용(status 제외) | 관리자 |
| GET | /admin/dispatches/options | 차량·운전자 선택 목록 | 관리자 |
| GET | /admin/dispatches/{dispatchId} | 배차 상세 | 관리자 |
| POST | /admin/dispatches | 배차 등록(차량 상태·시간 중복 검증 후 저장) | 관리자 |
| PATCH | /admin/dispatches/{dispatchId} | 배차 수정(차량·날짜·시각이 바뀌면 status → CHANGED) | 관리자 |
| PATCH | /admin/dispatches/{dispatchId}/cancel | 배차 취소(status → CANCELLED) | 관리자 |

- 등록 본문: dispatchDate, vehicleId, driverName, departureTime, arrivalTime, remark
- 상태(status): COMPLETED(배차 완료), WAITING(배차 대기), CHANGED(배차 변경), CANCELLED(배차 취소)
- 같은 차량이 같은 날 겹치는 시간에 배차되면 `409 SCHEDULE_CONFLICT`, 정비·운행정지 차량이면 `409 TRAINSET_NOT_AVAILABLE` 입니다.
- 목록 기본 정렬은 `dispatchDate,desc` 다음 `dispatchNo,asc` 입니다. 정렬 가능 필드: id, dispatchNo, dispatchDate, driverName, departureTime, arrivalTime, status, createdAt

#### ⑤ 운행 (Operation) — [dashboard]

| Method | URI | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | /admin/operations | 노선도(노선 정보, 역 목록) + 운행 중인 열차 위치 | 관리자 |

#### ⑥ 대시보드 (Dashboard) — [dashboard]

| Method | URI | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | /admin/dashboard | 대시보드 전체(노선운영현황, 차량운행현황, 가동률, 민원건수, 답변건수) | 관리자 |
| GET | /admin/dashboard/periods | 기간 선택 목록 | 관리자 |

- 민원건수·답변건수 카드의 기간은 `complainPeriod`, `answerPeriod` 로 각각 정합니다. 기본값은 `THIS_MONTH` 입니다.
- 기간 값: TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, ALL

#### ⑦ 민원 (Complaint)

민원 API 는 아직 이 명세서의 공통 규칙으로 옮기지 않았습니다. 아래는 현재 동작 그대로입니다.

- 주소는 `/api/v1` 이 아니라 `/api` 로 시작합니다.
- 성공 응답은 `{ success, data, error }` 로 감싸지 않고 내용을 바로 내려줍니다.
- 목록의 `page` 는 1부터 시작합니다. (기본 `page=1&size=10`)
- 날짜·시각에는 `+09:00` 이 붙지 않습니다. (`2026-10-01T14:40:00`)
- 인증과 실패 응답은 다른 API 와 같습니다. (`Authorization: Bearer` 헤더, 3-1 의 실패 응답 형식)

**[main]** 일반 사용자

| Method | URI | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | /api/complaints | 민원 목록(keyword, category, type, status, page, size) | 회원 |
| GET | /api/complaints/options | 유형·분류·처리상태 선택 목록 | 회원 |
| GET | /api/complaints/{id} | 민원 상세(본인 민원만) | 회원 |
| POST | /api/complaints | 민원 등록(multipart: type, category, title, content, contentHtml, files) | 회원 |
| PUT | /api/complaints/{id} | 민원 수정(multipart, 접수대기만, deleteFileIds) | 회원 |
| DELETE | /api/complaints/{id} | 민원 삭제(접수대기만). 실제로는 철회 처리 — 아래 "민원 삭제(철회)" 참고 | 회원 |
| GET | /api/complaints/{id}/attachments/{attachmentId} | 첨부파일 다운로드 | 회원 |

**[dashboard]** 관리자

| Method | URI | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | /api/admin/complaints | 민원 목록(keyword, category, type, status, page, size) | 관리자 |
| GET | /api/admin/complaints/options | 유형·분류·처리상태 선택 목록 | 관리자 |
| GET | /api/admin/complaints/{id} | 민원 상세 | 관리자 |
| POST | /api/admin/complaints | 민원 등록(multipart) | 관리자 |
| PUT | /api/admin/complaints/{id}/answer | 답변 등록(status: RECEIVED, ANSWERED, TRANSFERRED / content) | 관리자 |
| GET | /api/admin/complaints/{id}/attachments/{attachmentId} | 첨부파일 다운로드 | 관리자 |

- 유형(type): SIMPLE(단순), SUGGESTION(건의), REPORT(제보), COMPLAINT(불만)
- 분류(category): OPERATION(운행관련), FACILITY(시설물), ROUTE_TIME(노선/시간), FARE_PAYMENT(요금/결제), ETC(기타)
- 처리상태(status): WAITING(접수대기), RECEIVED(답변중), ANSWERED(답변완료), TRANSFERRED(이관안내), WITHDRAWN(철회, 관리자 API 에서만 보임)
- 첨부파일은 jpg, png, gif, pdf 만 가능하고 파일당 10MB 이하, 민원 1건당 최대 5개입니다.

**민원 삭제(철회)**

일반 사용자가 본인 민원을 삭제하면(`DELETE /api/complaints/{id}`, 접수대기일 때만) DB 에서 지우지 않고 처리상태를 `WITHDRAWN`(철회)으로 바꿉니다.

- 사용자 화면: 삭제된 것과 같습니다. 응답은 그대로 204 이고, 이후 사용자 API(`/api/complaints` 목록·상세·수정·삭제·첨부 다운로드)에서는 그 민원이 나오지 않습니다(상세 등은 404). `GET /api/complaints/options` 의 `statuses` 에도 `WITHDRAWN` 은 없습니다.
- 관리자 화면: 철회된 민원이 내용·첨부파일과 함께 그대로 남습니다. 목록에서 `status=WITHDRAWN` 으로 모아 볼 수 있고(응답의 `totalElements` 가 철회 건수), 목록·상세 응답의 `withdrawnAt` 에 철회 시각이 들어갑니다(철회 전이면 `null`).
- 철회된 민원에는 답변을 등록할 수 없습니다(409 `COMPLAIN_WITHDRAWN`). 답변 등록의 `status` 로 `WITHDRAWN` 을 보낼 수도 없습니다(400 `INVALID_ANSWER_STATUS`).
- 대시보드 답변건수 카드(`answers.byStatus`)에 `WITHDRAWN`(철회) 항목이 추가되어 기간별 철회 건수가 내려갑니다. 민원건수·답변건수 카드의 `total` 은 철회 건을 포함한 접수 건수입니다.

**민원 분류(category) 등록**

일반 사용자가 민원 양식에서 고른 분류가 그대로 그 민원의 분류로 저장됩니다.

- 민원 양식의 분류 선택 목록은 `GET /api/complaints/options` 응답의 `categories` 를 씁니다. `code` 를 서버로 보내고 `label` 을 화면에 보여줍니다.
- 등록(`POST /api/complaints`)과 수정(`PUT /api/complaints/{id}`)에서 `category` 는 multipart 의 일반 필드로, 위 `code` 값을 보냅니다. 예) `category=FACILITY`
- `category` 는 선택 항목입니다. 보내지 않거나 비워 두면 `ETC`(기타)로 저장됩니다. (`type`, `title`, `content` 는 필수)
- `contentHtml` 은 선택 항목입니다. 에디터의 서식(굵게, 목록, 링크)이 들어간 HTML 을 보내면 서버가 허용 태그(b, strong, i, em, u, br, p, div, ul, ol, li, a[href])만 남겨 저장하고, 상세 응답의 `contentHtml` 로 내려줍니다. 보내지 않으면 `null` 이며 화면은 `content` 를 그대로 보여주면 됩니다. 수정할 때 보내지 않으면 기존 서식은 지워집니다.
- 저장된 분류는 목록·상세 응답에 `category`(코드)와 `categoryLabel`(화면 표시용 이름)로 내려가고, 목록의 `category` 필터로 검색할 수 있습니다.
- 관리자 민원 등록(`POST /api/admin/complaints`)도 같은 규칙입니다.

선택 목록 응답:

```json
{
  "types": [ { "code": "SIMPLE", "label": "단순" } ],
  "categories": [
    { "code": "OPERATION", "label": "운행관련" },
    { "code": "FACILITY", "label": "시설물" },
    { "code": "ROUTE_TIME", "label": "노선/시간" },
    { "code": "FARE_PAYMENT", "label": "요금/결제" },
    { "code": "ETC", "label": "기타" }
  ],
  "statuses": [ { "code": "WAITING", "label": "접수대기" } ]
}
```

등록·상세 응답에서 분류 부분:

```json
{
  "category": "FACILITY",
  "categoryLabel": "시설물"
}
```
