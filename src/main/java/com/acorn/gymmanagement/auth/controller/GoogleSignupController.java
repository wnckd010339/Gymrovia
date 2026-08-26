package com.acorn.gymmanagement.auth.controller;


import com.acorn.gymmanagement.auth.form.GoogleSignupForm;
import com.acorn.gymmanagement.auth.model.PendingGoogleSignup;
import com.acorn.gymmanagement.auth.service.GoogleOAuthService;
import com.acorn.gymmanagement.auth.service.SessionService;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.security.SessionSecurityContextService;
import com.acorn.gymmanagement.security.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class GoogleSignupController {

    private final GoogleOAuthService googleOAuthService;
    private final SessionService sessionService;
    private final SessionSecurityContextService sessionSecurityContextService;

    @GetMapping("/signup/google/profile")
    public String profileForm(
            HttpSession session,
            Model model
    ) {
        PendingGoogleSignup pendingSignup  =
                getPendingSignup(session);

        if(pendingSignup == null) {
            return "redirect:/login?oauthError=expired";
        }

        if(!model.containsAttribute("googleSignupForm")) {
            GoogleSignupForm form = new GoogleSignupForm();
            form.setName(pendingSignup.name());

            model.addAttribute("googleSignupForm", form);
        }

        model.addAttribute("googleEmail", pendingSignup.email());

        return "auth/signup/google-profile";
    }

    @PostMapping("/signup/google/profile")
    public String completeSignup(
            @Valid @ModelAttribute("googleSignupForm")
            GoogleSignupForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        PendingGoogleSignup pendingSignup =
                getPendingSignup(request.getSession(false));

        if(pendingSignup == null) {
            return "redirect:/login?oauthError=expired";
        }

        if(bindingResult.hasErrors()) {
            model.addAttribute(
                    "googleEmail",
                    pendingSignup.email()
            );
            return "auth/signup/google-profile";
        }

        try {
            SessionUser sessionUser =
                    googleOAuthService.registerGoogleMember(
                            pendingSignup,
                            form
                    );

            request.changeSessionId();

            HttpSession session = request.getSession();
            session.removeAttribute(
                    PendingGoogleSignup.SESSION_KEY
            );

            sessionService.saveUser(session, sessionUser);

            sessionSecurityContextService.authenticate(
                    request,
                    response,
                    sessionUser
            );

            return "redirect:" +
                    sessionUser.defaultRedirectPath();
        } catch (BusinessException exception) {
            model.addAttribute(
                    "googleEmail",
                    pendingSignup.email()
            );
            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
            return "auth/signup/google-profile";
        }
    }

    private PendingGoogleSignup getPendingSignup(
            HttpSession session
    ) {
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(
                PendingGoogleSignup.SESSION_KEY
        );

        if (value instanceof PendingGoogleSignup pendingGoogleSignup) {
            return pendingGoogleSignup;
        }

        return null;
    }
}
