package com.acorn.gymmanagement.auth.model;

import com.acorn.gymmanagement.auth.form.GoogleSignupForm;
import com.acorn.gymmanagement.member.model.MemberGender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
public class GoogleMemberRegistration {

    @Setter
    private Long userId;

    @Setter
    private Long memberId;

    private final String provider;
    private final String providerSubject;
    private final String email;
    private final String name;
    private final String phone;
    private final LocalDate birthDate;
    private final MemberGender gender;
    private final boolean trainerRequested;

    public GoogleMemberRegistration(
            PendingGoogleSignup pendingSignup,
            GoogleSignupForm form,
            String normalizedPhone
    ) {
        this.provider = "GOOGLE";
        this.providerSubject = pendingSignup.providerSubject();
        this.email = pendingSignup.email().trim().toLowerCase();
        this.name = form.getName().trim();
        this.phone = normalizedPhone;
        this.birthDate = form.getBirthDate();
        this.gender = form.getGender();
        this.trainerRequested = form.isTrainerRequested();
    }
}
