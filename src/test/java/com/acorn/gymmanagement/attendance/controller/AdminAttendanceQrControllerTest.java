package com.acorn.gymmanagement.attendance.controller;

import com.acorn.gymmanagement.attendance.dto.response.AttendanceQrIssueResponse;
import com.acorn.gymmanagement.attendance.service.AttendanceQrImageService;
import com.acorn.gymmanagement.attendance.service.AttendanceQrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAttendanceQrControllerTest {

    @Mock
    private AttendanceQrService attendanceQrService;
    @Mock
    private AttendanceQrImageService imageService;

    private AdminAttendanceQrController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAttendanceQrController(attendanceQrService, imageService);
        ReflectionTestUtils.setField(controller, "centerCode", "FITFLOW_MAIN");
        ReflectionTestUtils.setField(controller, "centerName", "핏플로우 강남센터");
        ReflectionTestUtils.setField(controller, "publicBaseUrl", "https://fitflow.example");
    }

    @Test
    void pageProvidesCenterName() {
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.page(model);

        assertEquals("admin/attendance/qr", view);
        assertEquals("핏플로우 강남센터", model.getAttribute("centerName"));
    }

    @Test
    void issueBuildsMemberUrlAndReturnsQrImage() {
        when(attendanceQrService.createCenterQr("FITFLOW_MAIN", "핏플로우 강남센터"))
                .thenReturn("raw-token");
        when(attendanceQrService.centerQrSeconds()).thenReturn(30L);
        when(imageService.createDataUrl(contains("/member/attendance/qr?token=raw-token")))
                .thenReturn("data:image/png;base64,AAA");

        AttendanceQrIssueResponse response = controller.issue();

        assertEquals("data:image/png;base64,AAA", response.imageDataUrl());
        assertEquals(30L, response.expiresInSeconds());
        assertEquals("핏플로우 강남센터", response.centerName());
        verify(imageService).createDataUrl(
                "https://fitflow.example/member/attendance/qr?token=raw-token"
        );
    }
}
