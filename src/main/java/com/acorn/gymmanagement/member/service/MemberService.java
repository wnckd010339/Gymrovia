package com.acorn.gymmanagement.member.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.common.pagination.PageRequest;
import com.acorn.gymmanagement.common.pagination.PageResult;
import com.acorn.gymmanagement.member.dto.request.CreateMemberRequest;
import com.acorn.gymmanagement.member.dto.request.MemberSearchRequest;
import com.acorn.gymmanagement.member.dto.request.UpdateMemberRequest;
import com.acorn.gymmanagement.member.dto.response.*;
import com.acorn.gymmanagement.member.mapper.MemberMapper;
import com.acorn.gymmanagement.member.model.MemberRegistration;
import com.acorn.gymmanagement.member.model.MemberUpdate;
import com.acorn.gymmanagement.member.view.MemberDetailView;
import com.acorn.gymmanagement.member.view.MemberHomeView;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.acorn.gymmanagement.common.time.CenterTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private static final int WEEKLY_WORKOUT_GOAL = 4;

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberHomeView findHomeView(Long userId) {
        MemberHomeSummaryResponse summary = memberMapper.findHomeSummaryByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "회원 정보를 찾을 수 없습니다."
                ));

        MemberHomeRoutineResponse routine = memberMapper.findHomeRoutineByUserId(userId)
                .orElse(null);

        List<MemberHomeExerciseResponse> exercises = routine == null
                ? List.of()
                : memberMapper.findTodayRoutineExercises(routine.routineId());

        return new MemberHomeView(
                CenterTime.today(),
                WEEKLY_WORKOUT_GOAL,
                summary,
                memberMapper.findOpenAttendanceByUserId(userId).orElse(null),
                memberMapper.findHomeTrainerByUserId(userId).orElse(null),
                routine,
                exercises,
                memberMapper.findRecentHomeWorkoutsByUserId(userId)
        );
    }


    public PageResult<MemberListResponse> search(
            MemberSearchRequest condition,
            PageRequest pageRequest
    ){
        List<MemberListResponse> members =
                memberMapper.search(condition, pageRequest);

        long totalElements =
                memberMapper.count(condition);

        return PageResult.of(
                members,
                totalElements,
                pageRequest
        );
    }

    public MemberStatusSummaryResponse findStatusSummary() {
        return memberMapper.findStatusSummary();
    }

    public MemberDetailResponse findDetailResponseById(Long memberId){
        return memberMapper.findDetailById(memberId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "회원을 찾을 수 없습니다."
                ));
    }

    public MemberDetailView findDetailView(Long memberId){
        MemberDetailResponse member = memberMapper.findDetailById(memberId)
                .orElseThrow( () -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "회원을 찾을 수 없습니다."
                ));

        CurrentMembershipResponse membership =
                memberMapper.findCurrentMembership(memberId)
                        .orElse(null);

        List<MemberActivityResponse> activities =
                memberMapper.findRecentActivities(memberId);

        return new MemberDetailView(
                member,
                membership,
                activities
        );
    }

    @Transactional
    public CreateMemberResponse create(
            CreateMemberRequest request
    ) {
        String loginId = normalizeLoginId(
                request.loginId()
        );

        if(memberMapper.existsByLoginId(loginId)){
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "이미 사용 중인 로그인 ID입니다."
            );
        }

        String passwordHash =
                passwordEncoder.encode(
                        request.initialPassword()
                );

        MemberRegistration registration =
                new MemberRegistration(
                        loginId,
                        passwordHash,
                        request.name().trim(),
                        normalizePhone(request.phone()),
                        request.birthDate(),
                        request.gender(),
                        request.trainerRequested()
                );

        validateInsert(
                memberMapper.insertUser(registration),
                "사용자 계쩡 생성에 실패했습니다."
        );

        try {
            validateInsert (
                    memberMapper.insertLocalCredential(registration),
                    "로그인 정보 생성에 실패했습니다."
            );
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "이미 사용 중인 로그인 ID입니다."
            );
        }

        validateInsert(
                memberMapper.insertRegisteredMember(registration),
                "회원 정보 생성에 실패했습니다."
        );

        return new CreateMemberResponse(
                registration.getMemberId(),
                registration.getUserId(),
                registration.getLoginId(),
                registration.getName(),
                "ACTIVE"
        );


    }

    private String normalizeLoginId(String loginId){
        return loginId.trim().toLowerCase();
    }

    private String normalizePhone(String phone) {
        String digits =
                phone.replaceAll("[^0-9]", "");

        if (digits.length() == 11){
            return digits.replaceFirst(
                    "(\\d{3})(\\d{4})(\\d{4})",
                    "$1-$2-$3"
            );
        }

        if(digits.length() == 10){
            return digits.replaceFirst(
                    "(\\d{3})(\\d{3})(\\d{4})",
                    "$1-$2-$3"
            );
        }

        return phone.trim();
    }

    private void validateInsert(
            int affectedRows,
            String message
    ) {
        if (affectedRows != 1){
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    message
            );
        }
    }

    @Transactional
    public MemberDetailResponse updateBasicInformation(
            Long memberId,
            UpdateMemberRequest request
    ) {
        memberMapper.findById(memberId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "회원을 찾을 수 없습니다."
                ));

        MemberUpdate update =new MemberUpdate(
                memberId,
                request.name().trim(),
                normalizePhone(request.phone()),
                request.birthDate(),
                request.gender(),
                request.status()
        );

        validateUpdate(
                memberMapper.updateBasicInformation(update),
                "회원 기본 정보 수집에 실패했습니다."
        );

        validateUpdate(
                memberMapper.updateUserStatus(memberId, request.status()),
                "회원 계정 상태 수정에 실패했습니다."
        );

        return findDetailResponseById(memberId);
    }

    private void validateUpdate(int affectedRows, String message){
        if(affectedRows != 1){
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    message
            );
        }
    }
}
