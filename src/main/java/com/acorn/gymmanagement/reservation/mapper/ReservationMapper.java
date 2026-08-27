package com.acorn.gymmanagement.reservation.mapper;

import com.acorn.gymmanagement.reservation.dto.request.ReservationSearchCondition;
import com.acorn.gymmanagement.reservation.dto.response.ReservationCalendarResponse;
import com.acorn.gymmanagement.reservation.dto.response.ReservationDetailResponse;
import com.acorn.gymmanagement.reservation.dto.response.ReservationOptionResponse;
import com.acorn.gymmanagement.reservation.dto.response.ReservationSummaryResponse;
import com.acorn.gymmanagement.reservation.model.ReservationRegistration;
import com.acorn.gymmanagement.reservation.model.ReservationStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ReservationMapper {

    List<ReservationCalendarResponse> findCalendar(
            ReservationSearchCondition condition
    );

    ReservationSummaryResponse findSummary(
            @Param("date") LocalDate date
    );

    Optional<ReservationDetailResponse> findById(
            @Param("reservationId") Long reservationId
    );

    List<ReservationOptionResponse> findActiveMembers(
            @Param("keyword") String keyword
    );

    List<ReservationOptionResponse> findActiveTrainers();

    Optional<ReservationOptionResponse> findActiveMemberById(
            @Param("memberId") Long memberId
    );

    boolean existsTrainerConflict(
            @Param("trainerId") Long trainerId,
            @Param("startsAt") LocalDateTime startsAt,
            @Param("endsAt") LocalDateTime endsAt,
            @Param("excludeReservationId") Long excludeReservationId
    );

    int insertReservation(ReservationRegistration registration);

    int updateReservation(ReservationRegistration registration);

    int updateStatus(
            @Param("reservationId") Long reservationId,
            @Param("currentStatus") ReservationStatus currentStatus,
            @Param("nextStatus") ReservationStatus nextStatus,
            @Param("cancellationReason") String cancellationReason
    );


    boolean existsActiveTrainer(
            @Param("trainerId") Long trainerId
    );
}
