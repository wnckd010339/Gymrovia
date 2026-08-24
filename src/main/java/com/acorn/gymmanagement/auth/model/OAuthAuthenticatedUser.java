package com.acorn.gymmanagement.auth.model;

public record OAuthAuthenticatedUser(
        Long userId,
        String email,
        String role,
        String status,
        Long memberId
) {
}
