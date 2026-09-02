package com.acorn.gymmanagement.payment.gateway.toss;

import com.acorn.gymmanagement.payment.gateway.PaymentApprovalResult;
import com.acorn.gymmanagement.payment.gateway.PaymentCancellationResult;
import com.acorn.gymmanagement.payment.gateway.PaymentGatewayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TossPaymentGatewayTest {

    private static final String BASE_URL =
            "https://api.tosspayments.test";

    private MockRestServiceServer server;
    private TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        TossPaymentProperties properties =
                new TossPaymentProperties(
                        "test_client_key",
                        "test_secret_key",
                        BASE_URL,
                        "http://localhost:8080/member/payments/success",
                        "http://localhost:8080/member/payments/fail"
                );

        TossPaymentErrorHandler errorHandler =
                new TossPaymentErrorHandler(
                        new ObjectMapper()
                );

        RestClient.Builder restClientBuilder =
                RestClient.builder();

        server = MockRestServiceServer
                .bindTo(restClientBuilder)
                .build();

        gateway = new TossPaymentGateway(
                properties,
                errorHandler,
                restClientBuilder
        );
    }

    @Test
    void 결제승인요청과응답을처리한다() {
        server.expect(
                        once(),
                        requestTo(
                                BASE_URL
                                        + "/v1/payments/confirm"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader(
                                "test_secret_key"
                        )
                ))
                .andExpect(header(
                        "Idempotency-Key",
                        "confirm-order-123456"
                ))
                .andExpect(content().json("""
                        {
                          "paymentKey": "payment-key-123",
                          "orderId": "order-123456",
                          "amount": 80000
                        }
                        """))
                .andRespond(withSuccess(
                        """
                        {
                          "paymentKey": "payment-key-123",
                          "orderId": "order-123456",
                          "status": "DONE",
                          "method": "카드",
                          "totalAmount": 80000,
                          "approvedAt": "2026-09-02T12:00:00+09:00",
                          "cancels": []
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        PaymentApprovalResult result =
                gateway.confirm(
                        "payment-key-123",
                        "order-123456",
                        new BigDecimal("80000"),
                        "confirm-order-123456"
                );

        assertEquals(
                "payment-key-123",
                result.paymentKey()
        );

        assertEquals(
                "order-123456",
                result.orderId()
        );

        assertEquals(
                0,
                new BigDecimal("80000")
                        .compareTo(result.amount())
        );

        assertEquals(
                "카드",
                result.method()
        );

        assertEquals(
                "DONE",
                result.status()
        );

        assertEquals(
                LocalDateTime.of(
                        2026,
                        9,
                        2,
                        12,
                        0
                ),
                result.approvedAt()
        );

        server.verify();
    }

    @Test
    void 결제취소요청과응답을처리한다() {
        server.expect(
                        once(),
                        requestTo(
                                BASE_URL
                                        + "/v1/payments/"
                                        + "payment-key-123/cancel"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader(
                                "test_secret_key"
                        )
                ))
                .andExpect(header(
                        "Idempotency-Key",
                        "cancel-payment-123"
                ))
                .andExpect(content().json("""
                        {
                          "cancelReason": "회원 요청",
                          "cancelAmount": 80000
                        }
                        """))
                .andRespond(withSuccess(
                        """
                        {
                          "paymentKey": "payment-key-123",
                          "orderId": "order-123456",
                          "status": "CANCELED",
                          "method": "카드",
                          "totalAmount": 80000,
                          "approvedAt": "2026-09-02T12:00:00+09:00",
                          "cancels": [
                            {
                              "transactionKey":
                                  "cancel-transaction-123",
                              "cancelAmount": 80000,
                              "cancelStatus": "DONE",
                              "canceledAt":
                                  "2026-09-02T12:10:00+09:00"
                            }
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        PaymentCancellationResult result =
                gateway.cancel(
                        "payment-key-123",
                        new BigDecimal("80000"),
                        "회원 요청",
                        "cancel-payment-123"
                );

        assertEquals(
                "payment-key-123",
                result.paymentKey()
        );

        assertEquals(
                "cancel-transaction-123",
                result.transactionKey()
        );

        assertEquals(
                0,
                new BigDecimal("80000")
                        .compareTo(result.cancelledAmount())
        );

        assertEquals(
                LocalDateTime.of(
                        2026,
                        9,
                        2,
                        12,
                        10
                ),
                result.cancelledAt()
        );

        server.verify();
    }

    @Test
    void 토스오류응답을결제예외로변환한다() {
        server.expect(
                        once(),
                        requestTo(
                                BASE_URL
                                        + "/v1/payments/confirm"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(
                        HttpStatus.BAD_REQUEST
                )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body("""
                                {
                                  "code":
                                    "ALREADY_PROCESSED_PAYMENT",
                                  "message":
                                    "이미 처리된 결제입니다."
                                }
                                """));

        PaymentGatewayException exception =
                assertThrows(
                        PaymentGatewayException.class,
                        () -> gateway.confirm(
                                "payment-key-123",
                                "order-123456",
                                new BigDecimal("80000"),
                                "confirm-order-123456"
                        )
                );

        assertEquals(
                "ALREADY_PROCESSED_PAYMENT",
                exception.getCode()
        );

        assertEquals(
                "이미 처리된 결제입니다.",
                exception.getMessage()
        );

        server.verify();
    }

    @Test
    void 승인응답의금액이다르면예외가발생한다() {
        server.expect(
                        once(),
                        requestTo(
                                BASE_URL
                                        + "/v1/payments/confirm"
                        )
                )
                .andRespond(withSuccess(
                        """
                        {
                          "paymentKey": "payment-key-123",
                          "orderId": "order-123456",
                          "status": "DONE",
                          "method": "카드",
                          "totalAmount": 70000,
                          "approvedAt":
                            "2026-09-02T12:00:00+09:00",
                          "cancels": []
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        PaymentGatewayException exception =
                assertThrows(
                        PaymentGatewayException.class,
                        () -> gateway.confirm(
                                "payment-key-123",
                                "order-123456",
                                new BigDecimal("80000"),
                                "confirm-order-123456"
                        )
                );

        assertEquals(
                "INVALID_GATEWAY_RESPONSE",
                exception.getCode()
        );

        assertEquals(
                "승인 응답의 결제 금액이 일치하지 않습니다.",
                exception.getMessage()
        );

        server.verify();
    }

    @Test
    void 결제금액이0원이하면요청을보내지않는다() {
        PaymentGatewayException exception =
                assertThrows(
                        PaymentGatewayException.class,
                        () -> gateway.confirm(
                                "payment-key-123",
                                "order-123456",
                                BigDecimal.ZERO,
                                "confirm-order-123456"
                        )
                );

        assertEquals(
                "INVALID_GATEWAY_REQUEST",
                exception.getCode()
        );

        assertEquals(
                "결제 금액은 0원보다 커야 합니다.",
                exception.getMessage()
        );

        server.verify();
    }

    private String authorizationHeader(
            String secretKey
    ) {
        String credentials = secretKey + ":";

        String encoded = Base64.getEncoder()
                .encodeToString(
                        credentials.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        return "Basic " + encoded;
    }
}