package com.acorn.gymmanagement.auth.mapper;

import com.acorn.gymmanagement.auth.model.LocalAuthenticatedUser;
import com.acorn.gymmanagement.auth.model.OAuthAuthenticatedUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface AuthMapper {

    Optional<LocalAuthenticatedUser> findLocalUserByLoginId(@Param("loginId") String loginId);

    Optional<OAuthAuthenticatedUser> findOAuthUser(
            @Param("provider") String provider,
            @Param("providerSubject") String providerSubject
    );

    boolean existsUserByEmail(
            @Param("email") String email
    );

    int updateLastLoginAt(
            @Param("userId") Long userId
    );
}
