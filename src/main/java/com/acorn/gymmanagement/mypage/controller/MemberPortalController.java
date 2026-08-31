package com.acorn.gymmanagement.mypage.controller;

import com.acorn.gymmanagement.membership.service.MembershipService;
import com.acorn.gymmanagement.mypage.form.WorkoutRecordForm;
import com.acorn.gymmanagement.mypage.form.WorkoutExerciseForm;
import com.acorn.gymmanagement.mypage.form.MemberProfileForm;
import com.acorn.gymmanagement.mypage.form.PasswordChangeForm;
import com.acorn.gymmanagement.mypage.service.MemberPortalService;
import com.acorn.gymmanagement.mypage.service.MemberPasswordService;
import com.acorn.gymmanagement.mypage.service.MemberWorkoutService;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.payment.gateway.toss.TossPaymentProperties;
import com.acorn.gymmanagement.security.SessionUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberPortalController {
    private final MemberPortalService memberPortalService;
    private final MembershipService membershipService;
    private final MemberPasswordService memberPasswordService;
    private final MemberWorkoutService memberWorkoutService;
    private final TossPaymentProperties tossPaymentProperties;

    @GetMapping("/memberships")
    public String memberships(
            @SessionAttribute(SessionUser.SESSION_KEY)
            SessionUser user,
            Model model
    ) {
        model.addAttribute(
                "memberships",
                memberPortalService.memberships(user.userId())
        );

        model.addAttribute(
                "membershipProducts",
                membershipService.findActiveProducts()
        );

        model.addAttribute(
                "tossClientKey",
                tossPaymentProperties.clientKey()
        );

        model.addAttribute(
                "tossSuccessUrl",
                tossPaymentProperties.successUrl()
        );

        model.addAttribute(
                "tossFailUrl",
                tossPaymentProperties.failUrl()
        );

        return "member/memberships";
    }

    @GetMapping("/attendance")
    public String attendance(
            @SessionAttribute(SessionUser.SESSION_KEY)
            SessionUser user,
            Model model
    ) {
        model.addAttribute(
                "attendances",
                memberPortalService.attendances(user.userId())
        );

        return "member/attendance";
    }

    @GetMapping("/workouts")
    public String workouts(
            @SessionAttribute(SessionUser.SESSION_KEY)
            SessionUser user,
            @RequestParam(required = false) Long routineId,
            Model model
    ) {
        var routine = memberWorkoutService.routine(user.userId());
        model.addAttribute(
                "routine",
                routine
        );

        model.addAttribute(
                "workoutDays",
                memberWorkoutService.workoutDays(user.userId())
        );

        Long activeRoutineId =
                routine.stream().findFirst()
                        .map(item
                                -> item.routineId()).orElse(null);

        Long selectedRoutineId =
                routineId != null && routine.stream()
                        .anyMatch(item -> routineId.equals(item.routineId()))
                        ? routineId : activeRoutineId;

        List<WorkoutExerciseForm> initialExercises = selectedRoutineId == null
                ? List.of(new WorkoutExerciseForm("", 3, null, null))
                : routine.stream()
                    .filter(item -> selectedRoutineId.equals(item.routineId()) && item.exerciseName() != null)
                    .map(item -> new WorkoutExerciseForm(
                            item.exerciseName(), item.targetSets() == null ? 3 : item.targetSets(),
                            item.targetWeight(), item.targetRepsMin()))
                    .toList();

        if (initialExercises.isEmpty()) {
            initialExercises = List.of(new WorkoutExerciseForm("", 3, null, null));
        }
        model.addAttribute("workoutRecordForm", new WorkoutRecordForm(
                selectedRoutineId, 60, "", initialExercises
        ));
        return "member/workouts";
    }

    @PostMapping("/workouts")
    public String saveWorkout(
            @SessionAttribute(SessionUser.SESSION_KEY)
            SessionUser user,
            @Valid @ModelAttribute WorkoutRecordForm workoutRecordForm, BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "routine",
                    memberWorkoutService.routine(user.userId())
            );

            model.addAttribute(
                    "workoutDays",
                    memberWorkoutService.workoutDays(user.userId())
            );

            return "member/workouts";
        }

        memberWorkoutService.save(user.userId(), workoutRecordForm);
        redirectAttributes.addFlashAttribute(
                "message",
                "운동 기록을 저장했습니다."
        );

        return "redirect:/member/workouts";
    }

    @GetMapping("/workouts/{sessionId}/edit")
    public String editWorkout(
            @SessionAttribute(SessionUser.SESSION_KEY)
            SessionUser user,
            @PathVariable Long sessionId,
            Model model
    ) {
        var workout =
                memberWorkoutService.workoutForEdit(user.userId(), sessionId);
        var exercises =
                memberWorkoutService.workoutExercises(user.userId(), sessionId).stream()
                .map(exercise -> new WorkoutExerciseForm(exercise.exerciseName(), exercise.sets(), exercise.weight(), exercise.reps()))
                .toList();

        model.addAttribute(
                "workoutRecordForm",
                new WorkoutRecordForm(
                    workout.routineId(), workout.durationMinutes(), workout.memo(), exercises
                ));

        model.addAttribute(
                "sessionId",
                sessionId
        );

        return "member/workout-edit";
    }

    @PostMapping("/workouts/{sessionId}")
    public String updateWorkout(
            @SessionAttribute(SessionUser.SESSION_KEY)
            SessionUser user,
            @PathVariable Long sessionId,
            @Valid @ModelAttribute WorkoutRecordForm workoutRecordForm, BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "sessionId", sessionId
            );

            return "member/workout-edit";
        }
        memberWorkoutService.update(user.userId(), sessionId, workoutRecordForm);
        redirectAttributes.addFlashAttribute("message", "운동 기록을 수정했습니다.");
        return "redirect:/member/workouts";
    }

    @GetMapping("/workouts/daily")
    public String workoutDaily(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                               @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date,
                               Model model) {
        var workouts = memberWorkoutService.workoutsByDate(user.userId(), date);
        if (workouts.isEmpty()) {
            throw new com.acorn.gymmanagement.common.exception.BusinessException(
                    com.acorn.gymmanagement.common.exception.ErrorCode.NOT_FOUND,
                    "해당 날짜의 운동 기록을 찾을 수 없습니다."
            );
        }
        model.addAttribute("workoutDate", date);
        model.addAttribute("workouts", workouts);
        model.addAttribute("exerciseCount", workouts.size());
        model.addAttribute("totalSets", workouts.stream().mapToInt(workout -> workout.sets() == null ? 0 : workout.sets()).sum());
        return "member/workout-daily-detail";
    }

    @PostMapping("/workouts/daily/delete")
    public String deleteWorkoutDay(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                                   @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date,
                                   RedirectAttributes redirectAttributes) {
        memberWorkoutService.deleteDay(user.userId(), date);
        redirectAttributes.addFlashAttribute("message", "해당 날짜의 운동 기록을 삭제했습니다.");
        return "redirect:/member/workouts";
    }

    @PostMapping("/workouts/{sessionId}/delete")
    public String deleteWorkout(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                                @PathVariable Long sessionId,
                                RedirectAttributes redirectAttributes) {
        memberWorkoutService.delete(user.userId(), sessionId);
        redirectAttributes.addFlashAttribute("message", "운동 기록을 삭제했습니다.");
        return "redirect:/member/workouts";
    }

    @GetMapping("/payments")
    public String payments(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user, Model model) {
        model.addAttribute("payments", memberPortalService.payments(user.userId()));
        return "member/payments";
    }

    @GetMapping("/profile")
    public String profile(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user, Model model) {
        var profile = memberPortalService.profile(user.userId());
        model.addAttribute("profile", profile);
        model.addAttribute("memberProfileForm", new MemberProfileForm(
                profile.name(), profile.phone(), profile.birthDate(), profile.gender(), profile.email()
        ));
        model.addAttribute("passwordChangeForm", new PasswordChangeForm("", "", ""));
        return "member/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                                 @Valid @ModelAttribute PasswordChangeForm passwordChangeForm,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/member/profile";
        }
        try {
            memberPasswordService.change(user.userId(), passwordChangeForm);
            redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/member/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                                @Valid @ModelAttribute MemberProfileForm memberProfileForm,
                                BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", memberPortalService.profile(user.userId()));
            model.addAttribute("passwordChangeForm", new PasswordChangeForm("", "", ""));
            return "member/profile";
        }
        memberPortalService.updateProfile(user.userId(), memberProfileForm);
        redirectAttributes.addFlashAttribute("message", "내 정보가 수정되었습니다.");
        return "redirect:/member/profile";
    }

    @GetMapping("/payments/success")
    public String paymentSuccess() {
        return "member/payment-success";
    }

    @GetMapping("/payments/fail")
    public String paymentFail() {
        return "member/payment-fail";
    }
}
