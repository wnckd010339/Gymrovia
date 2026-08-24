package com.acorn.gymmanagement.auth.handler;

import com.acorn.gymmanagement.auth.model.OAuthAuthenticatedUser;
import com.acorn.gymmanagement.auth.model.PendingGoogleSignup;
import com.acorn.gymmanagement.auth.service.GoogleOAuthService;
import com.acorn.gymmanagement.auth.service.SessionService;
import com.acorn.gymmanagement.security.SessionSecurityContextService;
import com.acorn.gymmanagement.security.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GoogleOAuthSuccessHandler
        implements AuthenticationSuccessHandler {

    private final GoogleOAuthService googleOAuthService;
    private final SessionService sessionService;
    private final SessionSecurityContextService securityContextService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            response.sendRedirect("/login?oauthError=invalid_provider");
            return;
        }

        String subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        Boolean emailVerified = oidcUser.getEmailVerified();

        if (subject == null || subject.isBlank()
                || email == null || email.isBlank()
                || !Boolean.TRUE.equals(emailVerified)) {
            response.sendRedirect("/login?oauthError=invalid_profile");
            return;
        }

        OAuthAuthenticatedUser user =
                googleOAuthService.findActiveUser(subject)
                        .orElse(null);

        if (user == null) {
            savePendingSignup(request, oidcUser);
            response.sendRedirect("/signup/google/profile");
            return;
        }

        request.changeSessionId();

        SessionUser sessionUser = new SessionUser(
                user.userId(),
                user.email(),
                user.email(),
                SessionUser.ROLE_MEMBER
        );

        sessionService.saveUser(
                request.getSession(),
                sessionUser
        );

        securityContextService.authenticate(
                request,
                response,
                sessionUser
        );

        response.sendRedirect(
                sessionUser.defaultRedirectPath()
        );
    }

    private void savePendingSignup(
            HttpServletRequest request,
            OidcUser oidcUser
    ) {
        PendingGoogleSignup pendingSignup =
                new PendingGoogleSignup(
                        oidcUser.getSubject(),
                        oidcUser.getEmail(),
                        oidcUser.getFullName()
                );

        request.getSession().setAttribute(
                PendingGoogleSignup.SESSION_KEY,
                pendingSignup
        );
    }
}