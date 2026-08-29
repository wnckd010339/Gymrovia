package com.acorn.gymmanagement.attendance.controller;

import com.acorn.gymmanagement.attendance.dto.request.AttendanceSearchCondition;
import com.acorn.gymmanagement.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status,
            Model model
    ) {
        AttendanceSearchCondition condition =
                new AttendanceSearchCondition(keyword, date, status);

        model.addAttribute("condition", condition);
        model.addAttribute(
                "summary",
                attendanceService.getSummary(condition.searchDate())
        );
        model.addAttribute(
                "currentAttendances",
                attendanceService.findCurrentAttendances(condition)
        );
        model.addAttribute(
                "attendanceHistory",
                attendanceService.findHistory(condition)
        );

        return "admin/attendance/index";
    }

    @PostMapping("/{attendanceId}/checkout")
    public String checkout(
            @PathVariable Long attendanceId,
            RedirectAttributes redirectAttributes
    ) {
        attendanceService.checkout(attendanceId);
        redirectAttributes.addFlashAttribute(
                "message",
                "퇴실 처리가 완료되었습니다."
        );

        return "redirect:/admin/attendance";
    }

    @PostMapping("/check-in")
    public String checkIn(
            @RequestParam Long memberId,
            RedirectAttributes redirectAttributes
    ) {
        attendanceService.checkIn(memberId);
        redirectAttributes.addFlashAttribute(
                "message",
                "입장 처리가 완료되었습니다."
        );

        return "redirect:/admin/attendance";
    }
}
