package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.membership.mapper.MembershipMapper;
import com.acorn.gymmanagement.membership.model.MembershipStatus;
import com.acorn.gymmanagement.payment.dto.request.CreatePaymentRequest;
import com.acorn.gymmanagement.payment.dto.response.*;
import com.acorn.gymmanagement.payment.mapper.PaymentMapper;
import com.acorn.gymmanagement.payment.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final MembershipMapper membershipMapper;

    public List<PaymentMemberOptionResponse> findActiveMembers() {

        return paymentMapper.findActiveMembers();
    }

    public List<PaymentHistoryResponse> findHistory(Long memberId) {
        if (memberId != null && !membershipMapper.existsMemberById(memberId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다.");
        }
        return paymentMapper.findHistory(memberId);
    }

    @Transactional
    public PaymentResponse completeMembershipPayment(
            Long membershipId,
            CreatePaymentRequest request
    ) {
        PaymentTargetResponse target = paymentMapper
                .findPaymentTargetForUpdate(membershipId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "결제할 회원권을 찾을 수 없습니다."
                ));

        if (target.membershipStatus() != MembershipStatus.PENDING_PAYMENT) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "결제 대기 상태의 회원권만 결제할 수 있습니다."
            );
        }

        if (target.price().signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "0원 상품은 결제 없이 활성화해야 합니다."
            );
        }

        if (paymentMapper.existsCompletedPaymentByMembershipId(membershipId)) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "이미 결제된 회원권입니다."
            );
        }

        PaymentRegistration registration = new PaymentRegistration(
                target.memberId(),
                membershipId,
                target.price(),
                request.paymentMethod(),
                PaymentStatus.COMPLETED,
                LocalDateTime.now()
        );

        validateAffectedRows(
                paymentMapper.insertPayment(registration),
                "결제 내역 저장에 실패했습니다."
        );

        if (membershipMapper.activateAfterPayment(target.memberId(), membershipId) != 1) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "회원권 상태가 변경되어 결제를 완료할 수 없습니다."
            );
        }

        return findPayment(registration.getPaymentId());
    }

    private PaymentResponse findPayment(Long paymentId) {
        return paymentMapper.findById(paymentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INTERNAL_ERROR,
                        "저장된 결제 내역을 조회하지 못했습니다."
                ));
    }

    private void validateAffectedRows(int affectedRows, String message) {
        if (affectedRows != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, message);
        }
    }
}
