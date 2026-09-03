# Gymrovia 이름 변경 및 배포 체크리스트

## 로컬 변경

- 화면의 브랜드 문구, 페이지 제목, 접근성 라벨: `Gymrovia`
- 브랜드 이니셜: `G`
- 한글 표시명: `짐로비아`
- Gradle 프로젝트 이름: `Gymrovia`
- Spring 애플리케이션 이름: `gymrovia`
- 오류 컨트롤러: `GymroviaErrorController`
- 오류 페이지 CSS 클래스: `gymrovia-error-page`
- README, API·인증·DB 문서, SVG 화면 설계와 생성 스크립트 반영

## 호환성을 위해 유지한 값

표시 이름 변경은 데이터 마이그레이션과 별개다. 아래 값을 일괄 치환하지 않는다.

- 기존 DB 스키마, DB 사용자, 로컬 `.env` 및 서버 환경변수의 접속 정보
- 기존 주문 ID와 주문 ID 접두사 `FITFLOW-`
- QR 센터 식별자 `FITFLOW_MAIN` 및 이를 검증하는 테스트
- 기존 샘플 계정 이메일·OAuth 식별자: SQL을 수정하거나 재실행해 실제 계정을 변경하지 않는다.
- 적용 이력이 있는 SQL 마이그레이션: 주석만 바꿔도 Flyway 체크섬이 달라질 수 있다.
- Java 패키지 `com.acorn.gymmanagement`와 메인 클래스 `GymManagementApplication`
- AWS 리소스·Docker 이름

`ATTENDANCE_QR_CENTER_NAME`을 환경변수로 지정했다면 기본 표시명보다 우선한다.
배포 시 해당 값만 `짐로비아 강남센터` 등으로 변경하고 센터 코드는 유지한다.

## GitHub — 저장소 이름 및 로컬 remote 변경 완료

2026-09-03 저장소를 `wnckd010339/Gymrovia`로 변경하고 로컬 `origin`도 새 URL로 갱신했다.
로컬 코드의 커밋·푸시는 별도이며, 아래는 이름 변경 절차 기록이다.

1. 저장소 관리 권한이 있는 계정으로 로그인한다.
2. 기존 저장소의 Settings → General → Repository name에서 `Gymrovia`로 변경한다.
3. 이름 변경 성공을 확인한 후 로컬에서 실행한다.

```powershell
git remote set-url origin https://github.com/wnckd010339/Gymrovia.git
git remote -v
```

4. About 설명에 Gymrovia 이름을 반영한다. Website는 실제 DNS·HTTPS 연결 검증 후 등록한다.
5. 로컬 변경을 검토하고 커밋·푸시한다. 저장소명 변경만으로 로컬 코드가 업로드되지는 않는다.

## 화면 이미지 및 배포 — 별도 수행 필요

- README의 PNG는 이름 변경 전 실행 화면이다. 실제 변경된 앱에서 로그인·관리자·트레이너·회원 화면을 재촬영하고 링크도 함께 교체한다.
- Notion 등 외부 설계 문서의 제목·표기와 기존 저장소 링크를 변경한다.
- 새 Docker 이미지를 빌드해 새 버전 태그로 게시한 후 EC2 컨테이너를 교체한다.
- 기존 실행 이미지 `joochang/fitflow:v2`는 로컬 소스 변경만으로 갱신되지 않는다.
- 도메인 연결과 HTTPS 준비가 끝난 뒤 Google OAuth 리디렉션, Toss 성공·실패 URL, QR 공개 URL을 갱신한다. API 키와 DB 비밀번호는 브랜드 변경 때문에 바꿀 필요가 없다.
- 실행 중인 RDS를 삭제하거나 이름 변경 목적으로 새 스키마에 연결하지 않는다.

## 검증

2026-09-03 로컬 JAR 실행에서 기존 `gym_management` 연결, Flyway 검증,
`/actuator/health`의 `UP`, `/login`의 HTTP 200 및 `Gymrovia` 표시를 확인했다.
IntelliJ 종료 후 실제 루트 폴더를 `C:\Project\Gymrovia`로 변경했다.
SVG manifest의 로컬 경로도 갱신했다. IntelliJ에서는 새 경로의 프로젝트를 다시 연다.

```powershell
.\gradlew.bat test bootJar
```

`BrandingResourceTest`는 HTML의 이전 브랜드 잔존, 공통 로고, 오류 페이지와 CSS 선택자의 일치를 검사한다.
새 이름은 기존보다 길므로 배포 전 모바일·사이드바 화면도 육안 확인한다.
