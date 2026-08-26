package com.acorn.gymmanagement.auth.handler;

import com.acorn.gymmanagement.auth.model.PendingGoogleSignup;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleOAuthFailureHandler
    implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception

    ) throws IOException, ServletException {
        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute(
                    PendingGoogleSignup.SESSION_KEY
            );
        }

        response.sendRedirect("/login?oauthError=failed");
    }

}

