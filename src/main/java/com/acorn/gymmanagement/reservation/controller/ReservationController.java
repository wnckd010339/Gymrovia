package com.acorn.gymmanagement.reservation.controller;

import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.reservation.dto.request.ReservationSearchCondition;
import com.acorn.gymmanagement.reservation.dto.response.ReservationOptionResponse;
import com.acorn.gymmanagement.reservation.form.ReservationForm;
import com.acorn.gymmanagement.reservation.model.ReservationStatus;
import com.acorn.gymmanagement.reservation.model.ReservationType;
import com.acorn.gymmanagement.reservation.service.ReservationService;
import com.acorn.gymmanagement.security.SessionUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Controller
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public String index(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStart,

            @RequestParam(required = false)
            Long trainerId,

            @RequestParam(required = false)
            String reservationType,

            @RequestParam(required = false)
            String status,

            Model model
    ) {
        LocalDate normalizedWeekStart =
                normalizeWeekStart(weekStart);

        ReservationSearchCondition condition =
                new ReservationSearchCondition(
                        normalizedWeekStart,
                        trainerId,
                        reservationType,
                        status
                );

        model.addAttribute("condition", condition);
        model.addAttribute(
                "reservations",
                reservationService.findCalendar(condition)
        );

        model.addAttribute(
                "summary",
                reservationService.findSummary(LocalDate.now())
       );

        model.addAttribute(
                "trainers",
                reservationService.findActiveTrainers()
        );

        model.addAttribute(
                "reservationTypes",
                ReservationType.values()
        );

        model.addAttribute(
                "reservationStatuses",
                ReservationStatus.values()
        );

        model.addAttribute(
                "reservationForm",
                new ReservationForm()
        );

        return "admin/reservation/index";

    }

    private LocalDate normalizeWeekStart(
            LocalDate weekStart
    ) {
        LocalDate target = weekStart == null
                ? LocalDate.now()
                : weekStart;

        return target.with(
                TemporalAdjusters.previousOrSame(
                        DayOfWeek.MONDAY
                )
        );
    }

    @PostMapping
    public String create(
            @Valid
            @ModelAttribute("reservationForm")
            ReservationForm form,
            BindingResult bindingResult,
            @SessionAttribute(SessionUser.SESSION_KEY)
            SessionUser user,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    firstErrorMessage(bindingResult)
            );
            return redirectToWeek(form.getStartsAt());
        }

        try {
            reservationService.create(
                    form,
                    user.userId()
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "예약이 등록되었습니다."
            );
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return redirectToWeek(form.getStartsAt());
    }

    @PostMapping("/{reservationId}")
    public String update(
            @PathVariable Long reservationId,
            @Valid
            @ModelAttribute("reservationForm")
            ReservationForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    firstErrorMessage(bindingResult)
            );
            return redirectToWeek(form.getStartsAt());
        }

        try {
            reservationService.update(
                    reservationId,
                    form
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "예약이 수정되었습니다."
            );
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return redirectToWeek(form.getStartsAt());
    }

    @PostMapping("/{reservationId}/status")
    public String updateStatus(
            @PathVariable Long reservationId,
            @RequestParam ReservationStatus status,
            @RequestParam(required = false)
            String cancellationReason,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStart,
            RedirectAttributes redirectAttributes
    ) {
        try {
            reservationService.updateStatus(
                    reservationId,
                    status,
                    cancellationReason
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "예약 상태가 변경되었습니다."
            );
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return redirectToWeek(weekStart);
    }

    @GetMapping("/member-options")
    @ResponseBody
    public List<ReservationOptionResponse> memberOptions(
            @RequestParam(defaultValue = "")
            String keyword
    ) {
        return reservationService.findActiveMembers(keyword);
    }

    private String redirectToWeek(
            LocalDateTime startsAt
    ) {
        LocalDate date = startsAt == null
                ? LocalDate.now()
                : startsAt.toLocalDate();

        return redirectToWeek(date);
    }

    private String redirectToWeek(LocalDate date) {
        return "redirect:/admin/reservations?weekStart="
                + normalizeWeekStart(date);
    }

    private String firstErrorMessage(
            BindingResult bindingResult
    ) {
        return bindingResult.getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력 내용을 확인해 주세요.");
    }
}
