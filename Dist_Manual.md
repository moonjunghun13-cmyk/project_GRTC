# GRTC 배차관리 시스템 배포 매뉴얼

## 1. 문서 개요

| 항목 | 내용 |
|---|---|
| 시스템 | 광주교통공사 배차관리 시스템 |
| 배포 방식 | Docker 단일 컨테이너(1-Tier) |
| 이미지 저장소 | Docker Hub |
| 실행 환경 | Windows + Docker Desktop |
| 웹 접속 | `http://localhost/` |

> **중요:** 아래 명령어의 `moonjunghun13cmyk`은 실제 Docker Hub 사용자 이름으로 바꿔 실행합니다. 이미지 태그와 환경변수는 실제 업로드한 이미지 설정에 맞춰 확인합니다.

## 2. 구성 개요

배포 이미지는 다음 구성요소를 포함합니다.

- **Nginx**: 프론트엔드 정적 파일 제공 및 `/api/` 요청 프록시
- **Spring Boot**: 백엔드 API
- **PostgreSQL**: 데이터베이스
- **더미 데이터**: 초기 실행 시 SQL 스크립트로 생성

컨테이너 내부 포트는 Nginx `80`, Spring Boot `8080`, PostgreSQL `5432`입니다. 일반적인 웹 사용에는 Nginx 포트만 호스트에 공개하면 됩니다.

## 3. 사전 준비

1. Windows에 Docker Desktop을 설치합니다.
2. Docker Desktop을 실행하고 엔진이 정상적으로 시작될 때까지 기다립니다.
3. CMD 또는 PowerShell을 엽니다.

설치 및 엔진 상태를 확인합니다.

```bash
docker --version
docker info
```

`docker info`에서 서버 정보가 출력되어야 합니다. 연결 오류가 발생하면 Docker Desktop이 실행 중인지 확인합니다.

## 4. Docker Hub에서 이미지 내려받기

이미지가 **Public** 저장소에 있다면 Docker Hub 로그인 없이 내려받을 수 있습니다.

```bash
docker pull moonjunghun13cmyk/grtc-1tier:1.0.0
```

이미지가 **Private** 저장소라면 접근 권한이 있는 계정으로 먼저 로그인합니다.

```bash
docker login
```

그다음 `docker pull` 명령을 다시 실행합니다.

다운로드된 이미지를 확인합니다.

```bash
docker images
```

목록에서 `moonjunghun13cmyk/grtc-1tier`와 `1.0.0` 태그가 표시되는지 확인합니다.

## 5. 컨테이너 실행

### 5.1 기본 실행

```bash
docker run -d --name grtc-demo -p 80:80 -e DB_NAME=project1 -e DB_PASSWORD=1004 moonjunghun13cmyk/grtc-1tier:1.0.0
```

- `-d`: 백그라운드 실행
- `--name grtc-demo`: 컨테이너 이름 지정
- `-p 80:80`: 호스트 80번 포트를 컨테이너 Nginx 80번 포트에 연결
- `DB_NAME`: 컨테이너 내부 PostgreSQL 데이터베이스 이름
- `DB_PASSWORD`: 컨테이너 내부 PostgreSQL의 `postgres` 계정 비밀번호 설정값

> `DB_PASSWORD=1004`는 현재 시연 환경의 예시값입니다. 이미지의 실제 환경변수 처리 방식과 일치하는지 확인하고, 운영 환경에서는 비밀번호와 JWT 비밀키 등 민감한 값을 안전하게 관리해야 합니다.

### 5.2 호스트의 80번 포트를 사용할 수 없는 경우

호스트 80번 포트가 이미 사용 중이면 다른 포트로 연결합니다. 예를 들어 호스트 8081번 포트를 사용하려면 다음과 같이 실행합니다.

```bash
docker run -d --name grtc-demo -p 8081:80 -e DB_NAME=project1 -e DB_PASSWORD=1004 moonjunghun13cmyk/grtc-1tier:1.0.0
```

이 경우 브라우저에서 `http://localhost:8081/`로 접속합니다.

