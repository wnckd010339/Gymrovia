package com.acorn.gymmanagement.payment.gateway.toss;

import com.acorn.gymmanagement.payment.gateway.PaymentGatewayException;
import com.acorn.gymmanagement.payment.gateway.toss.dto.response.TossErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import tools.jackson.core.JacksonException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentErrorHandler {

    private final ObjectMapper objectMapper;

    public void handle(
            HttpRequest request,
            ClientHttpResponse response
    ) throws IOException {
        int statusCode = response.getStatusCode().value();

        String responseBody = new String(
                response.getBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        TossErrorResponse tossError =
                parseErrorResponse(responseBody);

        String errorCode = tossError.code() == null
                    || tossError.code().isBlank()
                    ? "TOSS_HTTP_" + statusCode
                    : tossError.code();

        log.warn(
                "토스페이먼츠 API 요청 실패 : method={}, uri={}, status={}, code={}",
                request.getMethod(),
                request.getURI(),
                statusCode,
                errorCode
        );

        throw new PaymentGatewayException(
                errorCode,
                toUserMessage(errorCode, statusCode)
        );
    }

    private TossErrorResponse parseErrorResponse(
            String responseBody
    ) {
        if (responseBody == null || responseBody.isBlank()) {
            return new TossErrorResponse(null,null);
        }

        try {
            return objectMapper.readValue(
                    responseBody,
                    TossErrorResponse.class
            );
        } catch (JacksonException exception) {
            log.warn(
                    "토스페이먼츠 오류 응답 JSON을 해석하지 못했습니다."
            );

            return new TossErrorResponse(
                    null,
                    null
            );
        }
    }

    private String toUserMessage(
            String errorCode, int statusCode
    ) {
        return switch (errorCode) {
            case "ALREADY_PROCESSED_PAYMENT" ->
                    "이미 처리된 결제입니다.";

            case "PROVIDER_ERROR" ->
                    "결제기관 처리 중 오류가 발생했습니다.";

            case "REJECT_CARD_COMPANY" ->
                    "카드사에서 결제를 거절했습니다.";

            case "EXCEED_MAX_CARD_INSTALLMENT_PLAN" ->
                    "지원하지 않는 카드 할부 개월입니다.";

            case "INVALID_CARD_INSTALLMENT_PLAN" ->
                    "카드 할부 정보를 확인해 주세요.";

            case "NOT_SUPPORTED_INSTALLMENT_PLAN_CARD_OR_MERCHANT" ->
                    "해당 카드 또는 상점에서는 선택한 할부를 지원하지 않습니다.";

            case "INVALID_API_KEY",
                 "UNAUTHORIZED_KEY",
                 "INCORRECT_BASIC_AUTH_FORMAT" ->
                    "결제 서비스 설정을 확인할 수 없습니다.";

            case "INVALID_REQUEST",
                 "INVALID_PAYMENT_KEY",
                 "INVALID_ORDER_ID" ->
                    "결제 요청정보가 올바르지 않습니다.";

            case "NOT_FOUND_PAYMENT" ->
                    "결제정보를 찾을 수 없습니다.";

            case "NOT_CANCELABLE_AMOUNT" ->
                    "취소할 수 있는 금액을 초과했습니다.";

            case "ALREADY_CANCELED_PAYMENT" ->
                    "이미 취소된 결제입니다.";

            case "NOT_CANCELABLE_PAYMENT" ->
                    "현재 취소할 수 없는 결제입니다.";

            case "FORBIDDEN_REQUEST" ->
                    "결제 요청을 처리할 권한이 없습니다.";

            default -> defaultMessage(statusCode);
        };
    }

    private String defaultMessage(int statusCode) {
        if (statusCode >= 500) {
            return "결제 서비스가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해 주세요.";
        }

        if (statusCode == 401 || statusCode == 403) {
            return "결제 서비스 인증 설정을 확인해 주세요.";
        }

        if (statusCode == 404) {
            return "결제정보를 찾을 수 없습니다.";
        }

        return "결제 요청을 처리하지 못했습니다.";
    }
}
