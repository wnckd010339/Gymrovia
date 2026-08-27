package com.acorn.gymmanagement.reservation.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.reservation.dto.request.ReservationSearchCondition;
import com.acorn.gymmanagement.reservation.dto.response.ReservationCalendarResponse;
import com.acorn.gymmanagement.reservation.dto.response.ReservationDetailResponse;
import com.acorn.gymmanagement.reservation.dto.response.ReservationOptionResponse;
import com.acorn.gymmanagement.reservation.dto.response.ReservationSummaryResponse;
import com.acorn.gymmanagement.reservation.form.ReservationForm;
import com.acorn.gymmanagement.reservation.mapper.ReservationMapper;
import com.acorn.gymmanagement.reservation.model.ReservationRegistration;
import com.acorn.gymmanagement.reservation.model.ReservationStatus;
import com.acorn.gymmanagement.reservation.model.ReservationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationMapper reservationMapper;

    public List<ReservationCalendarResponse> findCalendar(
            ReservationSearchCondition condition
    ) {
        return reservationMapper.findCalendar(condition);
    }

    public ReservationSummaryResponse findSummary(LocalDate date) {
        return reservationMapper.findSummary(date);
    }

    public ReservationDetailResponse findDetail(Long reservationId) {
        return reservationMapper.findById(reservationId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "예약을 찾을 수 없습니다."
                ));
    }

    public List<ReservationOptionResponse> findActiveMembers (
            String keyword
    ) {
        return reservationMapper.findActiveMembers(
                normalizeKeyword(keyword)
        );
    }

    public List<ReservationOptionResponse> findActiveTrainers() {
        return reservationMapper.findActiveTrainers();
    }

    @Transactional
    public void create(
            ReservationForm form,
            Long adminUserId
    ) {

        if (adminUserId == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "관리자 로그인 정보가 필요합니다."
            );
        }

        validatePeriod(
                form.getStartsAt(),
                form.getEndsAt()
        );
        validateReservationRequirements(form);
        validateTrainer(form.getTrainerId());
        validateTrainerConflict(
                form.getTrainerId(),
                form.getStartsAt(),
                form.getEndsAt(),
                null
        );

        CustomerInformation customer =
                resolveCustomerInformation(form);

        ReservationRegistration registration =
                new ReservationRegistration(
                        null,
                        form.getMemberId(),
                        form.getTrainerId(),
                        customer.name(),
                        customer.phone(),
                        form.getReservationType(),
                        ReservationStatus.PENDING,
                        form.getStartsAt(),
                        form.getEndsAt(),
                        normalizeMemo(form.getMemo()),
                        adminUserId
                );

        if (reservationMapper.insertReservation(registration) != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "예약 등록에 실패했습니다."
            );
        }
    }

    private CustomerInformation resolveCustomerInformation(
            ReservationForm form
    ) {
        if (form.getMemberId() != null) {
            ReservationOptionResponse member =
                    reservationMapper.findActiveMemberById(
                            form.getMemberId()
                    ).orElseThrow(() -> new BusinessException(
                            ErrorCode.NOT_FOUND,
                            "활성 회원을 찾을 수 없습니다."
                    ));

            return new CustomerInformation(
                    member.name(),
                    member.description()
            );
        }

        return new CustomerInformation(
                normalizeRequired(
                        form.getCustomerName(),
                        "고객 이름을 입력해 주세요."
                ),
                 normalizeRequired(
                         form.getCustomerPhone(),
                         "연락처를 입력해 주세요."
                 )
        );

    }

    private record CustomerInformation(
            String name,
            String phone
    ) {

    }

    private void validatePeriod(
            LocalDateTime startsAt,
            LocalDateTime endsAt
    ) {
        if (startsAt == null || endsAt == null) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "예약 시간을 입력해 주세요."
            );
        }

        if (!endsAt.isAfter(startsAt)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "종료 시간은 시작 시간보다 늦어야 합니다."
            );
        }

        if (Duration.between(startsAt, endsAt).toMinutes() > 240) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "예약 시간은 최대 4시간까지 설정할 수 있습니다."
            );
        }
    }

    private void validateTypeAndMember(ReservationForm form) {
        if (form.getReservationType()
                == ReservationType.REGULAR_PT
                && form.getMemberId() == null) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "정규 PT 예약은 회원을 선택해야 합니다."
            );
        }
    }

    private void validateTrainer(Long trainerId) {
        if (trainerId == null) {
            return;
        }

        if (!reservationMapper.existsActiveTrainer(trainerId)) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "활성 트레이너를 찾을 수 없습니다."
            );
        }
    }

    private void validateTrainerConflict(
            Long trainerId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            Long excludeReservationId
    ) {
        if (trainerId == null) {
            return ;
        }

        if (reservationMapper.existsTrainerConflict(
                trainerId,
                startsAt,
                endsAt,
                excludeReservationId
        )) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "선택한 시간에 트레이너의 다른 예약이 있습니다."
            );
        }
    }

    @Transactional
    public void update(
            Long reservationId,
            ReservationForm form
    ) {
        ReservationDetailResponse current =
                findDetail(reservationId);

        validateEditable(current.status());
        validatePeriod(form.getStartsAt(), form.getEndsAt());
        validateReservationRequirements(form);
        validateTrainer(form.getTrainerId());
        validateTrainerConflict(
                form.getTrainerId(),
                form.getStartsAt(),
                form.getEndsAt(),
                reservationId
        );

        CustomerInformation customer =
                resolveCustomerInformation(form);

        ReservationRegistration registration =
                new ReservationRegistration(
                        reservationId,
                        form.getMemberId(),
                        form.getTrainerId(),
                        customer.name(),
                        customer.phone(),
                        form.getReservationType(),
                        current.status(),
                        form.getStartsAt(),
                        form.getEndsAt(),
                        normalizeMemo(form.getMemo()),
                        null
                );

        if (reservationMapper.updateReservation(registration) != 1) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "예약 상태가 변경되어 수정하지 못했습니다."
            );
        }
    }

    @Transactional
    public void updateStatus(
            Long reservationId,
            ReservationStatus nextStatus,
            String cancellationReason
    ) {
        ReservationDetailResponse current =
                findDetail(reservationId);

        validateStatusTransition(
                current.status(),
                nextStatus
        );

        String normalizedReason =
                nextStatus == ReservationStatus.CANCELLED
                        ? normalizeCancellationReason(
                                cancellationReason
                )
                 : null;

        if (reservationMapper.updateStatus(
                reservationId,
                current.status(),
                nextStatus,
                normalizedReason
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "예약 상태를 변경하지 못했습니다."
            );
        }
    }

    private void validateStatusTransition(
            ReservationStatus current,
            ReservationStatus next
    ) {
        if (next == null) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "변경할 예약 상태를 선택해 주세요."
            );
        }

        boolean allowed = switch(current) {
            case PENDING ->
                next == ReservationStatus.CONFIRMED
                        || next == ReservationStatus.CANCELLED;

            case CONFIRMED ->
                next == ReservationStatus.COMPLETED
                        || next == ReservationStatus.CANCELLED
                        || next == ReservationStatus.NO_SHOW;

            case COMPLETED, CANCELLED, NO_SHOW -> false;
        };

        if(!allowed) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "현재 상태에서는 요청한 상태로 변경할 수 없습니다."
            );
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeMemo(String memo) {
        if (memo == null) {
            return null;
        }

        String normalized = memo.trim();
        return normalized.isEmpty() ? null : normalized;
    }



    private String normalizeRequired(
            String value,
            String errorMessage
    ) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    errorMessage
            );
        }

        return value.trim();
    }

    private void validateEditable(
            ReservationStatus status
    ) {
        if (status == ReservationStatus.COMPLETED
              || status == ReservationStatus.CANCELLED
              || status == ReservationStatus.NO_SHOW) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "완료·취소 노쇼 처리된 예약은 수정할 수 없습니다."
            );
        }
    }

    private void validateReservationRequirements(
            ReservationForm form
    ) {
        ReservationType type = form.getReservationType();

        if (type == null) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "예약 종류를 선택해 주세요."
            );
        }

        if (type == ReservationType.REGULAR_PT
                  && form.getMemberId() == null) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "정규 PT 예약은 히원을 선택해야 합니다."
            );
        }

        if (type != ReservationType.CONSULTATION
                 && form.getTrainerId() == null) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "PT 예약은 담당 트레이너를 선택해야 합니다."
            );
        }
    }

    private String normalizeCancellationReason(
            String cancellationReason
    ){
        if (cancellationReason == null
                || cancellationReason.isBlank()) {
            return null;
        }

        String normalized = cancellationReason.trim();

        if (normalized.length() > 500) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "취소 사유는 500자 이하여야 합니다."
            );
        }

        return normalized;
    }
}


