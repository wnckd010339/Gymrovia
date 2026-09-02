package com.acorn.gymmanagement.payment.gateway.toss;

import com.acorn.gymmanagement.payment.gateway.PaymentApprovalResult;
import com.acorn.gymmanagement.payment.gateway.PaymentCancellationResult;
import com.acorn.gymmanagement.payment.gateway.PaymentGateway;
import com.acorn.gymmanagement.payment.gateway.PaymentGatewayException;
import com.acorn.gymmanagement.payment.gateway.toss.dto.request.TossCancelRequest;
import com.acorn.gymmanagement.payment.gateway.toss.dto.request.TossConfirmRequest;
import com.acorn.gymmanagement.payment.gateway.toss.dto.response.TossCancelResponse;
import com.acorn.gymmanagement.payment.gateway.toss.dto.response.TossPaymentResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;


import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;


@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final String IDEMPOTENCY_KEY_HEADER =
            "Idempotency-Key";
    private static final ZoneId PAYMENT_TIME_ZONE =
            ZoneId.of("Asia/Seoul");

    private final TossPaymentProperties properties;
    private final TossPaymentErrorHandler errorHandler;
    private final RestClient restClient;

    public TossPaymentGateway(
            TossPaymentProperties properties,
            TossPaymentErrorHandler errorHandler,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.errorHandler = errorHandler;

        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    @Override
    public PaymentApprovalResult confirm(
            String paymentKey,
            String orderId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        validateConfirmParameters(
                paymentKey,
                orderId,
                amount,
                idempotencyKey
        );

        TossPaymentResponse response = execute(
                () -> restClient.post()
                        .uri("/v1/payments/confirm")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                createAuthorizationHeader()
                        )
                        .header(
                                IDEMPOTENCY_KEY_HEADER,
                                idempotencyKey
                        )
                        .body(new TossConfirmRequest(
                                paymentKey,
                                orderId,
                                amount
                        ))
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::isError,
                                errorHandler::handle
                        )
                        .body(TossPaymentResponse.class)
        );

        validateApprovalResponse(
                response,
                paymentKey,
                orderId,
                amount
        );

        return new PaymentApprovalResult(
                response.paymentKey(),
                response.orderId(),
                response.totalAmount(),
                response.method(),
                response.status(),
                toLocalDateTime(response.approvedAt())
        );
    }

    @Override
    public PaymentCancellationResult cancel(
            String paymentKey,
            BigDecimal amount,
            String reason,
            String idempotencyKey
    ) {
        validateCancelParameters(
                paymentKey,
                amount,
                reason,
                idempotencyKey
        );

        TossPaymentResponse response = execute(
                () -> restClient.post()
                        .uri(
                                "/v1/payments/{paymentKey}/cancel",
                                paymentKey
                        )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                createAuthorizationHeader()
                        )
                        .header(
                                IDEMPOTENCY_KEY_HEADER,
                                idempotencyKey
                        )
                        .body(new TossCancelRequest(
                                reason.trim(),
                                amount
                        ))
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::isError,
                                errorHandler::handle
                        )
                        .body(TossPaymentResponse.class)
        );

        validateCancellationResponse(
                response,
                paymentKey,
                amount
        );

        TossCancelResponse latestCancellation =
                findLatestCancellation(response.cancels());

        return new PaymentCancellationResult(
                response.paymentKey(),
                latestCancellation.transactionKey(),
                latestCancellation.cancelAmount(),
                toLocalDateTime(
                        latestCancellation.canceledAt()
                )
        );
    }

    private String createAuthorizationHeader() {
        requireConfigured();

        String credentials =
                properties.secretKey() + ":";

        String encoded = Base64.getEncoder()
                .encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8)
                );

        return "Basic " + encoded;
    }

    private void requireConfigured() {
        if (properties.secretKey() == null
              || properties.secretKey().isBlank()) {
            throw new PaymentGatewayException(
                    "TOSS_SECRET_KEY_MISSING",
                    "토스페이먼츠 시크릿 키가 설정되지 않았습니다."
            );
        }

        if (properties.baseUrl() == null
                || properties.baseUrl().isBlank()) {
            throw new PaymentGatewayException(
                    "TOSS_BASE_URL_MISSING",
                    "토스페이먼츠 API 주소가 설정되지 않았습니다."
            );
        }
    }

    private void validateConfirmParameters(
            String paymentKey,
            String orderId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw invalidRequest(
                    "결제 키가 비어 있습니다."
            );
        }

        if (paymentKey.length() > 200) {
            throw invalidRequest(
                    "결제 키의 길이가 올바르지 않습니다."
            );
        }

        if (orderId == null || orderId.isBlank()) {
            throw invalidRequest(
                    "주문번호가 비어 있습니다."
            );
        }

        if (orderId.length() < 6
                || orderId.length() > 64) {
            throw invalidRequest(
                    "주문번호의 길이가 올바르지 않습니다."
            );
        }

        validateAmount(amount);
        validateIdempotencyKey(idempotencyKey);
    }

    private void validateCancelParameters(
            String paymentKey,
            BigDecimal amount,
            String reason,
            String idempotencyKey
    ) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw invalidRequest(
                    "결제 키가 비어 있습니다."
            );
        }

        if (paymentKey.length() > 200) {
            throw invalidRequest(
                    "결제 키의 길이가 올바르지 않습니다."
            );
        }

        validateAmount(amount);
        validateIdempotencyKey(idempotencyKey);

        if (reason == null || reason.isBlank()) {
            throw invalidRequest(
                    "취소 사유를 입력해 주세요."
            );
        }

        if (reason.trim().length() > 200) {
            throw invalidRequest(
                    "취소 사유는 200자 이내로 입력해 주세요."
            );
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw invalidRequest(
                    "결제 금액은 0원보다 커야 합니다."
            );
        }

        if (amount.stripTrailingZeros().scale() > 0) {
            throw invalidRequest(
                    "원화 결제 금액에는 소수점을 사용할 수 없습니다."
            );
        }
    }

    private void validateIdempotencyKey(
            String idempotencyKey
    ) {
        if (idempotencyKey == null
                || idempotencyKey.isBlank()) {
            throw invalidRequest(
                    "멱등키가 비어 있습니다."
            );
        }

        if (idempotencyKey.length() > 300) {
            throw invalidRequest(
                    "멱등키는 300자 이하여야 합니다."
            );
        }
    }

    private void validateApprovalResponse(
            TossPaymentResponse response,
            String expectedPaymentKey,
            String expectedOrderId,
            BigDecimal expectedAmount
    ) {
        if (response == null) {
            throw emptyResponse(
                    "결제 승인 응답이 없습니다."
            );
        }

        if (!Objects.equals(
                expectedPaymentKey,
                response.paymentKey()
        )) {
            throw invalidResponse(
                    "승인 응답의 결제 키가 일치하지 않습니다."
            );
        }

        if (!Objects.equals(
                expectedOrderId,
                response.orderId()
        )) {
            throw invalidResponse(
                    "승인 응답의 주문번호가 일치하지 않습니다."
            );
        }

        if (response.totalAmount() == null
                || expectedAmount.compareTo(
                        response.totalAmount()
        ) != 0) {
            throw invalidResponse(
                    "승인 응답의 결제 금액이 일치하지 않습니다."
            );
        }

        if (!"DONE".equals(response.status())) {
            throw invalidResponse(
                    "결제가 승인 완료 상태가 아닙니다."
            );
        }

        if (response.method() == null
                || response.method().isBlank()) {
            throw invalidResponse(
                    "승인 응답에 결제수단이 없습니다."
            );
        }

        if (response.approvedAt() == null) {
            throw invalidResponse(
                    "승인 응답에 승인시간이 없습니다."
            );
        }
    }

    private void validateCancellationResponse(
            TossPaymentResponse response,
            String expectedPaymentKey,
            BigDecimal expectedAmount
    ) {
        if (response == null) {
            throw emptyResponse(
                    "결제 취소 응답이 없습니다."
            );
        }

        if (!Objects.equals(
                expectedPaymentKey,
                response.paymentKey()
        )) {
            throw invalidResponse(
                    "취소 응답의 결제 키가 일치하지 않습니다."
            );
        }

        TossCancelResponse cancellation =
                findLatestCancellation(response.cancels());

        if (cancellation.cancelAmount() == null
                || expectedAmount.compareTo(
                        cancellation.cancelAmount()
        ) != 0) {
            throw invalidResponse(
                    "취소 응답의 금액이 요청금액과 일치하지 않습니다."
            );
        }

        if (!"DONE".equals(cancellation.cancelStatus())) {
            throw invalidResponse(
                    "결제 취소가 완료 상태가 아닙니다."
            );
        }

        if (cancellation.transactionKey() == null
                || cancellation.transactionKey().isBlank()) {
            throw invalidResponse(
                    "취소 응답에 거래 키가 없습니다."
            );
        }

        if (cancellation.canceledAt() == null) {
            throw invalidResponse(
                    "취소 응답에 취소시간이 없습니다."
            );
        }
    }

    private TossCancelResponse findLatestCancellation(
            List<TossCancelResponse> cancellations
    ) {
        if (cancellations == null
                || cancellations.isEmpty()) {
            throw invalidResponse(
                    "취소 응답에 취소 내역이 없습니다."
            );
        }

        return cancellations.get(cancellations.size() - 1);
    }

    private <T> T execute(Supplier<T> request) {
        try {
            return request.get();
        } catch (PaymentGatewayException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new PaymentGatewayException(
                    "TOSS_NETWORK_ERROR",
                    "결제 서비스 통신 중 오류가 발생했습니다."
            );
        }
    }

    private LocalDateTime toLocalDateTime(
            OffsetDateTime dateTime
    ) {
        if (dateTime == null) {
            throw invalidResponse(
                    "PG 응답에 날짜와 시간이 없습니다."
            );
        }

        return dateTime
                .atZoneSameInstant(PAYMENT_TIME_ZONE)
                .toLocalDateTime();
    }

    private PaymentGatewayException invalidRequest(
            String message
    ) {
        return new PaymentGatewayException(
                "INVALID_GATEWAY_REQUEST",
                message
        );
    }

    private PaymentGatewayException invalidResponse(
            String message
    ) {
        return new PaymentGatewayException(
                "INVALID_GATEWAY_RESPONSE",
                message
        );
    }

    private PaymentGatewayException emptyResponse(
            String message
    ) {
        return new PaymentGatewayException(
                "EMPTY_GATEWAY_RESPONSE",
                message
        );
    }
}
