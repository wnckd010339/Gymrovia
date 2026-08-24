package com.acorn.gymmanagement.auth.model;

import java.io.Serializable;

public record PendingGoogleSignup(
        String providerSubject,
        String email,
        String name
) implements Serializable {

    public static final String SESSION_KEY =
            "PENDING_GOOGLE_SIGNUP";
}
