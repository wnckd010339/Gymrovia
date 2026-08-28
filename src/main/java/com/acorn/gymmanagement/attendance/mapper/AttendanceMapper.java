package com.acorn.gymmanagement.attendance.mapper;

import com.acorn.gymmanagement.attendance.dto.request.AttendanceSearchCondition;
import com.acorn.gymmanagement.attendance.dto.response.AttendanceListResponse;
import com.acorn.gymmanagement.attendance.dto.response.AttendanceSummaryResponse;
import com.acorn.gymmanagement.attendance.form.AttendanceRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceMapper {
    AttendanceSummaryResponse findSummary(LocalDate date);
    List<AttendanceListResponse> findCurrentAttendances(AttendanceSearchCondition condition);
    List<AttendanceListResponse> findHistory(AttendanceSearchCondition condition);
    Optional<AttendanceListResponse> findOpenAttendanceForUpdate(Long attendanceId);
    Optional<AttendanceListResponse> findOpenAttendanceForMemberForUpdate(Long memberId);
    int checkout(@Param("attendanceId") Long attendanceId, @Param("checkedOutAt") LocalDateTime checkedOutAt);

    Optional<Long> findActiveMemberIdByUserId(Long userId);

    Optional<Long> findActiveMemberForUpdate(Long memberId);

    boolean existsUsableMembership(
            @Param("memberId") Long memberId,
            @Param("date") LocalDate date
    );

    boolean existsOpenAttendance(Long memberId);

    boolean existsOpenAttendanceByUserId(Long userId);

    int insertAttendance(AttendanceRegistration registration);
}
