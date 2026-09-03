# Gymrovia 데이터베이스

Gymrovia는 MySQL 8을 사용하며 DB 스키마는 Flyway로 관리합니다. 애플리케이션을 실행하면 아직 적용되지 않은 마이그레이션이 자동으로 실행되고, 결과는 `flyway_schema_history` 테이블에 기록됩니다.

## 신규 환경 구성

1. 빈 MySQL 데이터베이스를 생성합니다.
2. DB 접속 환경변수를 설정합니다.
3. 애플리케이션을 실행해 Flyway 마이그레이션을 적용합니다.
4. 로컬 개발 또는 시연 데이터가 필요하면 비어 있는 전용 DB에서 [`sample-data.sql`](sample-data.sql)을 한 번 실행합니다.

```properties
DB_URL=jdbc:mysql://localhost:3306/gym_management
DB_USERNAME=사용자명
DB_PASSWORD=비밀번호
```

최초 실행 시 `src/main/resources/db/migration/V1__baseline.sql`이 전체 테이블을 생성합니다. 정상 적용 여부는 다음 SQL로 확인합니다.

```sql
SELECT version, description, type, script, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## 기존 DB를 Flyway에 등록하는 경우

이미 테이블과 데이터가 존재하는 DB에서는 `schema.sql`이나 `V1__baseline.sql`을 직접 실행하지 않습니다.

1. DB를 백업합니다.
2. 기존 DB가 `V1__baseline.sql`과 동일한 최신 구조인지 확인합니다.
3. 최초 실행에만 `FLYWAY_BASELINE_ON_MIGRATE=true`를 지정합니다.
4. 애플리케이션을 실행하고 `flyway_schema_history`의 버전 1 `BASELINE` 기록을 확인합니다.
5. 이후 `FLYWAY_BASELINE_ON_MIGRATE`를 제거하거나 `false`로 되돌립니다.

Baseline은 기존 구조를 자동으로 수정하거나 검증하지 않습니다. 누락된 테이블·컬럼이 있다면 등록 전에 필요한 변경 SQL을 먼저 적용해야 합니다.

## 마이그레이션 관리

- 실행 경로: `src/main/resources/db/migration`
- 최초 기준 스키마: `V1__baseline.sql`
- 적용 이력: `flyway_schema_history`
- 적용된 마이그레이션 파일은 수정하거나 다시 실행하지 않습니다.
- 새로운 변경은 다음 버전의 SQL 파일로 추가합니다.

예:

```text
V2__add_member_note.sql
V3__add_payment_index.sql
```

## 문서용 SQL

[`schema.sql`](schema.sql)은 전체 DB 구조를 한 파일에서 확인하거나 별도 테스트 DB를 수동으로 초기화할 때 사용하는 참고 파일입니다. 파일 상단에 `DROP TABLE`이 있으므로 기존 데이터가 있는 DB에는 실행하지 않습니다.

`migrations` 디렉터리의 001~008 파일은 Flyway 도입 전 기능별 DB 변경 이력입니다. 이미 Flyway로 관리하는 DB에는 다시 실행하지 않으며, 향후 변경은 `src/main/resources/db/migration`에 추가합니다.

| 번호 | 파일 | 변경 내용 |
| ---: | --- | --- |
| 001 | `001-add-routine-workout-groups.sql` | 운동 그룹 구조 추가 |
| 002 | `002-add-payment-orders.sql` | PG 결제 주문 구조 추가 |
| 003 | `003-add-notifications.sql` | 알림 테이블과 조회 인덱스 추가 |
| 004 | `004-add-reservations.sql` | 예약 테이블과 일정 조회 인덱스 추가 |
| 005 | `005-add-attendance-qr.sql` | QR 출석 토큰과 검증 테이블 추가 |
| 006 | `006-add-statistics-indexes.sql` | 통계 조회 인덱스 추가 |
| 007 | `007-add-toss-payment-integration.sql` | Toss 환불 추적 정보 추가 |
| 008 | `008-add-payment-compensation.sql` | 결제 보상 취소 상태와 거래 정보 추가 |

## 테스트 데이터

- [`sample-data.sql`](sample-data.sql): 실행일 기준으로 생성되는 로컬 개발·포트폴리오 시연용 종합 데이터
- [`notification-test-data.sql`](notification-test-data.sql): 회원권 만료 알림 수동 검증용 데이터

`sample-data.sql`은 관리자 1명, 트레이너 3명, 회원 10명과 회원권·결제·환불·예약·출석·운동·시설·알림의 여러 상태를 포함합니다. 날짜는 실행일을 기준으로 계산되므로 오늘 출석, 예정 예약, 이번 달 매출, 만료 예정 회원권을 바로 확인할 수 있습니다.

샘플 계정의 비밀번호는 모두 `password`입니다. 로그인 ID는 관리자 `admin`, 트레이너 `trainer01` ~ `trainer03`, 회원 `member01` ~ `member09`입니다.

명시적인 ID를 사용하므로 **Flyway가 적용된 비어 있는 시연 전용 DB에서 한 번만 실행**합니다. 화면 시연용 `payment_key`는 실제 Toss 키가 아니므로 해당 결제에는 실제 PG 환불을 요청하지 않습니다.

테스트 데이터는 실제 운영 DB에 실행하지 않습니다. 실제 비밀번호, OAuth 시크릿, Toss 시크릿 키도 SQL이나 문서에 저장하지 않습니다.
