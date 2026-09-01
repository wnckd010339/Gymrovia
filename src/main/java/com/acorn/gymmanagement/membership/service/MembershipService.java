package com.acorn.gymmanagement.membership.service;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.membership.dto.request.CreateMemberMembershipRequest;
import com.acorn.gymmanagement.membership.dto.request.MembershipProductRequest;
import com.acorn.gymmanagement.membership.dto.response.MemberMembershipResponse;
import com.acorn.gymmanagement.membership.dto.response.MembershipProductOptionResponse;
import com.acorn.gymmanagement.membership.mapper.MembershipMapper;
import com.acorn.gymmanagement.membership.model.MemberMembershipRegistration;
import com.acorn.gymmanagement.membership.model.MembershipProduct;
import com.acorn.gymmanagement.membership.model.MembershipStatus;
import com.acorn.gymmanagement.membership.model.PendingMembershipPaymentTarget;
import com.acorn.gymmanagement.payment.mapper.PaymentOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipService {

    private final MembershipMapper membershipMapper;
    private final PaymentOrderMapper paymentOrderMapper;

    public List<MemberMembershipResponse> findAllByMemberId(Long memberId) {
        validateMember(memberId);
        return membershipMapper.findAllByMemberId(memberId);
    }

    public List<MembershipProductOptionResponse> findActiveProducts() {
        return membershipMapper.findActiveProducts();
    }

    public List<MembershipProduct> findAllProducts() {
        return membershipMapper.findAllProducts();
    }

    @Transactional
    public void createProduct(MembershipProductRequest request) {
        validateProduct(request, null);
        validateAffectedRows(membershipMapper.insertProduct(request), "회원권 상품 등록에 실패했습니다.");
    }

    @Transactional
    public void updateProduct(Long productId, MembershipProductRequest request) {
        validateProduct(request, productId);
        validateAffectedRows(membershipMapper.updateProduct(productId, request), "회원권 상품 수정에 실패했습니다.");
    }

    private void validateProduct(MembershipProductRequest request, Long excludedId) {
        String normalizedName = request.name().trim();
        if (membershipMapper.existsProductName(normalizedName, excludedId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 회원권 상품명입니다.");
        }
        boolean ptProduct = request.productType().name().equals("PT")
                || request.productType().name().equals("COMBINED");
        if (ptProduct && request.ptSessionCount() == 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "PT 상품은 PT 횟수가 1회 이상이어야 합니다.");
        }
        if (!ptProduct && request.ptSessionCount() != 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "헬스 전용 상품의 PT 횟수는 0이어야 합니다.");
        }
    }

    @Transactional
    public MemberMembershipResponse create(
            Long memberId,
            CreateMemberMembershipRequest request
    ) {
        validateMember(memberId);

        MembershipProduct product = membershipMapper
                .findActiveProductById(request.productId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "등록 가능한 회원권 상품을 찾을 수 없습니다."
                ));

        LocalDate endDate = request.startDate()
                .plusDays(product.durationDays() - 1L);

        if (membershipMapper.existsOverlappingMembership(
                memberId,
                product.productType(),
                request.startDate(),
                endDate
        )) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "동일한 이용 유형의 회원권이 해당 기간과 겹칩니다."
            );
        }

        MemberMembershipRegistration registration =
                new MemberMembershipRegistration(
                        memberId,
                        product.id(),
                        request.startDate(),
                        endDate,
                        product.ptSessionCount(),
                        MembershipStatus.PENDING_PAYMENT
                );

        validateAffectedRows(
                membershipMapper.insertMemberMembership(registration),
                "회원권 등록에 실패했습니다."
        );

        return findMembership(memberId, registration.getMembershipId());
    }

    @Transactional
    public MemberMembershipResponse pause(
            Long memberId,
            Long membershipId
    ) {
        return changeStatus(
                memberId,
                membershipId,
                MembershipStatus.ACTIVE,
                MembershipStatus.PAUSED
        );
    }

    @Transactional
    public MemberMembershipResponse resume(
            Long memberId,
            Long membershipId
    ) {
        return changeStatus(
                memberId,
                membershipId,
                MembershipStatus.PAUSED,
                MembershipStatus.ACTIVE
        );
    }

    @Transactional
    public MemberMembershipResponse cancel(
            Long memberId,
            Long membershipId
    ) {
        MemberMembershipResponse membership = findMembership(memberId, membershipId);

        if (membership.status() != MembershipStatus.PENDING_PAYMENT) {
            throw invalidTransition(membership.status(), MembershipStatus.CANCELLED);
        }

        validateAffectedRows(
                membershipMapper.updateStatus(
                        memberId,
                        membershipId,
                        MembershipStatus.CANCELLED
                ),
                "회원권 취소에 실패했습니다."
        );

        return findMembership(memberId, membershipId);
    }

    @Transactional
    public MemberMembershipResponse cancelPendingForMember(
            Long userId,
            Long membershipId
    ) {
        Long memberId = membershipMapper
                .findActiveMemberIdByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "활성 회원 정보를 찾을 수 없습니다."
                ));

        MemberMembershipResponse membership =
                findMembership(memberId, membershipId);

        if (membership.status()
                != MembershipStatus.PENDING_PAYMENT) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "결제 대기 상태의 회원권만 취소할 수 있습니다."
            );
        }

        paymentOrderMapper.cancelReadyOrdersForMembership(
                memberId,
                membershipId
        );

        int affectedRows =
                membershipMapper.cancelPendingMembership(
                        memberId,
                        membershipId
                );

        validateAffectedRows(
                affectedRows,
                "결제 대기 회원권을 취소하지 못했습니다."
        );

        return findMembership(memberId, membershipId);
    }

    private MemberMembershipResponse changeStatus(
            Long memberId,
            Long membershipId,
            MembershipStatus expected,
            MembershipStatus target
    ) {
        MemberMembershipResponse membership = findMembership(memberId, membershipId);

        if (membership.status() != expected) {
            throw invalidTransition(membership.status(), target);
        }

        validateAffectedRows(
                membershipMapper.updateStatus(memberId, membershipId, target),
                "회원권 상태 변경에 실패했습니다."
        );

        return findMembership(memberId, membershipId);
    }

    private MemberMembershipResponse findMembership(
            Long memberId,
            Long membershipId
    ) {
        return membershipMapper.findById(memberId, membershipId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "회원권을 찾을 수 없습니다."
                ));
    }

    private void validateMember(Long memberId) {
        if (!membershipMapper.existsMemberById(memberId)) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "회원을 찾을 수 없거나 탈퇴한 회원입니다."
            );
        }
    }

    private void validateAffectedRows(int affectedRows, String message) {
        if (affectedRows != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, message);
        }
    }

    private BusinessException invalidTransition(
            MembershipStatus current,
            MembershipStatus target
    ) {
        return new BusinessException(
                ErrorCode.CONFLICT,
                "회원권 상태를 " + current + "에서 " + target + "(으)로 변경할 수 없습니다."
        );
    }

    @Transactional
    public PendingMembershipPaymentTarget createPendingForMember(
            Long userId,
            Long productId,
            LocalDate startDate
    ) {
        Long memberId = membershipMapper
                .findActiveMemberIdByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "활성 회원 정보를 찾을 수 없습니다."
                ));
        MembershipProduct product = membershipMapper
                .findActiveProductById(productId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND,
                        "구매 가능한 회원권 상품을 찾을 수 없습니다."
                ));

        LocalDate endDate = startDate
                .plusDays(product.durationDays() - 1L);

        if(membershipMapper.existsOverlappingMembership(
                memberId,
                product.productType(),
                startDate,
                endDate
        )) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "동일한 이용 유형의 회원권이 해당 기간과 겹칩니다."
            );
        }

        MemberMembershipRegistration registration =
                new MemberMembershipRegistration(
                        memberId,
                        product.id(),
                        startDate,
                        endDate,
                        product.ptSessionCount(),
                        MembershipStatus.PENDING_PAYMENT
                );

        validateAffectedRows(
                membershipMapper.insertMemberMembership(registration),
                "결제 대기 회원권 생성에 실패했습니다."
        );

        return new PendingMembershipPaymentTarget(
                memberId,
                registration.getMembershipId(),
                product.name(),
                product.price()
        );
    }
}
