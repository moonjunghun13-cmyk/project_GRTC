# 광주교통공사 배차관리 · 민원관리 웹 업무 시스템 (팀 떡갈비)

Vue 3 (Vite) + Spring Boot 4 (Java 21) + PostgreSQL

## 폴더 구조

```
project_GRTC
├── backend/main   Spring Boot 통합 서버 (포트 8081) — 로그인·회원·민원 + 관리자(대시보드·차량·배차·운행·회원관리)
├── frontend       Vue 3 + Vite (포트 5173)
└── docs           백엔드 API 명세 · 연동 계획
```

> 예전에는 백엔드가 main(8081) · dashboard(8082) 두 서버였지만 하나로 합쳤습니다.
> 관리자 코드는 `com.grtc.main.admin` 패키지에 있습니다.

## 실행 방법

### 1. DB 준비 (처음 한 번)

PostgreSQL에 `project` 데이터베이스를 만듭니다. 접속 정보는 `backend/main/src/main/resources/application.yaml` 기준입니다. (사용자 `postgres`, 비밀번호 기본값 `1004`)

postgres 비밀번호가 `1004`가 아니면 PowerShell에서 한 번만 아래를 실행하고 IntelliJ를 다시 켭니다.

```powershell
setx DB_PASSWORD 내비밀번호
```

```sql
CREATE DATABASE project;
```

첨부파일 저장 폴더 `C:/mmg/uploads` 를 만듭니다.

### 2. 백엔드 실행

IntelliJ로 `backend/main` 폴더를 열고 `MainApplication` 을 실행합니다.
또는 터미널에서:

```bash
cd backend/main
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

처음 실행하면 관리자 계정(`admin` / `admin1234!`)과 노선·역·차량·배차 예시 데이터가 자동으로 만들어집니다.

### 3. 프론트 실행

```bash
cd frontend
copy .env.example .env   # macOS/Linux: cp .env.example .env
npm install
npm run dev
```

브라우저에서 http://localhost:5173 을 엽니다.
실제 서버와 연결하려면 `.env` 의 `VITE_ENABLE_API` 를 `true` 로 바꿉니다. (프론트 담당자와 연동 코드 준비 여부를 먼저 확인)

## Branch 규칙

`main` 에 직접 Push 하지 않고, `feature/*` · `fix/*` 브랜치에서 Pull Request 로 Merge 합니다.
