package com.acorn.gymmanagement.auth.handler;

import com.acorn.gymmanagement.auth.model.PendingGoogleSignup;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthFailureHandlerTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private AuthenticationException exception;
    @InjectMocks
    private GoogleOAuthFailureHandler failureHandler;

    @Test
    void 인증실패시임시가입정보를제거하고로그인화면으로이동한다() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        failureHandler.onAuthenticationFailure(
                request,
                response,
                exception
        );

        verify(session).removeAttribute(PendingGoogleSignup.SESSION_KEY);
        verify(response).sendRedirect("/login?oauthError=failed");
    }
}
