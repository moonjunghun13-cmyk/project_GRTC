# 광주교통공사 최신 백엔드 API 명세

기준 갱신일: 2026-10-02. 사용자가 제공한 아래 명세가 최신 기준이며, 이전 SESSION 명세와 충돌하는 내용은 폐기한다. 아래 최초 제공 명세에 후속 보완을 통합한다. 충돌 시 아래 보완 규칙을 우선한다.

**현재는 프론트 화면 제작 단계다. 실제 API 호출·JWT 발급·재발급은 최종 연동 요청 전까지 구현하거나 활성화하지 않는다.** 기존 mock 데이터와 화면 미리보기를 유지한다. `VITE_ENABLE_API=false`를 유지하며, 기존 비활성 연동 코드는 최신 계약에 맞게 수정되기 전에는 활성화하면 안 된다.

이전 기준과의 차이, 화면과의 불일치, 향후 작업은 [backend-integration-plan.md](backend-integration-plan.md)를 참고한다. 그 문서는 비교 메모이며 아래 원문을 대체하지 않는다.

---

## 사용자 제공 명세 (후속 보완 반영)

API 명세서
광주교통공사 백엔드 API 명세서입니다. 이 문서가 기준이며, 코드는 이 문서에 맞춰져 있습니다.
3-0. 서버 구성
백엔드는 두 개의 서버로 나뉩니다. 두 서버는 같은 PostgreSQL DB(`project`)를 씁니다.
서버	폴더	주소	담당
main	`main`	`http://localhost:8081`	공통(회원가입, 로그인, 토큰) + 일반 사용자 화면(민원관리, 내 정보)
dashboard	`dashboard`	`http://localhost:8082`	관리자 화면(대시보드, 차량관리, 배차관리, 운행관리, 민원관리, 회원관리)
로그인은 main 서버에서만 합니다. 로그인으로 받은 Access 토큰 하나로 두 서버를 모두 호출합니다.
두 서버의 `application.yaml` 에 있는 `app.jwt.secret` 값은 반드시 같아야 합니다.
아래 3-3 API 목록에서 [main] 은 8081, [dashboard] 는 8082 서버의 API 입니다.
3-1. 공통 규칙
Base URL: `/api/v1`
인증: `Authorization: Bearer {accessToken}` 헤더. Access 토큰은 30분, Refresh 토큰은 14일이며 Refresh 토큰은 HttpOnly 쿠키(`refreshToken`)로 전달합니다.
Refresh 쿠키가 오가려면 프론트(axios)는 `withCredentials: true` 로 호출해야 합니다.
Access 토큰이 만료되면 `401 TOKEN_EXPIRED` 가 내려옵니다. 이때 `POST /auth/reissue` 로 새 토큰을 받아 다시 요청합니다.
Content-Type: `application/json` (파일 업로드는 `multipart/form-data`)
날짜 형식: ISO-8601 (`2026-10-15`, `2026-10-15T05:30:00+09:00`, 시각만 있는 값은 `05:30:00`)
페이징: `?page=0&size=20&sort=createdAt,desc` (page 는 0부터, size 기본 20·최대 100)
`sort` 는 `필드,방향` 형식입니다. 방향을 빼면 `asc` 이고, 보내지 않으면 API 마다 정해진 기본 정렬을 씁니다.
정렬할 수 없는 필드를 보내면 `400 INVALID_INPUT` 입니다.
수정(PATCH): 보낸 항목만 바뀝니다. 보내지 않은 항목은 그대로 둡니다.
권한 표기: 전체(비로그인 포함), 회원(로그인한 모든 사용자), 관리자(ADMIN)
회원 역할은 관리자(`ADMIN`)와 일반회원(`USER`) 두 가지입니다.
일반회원이 관리자 API 를 호출하면 `403 FORBIDDEN` 이 내려옵니다. (프론트는 "관리자 페이지이므로 열람이 불가합니다" 화면 표시)
성공 응답
```json
{
  "success": true,
  "data": { },
  "error": null
}
```
페이징 응답의 data
```json
{
  "content": [ ],
  "page": 0,
  "size": 20,
  "totalElements": 135,
  "totalPages": 7
}
```
실패 응답
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
3-2. 에러 코드
HTTP	코드	설명
400	INVALID_INPUT	요청값 검증 실패
400	PASSWORD_MISMATCH	비밀번호와 비밀번호 확인이 다름
400	CURRENT_PASSWORD_MISMATCH	현재 비밀번호가 틀림(비밀번호 변경)
400	CANNOT_MODIFY_SELF	관리자가 본인의 역할·상태를 변경하려 함
400	INVALID_DISPATCH_TIME	도착시간이 출발시간보다 빠르거나 같음
400	DISPATCH_CANCELLED	취소된 배차를 수정하려 함
400	INVALID_ANSWER_STATUS	민원 답변 처리상태가 접수·이관·답변완료가 아님
400	FILE_TOO_MANY	첨부파일 개수 초과(민원 1건당 5개)
400	FILE_INVALID_NAME	첨부파일 이름이 올바르지 않음
401	UNAUTHORIZED	인증 정보 없음(토큰 없음·잘못됨, Refresh 토큰 만료)
401	TOKEN_EXPIRED	Access 토큰 만료(재발급 필요)
401	LOGIN_FAILED	아이디 또는 비밀번호 불일치
403	FORBIDDEN	권한 없음
403	ACCOUNT_SUSPENDED	이용이 정지된 계정
403	ACCOUNT_WITHDRAWN	탈퇴한 계정
403	COMPLAIN_NOT_OWNER	본인이 작성하지 않은 민원 수정·삭제 시도
404	RESOURCE_NOT_FOUND	대상 없음
405	METHOD_NOT_ALLOWED	허용되지 않은 요청 방식
409	DUPLICATE_LOGIN_ID	이미 사용 중인 아이디
409	DUPLICATE_EMAIL	이미 가입된 이메일
409	DUPLICATE_VEHICLE_NO	이미 등록된 차량번호
409	VEHICLE_IN_USE	배차·운행 이력이 있는 차량 삭제 시도
409	SCHEDULE_CONFLICT	편성 시간 중복 배정
409	TRAINSET_NOT_AVAILABLE	정비·운행정지 편성 배차 시도
409	COMPLAIN_NOT_MODIFIABLE	접수대기가 아닌 민원 수정·삭제 시도
413	FILE_TOO_LARGE	파일 크기 초과(파일당 10MB)
415	UNSUPPORTED_FILE_TYPE	허용되지 않는 파일 형식
500	INTERNAL_ERROR	서버 오류
3-3. API 목록
URI 는 모두 Base URL(`/api/v1`) 뒤에 붙습니다. 예) `POST http://localhost:8081/api/v1/auth/login`
① 인증 (Auth) — [main]
Method	URI	설명	권한
POST	/auth/signup	회원가입(name, loginId, password, passwordConfirm, over14)	전체
GET	/auth/check-id	아이디 사용 가능 여부(loginId)	전체
POST	/auth/login	로그인, 토큰 발급(loginId, password)	전체
POST	/auth/reissue	Access 토큰 재발급(쿠키의 Refresh 토큰 사용)	전체
POST	/auth/logout	로그아웃, Refresh 토큰 삭제	회원
GET	/auth/me	로그인한 회원 요약 정보(사이드바 하단, 메뉴 표시용)	회원
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
`redirectPath` 는 로그인 직후 이동할 화면입니다. 관리자는 `/dashboard`, 일반회원은 `/complaints` 입니다.
개발용 관리자 계정은 `admin` / `admin1234!` 입니다. `app.seed.enabled=true` 일 때 자동으로 생성됩니다.
② 회원 (Member)
[main] 로그인한 회원 본인
Method	URI	설명	권한
GET	/members/me	내 정보 조회	회원
PATCH	/members/me	내 정보 수정(name, email, phone, department, position)	회원
PUT	/members/me/profile-image	프로필 이미지 변경(multipart, 파트 이름 file, 썸네일 생성)	회원
PATCH	/members/me/password	비밀번호 변경(currentPassword, newPassword, newPasswordConfirm)	회원
GET	/members/options	소속 부서·직급 선택 목록	회원
GET	/files/profile/{fileName}	프로필 이미지 파일 조회(이미지 그대로 응답)	전체
프로필 이미지는 jpg, png, gif 만 가능하고 파일당 10MB 이하입니다. 썸네일은 160px 정사각형 jpg 로 만들어집니다.
회원 정보 응답의 `profileImageUrl`, `profileThumbnailUrl` 은 main 서버 기준 경로입니다. 예) `http://localhost:8081` + `/api/v1/files/profile/xxxx_thumb.jpg`
비밀번호를 바꾸면 발급돼 있던 Refresh 토큰이 모두 지워집니다. 다시 로그인해야 합니다.
[dashboard] 회원관리
Method	URI	설명	권한
GET	/admin/members	회원 목록(keyword, role, status 필터, 페이징)	관리자
GET	/admin/members/options	소속 부서·직급 선택 목록	관리자
GET	/admin/members/{memberId}	회원 상세	관리자
PATCH	/admin/members/{memberId}	회원 정보 수정	관리자
PATCH	/admin/members/{memberId}/role	역할 변경(role: ADMIN, USER)	관리자
PATCH	/admin/members/{memberId}/status	계정 상태 변경(status: NORMAL, SUSPENDED, WITHDRAWN)	관리자
GET	/admin/me	로그인한 관리자 본인 정보	관리자
PATCH	/admin/me	관리자 본인 정보 수정	관리자
목록의 연락처는 `010-****-1234` 처럼 가려서 내려갑니다. 상세에서는 전체가 보입니다.
목록 기본 정렬은 가입 순서(`id,asc`)입니다. 정렬 가능 필드: id, name, loginId, email, role, status, createdAt
③ 차량 (Vehicle) — [dashboard]
Method	URI	설명	권한
GET	/admin/vehicles	차량 목록(keyword, status 필터, 페이징)	관리자
GET	/admin/vehicles/summary	요약 카드(전체·운행중·대기·정비·운행정지 수, 가동률)	관리자
GET	/admin/vehicles/{vehicleId}	차량 상세	관리자
POST	/admin/vehicles	차량 등록(vehicleNo, status, lastInspectionDate)	관리자
PATCH	/admin/vehicles/{vehicleId}	차량 정보 수정	관리자
DELETE	/admin/vehicles/{vehicleId}	차량 삭제(배차·운행 이력이 없을 때만)	관리자
상태(status): RUNNING(운행중), STANDBY(대기), MAINTENANCE(정비), STOPPED(운행정지)
목록 기본 정렬은 차량번호(`vehicleNo,asc`)입니다. 정렬 가능 필드: id, vehicleNo, status, lastInspectionDate
④ 배차 (Dispatch) — [dashboard]
Method	URI	설명	권한
GET	/admin/dispatches	배차 목록(keyword, date, vehicleId, driver, status 필터, 페이징)	관리자
GET	/admin/dispatches/summary	요약 카드(전체·완료·대기·변경·취소 건수). 목록과 같은 필터 사용(status 제외)	관리자
GET	/admin/dispatches/options	차량·운전자 선택 목록	관리자
GET	/admin/dispatches/{dispatchId}	배차 상세	관리자
POST	/admin/dispatches	배차 등록(차량 상태·시간 중복 검증 후 저장)	관리자
PATCH	/admin/dispatches/{dispatchId}	배차 수정(차량·날짜·시각이 바뀌면 status → CHANGED)	관리자
PATCH	/admin/dispatches/{dispatchId}/cancel	배차 취소(status → CANCELLED)	관리자
등록 본문: dispatchDate, vehicleId, driverName, departureTime, arrivalTime, remark
상태(status): COMPLETED(배차 완료), WAITING(배차 대기), CHANGED(배차 변경), CANCELLED(배차 취소)
같은 차량이 같은 날 겹치는 시간에 배차되면 `409 SCHEDULE_CONFLICT`, 정비·운행정지 차량이면 `409 TRAINSET_NOT_AVAILABLE` 입니다.
목록 기본 정렬은 `dispatchDate,desc` 다음 `dispatchNo,asc` 입니다. 정렬 가능 필드: id, dispatchNo, dispatchDate, driverName, departureTime, arrivalTime, status, createdAt
⑤ 운행 (Operation) — [dashboard]
Method	URI	설명	권한
GET	/admin/operations	노선도(노선 정보, 역 목록) + 운행 중인 열차 위치	관리자
⑥ 대시보드 (Dashboard) — [dashboard]
Method	URI	설명	권한
GET	/admin/dashboard	대시보드 전체(노선운영현황, 차량운행현황, 가동률, 민원건수, 답변건수)	관리자
GET	/admin/dashboard/periods	기간 선택 목록	관리자
민원건수·답변건수 카드의 기간은 `complainPeriod`, `answerPeriod` 로 각각 정합니다. 기본값은 `THIS_MONTH` 입니다.
기간 값: TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, ALL
⑦ 민원 (Complaint)
민원 API 는 아직 이 명세서의 공통 규칙으로 옮기지 않았습니다. 아래는 현재 동작 그대로입니다.
주소는 `/api/v1` 이 아니라 `/api` 로 시작합니다.
성공 응답은 `{ success, data, error }` 로 감싸지 않고 내용을 바로 내려줍니다.
목록의 `page` 는 1부터 시작합니다. (기본 `page=1&size=10`)
날짜·시각에는 `+09:00` 이 붙지 않습니다. (`2026-10-01T14:40:00`)
인증과 실패 응답은 다른 API 와 같습니다. (`Authorization: Bearer` 헤더, 3-1 의 실패 응답 형식)
[main] 일반 사용자
Method	URI	설명	권한
GET	/api/complaints	민원 목록(keyword, category, type, status, page, size)	회원
GET	/api/complaints/options	유형·분류·처리상태 선택 목록	회원
GET	/api/complaints/{id}	민원 상세(본인 민원만)	회원
POST	/api/complaints	민원 등록(multipart: type, category, title, content, files)	회원
PUT	/api/complaints/{id}	민원 수정(multipart, 접수대기만, deleteFileIds)	회원
DELETE	/api/complaints/{id}	민원 삭제(접수대기만)	회원
GET	/api/complaints/{id}/attachments/{attachmentId}	첨부파일 다운로드	회원
[dashboard] 관리자
Method	URI	설명	권한
GET	/api/admin/complaints	민원 목록(keyword, category, type, status, page, size)	관리자
GET	/api/admin/complaints/options	유형·분류·처리상태 선택 목록	관리자
GET	/api/admin/complaints/{id}	민원 상세	관리자
POST	/api/admin/complaints	민원 등록(multipart)	관리자
PUT	/api/admin/complaints/{id}/answer	답변 등록(status: RECEIVED, TRANSFERRED, ANSWERED / content)	관리자
GET	/api/admin/complaints/{id}/attachments/{attachmentId}	첨부파일 다운로드	관리자
유형(type): SIMPLE(단순), SUGGESTION(건의), REPORT(제보), COMPLAINT(불만)
분류(category): OPERATION(운행관련), FACILITY(시설물), ROUTE_TIME(노선/시간), FARE_PAYMENT(요금/결제), ETC(기타)
처리상태(status) 표시 순서: WAITING(접수대기), RECEIVED(답변중), ANSWERED(답변완료), TRANSFERRED(이관안내)
첨부파일은 jpg, png, gif, pdf 만 가능하고 파일당 10MB 이하, 민원 1건당 최대 5개입니다.

