package com.acorn.gymmanagement.auth.service;

import com.acorn.gymmanagement.auth.form.GoogleSignupForm;
import com.acorn.gymmanagement.auth.mapper.AuthMapper;
import com.acorn.gymmanagement.auth.model.GoogleMemberRegistration;
import com.acorn.gymmanagement.auth.model.OAuthAuthenticatedUser;
import com.acorn.gymmanagement.auth.model.PendingGoogleSignup;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleOAuthService {

    private static final String PROVIDER_GOOGLE = "GOOGLE";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final AuthMapper authMapper;

    @Transactional
    public Optional<OAuthAuthenticatedUser> findActiveUser(
            String providerSubject
    ) {
        OAuthAuthenticatedUser user = authMapper
                .findOAuthUser(PROVIDER_GOOGLE, providerSubject)
                .orElse(null);

        if(user == null){
            return Optional.empty();
        }

        if(!STATUS_ACTIVE.equals(user.status())){
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "현재 로그인할 수 없는 계정입니다."
            );

        }

        if(user.memberId() == null) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "회원 가입 정보가 완료되지 않았습니다."
            );
        }

        authMapper.updateLastLoginAt(user.userId());

        return Optional.of(user);
    }

    public boolean emailAlreadyExists(String email){
        return authMapper.existsUserByEmail(email);
    }

    @Transactional
    public SessionUser registerGoogleMember(
            PendingGoogleSignup pendingSignup,
            GoogleSignupForm form
    ) {
        if(pendingSignup == null){
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "Google 인증 정보가 만료되었습니다. 다시 로그인해 주세요."
            );
        }

        String email = pendingSignup.email()
                .trim()
                .toLowerCase();

        if(authMapper.existsUserByEmail(email)) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "같은 이메일로 가입된 계정이 있습니다. 기존 계정으로 로그인해 주세요."
            );
        }

        String normalizedPhone = normalizePhone(form.getPhone());

        GoogleMemberRegistration registration =
                new GoogleMemberRegistration(
                        pendingSignup,
                        form,
                        normalizedPhone
                );

        try {
            validateInsert(
                    authMapper.insertGoogleUser(registration),
                    "Google 사용자 계정 생성에 실패했습니다."
            );

            validateInsert(
                    authMapper.insertGoogleOAuthAccount(registration),
                    "Google 계정 연결에 실패했습니다."
            );

            validateInsert(
                    authMapper.insertGoogleMember(registration),
                    "회원 정보 생성에 실패했습니다."
            );
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "이미 가입되었거나 연결된 Google 계정입니다."
            );
        }

        return new SessionUser(
                registration.getUserId(),
                email,
                email,
                SessionUser.ROLE_MEMBER
        );
    }

    private String normalizePhone(String phone) {

        String digits = phone.replaceAll("[^0-9]", "");

        if(digits.length() == 11) {
            return digits.replaceFirst(
                    "(\\d{3})(\\d{4})(\\d{4})",
                    "$1-$2-$3"
            );
        }

        if(digits.length() == 10){
            return digits.replaceFirst(
                    "(\\d{3})(\\d{3})(\\d{4})",
                    "$1-$2-$3"
            );
        }

        return phone.trim();
    }

    private void validateInsert(int affectedRows, String message) {
        if(affectedRows != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    message
            );
        }
    }
}