> 동일한 이름의 `grtc-demo` 컨테이너가 이미 있으면 컨테이너 이름을 바꾸거나 기존 컨테이너를 중지·삭제한 뒤 실행해야 합니다.

## 6. 접속 및 상태 확인

브라우저에서 다음 주소로 접속합니다.

- 기본 포트: `http://localhost/`
- 대체 포트 예시: `http://localhost:8081/`

컨테이너 실행 상태를 확인합니다.

```bash
docker ps
```

컨테이너 로그를 확인합니다.

```bash
docker logs -f grtc-demo
```

로그 출력을 종료하려면 `Ctrl + C`를 누릅니다. 이 동작은 로그 확인만 종료하며 컨테이너는 계속 실행됩니다.

## 7. 컨테이너 제어

컨테이너 중지:

```bash
docker stop grtc-demo
```

중지한 컨테이너 다시 실행:

```bash
docker start grtc-demo
```

컨테이너 삭제:

```bash
docker rm -f grtc-demo
```

> **데이터 주의:** 현재 구성이 PostgreSQL 데이터를 별도의 Docker volume에 저장하지 않는다면 컨테이너를 삭제할 때 데이터가 함께 사라질 수 있습니다. 시연 데이터를 유지해야 한다면 삭제 전에 필요한 데이터를 백업하거나, 별도 volume 구성을 마련합니다.

## 8. 이미지 업데이트

배포 담당자가 새 이미지 버전을 Docker Hub에 올리면 팀원은 새 태그를 지정해 내려받고 컨테이너를 다시 생성합니다.

예를 들어 새 버전이 `1.0.1`인 경우:

```bash
docker pull moonjunghun13cmyk/grtc-1tier:1.0.1
docker rm -f grtc-demo
docker run -d --name grtc-demo -p 80:80 -e DB_NAME=project1 -e DB_PASSWORD=1004 moonjunghun13cmyk/grtc-1tier:1.0.1
```

> 이미 실행 중인 컨테이너는 `docker pull`만으로 새 이미지 버전으로 교체되지 않습니다. 새 이미지로 컨테이너를 다시 생성해야 합니다. 기존 컨테이너에 보존해야 할 데이터가 있다면 삭제 전에 백업 여부를 확인합니다.

## 9. 문제 해결

### Docker 엔진에 연결할 수 없음

- Docker Desktop이 실행 중인지 확인합니다.
- Docker Desktop의 엔진 시작이 완료되었는지 확인합니다.
- 다음 명령으로 서버 정보가 출력되는지 확인합니다.

```bash
docker info
```

### 포트가 이미 사용 중임

`-p 80:80`에서 오류가 발생하면 호스트 포트를 변경합니다. 예를 들어 `-p 8081:80`으로 실행하고 `http://localhost:8081/`에 접속합니다.

### 컨테이너가 실행되지 않거나 웹 화면이 열리지 않음

```bash
docker ps -a
docker logs grtc-demo
```

로그에서 PostgreSQL 시작 오류, Spring Boot 오류, 환경변수 누락 등을 확인합니다.

### 이미지 pull 권한 오류

- 저장소가 Public인지 확인합니다.
- Private 저장소라면 Docker Hub에 로그인했는지, 계정에 pull 권한이 있는지 확인합니다.
- 저장소 이름과 태그가 정확한지 확인합니다.

## 10. 팀원 배포 체크리스트

- [ ] Docker Desktop이 실행 중이다.
- [ ] Docker Hub에서 지정된 이미지 태그를 pull했다.
- [ ] 컨테이너 실행 명령이 오류 없이 완료되었다.
- [ ] `docker ps`에서 `grtc-demo`가 실행 중이다.
- [ ] 브라우저에서 웹 화면이 열린다.
- [ ] 로그인, 주요 화면, 민원 더미 데이터 조회를 확인했다.

## 11. 배포 정보 기입란

배포 담당자는 팀에 공유하기 전에 아래 항목을 실제 값으로 갱신합니다.

- Docker Hub 저장소: `moonjunghun13cmyk/grtc-1tier`
- 배포 태그: `1.0.0`
- 웹 접속 주소: `http://localhost/`
- 기본 DB 이름: `project1`
- 기본 호스트 포트: `80`
