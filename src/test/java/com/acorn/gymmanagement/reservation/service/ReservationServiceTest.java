package com.acorn.gymmanagement.reservation.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.reservation.form.ReservationForm;
import com.acorn.gymmanagement.reservation.mapper.ReservationMapper;
import com.acorn.gymmanagement.reservation.model.ReservationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    private ReservationMapper reservationMapper;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationMapper = mock(ReservationMapper.class);
        reservationService = new ReservationService(reservationMapper);
    }

    @Test
    void locksTrainerBeforeCheckingReservationConflict() {
        Long trainerId = 10L;
        ReservationForm form = reservationForm(trainerId);

        when(reservationMapper.lockActiveTrainer(trainerId))
                .thenReturn(trainerId);
        when(reservationMapper.existsTrainerConflict(
                trainerId,
                form.getStartsAt(),
                form.getEndsAt(),
                null
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService.create(form, 1L)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());

        InOrder inOrder = inOrder(reservationMapper);
        inOrder.verify(reservationMapper).lockActiveTrainer(trainerId);
        inOrder.verify(reservationMapper).existsTrainerConflict(
                trainerId,
                form.getStartsAt(),
                form.getEndsAt(),
                null
        );

        verify(reservationMapper, never()).insertReservation(any());
    }

    @Test
    void rejectsInactiveTrainerWithoutCheckingConflict() {
        Long trainerId = 10L;
        ReservationForm form = reservationForm(trainerId);

        when(reservationMapper.lockActiveTrainer(trainerId))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService.create(form, 1L)
        );

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        verify(reservationMapper, never()).existsTrainerConflict(
                anyLong(),
                any(),
                any(),
                any()
        );
        verify(reservationMapper, never()).insertReservation(any());
    }

    private ReservationForm reservationForm(Long trainerId) {
        ReservationForm form = new ReservationForm();
        form.setMemberId(1L);
        form.setTrainerId(trainerId);
        form.setReservationType(ReservationType.REGULAR_PT);
        form.setStartsAt(LocalDateTime.of(2026, 9, 10, 14, 0));
        form.setEndsAt(LocalDateTime.of(2026, 9, 10, 15, 0));
        return form;
    }
}
