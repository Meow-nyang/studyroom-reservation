# 스터디룸 예약 서비스

스터디카페의 룸을 시간 단위로 예약하는 웹 서비스입니다.
**같은 방, 같은 시간대에 중복 예약이 생기지 않도록 하는 동시성 제어**를 핵심 주제로 다룹니다.

> 현재 0단계(환경 세팅)를 마쳤고, 1단계(도메인 & 조회)를 준비하고 있습니다.

## 기술 스택

| 구분 | 기술 |
|---|---|
| Backend | Java 17, Spring Boot 4.1, Spring Data JPA, Validation, Lombok, Gradle |
| Frontend | React 19, Vite 8, axios (JavaScript) |
| Database | MySQL 8.0 |

## 폴더 구조

```
.
├── backend/    # Spring Boot REST API (포트 8080)
└── frontend/   # React 웹 화면 (포트 5173)
```

프론트엔드는 `/api`로 시작하는 요청을 Vite 프록시로 백엔드에 전달합니다.

## 로컬 실행 방법

### 0. 필요한 것

- JDK 17 이상
- Node.js 20.19 이상
- MySQL 8.0

### 1. 데이터베이스 준비

MySQL에 root로 접속해 데이터베이스와 전용 계정을 만듭니다.

```sql
CREATE DATABASE studyroom DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'studyroom'@'localhost' IDENTIFIED BY '원하는_비밀번호';
GRANT ALL PRIVILEGES ON studyroom.* TO 'studyroom'@'localhost';
```

### 2. 백엔드 실행

```bash
cd backend
cp .env.example .env    # .env를 열어 DB_PASSWORD에 위 비밀번호 입력
./gradlew bootRun
```

`.env`는 Git에 올라가지 않습니다. 같은 이름의 환경 변수(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)를 설정해도 됩니다.

확인: `curl http://localhost:8080/api/health` → `{"status":"OK"}`

### 3. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

http://localhost:5173 에서 "서버 상태: OK"가 보이면 정상입니다.

## 개발 로드맵

| 단계 | 내용 | 상태 |
|---|---|---|
| 0. 환경 세팅 | 프로젝트 생성, MySQL 연결, 서버 상태 확인 | ✅ |
| 1. 도메인 & 조회 | ERD, 지점/방 엔티티와 조회 API, 목록/상세 화면 | ⏳ |
| 2. 회원 & 인증 | Spring Security + JWT, 회원가입/로그인 | |
| 3. 예약 | 예약 생성/조회/취소, 시간 겹침 검사 | |
| 4. 동시성 ★ | 중복 예약 재현 → 비관적 락 / 낙관적 락 / 유니크 제약 / Redis 락 비교 | |
| 5. 결제 & 자동화 | 토스페이먼츠 테스트 결제, 멱등성, 미결제 자동 취소 | |
| 6. 관리자 | 지점/방 등록, 예약 현황 | |
| 7. 배포 | Docker, GitHub Actions, AWS(EC2 + RDS), API 문서 | |
