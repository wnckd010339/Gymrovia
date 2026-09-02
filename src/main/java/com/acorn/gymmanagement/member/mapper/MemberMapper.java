package com.acorn.gymmanagement.member.mapper;

import com.acorn.gymmanagement.common.pagination.PageRequest;
import com.acorn.gymmanagement.member.dto.request.MemberSearchRequest;
import com.acorn.gymmanagement.member.dto.response.*;
import com.acorn.gymmanagement.member.model.Member;
import com.acorn.gymmanagement.member.model.MemberRegistration;
import com.acorn.gymmanagement.member.model.MemberStatus;
import com.acorn.gymmanagement.member.model.MemberUpdate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberMapper {

    List<MemberListResponse> search(
            @Param("condition") MemberSearchRequest condition,
            @Param("page")PageRequest pageRequest
            );

    long count(@Param("condition") MemberSearchRequest condition);

    MemberStatusSummaryResponse findStatusSummary();

    List<Member> findAll();

    Optional<Member> findById(Long id);

    int insert(Member member);

    int update(Member member);

    Optional<MemberDetailResponse> findDetailById(
            @Param("memberId") Long memberId
    );

    Optional<CurrentMembershipResponse> findCurrentMembership(
            @Param("memberId") Long memberId
    );

    List<MemberActivityResponse> findRecentActivities(
            @Param("memberId") Long memberId
    );

    boolean existsByLoginId(@Param("loginId") String loginId);

    int insertUser(MemberRegistration registration);
    int insertLocalCredential(MemberRegistration registration);
    int insertRegisteredMember(MemberRegistration registration);

    int updateBasicInformation(MemberUpdate update);

    int updateUserStatus(
            @Param("memberId") Long memberId,
            @Param("status") MemberStatus status
    );

    Optional<MemberHomeSummaryResponse> findHomeSummaryByUserId(
            @Param("userId") Long userId
    );

    Optional<MemberHomeAttendanceResponse> findOpenAttendanceByUserId(
            @Param("userId") Long userId
    );

    Optional<MemberHomeTrainerResponse> findHomeTrainerByUserId(
            @Param("userId") Long userId
    );

    Optional<MemberHomeRoutineResponse> findHomeRoutineByUserId(
            @Param("userId") Long userId
    );

    List<MemberHomeExerciseResponse> findTodayRoutineExercises(
            @Param("routineId") Long routineId
    );

    List<MemberHomeWorkoutResponse> findRecentHomeWorkoutsByUserId(
            @Param("userId") Long userId
    );
}
