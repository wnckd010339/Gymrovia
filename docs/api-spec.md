# Gymrovia API 명세

이 문서는 JSON을 요청·응답으로 사용하는 `/api/**` 엔드포인트를 정리합니다. 로그인과 역할별 화면의 MVC 경로는 포함하지 않습니다.

## 공통 응답

### 성공

```json
{
  "success": true,
  "message": "처리 결과 메시지",
  "data": {},
  "error": null
}
```

### 실패

```json
{
  "success": false,
  "message": "오류 메시지",
  "data": null,
  "error": {
    "code": "NOT_FOUND",
    "detail": "상세 오류 메시지"
  }
}
```

입력 검증 또는 비즈니스 규칙 위반이 `/api/**`에서 발생하면 JSON으로 반환합니다. 일반 화면 요청의 오류는 Gymrovia HTML 오류 화면으로 처리합니다.

## 인증과 권한

Gymrovia는 세션 기반 인증을 사용합니다. 로그인과 로그아웃은 JSON API가 아닌 다음 MVC 경로에서 처리합니다.

```text
GET  /login
POST /login
POST /logout
```

| API 경로 | 접근 권한 |
| --- | --- |
| `/api/members/**` | 관리자 |
| `/api/membership-products/**` | 관리자 |
| `/api/payments/**` | 관리자 |
| `/api/member/**` | 회원 |
| 그 외 `/api/**` | 로그인 사용자 |

## 회원

### 회원 등록

```text
POST /api/members
권한: 관리자
```

요청 필드: `name`, `phone`, `birthDate`, `gender`, `loginId`, `initialPassword`, `trainerRequested`

### 회원 수정

```text
PATCH /api/members/{memberId}
권한: 관리자
```

요청 필드: `name`, `phone`, `birthDate`, `gender`, `status`

## 회원권

### 활성 회원권 상품 조회

```text
GET /api/membership-products
권한: 관리자
```

### 회원의 회원권 조회

```text
GET /api/members/{memberId}/memberships
권한: 관리자
```

### 회원권 등록

```text
POST /api/members/{memberId}/memberships
권한: 관리자
```

```json
{
  "productId": 1,
  "startDate": "2026-09-02"
}
```

### 회원권 일시정지·재개·취소

```text
PATCH /api/members/{memberId}/memberships/{membershipId}/pause
PATCH /api/members/{memberId}/memberships/{membershipId}/resume
PATCH /api/members/{memberId}/memberships/{membershipId}/cancel
권한: 관리자
```

관리자가 결제 대기 회원권을 취소하면 연결된 결제 대기 주문도 함께 취소합니다.

### 회원 본인의 결제 대기 회원권 취소

```text
PATCH /api/member/memberships/{membershipId}/cancel
권한: 회원
```

## 관리자 결제

### 결제·환불 내역 조회

```text
GET /api/payments
GET /api/payments?memberId={memberId}
권한: 관리자
```

`memberId`를 지정하면 해당 회원의 내역만 조회합니다. 존재하지 않는 회원 ID는 `404 NOT_FOUND`를 반환합니다.

### 현장 결제 등록

```text
POST /api/payments
권한: 관리자
```

```json
{
  "membershipId": 10,
  "paymentMethod": "CARD"
}
```

### 환불

```text
POST /api/payments/{paymentId}/refunds
권한: 관리자
```

```json
{
  "amount": 30000,
  "reason": "고객 요청"
}
```

- Toss Payments 결제는 PG 취소 API를 호출한 후 로컬 환불 상태를 반영합니다.
- 현장 결제는 로컬 환불로 처리합니다.
- 전액 환불 시 결제 상태와 연결된 회원권 상태를 함께 변경합니다.

## 회원 PG 결제 주문

### 주문 생성

```text
POST /api/member/payment-orders
권한: 회원
```

```json
{
  "productId": 1,
  "startDate": "2026-09-02"
}
```

- 상품 가격은 클라이언트 요청값이 아니라 서버의 활성 상품 가격으로 결정합니다.
- 회원권은 `PENDING_PAYMENT`, 주문은 `READY` 상태로 생성됩니다.
- 만료된 `READY` 주문은 `EXPIRED`, 연결된 결제 대기 회원권은 `CANCELLED`로 변경됩니다.

### 주문 승인 확인

```text
POST /api/member/payment-orders/{orderId}/confirm
권한: 회원
```

```json
{
  "paymentKey": "Toss가 발급한 결제 키",
  "amount": 80000
}
```

처리 흐름:

1. 로그인 회원과 주문 소유자 일치 여부를 확인합니다.
2. 서버가 주문 상태와 결제 금액을 검증합니다.
3. Toss Payments 승인 API를 호출합니다.
4. 승인 결과와 결제·회원권 상태를 트랜잭션으로 저장합니다.
5. PG 승인 후 로컬 저장이 실패하면 승인된 결제를 자동 취소합니다.
6. 자동 취소도 실패하면 수동 대사가 필요한 상태로 주문을 보존합니다.

## 주요 오류 상태

| 오류 코드 | HTTP 상태 | 의미 |
| --- | ---: | --- |
| `VALIDATION_ERROR` | 400 | 입력값 검증 실패 |
| `AUTH_INVALID_CREDENTIALS` | 401 | 로그인 정보 불일치 |
| `UNAUTHORIZED` | 401 | 인증 필요 |
| `FORBIDDEN` | 403 | 접근 권한 없음 |
| `NOT_FOUND` | 404 | 대상 없음 |
| `CONFLICT` | 409 | 현재 상태 또는 비즈니스 규칙 충돌 |
| `INTERNAL_ERROR` | 500 | 내부 처리 실패 |

Toss Payments 등 외부 결제 시스템 호출 실패는 `502 Bad Gateway`로 반환합니다.
