package com.acorn.gymmanagement.common.exception;

import com.acorn.gymmanagement.common.response.ApiResponse;
import com.acorn.gymmanagement.payment.gateway.PaymentGatewayException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String detail = exception
                .getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값을 확인해 주세요.");

        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (!isApiRequest(request)) {
            response.sendError(status.value());
            return null;
        }

        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(
                        "입력 검증에 실패했습니다.",
                        ErrorCode.VALIDATION_ERROR.name(),
                        detail
                ));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(
            BusinessException exception,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        HttpStatus status =
                resolveStatus(exception.getErrorCode());

        if (!isApiRequest(request)) {
            response.sendError(status.value());
            return null;
        }

        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(
                        exception.getMessage(),
                        exception.getErrorCode().name(),
                        exception.getMessage()
                ));

    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentGateway(
            PaymentGatewayException exception,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        HttpStatus status = HttpStatus.BAD_GATEWAY;

        if (!isApiRequest(request)) {
            response.sendError(status.value());
            return null;
        }

        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(
                        exception.getMessage(),
                        exception.getCode(),
                        exception.getMessage()
                ));
    }

    private HttpStatus resolveStatus(
            ErrorCode errorCode
    ) {
        return switch (errorCode) {
            case AUTH_INVALID_CREDENTIALS,
                 UNAUTHORIZED ->
                    HttpStatus.UNAUTHORIZED;

            case FORBIDDEN ->
                    HttpStatus.FORBIDDEN;

            case NOT_FOUND ->
                    HttpStatus.NOT_FOUND;

            case CONFLICT ->
                    HttpStatus.CONFLICT;

            case VALIDATION_ERROR ->
                    HttpStatus.BAD_REQUEST;

            case INTERNAL_ERROR ->
                    HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private boolean isApiRequest(
            HttpServletRequest request
    ) {
        String apiPrefix =
                request.getContextPath() + "/api/";

        return request
                .getRequestURI()
                .startsWith(apiPrefix);
    }
}
