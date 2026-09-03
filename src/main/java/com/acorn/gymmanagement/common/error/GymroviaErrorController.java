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
public class GymroviaErrorController
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
                case 400 -> new ErrorPageContent(
                        400,
                        "INVALID REQUEST",
                        "요청 내용을 확인해 주세요.",
                        "입력한 정보가 올바르지 않거나 "
                                + "처리할 수 없는 요청입니다."
                );

                case 401 -> new ErrorPageContent(
                        401,
                        "LOGIN REQUIRED",
                        "로그인이 필요합니다.",
                        "이 기능을 이용하려면 "
                                + "먼저 로그인해 주세요."
                );

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

                case 405 -> new ErrorPageContent(
                        405,
                        "METHOD NOT ALLOWED",
                        "지원하지 않는 요청 방식입니다.",
                        "현재 주소에서는 해당 요청 방식을 사용할 수 없습니다."
                );

                case 409 -> new ErrorPageContent(
                        409,
                        "REQUEST CONFLICT",
                        "현재 상태에서는 처리할 수 없습니다.",
                        "다른 요청으로 상태가 변경되었거나 "
                                + "이미 처리된 요청일 수 있습니다."
                );

                case 502 -> new ErrorPageContent(
                        502,
                        "PAYMENT SERVICE ERROR",
                        "결제 서비스에 연결하지 못했습니다.",
                        "잠시 후 다시 시도해 주세요. "
                                + "문제가 계속되면 관리자에게 문의해 주세요."
                );

                default -> new ErrorPageContent(
                        statusCode >= 400
                                ? statusCode
                                : 500,
                        "SERVICE ERROR",
                        "요청을 처리하지 못했습니다.",
                        "잠시 후 다시 시도해 주세요. "
                                + "문제가 계속되면 관리자에게 문의해 주세요."
                );
            };
        }
    }

}
