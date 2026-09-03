package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.membership.model.PendingMembershipPaymentTarget;
import com.acorn.gymmanagement.membership.service.MembershipService;
import com.acorn.gymmanagement.payment.dto.request.CreateMemberPaymentOrderRequest;
import com.acorn.gymmanagement.payment.dto.request.ConfirmMemberPaymentOrderRequest;
import com.acorn.gymmanagement.payment.dto.response.MemberPaymentConfirmationResponse;
import com.acorn.gymmanagement.payment.dto.response.PaymentOrderResponse;
import com.acorn.gymmanagement.payment.gateway.PaymentCancellationResult;
import com.acorn.gymmanagement.payment.mapper.PaymentOrderMapper;
import com.acorn.gymmanagement.payment.model.PaymentOrderRegistration;
import com.acorn.gymmanagement.payment.model.PaymentOrderStatus;
import com.acorn.gymmanagement.payment.gateway.PaymentApprovalResult;
import com.acorn.gymmanagement.payment.gateway.PaymentGateway;
import com.acorn.gymmanagement.payment.gateway.PaymentGatewayException;
import com.acorn.gymmanagement.payment.model.PaymentApprovalCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberPaymentOrderService {

    private static final Duration ORDER_EXPIRATION = Duration.ofMinutes(10);

    private static final String PG_PROVIDER =
            "TOSS_PAYMENTS";

    private final MembershipService membershipService;
    private final PaymentOrderMapper paymentOrderMapper;
    private final PaymentOrderExpirationService paymentOrderExpirationService;
    private final PaymentGateway paymentGateway;
    private final PaymentOrderTransactionService transactionService;

    @Transactional
    public PaymentOrderResponse create(
            Long userId,
            CreateMemberPaymentOrderRequest request
    ){
        paymentOrderExpirationService.expireForMember(
                userId,
                LocalDateTime.now()
        );

        PendingMembershipPaymentTarget target =
                membershipService.createPendingForMember(
                        userId,
                        request.productId(),
                        request.startDate()
                );

        String orderId = createOrderId();
        String idempotencyKey = UUID.randomUUID().toString();
        OffsetDateTime expiresAt =
                OffsetDateTime.now().plus(ORDER_EXPIRATION);

        PaymentOrderRegistration registration =
                new PaymentOrderRegistration(
                        orderId,
                        target.memberId(),
                        target.membershipId(),
                        PG_PROVIDER,
                        target.price(),
                        PaymentOrderStatus.READY,
                        idempotencyKey,
                        expiresAt.toLocalDateTime()
                );

        int affectedRows =
                paymentOrderMapper.insert(registration);

        if(affectedRows != 1){
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "결제 주문 저장에 실패했습니다."
            );
        }

        return new PaymentOrderResponse(
                orderId,
                target.productName(),
                target.price(),
                expiresAt
        );
    }

    private String createOrderId(){
        return "FITFLOW-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "");
    }

    public MemberPaymentConfirmationResponse confirm(
            Long userId,
            String orderId,
            ConfirmMemberPaymentOrderRequest request
    ) {
       PaymentApprovalCommand command =
               transactionService.prepareApproval(
                       userId,
                       orderId,
                       request.paymentKey(),
                       request.amount()
               );

       PaymentApprovalResult approvalResult;

       try {
           approvalResult =
                   paymentGateway.confirm(
                           command.paymentKey(),
                           command.orderId(),
                           command.amount(),
                           command.idempotencyKey()
                   );
       } catch (PaymentGatewayException exception) {
           transactionService.failApproval(
                   command.paymentOrderId(),
                   exception.getCode(),
                   exception.getMessage()
           );

           throw exception;
       }

       try {
           return transactionService.completeApproval(
                   command,
                   approvalResult
           );
       } catch (RuntimeException localException) {
           compensateApprovedPayment(
                   command,
                   localException
           );

           throw localException;
       }



    }

    private void compensateApprovedPayment(
            PaymentApprovalCommand command,
            RuntimeException localException
    ) {
        try {
            transactionService.prepareCompensation(
                    command,
                    localException
            );
        } catch (RuntimeException prepareationException) {
            localException.addSuppressed(
                    prepareationException
            );

            throw localException;
        }

        try {
            PaymentCancellationResult cancellationResult =
                    paymentGateway.cancel(
                            command.paymentKey(),
                            command.amount(),
                            "결제 승인 후 내부 처리 실패로 인한 자동 취소",
                            command.compensationIdempotencyKey()
                    );

            transactionService.completeCompensation(
                    command,
                    cancellationResult
            );
        } catch (PaymentGatewayException cancellationException) {
            transactionService.requireReconciliation(
                    command.paymentOrderId(),
                    cancellationException.getCode(),
                    cancellationException.getMessage()
            );

            localException.addSuppressed(
                    cancellationException
            );
        } catch (RuntimeException compensationException) {
            transactionService.requireReconciliation(
                    command.paymentOrderId(),
                    "COMPENSATION_RESULT_ERROR",
                    compensationException.getMessage()
            );

            localException.addSuppressed(
                    compensationException
            );
        }
    }




}
