package com.acorn.gymmanagement.membership.service;

import com.acorn.gymmanagement.membership.dto.response.MemberMembershipResponse;
import com.acorn.gymmanagement.membership.mapper.MembershipMapper;
import com.acorn.gymmanagement.membership.model.MembershipProductType;
import com.acorn.gymmanagement.membership.model.MembershipStatus;
import com.acorn.gymmanagement.payment.mapper.PaymentOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipCancellationServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long MEMBERSHIP_ID = 10L;

    @Mock
    private MembershipMapper membershipMapper;

    @Mock
    private PaymentOrderMapper paymentOrderMapper;

    private MembershipService membershipService;

    @BeforeEach
    void setUp() {
        membershipService = new MembershipService(
                membershipMapper,
                paymentOrderMapper
        );
    }

    @Test
    void adminCancellationCancelsReadyOrderAndMembership() {
        MemberMembershipResponse pending =
                membership(MembershipStatus.PENDING_PAYMENT);

        MemberMembershipResponse cancelled =
                membership(MembershipStatus.CANCELLED);

        when(membershipMapper.findById(
                MEMBER_ID,
                MEMBERSHIP_ID
        ))
                .thenReturn(Optional.of(pending))
                .thenReturn(Optional.of(cancelled));

        when(paymentOrderMapper
                .cancelReadyOrdersForMembership(
                        MEMBER_ID,
                        MEMBERSHIP_ID
                ))
                .thenReturn(1);

        when(membershipMapper.cancelPendingMembership(
                MEMBER_ID,
                MEMBERSHIP_ID
        )).thenReturn(1);

        MemberMembershipResponse result =
                membershipService.cancel(
                        MEMBER_ID,
                        MEMBERSHIP_ID
                );

        assertEquals(
                 MembershipStatus.CANCELLED,
                result.status()
        );

        InOrder inOrder = inOrder(
                paymentOrderMapper,
                membershipMapper
        );

        inOrder.verify(paymentOrderMapper)
                .cancelReadyOrdersForMembership(
                        MEMBER_ID,
                        MEMBERSHIP_ID
                );

        inOrder.verify(membershipMapper)
                .cancelPendingMembership(
                        MEMBER_ID,
                        MEMBERSHIP_ID
                );
    }

    private MemberMembershipResponse membership(
            MembershipStatus status
    ) {
        return new MemberMembershipResponse(
                MEMBERSHIP_ID,
                3L,
                "1개월 자유 이용권",
                MembershipProductType.GYM,
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 10, 9),
                0,
                0,
                status,
                LocalDateTime.of(
                        2026, 9, 2, 10, 0
                )
        );
    }
}

