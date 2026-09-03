package com.acorn.gymmanagement.attendance.service;

import com.acorn.gymmanagement.attendance.dto.request.AttendanceSearchCondition;
import com.acorn.gymmanagement.attendance.dto.response.AttendanceListResponse;
import com.acorn.gymmanagement.attendance.dto.response.AttendanceSummaryResponse;
import com.acorn.gymmanagement.attendance.form.AttendanceRegistration;
import com.acorn.gymmanagement.attendance.mapper.AttendanceMapper;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.acorn.gymmanagement.common.time.CenterTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {
    private final AttendanceMapper attendanceMapper;

    public AttendanceSummaryResponse getSummary(LocalDate date) {
        return attendanceMapper.findSummary(date);
    }

    public List<AttendanceListResponse> findCurrentAttendances(AttendanceSearchCondition condition) {
        return attendanceMapper.findCurrentAttendances(condition);
    }

    public List<AttendanceListResponse> findHistory(AttendanceSearchCondition condition) {
        return attendanceMapper.findHistory(condition);
    }

    @Transactional
    public void checkout(Long attendanceId) {
        AttendanceListResponse attendance = attendanceMapper.findOpenAttendanceForUpdate(attendanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "퇴실 처리할 입장 기록을 찾을 수 없습니다."));
        completeCheckout(attendance.attendanceId());
    }

    @Transactional
    public void checkoutMember(Long userId) {
        Long memberId = findActiveMemberId(userId);
        AttendanceListResponse attendance = attendanceMapper.findOpenAttendanceForMemberForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "체크아웃할 출석 기록이 없습니다."));
        completeCheckout(attendance.attendanceId());
    }

    private void completeCheckout(Long attendanceId) {
        if (attendanceMapper.checkout(attendanceId, CenterTime.now(), LocalDateTime.now()) != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "출석 상태가 변경되어 퇴실 처리하지 못했습니다.");
        }
    }

    @Transactional
    public void checkIn(Long memberId) {
        LocalDateTime checkedInAt = CenterTime.now();
        LocalDate today = checkedInAt.toLocalDate();
        if (attendanceMapper.findActiveMemberForUpdate(memberId).isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "활성 회원을 찾을 수 없습니다.");
        }
        if (!attendanceMapper.existsUsableMembership(memberId, today)) {
            throw new BusinessException(ErrorCode.CONFLICT, "사용 가능한 회원권이 없습니다.");
        }
        if (attendanceMapper.existsOpenAttendance(memberId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 입장 처리된 회원입니다.");
        }
        AttendanceRegistration registration = new AttendanceRegistration(memberId, today, checkedInAt);
        if (attendanceMapper.insertAttendance(registration) != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "입장 처리에 실패했습니다.");
        }
    }

    @Transactional
    public void checkInMember(Long userId) {
        checkIn(findActiveMemberId(userId));
    }

    private Long findActiveMemberId(Long userId) {
        return attendanceMapper.findActiveMemberIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "활성 회원 정보를 찾을 수 없습니다."));
    }
}
