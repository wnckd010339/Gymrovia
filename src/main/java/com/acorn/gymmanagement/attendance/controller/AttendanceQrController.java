package com.acorn.gymmanagement.attendance.controller;

import com.acorn.gymmanagement.attendance.service.AttendanceQrService;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/member/attendance")
@RequiredArgsConstructor
public class AttendanceQrController {

    private final AttendanceQrService attendanceQrService;

    @GetMapping("/qr")
    public String verifyQr(
            @SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
            @RequestParam String token,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String verificationToken =
                    attendanceQrService.verifyScan(
                            user.userId(),
                            token
                    );

            boolean checkedIn =
                    attendanceQrService.isCheckedIn(
                            user.userId()
                    );

            model.addAttribute(
                    "verificationToken",
                    verificationToken
            );
            model.addAttribute(
                    "checkedIn",
                    checkedIn
            );
            model.addAttribute(
                    "attendanceAction",
                    checkedIn ? "CHECK_OUT" : "CHECK_IN"
            );

            return "member/attendance-qr";
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/member/attendance";
        }


    }

    @PostMapping("/qr/check-in")
    public String checkIn(
          @SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
          @RequestParam String verificationToken,
          RedirectAttributes redirectAttributes
    ) {
        try {
            attendanceQrService.checkIn(
                    user.userId(),
                    verificationToken
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "센터 QR 인증이 완료되어 체크인되었습니다."
            );
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/member/attendance";
    }

    @PostMapping("/qr/check-out")
    public String checkOut(
            @SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
            @RequestParam String verificationToken,
            RedirectAttributes redirectAttributes
    ) {
        try {
            attendanceQrService.checkOut(
                    user.userId(),
                    verificationToken
            );
            redirectAttributes.addFlashAttribute(
                    "message",
                    "센터 QR 인증이 완료되어 체크아웃되었습니다."
            );
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/member/attendance";
    }
}
