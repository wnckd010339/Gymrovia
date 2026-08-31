package com.acorn.gymmanagement.payment.service;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.membership.model.PendingMembershipPaymentTarget;
import com.acorn.gymmanagement.membership.service.MembershipService;
import com.acorn.gymmanagement.payment.dto.request.CreateMemberPaymentOrderRequest;
import com.acorn.gymmanagement.payment.dto.request.ConfirmMemberPaymentOrderRequest;
import com.acorn.gymmanagement.payment.dto.response.MemberPaymentConfirmationResponse;
import com.acorn.gymmanagement.payment.dto.response.PaymentOrderResponse;
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
        LocalDateTime expiresAt =
                LocalDateTime.now().plus(ORDER_EXPIRATION);

        PaymentOrderRegistration registration =
                new PaymentOrderRegistration(
                        orderId,
                        target.memberId(),
                        target.membershipId(),
                        PG_PROVIDER,
                        target.price(),
                        PaymentOrderStatus.READY,
                        idempotencyKey,
                        expiresAt
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

       try {
           PaymentApprovalResult result =
                   paymentGateway.confirm(
                           command.paymentKey(),
                           command.orderId(),
                           command.amount(),
                           command.idempotencyKey()
                   );

           return  transactionService.completeApproval(
                   command,
                   result
           );
       } catch (PaymentGatewayException exception) {
           transactionService.failApproval(
                   command.paymentOrderId(),
                   exception.getCode(),
                   exception.getMessage()
           );

           throw exception;
       } catch (RuntimeException exception) {
           transactionService.failApproval(
                   command.paymentOrderId(),
                   "INTERNAL_APPROVAL_ERROR",
                   "결제 승인 결과를 처리하지 못했습니다."
           );

           throw exception;
       }

    }




}
