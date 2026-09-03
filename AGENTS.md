# AGENTS.md

## 프로젝트 개요

Gymrovia는 Spring Boot 기반 헬스장 운영 관리 웹 프로젝트입니다.

주요 기술 스택:

- Java 17
- Spring Boot 4.0.7
- Gradle
- Spring MVC
- Spring Security
- MyBatis
- Thymeleaf
- MySQL
- Lombok

프로젝트 구조와 기능 우선순위는 `PROJECT_STRUCTURE.md`를 먼저 참고합니다.

---

## 기본 작업 원칙

- 작업 전에 관련 파일과 기존 구현 방식을 먼저 확인합니다.
- 기존 패키지 구조와 네이밍 규칙을 유지합니다.
- 요청받은 범위만 수정합니다.
- 불필요한 전체 리팩토링은 하지 않습니다.
- 기존 기능을 삭제하거나 변경해야 할 경우 먼저 이유를 설명합니다.
- 한 번에 하나의 기능 단위로 구현합니다.
- 임시 데이터나 임시 하드코딩을 추가했다면 명확하게 표시합니다.

---

## 아키텍처 규칙

기본 처리 흐름은 다음을 따릅니다.

Controller → Service → Mapper → XML Mapper

각 계층의 역할:

- Controller
  - 요청과 응답 처리
  - 입력값 검증
  - 화면 이동과 Model 데이터 전달
  - 직접 SQL 또는 DB 접근 금지
  - 복잡한 비즈니스 로직 작성 금지

- Service
  - 비즈니스 로직 처리
  - 트랜잭션 관리
  - Mapper 호출
  - 여러 Mapper 작업을 조합할 경우 Service에서 처리

- Mapper
  - DB 접근 메서드 정의
  - SQL은 Mapper XML에 작성
  - 복잡한 비즈니스 로직 작성 금지

- XML Mapper
  - SQL 작성
  - 테이블명과 컬럼명을 실제 DB 스키마와 일치시킴
  - `SELECT *` 사용을 피하고 필요한 컬럼을 명시함

---

## 패키지 규칙

기능별 도메인 패키지 구조를 사용합니다.

필요한 경우 각 도메인 아래에 다음 패키지를 둡니다.

- controller
- service
- mapper
- model
- form
- dto.request
- dto.response

새 기능을 추가할 때 기존 기능과 동일한 패키지 구조를 우선 사용합니다.

공통 기능은 무조건 새로운 패키지를 만들지 말고 기존 공통 패키지가 있는지 먼저 확인합니다.

---

## Java 규칙

- Java 17 문법을 사용합니다.
- 클래스명은 PascalCase를 사용합니다.
- 변수명과 메서드명은 camelCase를 사용합니다.
- 상수는 UPPER_SNAKE_CASE를 사용합니다.
- 메서드는 한 가지 역할만 수행하도록 작성합니다.
- 의미 없는 축약어 사용을 피합니다.
- null 가능성이 있는 결과는 기존 프로젝트 방식에 따라 Optional 사용 여부를 결정합니다.
- Lombok은 기존 코드에서 사용하는 범위 안에서만 사용합니다.
- 무분별한 `@Data` 사용을 피합니다.

---

## DTO와 Form 규칙

- 사용자 입력은 Form 또는 Request DTO로 받습니다.
- 화면이나 API에 반환할 데이터는 Response DTO 사용을 우선합니다.
- Entity 또는 Model 객체를 화면에 직접 노출하지 않도록 주의합니다.
- 입력값 검증은 Bean Validation을 사용합니다.
- 비밀번호, 권한, 내부 식별자 등 민감한 필드는 응답 DTO에 포함하지 않습니다.

---

## MyBatis 규칙

- Mapper 인터페이스와 XML Mapper의 namespace를 일치시킵니다.
- XML의 id와 Mapper 메서드명을 일치시킵니다.
- 파라미터가 여러 개인 경우 `@Param` 사용을 고려합니다.
- 컬럼명과 Java 필드명이 다르면 resultMap을 사용합니다.
- 동적 SQL은 필요한 경우에만 사용합니다.
- SQL 수정 시 관련 DTO, Model, Mapper 메서드도 함께 확인합니다.
- INSERT, UPDATE, DELETE 결과값 처리 방식을 기존 코드와 통일합니다.

---

## Spring Security 규칙

- 인증 사용자 정보는 임시 사용자 ID로 하드코딩하지 않습니다.
- 로그인 사용자 정보는 SecurityContext 또는 기존 인증 객체를 사용합니다.
- 비밀번호는 평문으로 저장하지 않습니다.
- 권한 검사는 Controller에서 임의로 처리하지 말고 Security 설정 또는 Service에서 처리합니다.
- 관리자 기능은 일반 사용자가 접근할 수 없도록 확인합니다.
- 인증 및 인가 코드를 수정할 때 기존 로그인 흐름에 미치는 영향을 먼저 확인합니다.

---

## 데이터베이스 규칙

DB 접속 정보는 다음 환경 변수를 사용합니다.

- DB_URL
- DB_USERNAME
- DB_PASSWORD

규칙:

- DB 비밀번호를 소스코드에 직접 작성하지 않습니다.
- 실제 비밀번호가 들어간 `.env` 또는 설정 파일은 Git에 올리지 않습니다.
- 테이블이나 컬럼 변경 시 관련 SQL, Mapper XML, DTO, Model을 함께 수정합니다.
- 외래키와 삭제 정책을 확인한 후 DELETE 기능을 구현합니다.
- 날짜와 시간 타입은 기존 프로젝트 방식과 통일합니다.

---

## Thymeleaf 및 화면 규칙

- 화면 파일은 기존 템플릿 폴더 구조를 유지합니다.
- Controller에서 사용하는 Model 속성명과 HTML에서 사용하는 이름을 일치시킵니다.
- 반복되는 UI는 fragment 사용을 우선합니다.
- 공통 레이아웃과 CSS가 있는지 먼저 확인합니다.
- 인라인 스타일과 인라인 JavaScript 사용을 최소화합니다.
- 화면 수정 시 모바일 또는 작은 화면에서 레이아웃이 깨지지 않는지 확인합니다.

---

## 기능 우선순위

다음 순서대로 구현하는 것을 우선합니다.

P1:

1. 인증 및 권한
2. 회원
3. 회원권 및 결제
4. 출석
5. 관리자 대시보드

P2:

1. 트레이너 배정
2. 운동 관리
3. 시설 관리
4. 마이페이지

우선순위가 낮은 기능 때문에 P1 구조를 불필요하게 변경하지 않습니다.

---

## 테스트 및 실행

작업 후 가능한 경우 다음 명령을 실행합니다.

Windows:

```powershell
.\gradlew.bat test
```
