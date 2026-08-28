package com.acorn.gymmanagement.attendance.service;

import com.acorn.gymmanagement.attendance.mapper.AttendanceMapper;
import com.acorn.gymmanagement.attendance.mapper.AttendanceQrMapper;
import com.acorn.gymmanagement.attendance.model.AttendanceQrToken;
import com.acorn.gymmanagement.attendance.model.AttendanceQrVerification;
import com.acorn.gymmanagement.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceQrServiceTest {

    @Mock
    private AttendanceQrMapper attendanceQrMapper;
    @Mock
    private AttendanceMapper attendanceMapper;
    @Mock
    private AttendanceService attendanceService;

    private AttendanceQrService service;

    @BeforeEach
    void setUp() {
        service = new AttendanceQrService(
                attendanceQrMapper,
                attendanceMapper,
                attendanceService
        );
    }

    @Test
    void createsCenterQrAndStoresOnlyItsHash() {
        when(attendanceQrMapper.insertQrToken(
                anyString(), eq("FITFLOW_MAIN"), eq("핏플로우 강남센터"), any(LocalDateTime.class)
        )).thenReturn(1);

        String rawToken = service.createCenterQr("FITFLOW_MAIN", "핏플로우 강남센터");

        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());
        verify(attendanceQrMapper).insertQrToken(
                anyString(), eq("FITFLOW_MAIN"), eq("핏플로우 강남센터"), any(LocalDateTime.class)
        );
    }

    @Test
    void rejectsBlankCenterInformation() {
        assertThrows(BusinessException.class, () -> service.createCenterQr(" ", "센터"));
        assertThrows(BusinessException.class, () -> service.createCenterQr("CENTER", " "));
        verify(attendanceQrMapper, never()).insertQrToken(anyString(), anyString(), anyString(), any());
    }

    @Test
    void scanCreatesMemberBoundVerification() {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(30);
        when(attendanceQrMapper.findValidQrTokenForUpdate(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(new AttendanceQrToken(1L, "FITFLOW_MAIN", "센터", expiresAt)));
        when(attendanceMapper.findActiveMemberIdByUserId(10L)).thenReturn(Optional.of(25L));
        when(attendanceQrMapper.insertVerification(eq(1L), eq(25L), anyString(), any(LocalDateTime.class)))
                .thenReturn(1);

        String verification = service.verifyScan(10L, "raw-center-token");

        assertNotNull(verification);
        verify(attendanceQrMapper).insertVerification(eq(1L), eq(25L), anyString(), any(LocalDateTime.class));
    }

    @Test
    void rejectsExpiredOrUnknownCenterQr() {
        when(attendanceQrMapper.findValidQrTokenForUpdate(anyString(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.verifyScan(10L, "invalid-token"));

        verify(attendanceQrMapper, never()).insertVerification(anyLong(), anyLong(), anyString(), any());
    }

    @Test
    void checkInConsumesVerificationBeforeCallingExistingAttendanceService() {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(1);
        when(attendanceMapper.findActiveMemberIdByUserId(10L)).thenReturn(Optional.of(25L));
        when(attendanceQrMapper.findUsableVerificationForUpdate(anyString(), eq(25L), any(LocalDateTime.class)))
                .thenReturn(Optional.of(new AttendanceQrVerification(7L, 25L, "FITFLOW_MAIN", "센터", expiresAt)));
        when(attendanceQrMapper.consumeVerification(eq(7L), any(LocalDateTime.class))).thenReturn(1);

        service.checkIn(10L, "member-verification");

        verify(attendanceQrMapper).consumeVerification(eq(7L), any(LocalDateTime.class));
        verify(attendanceService).checkIn(25L);
    }

    @Test
    void rejectsAlreadyConsumedVerification() {
        when(attendanceMapper.findActiveMemberIdByUserId(10L)).thenReturn(Optional.of(25L));
        when(attendanceQrMapper.findUsableVerificationForUpdate(anyString(), eq(25L), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.checkIn(10L, "used-verification"));

        verify(attendanceService, never()).checkIn(anyLong());
    }

    @Test
    void exposesDurationsAndCurrentAttendanceState() {
        when(attendanceMapper.existsOpenAttendanceByUserId(10L)).thenReturn(true);

        assertEquals(30L, service.centerQrSeconds());
        assertEquals(120L, service.verificationSeconds());
        assertEquals(true, service.isCheckedIn(10L));
    }
}
