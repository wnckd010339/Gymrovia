package com.acorn.gymmanagement.membership.mapper;
import com.acorn.gymmanagement.membership.dto.response.MemberMembershipResponse;
import com.acorn.gymmanagement.membership.dto.response.MembershipProductOptionResponse;
import com.acorn.gymmanagement.membership.model.MemberMembershipRegistration;
import com.acorn.gymmanagement.membership.model.MembershipProductType;
import com.acorn.gymmanagement.membership.model.MembershipStatus;
import com.acorn.gymmanagement.membership.model.MembershipProduct;
import com.acorn.gymmanagement.membership.dto.request.MembershipProductRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface MembershipMapper {
    List<MemberMembershipResponse> findAllByMemberId(
            @Param("memberId") Long memberId
    );

    Optional<MemberMembershipResponse> findById(
            @Param("memberId") Long memberId,
            @Param("membershipId") Long membershipId
    );

    Optional<Long> findActiveMemberIdByUserId(
            @Param("userId") Long userId
    );

    Optional<Long> findActiveMemberIdByUserIdForUpdate(@Param("userId") Long userId);

    List<MembershipProductOptionResponse> findActiveProducts();

    List<MembershipProduct> findAllProducts();

    boolean existsProductName(@Param("name") String name, @Param("excludedId") Long excludedId);

    int insertProduct(MembershipProductRequest request);

    int updateProduct(@Param("productId") Long productId, @Param("request") MembershipProductRequest request);

    Optional<MembershipProduct> findActiveProductById(
            @Param("productId") Long productId
    );

    boolean existsMemberById(
            @Param("memberId") Long memberId
    );

    boolean existsOverlappingMembership(
            @Param("memberId") Long memberId,
            @Param("productType")
            MembershipProductType productType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    int insertMemberMembership(
            MemberMembershipRegistration registration
    );

    int updateStatus(
            @Param("memberId") Long memberId,
            @Param("membershipId") Long membershipId,
            @Param("status") MembershipStatus status
    );

    int activateAfterPayment(
            @Param("memberId") Long memberId,
            @Param("membershipId") Long membershipId
    );

    int cancelAfterFullRefund(
            @Param("memberId") Long memberId,
            @Param("membershipId") Long membershipId
    );

    int cancelPendingMembership(
            @Param("memberId") Long memberId,
            @Param("membershipId") Long membershipId
    );


}
