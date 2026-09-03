package com.acorn.gymmanagement.payment.mapper;

import com.acorn.gymmanagement.payment.model.ExpiredPaymentOrderTarget;
import com.acorn.gymmanagement.payment.model.PaymentOrder;
import com.acorn.gymmanagement.payment.model.PaymentOrderRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PaymentOrderMapper {

    int insert(PaymentOrderRegistration registration);

    Optional<PaymentOrder> findByOrderIdForUpdate(
            @Param("orderId") String orderId,
            @Param("userId") Long userId
    );

    int markApproving(
            @Param("id") Long id,
            @Param("paymentKey") String paymentKey,
            @Param("now") LocalDateTime now
    );

    int markPaid(
            @Param("id") Long id,
            @Param("paymentId") Long paymentId,
            @Param("approvedAt") LocalDateTime approvedAt
    );

    int markFailed(
            @Param("id") Long id,
            @Param("failureCode") String failureCode,
            @Param("failureMessage") String failureMessage
    );

    int cancelReadyOrdersForMembership(
            @Param("memberId") Long memberId,
            @Param("membershipId") Long membershipId
    );

    List<ExpiredPaymentOrderTarget> findExpiredReadyOrdersForUpdate(
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );

    List<ExpiredPaymentOrderTarget> findExpiredReadyOrdersByUserIdForUpdate(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    int markExpired(
            @Param("paymentOrderId") Long paymentOrderId
    );

    int markCompensating(
            @Param("id") Long id,
            @Param("idempotencyKey") String idempotencyKey,
            @Param("failureCode") String failureCode,
            @Param("failureMessage") String failureMessage
    );

    int markCompensated(
            @Param("id") Long id,
            @Param("transactionKey") String transactionKey,
            @Param("compensatedAt") LocalDateTime compensatedAt
    );

    int markReconciliationRequired(
            @Param("id") Long id,
            @Param("failureCode") String failureCode,
            @Param("failureMessage") String failureMessage
    );
}
