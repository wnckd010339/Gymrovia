package com.acorn.gymmanagement.notification.controller;

import com.acorn.gymmanagement.notification.service.NotificationService;
import com.acorn.gymmanagement.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping({"/member/notifications", "/admin/notifications"})
    public String list(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                       @RequestParam(defaultValue = "false") boolean unreadOnly,
                       Model model) {
        model.addAttribute("notifications", notificationService.findAll(user.userId(), unreadOnly));
        model.addAttribute("unreadOnly", unreadOnly);
        model.addAttribute("adminView", user.admin());
        return "notification/index";
    }

    @PostMapping({"/member/notifications/{id}/read", "/admin/notifications/{id}/read"})
    public String read(@PathVariable Long id,
                       @RequestParam(required = false) String target,
                       @SessionAttribute(SessionUser.SESSION_KEY) SessionUser user) {
        notificationService.markRead(id, user.userId());
        return "redirect:" + safeTarget(target, user);
    }

    @PostMapping({"/member/notifications/read-all", "/admin/notifications/read-all"})
    public String readAll(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user) {
        notificationService.markAllRead(user.userId());
        return "redirect:" + basePath(user) + "/notifications";
    }

    private String safeTarget(String target, SessionUser user) {
        String base = basePath(user);
        return target != null && (target.equals(base) || target.startsWith(base + "/"))
                ? target : base + "/notifications";
    }

    private String basePath(SessionUser user) {
        return user.admin() ? "/admin" : "/member";
    }
}
