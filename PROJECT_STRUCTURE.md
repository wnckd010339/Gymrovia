# Gymrovia 구조

Spring Boot, MyBatis, Thymeleaf 기반 헬스장 운영 관리 프로젝트입니다.

## 패키지 원칙

기능별 도메인 아래에 `controller`, `service`, `mapper`, `model`, `form`,
`dto/request`, `dto/response`, `view` 계층을 배치합니다.

처리 흐름은 `Controller → Service → Mapper → XML Mapper`를 따릅니다.

## 우선순위

- P1: 인증·권한, 회원, 회원권·결제, 출석, 관리자 대시보드
- P2: 트레이너 배정, 운동, 시설, 마이페이지

## 실행

```powershell
.\gradlew.bat bootRun
```

DB 접속 정보는 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경 변수로 주입합니다.
