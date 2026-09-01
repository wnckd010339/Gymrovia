package com.acorn.gymmanagement.payment.mapper;
import com.acorn.gymmanagement.payment.dto.response.*;
import com.acorn.gymmanagement.payment.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Mapper
public interface PaymentMapper {

    Optional<PaymentTargetResponse> findPaymentTargetForUpdate(
            @Param("membershipId") Long membershipId
    );

    boolean existsCompletedPaymentByMembershipId(
            @Param("membershipId") Long membershipId
    );

    int insertPayment(PaymentRegistration registration);

    Optional<PaymentResponse> findById(
            @Param("paymentId") Long paymentId
    );

    Optional<Payment> findPaymentForUpdate(
            @Param("paymentId") Long paymentId
    );

    List<PaymentHistoryResponse> findHistory(
            @Param("memberId") Long memberId
    );

    List<PaymentMemberOptionResponse> findActiveMembers();

    int insertRefund(RefundRegistration registration);

    Optional<RefundResponse> findRefundById(
            @Param("refundId") Long refundId
    );

    Optional<RefundPaymentTarget> findRefundTargetForUpdate(
            @Param("paymentId") Long paymentId
    );

    boolean existsPendingRefundByPaymentId(
            @Param("paymentId") Long paymentId
    );

    int completeRefund(
            @Param("refundId") Long refundId,
            @Param("transactionKey") String transactionKey,
            @Param("refundedAt") LocalDateTime refundedAt
    );

    int rejectRefund(
            @Param("refundId") Long refundId,
            @Param("failureCode") String failureCode,
            @Param("failureMessage") String failureMessage
    );

    int updatePaymentStatus(
            @Param("paymentId") Long paymentId,
            @Param("status") PaymentStatus status
    );
}
