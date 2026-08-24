package com.acorn.gymmanagement.auth.service;

import com.acorn.gymmanagement.auth.mapper.AuthMapper;
import com.acorn.gymmanagement.auth.model.OAuthAuthenticatedUser;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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
}
