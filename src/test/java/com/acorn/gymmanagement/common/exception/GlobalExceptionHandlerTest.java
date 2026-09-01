package com.acorn.gymmanagement.common.exception;

import com.acorn.gymmanagement.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void apiBusinessExceptionReturnsJsonResponse() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/payments"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        BusinessException exception =
                new BusinessException(
                        ErrorCode.CONFLICT,
                        "이미 처리된 요청입니다."
                );

        ResponseEntity<ApiResponse<Void>> result =
                handler.handleBusiness(
                        exception,
                        request,
                        response
                );

        assertNotNull(result);
        assertEquals(
                HttpStatus.CONFLICT,
                result.getStatusCode()
        );

        assertNotNull(result.getBody());
        assertFalse(result.getBody().success());
        assertEquals(
                "이미 처리된 요청입니다.",
                result.getBody().message()
        );
    }

    @Test
    void pageBusinessExceptionSendsHttpError() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/admin/reservations"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        BusinessException exception =
                new BusinessException(
                        ErrorCode.CONFLICT,
                        "이미 처리된 요청입니다."
                );

        ResponseEntity<ApiResponse<Void>> result =
                handler.handleBusiness(
                        exception,
                        request,
                        response
                );

        assertNull(result);

        assertEquals(
                HttpStatus.CONFLICT.value(),
                response.getStatus()
        );

        assertTrue(response.isCommitted());
    }

    @Test
    void pageNotFoundExceptionSends404() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/admin/reservations/999"
                );

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        BusinessException exception =
                new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "예약을 찾을 수 없습니다."
                );

        ResponseEntity<ApiResponse<Void>> result =
                handler.handleBusiness(
                        exception,
                        request,
                        response
                );

        assertNull(result);
        assertEquals(
                HttpStatus.NOT_FOUND.value(),
                response.getStatus()
        );
    }
}