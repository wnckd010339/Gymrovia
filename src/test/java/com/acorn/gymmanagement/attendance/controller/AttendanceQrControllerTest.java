package com.acorn.gymmanagement.attendance.controller;

import com.acorn.gymmanagement.attendance.service.AttendanceQrService;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.security.SessionUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceQrControllerTest {

    @Mock
    private AttendanceQrService attendanceQrService;

    private AttendanceQrController controller;
    private SessionUser member;

    @BeforeEach
    void setUp() {
        controller = new AttendanceQrController(attendanceQrService);
        member = new SessionUser(10L, "member", "member@example.com", SessionUser.ROLE_MEMBER);
    }

    @Test
    void validScanForCheckedInMemberProvidesCheckOutAction() {
        when(attendanceQrService.verifyScan(10L, "center-token")).thenReturn("verification-token");
        when(attendanceQrService.isCheckedIn(10L)).thenReturn(true);
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.verifyQr(
                member,
                "center-token",
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("member/attendance-qr", view);
        assertEquals("verification-token", model.getAttribute("verificationToken"));
        assertEquals(true, model.getAttribute("checkedIn"));
        assertEquals("CHECK_OUT", model.getAttribute("attendanceAction"));
    }

    @Test
    void validScanForUncheckedMemberProvidesCheckInAction() {
        when(attendanceQrService.verifyScan(10L, "center-token")).thenReturn("verification-token");
        when(attendanceQrService.isCheckedIn(10L)).thenReturn(false);
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.verifyQr(
                member,
                "center-token",
                model,
                new RedirectAttributesModelMap()
        );

        assertEquals("member/attendance-qr", view);
        assertEquals("verification-token", model.getAttribute("verificationToken"));
        assertEquals(false, model.getAttribute("checkedIn"));
        assertEquals("CHECK_IN", model.getAttribute("attendanceAction"));
    }

    @Test
    void invalidScanRedirectsWithErrorMessage() {
        when(attendanceQrService.verifyScan(10L, "expired-token"))
                .thenThrow(new BusinessException(ErrorCode.CONFLICT, "QR이 만료되었습니다."));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.verifyQr(
                member,
                "expired-token",
                new ConcurrentModel(),
                redirect
        );

        assertEquals("redirect:/member/attendance", view);
        assertEquals("QR이 만료되었습니다.", redirect.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void qrCheckInRedirectsWithSuccessMessage() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.checkIn(member, "verification-token", redirect);

        assertEquals("redirect:/member/attendance", view);
        assertEquals(
                "센터 QR 인증이 완료되어 체크인되었습니다.",
                redirect.getFlashAttributes().get("message")
        );
        verify(attendanceQrService).checkIn(10L, "verification-token");
    }

    @Test
    void qrCheckOutRedirectsWithBusinessError() {
        doThrow(new BusinessException(ErrorCode.CONFLICT, "인증권이 만료되었습니다."))
                .when(attendanceQrService).checkOut(10L, "expired-verification");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.checkOut(member, "expired-verification", redirect);

        assertEquals("redirect:/member/attendance", view);
        assertEquals("인증권이 만료되었습니다.", redirect.getFlashAttributes().get("errorMessage"));
    }
}
