package com.acorn.gymmanagement.attendance.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendanceQrImageServiceTest {

    private final AttendanceQrImageService service = new AttendanceQrImageService();

    @Test
    void createsPngDataUrlForAttendanceUrl() {
        String dataUrl = service.createDataUrl(
                "https://gymrovia.example/member/attendance/qr?token=test-token"
        );

        String prefix = "data:image/png;base64,";
        assertTrue(dataUrl.startsWith(prefix));

        byte[] png = Base64.getDecoder().decode(dataUrl.substring(prefix.length()));
        assertTrue(png.length > 8);
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 0x50, png[1]);
        assertEquals((byte) 0x4E, png[2]);
        assertEquals((byte) 0x47, png[3]);
    }

    @Test
    void rejectsBlankQrContent() {
        assertThrows(BusinessException.class, () -> service.createDataUrl(" "));
    }
}
