package com.acorn.gymmanagement.common.exception;

import com.acorn.gymmanagement.common.response.ApiResponse;
import com.acorn.gymmanagement.payment.gateway.PaymentGatewayException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값을 확인해 주세요.");
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("입력 검증에 실패했습니다.", ErrorCode.VALIDATION_ERROR.name(), detail));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
        HttpStatus status = switch (exception.getErrorCode()) {
            case AUTH_INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status)
                .body(ApiResponse.failure(exception.getMessage(), exception.getErrorCode().name(), exception.getMessage()));
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentGateway(
            PaymentGatewayException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.failure(
                        exception.getMessage(),
                        exception.getCode(),
                        exception.getMessage()
                ));
    }
}
