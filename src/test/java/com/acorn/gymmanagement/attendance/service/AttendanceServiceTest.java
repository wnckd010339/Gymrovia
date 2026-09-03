package com.acorn.gymmanagement.attendance.service;

import com.acorn.gymmanagement.attendance.dto.response.AttendanceListResponse;
import com.acorn.gymmanagement.attendance.mapper.AttendanceMapper;
import com.acorn.gymmanagement.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceMapper attendanceMapper;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(attendanceMapper);
    }

    @Test
    void memberCheckInResolvesMemberIdFromLoggedInUserId() {
        when(attendanceMapper.findActiveMemberIdByUserId(10L)).thenReturn(Optional.of(25L));
        when(attendanceMapper.findActiveMemberForUpdate(25L)).thenReturn(Optional.of(25L));
        when(attendanceMapper.existsUsableMembership(any(), any())).thenReturn(true);
        when(attendanceMapper.existsOpenAttendance(25L)).thenReturn(false);
        when(attendanceMapper.insertAttendance(any())).thenReturn(1);

        attendanceService.checkInMember(10L);

        verify(attendanceMapper).findActiveMemberForUpdate(25L);
        verify(attendanceMapper).existsUsableMembership(org.mockito.ArgumentMatchers.eq(25L), any(LocalDate.class));
        verify(attendanceMapper).insertAttendance(any());
    }

    @Test
    void memberCheckoutOnlyClosesTheLoggedInMembersOpenAttendance() {
        AttendanceListResponse attendance = new AttendanceListResponse(
                77L, 25L, "회원", "010-0000-0000", "자유 이용권",
                LocalDate.now(), LocalDateTime.now().minusHours(1), null, 60L, "IN_CENTER"
        );
        when(attendanceMapper.findActiveMemberIdByUserId(10L)).thenReturn(Optional.of(25L));
        when(attendanceMapper.findOpenAttendanceForMemberForUpdate(25L)).thenReturn(Optional.of(attendance));
        when(attendanceMapper.checkout(org.mockito.ArgumentMatchers.eq(77L), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1);

        attendanceService.checkoutMember(10L);

        verify(attendanceMapper).findOpenAttendanceForMemberForUpdate(25L);
        verify(attendanceMapper).checkout(org.mockito.ArgumentMatchers.eq(77L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void memberWithoutActiveMembershipCannotCheckIn() {
        when(attendanceMapper.findActiveMemberIdByUserId(10L)).thenReturn(Optional.of(25L));
        when(attendanceMapper.findActiveMemberForUpdate(25L)).thenReturn(Optional.of(25L));
        when(attendanceMapper.existsUsableMembership(any(), any())).thenReturn(false);

        assertThrows(BusinessException.class, () -> attendanceService.checkInMember(10L));

        verify(attendanceMapper, never()).insertAttendance(any());
    }
}
