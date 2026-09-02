package com.acorn.gymmanagement.member.dto.response;

public record MemberStatusSummaryResponse(
        long activeCount,
        long suspendedCount,
        long withdrawnCount
) {
}
