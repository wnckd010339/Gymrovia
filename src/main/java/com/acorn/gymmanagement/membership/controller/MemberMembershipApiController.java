package com.acorn.gymmanagement.membership.controller;

import com.acorn.gymmanagement.common.response.ApiResponse;
import com.acorn.gymmanagement.membership.dto.response.MemberMembershipResponse;
import com.acorn.gymmanagement.membership.service.MembershipService;
import com.acorn.gymmanagement.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member/memberships")
public class MemberMembershipApiController {

    private final MembershipService membershipService;

    @PatchMapping("/{membershipId}/cancel")
    public ApiResponse<MemberMembershipResponse>
    cancelPendingMembership(
            @SessionAttribute(SessionUser.SESSION_KEY)
            SessionUser sessionUser,

            @PathVariable
            Long membershipId
    ) {
        MemberMembershipResponse response =
                membershipService.cancelPendingForMember(
                        sessionUser.userId(),
                        membershipId
                );

        return ApiResponse.success(
                "결제 대기 회원권을 취소했습니다.",
                response
        );
    }
}
