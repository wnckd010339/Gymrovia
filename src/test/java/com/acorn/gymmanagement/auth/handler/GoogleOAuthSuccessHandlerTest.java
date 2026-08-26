package com.acorn.gymmanagement.auth.handler;

import com.acorn.gymmanagement.auth.model.PendingGoogleSignup;
import com.acorn.gymmanagement.auth.service.GoogleOAuthService;
import com.acorn.gymmanagement.auth.service.SessionService;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.common.exception.ErrorCode;
import com.acorn.gymmanagement.security.SessionSecurityContextService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthSuccessHandlerTest {

    @Mock
    private GoogleOAuthService googleOAuthService;
    @Mock
    private SessionService sessionService;
    @Mock
    private SessionSecurityContextService securityContextService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private Authentication authentication;
    @Mock
    private OidcUser oidcUser;

    private GoogleOAuthSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        successHandler = new GoogleOAuthSuccessHandler(
                googleOAuthService,
                sessionService,
                securityContextService
        );
    }

    @Test
    void 비활성계정은로그인화면으로안전하게돌려보낸다() throws Exception {
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getSubject()).thenReturn("google-sub-123");
        when(oidcUser.getEmail()).thenReturn("member@gmail.com");
        when(oidcUser.getEmailVerified()).thenReturn(true);
        when(request.getSession(false)).thenReturn(session);
        when(googleOAuthService.findActiveUser("google-sub-123"))
                .thenThrow(new BusinessException(
                        ErrorCode.FORBIDDEN,
                        "현재 로그인할 수 없는 계정입니다."
                ));

        successHandler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        verify(session).removeAttribute(PendingGoogleSignup.SESSION_KEY);
        verify(response).sendRedirect("/login?oauthError=account_unavailable");
        verifyNoInteractions(sessionService, securityContextService);
    }

    @Test
    void 이메일미인증계정은회원조회전에차단한다() throws Exception {
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getSubject()).thenReturn("google-sub-123");
        when(oidcUser.getEmail()).thenReturn("member@gmail.com");
        when(oidcUser.getEmailVerified()).thenReturn(false);

        successHandler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        verify(response).sendRedirect("/login?oauthError=invalid_profile");
        verifyNoInteractions(googleOAuthService, sessionService, securityContextService);
    }
}
