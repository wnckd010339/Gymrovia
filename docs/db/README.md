# FitFlow 데이터베이스

FitFlow는 MySQL 8을 기준으로 합니다. 신규 환경은 전체 스키마로 구성하고, 데이터가 존재하는 환경은 번호가 붙은 마이그레이션을 순서대로 적용합니다.

## 신규 환경 구성

1. 빈 MySQL 데이터베이스를 생성합니다.
2. [`schema.sql`](schema.sql)을 실행합니다.
3. 로컬 개발 또는 시연 데이터가 필요하면 [`sample-data.sql`](sample-data.sql)을 실행합니다.
4. DB 접속 환경변수를 설정하고 애플리케이션을 실행합니다.

```properties
DB_URL=jdbc:mysql://localhost:3306/fitflow
DB_USERNAME=사용자명
DB_PASSWORD=비밀번호
```

> `schema.sql`은 기존 FitFlow 테이블을 제거하고 다시 생성합니다. 보존해야 하는 데이터가 있는 DB에는 실행하지 마세요.

## 기존 환경 업데이트

기존 데이터를 유지해야 하는 환경에서는 `schema.sql`을 실행하지 않습니다.

1. 변경 전 DB를 백업합니다.
2. 현재까지 적용된 마지막 마이그레이션 번호를 확인합니다.
3. `migrations` 디렉터리에서 적용되지 않은 파일만 번호순으로 실행합니다.
4. 애플리케이션을 실행하고 관련 기능과 전체 테스트를 확인합니다.

현재 별도의 마이그레이션 이력 테이블은 사용하지 않으므로, 각 환경에서 마지막 적용 번호를 직접 기록해야 합니다.

## 마이그레이션 목록

| 번호 | 파일 | 변경 내용 |
| ---: | --- | --- |
| 001 | `001-add-routine-workout-groups.sql` | 운동 루틴과 운동 기록의 운동 그룹 구조 추가 |
| 002 | `002-add-payment-orders.sql` | 회원 PG 결제 주문 구조 추가 |
| 003 | `003-add-notifications.sql` | 사용자 알림 테이블과 조회 인덱스 추가 |
| 004 | `004-add-reservations.sql` | 예약 테이블, 상태 제약조건과 일정 조회 인덱스 추가 |
| 005 | `005-add-attendance-qr.sql` | 센터 QR 토큰과 회원 검증권 테이블 추가 |
| 006 | `006-add-statistics-indexes.sql` | 회원·출석·결제·환불 통계 조회 인덱스 추가 |
| 007 | `007-add-toss-payment-integration.sql` | Toss 환불 거래 키, 멱등성 키와 실패 정보 추가 |
| 008 | `008-add-payment-compensation.sql` | PG 승인 후 저장 실패에 대한 결제 보상 상태와 거래 정보 추가 |

각 마이그레이션은 앞 번호의 변경과 선행 테이블이 적용된 상태를 전제로 합니다.

## 변경 관리 원칙

- 이미 적용된 마이그레이션 파일은 수정하거나 다시 실행하지 않습니다.
- 새로운 DB 변경은 다음 번호의 SQL 파일로 추가합니다.
- 신규 환경용 `schema.sql`에도 동일한 최종 구조를 반영합니다.
- 기존 데이터가 있는 환경은 변경 전에 백업합니다.
- 로컬 시연 데이터와 운영 데이터를 구분합니다.
- 실제 비밀번호, OAuth 시크릿, Toss 시크릿 키를 SQL이나 문서에 저장하지 않습니다.

## 주요 파일

```text
docs/db/
├── schema.sql                 # 신규 환경용 전체 스키마
├── sample-data.sql            # 선택적 로컬·시연 데이터
├── notification-test-data.sql # 알림 기능 확인용 데이터
├── migrations/                # 기존 DB 순차 업데이트 SQL
└── README.md                  # DB 적용 및 변경 관리 지침
```

## 스키마 관리 방향

DB 구조의 기준은 `schema.sql`과 `migrations`입니다. 애플리케이션 실행 여부에 따라 테이블 구조가 달라지지 않도록, 런타임 자동 테이블 생성 방식은 제거하고 SQL 적용 절차를 문서화된 한 경로로 통일합니다.

이 구조 개선이 완료되기 전까지는 `notifications`와 `reservations`에 런타임 초기화 코드가 남아 있을 수 있습니다. 관련 Config와 중복 SQL을 제거한 뒤에는 신규 환경에서 반드시 `schema.sql`을 먼저 실행해야 합니다.
