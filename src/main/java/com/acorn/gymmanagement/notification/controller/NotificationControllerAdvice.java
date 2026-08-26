package com.acorn.gymmanagement.notification.controller;

import com.acorn.gymmanagement.notification.service.NotificationService;
import com.acorn.gymmanagement.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
@RequiredArgsConstructor
public class NotificationControllerAdvice {
    private final NotificationService notificationService;

    @ModelAttribute("notificationHeader")
    public Object notificationHeader(HttpSession session) {
        Object value = session.getAttribute(SessionUser.SESSION_KEY);
        if (!(value instanceof SessionUser user)) {
            return null;
        }
        return notificationService.header(user.userId());
    }
}