## 2026-10-02 후속 보완: 민원 분류 및 상태

이 절은 이전 분류 미확정 설명과 상태 표시 설명보다 우선한다. 실제 API 연결은 계속 보류한다.

### 분류(category)

- 유형(type)과 별도 항목이다. mock 코드는 OPERATION / FACILITY / ROUTE_TIME / FARE_PAYMENT / ETC이며 표시명은 운행관련 / 시설물 / 노선·시간 / 요금·결제 / 기타다.
- main GET /api/complaints/options의 categories가 분류 선택 목록이다. options 내부 항목의 구체적 JSON 구조는 예시 확인 전 확정하지 않는다.
- main POST /api/complaints, PUT /api/complaints/{id} 및 dashboard POST /api/admin/complaints는 multipart 일반 필드 category에 선택한 코드를 전달한다. 파일 파트나 숨긴 임의 기본값으로 처리하지 않는다.
- main GET /api/complaints의 category 쿼리로 필터링한다. 작성·수정 선택과 목록 분류 필터는 동일 코드 사용.

### 상태 표시와 답변

- main GET /api/complaints/options 및 dashboard GET /api/admin/complaints/options의 statuses 배열 순서와 label을 그대로 사용한다. 현재 mock 순서는 WAITING 접수대기 → RECEIVED 답변중 → ANSWERED 답변완료 → TRANSFERRED 이관안내다.
- 양쪽 목록·상세 응답의 statusLabel이 표시 기준이다. 색상·처리 로직은 status 코드 기준이다. 서버 label을 기존 접수/이관 명칭으로 바꾸지 않는다.
- 관리자 PUT /api/admin/complaints/{id}/answer에 허용되는 status는 RECEIVED / ANSWERED / TRANSFERRED뿐이다. WAITING은 목록·필터에 포함하고 답변 선택에서는 제외한다.
- 대시보드 GET /api/v1/admin/dashboard의 답변건수 byStatus는 응답 label과 배열 순서를 유지한다. 프론트에서 재정렬하거나 다른 명칭으로 대체하지 않는다. 실제 byStatus의 수치 필드 구조는 예시 확인 전 확정하지 않는다. 데이터 미연결 상태의 수치는 null/— 유지.
- 민원은 /api, 원본 성공 응답, 1 기반 page를 유지한다. 대시보드는 /api/v1의 일반 규칙을 유지한다.
