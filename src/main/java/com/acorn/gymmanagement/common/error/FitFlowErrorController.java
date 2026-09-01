package com.acorn.gymmanagement.common.error;

import com.acorn.gymmanagement.security.SessionUser;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FitFlowErrorController
    implements ErrorController {

    @RequestMapping("/error")
    public String error(
            HttpServletRequest request,
            Model model
    ) {
        int statusCode = resolveStatusCode(request);

        ErrorPageContent content =
                ErrorPageContent.from(statusCode);

        SessionUser sessionUser =
                findSessionUser(request);

        String homePath =
                resolveHomePath(sessionUser);

        String homeLabel =
                sessionUser == null
                    ? "로그인 화면으로"
                    : "내 홈으로";

        model.addAttribute(
                "statusCode",
                content.statusCode()
        );

        model.addAttribute(
                "eyebrow",
                content.eyebrow()
        );

        model.addAttribute(
                "title",
                content.title()
        );

        model.addAttribute(
                "description",
                content.description()
        );

        model.addAttribute(
                "homePath",
                homePath
        );

        model.addAttribute(
                "homeLabel",
                homeLabel
        );

        return "error/error";
    }

    private int resolveStatusCode(
            HttpServletRequest request
    ) {
        Object status =
                request.getAttribute(
                        RequestDispatcher.ERROR_STATUS_CODE
                );

        if (status instanceof Integer statusCode) {
            return statusCode;
        }

        return 500;
    }

    private SessionUser findSessionUser(
            HttpServletRequest request
    ) {
        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return null;
        }

        Object sessionUser =
                session.getAttribute(
                        SessionUser.SESSION_KEY
                );

        if (sessionUser instanceof SessionUser user) {
            return user;
        }

        return null;
    }

    private String resolveHomePath(
            SessionUser sessionUser
    ) {
        if (sessionUser == null
                || !sessionUser.hasValidRole()) {
            return "/login";
        }

        return sessionUser.defaultRedirectPath();
    }

    private record ErrorPageContent(
            int statusCode,
            String eyebrow,
            String title,
            String description
    ) {
        private static ErrorPageContent from(
                int statusCode
        ) {
            return switch (statusCode) {
                case 403 -> new ErrorPageContent(
                        403,
                        "ACCESS DENIED",
                        "접근 권한이 없습니다.",
                        "현재 계정으로는 이 페이지를 "
                            + "이용할 수 없습니다."
                );

                case 404 -> new ErrorPageContent(
                        404,
                        "PAGE NOT FOUND",
                        "페이지를 찾을 수 없습니다.",
                        "주소가 변경되었거나 "
                            + "삭제된 페이지일 수 있습니다."
                );

                default -> new ErrorPageContent(
                        500,
                        "SERVICE ERROR",
                        "요청을 처리하지 못했습니다.",
                        "잠시 후 다시 시도해주세요. "
                            + "문제가 계속되면 관리자에게 "
                            + "문의해 주세요."
                );
            };
        }
    }

}
