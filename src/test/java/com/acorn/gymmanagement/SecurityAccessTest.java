package com.acorn.gymmanagement;

import com.acorn.gymmanagement.dashboard.dto.response.DashboardResponse;
import com.acorn.gymmanagement.dashboard.dto.response.DashboardSummaryResponse;
import com.acorn.gymmanagement.dashboard.service.DashboardService;
import com.acorn.gymmanagement.attendance.service.AttendanceQrService;
import com.acorn.gymmanagement.attendance.service.AttendanceService;
import com.acorn.gymmanagement.member.dto.response.MemberHomeSummaryResponse;
import com.acorn.gymmanagement.member.dto.request.CreateMemberRequest;
import com.acorn.gymmanagement.member.service.MemberService;
import com.acorn.gymmanagement.member.view.MemberHomeView;
import com.acorn.gymmanagement.membership.service.MembershipService;
import com.acorn.gymmanagement.payment.service.PaymentService;
import com.acorn.gymmanagement.facility.service.FacilityService;
import com.acorn.gymmanagement.facility.dto.response.FacilitySummaryResponse;
import com.acorn.gymmanagement.mypage.service.MemberPortalService;
import com.acorn.gymmanagement.mypage.service.MemberWorkoutService;
import com.acorn.gymmanagement.mypage.dto.response.MemberProfileView;
import com.acorn.gymmanagement.trainer.dto.response.TrainerHomeProfileResponse;
import com.acorn.gymmanagement.trainer.service.TrainerAdminService;
import com.acorn.gymmanagement.trainer.dto.response.TrainerProfileView;
import com.acorn.gymmanagement.trainer.service.TrainerMemberService;
import com.acorn.gymmanagement.trainer.service.TrainerRoutineService;
import com.acorn.gymmanagement.trainer.service.TrainerWorkoutService;
import com.acorn.gymmanagement.member.model.MemberGender;
import com.acorn.gymmanagement.security.SessionUser;
import com.acorn.gymmanagement.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private AttendanceQrService attendanceQrService;

    @MockitoBean
    private MemberPortalService memberPortalService;

    @MockitoBean
    private MemberWorkoutService memberWorkoutService;

    @MockitoBean
    private MembershipService membershipService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private FacilityService facilityService;

    @MockitoBean
    private TrainerAdminService trainerService;

    @MockitoBean
    private TrainerMemberService trainerMemberService;

    @MockitoBean
    private TrainerRoutineService trainerRoutineService;

    @MockitoBean
    private TrainerWorkoutService trainerWorkoutService;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeEach
    void setUpDashboard() {
        when(notificationService.header(org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(new NotificationService.NotificationHeaderView(0, List.of()));
        when(notificationService.findAll(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyBoolean()
        )).thenReturn(List.of());
        DashboardSummaryResponse summary = new DashboardSummaryResponse(
                0, 0, 0, 0, BigDecimal.ZERO, 0, 0, 0, 0, 0, 0, 0
        );
        when(dashboardService.getDashboard()).thenReturn(new DashboardResponse(
                "테스트 날짜", summary, 0, List.of(), List.of(), List.of()
        ));

        MemberHomeSummaryResponse memberSummary = new MemberHomeSummaryResponse(
                1L, "테스트 회원", 0, 0, 0,
                null, null, null, 0
        );
        when(memberService.findHomeView(1L)).thenReturn(new MemberHomeView(
                LocalDate.of(2026, 8, 12), 4, memberSummary,
                null, null, null, List.of(), List.of()
        ));
        when(memberPortalService.profile(1L)).thenReturn(new MemberProfileView(
                1L, "tester", "테스트 회원", "010-1234-5678",
                LocalDate.of(1990, 1, 1), MemberGender.MALE,
                "tester@gymrovia.example", "ACTIVE", 10, 3, 5
        ));
        when(membershipService.findActiveProducts()).thenReturn(List.of());
        when(memberWorkoutService.routine(1L)).thenReturn(List.of());
        when(memberWorkoutService.workoutDays(1L)).thenReturn(List.of());
        when(membershipService.findAllProducts()).thenReturn(List.of());
        when(paymentService.findActiveMembers()).thenReturn(List.of());
        when(facilityService.getSummary()).thenReturn(new FacilitySummaryResponse(0, 0, 0, 0));
        when(facilityService.findEquipment(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
        when(trainerMemberService.homeProfile(1L)).thenReturn(new TrainerHomeProfileResponse(
                "테스트 트레이너", "근력 운동", 0, 0, 0, 0
        ));
        when(trainerMemberService.homeMembers(1L)).thenReturn(List.of());
        when(trainerMemberService.profile(1L)).thenReturn(
                new TrainerProfileView("테스트 트레이너", "010-1234-5678", "근력 운동"));
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("name=\"_csrf\"")
                ));
    }

    @Test
    void signupPageIsPublic() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("회원가입")
                ));
    }

    @Test
    void anonymousUserCanSignupWithLocalAccount() throws Exception {
        CreateMemberRequest request = new CreateMemberRequest(
                "신규 회원", "010-9876-5432", LocalDate.of(1995, 5, 10),
                MemberGender.FEMALE, "newmember", "password123", true
        );

        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .param("name", "신규 회원")
                        .param("phone", "010-9876-5432")
                        .param("birthDate", "1995-05-10")
                        .param("gender", "FEMALE")
                        .param("loginId", "newmember")
                        .param("password", "password123")
                        .param("passwordConfirmation", "password123")
                        .param("trainerRequested", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(memberService).create(request);
    }

    @Test
    void signupRejectsMismatchedPasswordConfirmation() throws Exception {
        mockMvc.perform(post("/signup")
                        .with(csrf())
                        .param("name", "신규 회원")
                        .param("phone", "010-9876-5432")
                        .param("birthDate", "1995-05-10")
                        .param("gender", "FEMALE")
                        .param("loginId", "newmember")
                        .param("password", "password123")
                        .param("passwordConfirmation", "different123"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("비밀번호와 비밀번호 확인이 일치하지 않습니다.")
                ));

        verify(memberService, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(
                        "Location",
                        "/login?redirect=%2Fadmin%2Fdashboard"
                ));
    }

    @Test
    void adminCanAccessAdminPage() throws Exception {
        mockMvc.perform(get("/admin/dashboard").session(session(SessionUser.ROLE_ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void adminMembershipProductTabRenders() throws Exception {
        mockMvc.perform(get("/admin/memberships")
                        .param("view", "products")
                        .session(session(SessionUser.ROLE_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("새 회원권 상품")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("등록 상품")));
    }

    @Test
    void adminMembershipHistoryTabRenders() throws Exception {
        mockMvc.perform(get("/admin/memberships")
                        .param("view", "history")
                        .session(session(SessionUser.ROLE_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("결제·환불 내역")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("refund-dialog")));
    }

    @Test
    void adminFacilityPageRendersEditAction() throws Exception {
        mockMvc.perform(get("/admin/facilities").session(session(SessionUser.ROLE_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("시설 및 기구 관리")));
    }

    @Test
    void memberCannotAccessAdminPage() throws Exception {
        mockMvc.perform(get("/admin/dashboard").session(session(SessionUser.ROLE_MEMBER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void trainerCanAccessTrainerPage() throws Exception {
        mockMvc.perform(get("/trainer/home").session(session(SessionUser.ROLE_TRAINER)))
                .andExpect(status().isOk());
    }

    @Test
    void memberCannotAccessTrainerPage() throws Exception {
        mockMvc.perform(get("/trainer/home").session(session(SessionUser.ROLE_MEMBER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotAccessTrainerPage() throws Exception {
        mockMvc.perform(get("/trainer/home").session(session(SessionUser.ROLE_ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCanAccessMemberPage() throws Exception {
        mockMvc.perform(get("/member/home").session(session(SessionUser.ROLE_MEMBER)))
                .andExpect(status().isOk());
    }

    @Test
    void memberNotificationPageRendersOnlyMemberNavigation() throws Exception {
        mockMvc.perform(get("/member/notifications").session(session(SessionUser.ROLE_MEMBER)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("회원 메뉴")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("관리자 메뉴")
                )));
    }

    @Test
    void adminAttendanceQrPageProvidesCsrfTokenForIssueRequest() throws Exception {
        mockMvc.perform(get("/admin/attendance/qr")
                        .session(session(SessionUser.ROLE_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "name=\"_csrf\""
                )))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "name=\"_csrf_header\""
                )));
    }

    @Test
    void memberCanAccessMemberPortalPages() throws Exception {
        MockHttpSession memberSession = session(SessionUser.ROLE_MEMBER);

        mockMvc.perform(get("/member/memberships").session(memberSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/member/attendance").session(memberSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/member/workouts").session(memberSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/member/payments").session(memberSession))
                .andExpect(status().isOk());
        mockMvc.perform(get("/member/profile").session(memberSession))
                .andExpect(status().isOk());
    }

    @Test
    void memberDirectCheckInEndpointDoesNotExist() throws Exception {
        mockMvc.perform(post("/member/attendance/check-in")
                        .with(csrf())
                        .session(session(SessionUser.ROLE_MEMBER)))
                .andExpect(status().isNotFound());

        verify(attendanceService, never()).checkInMember(
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    @Test
    void memberDirectCheckOutEndpointDoesNotExist() throws Exception {
        mockMvc.perform(post("/member/attendance/check-out")
                        .with(csrf())
                        .session(session(SessionUser.ROLE_MEMBER)))
                .andExpect(status().isNotFound());

        verify(attendanceService, never()).checkoutMember(
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    @Test
    void memberQrPageRendersAutomaticCsrfProtectedCheckInForm() throws Exception {
        when(attendanceQrService.verifyScan(1L, "center-token"))
                .thenReturn("verification-token");
        when(attendanceQrService.isCheckedIn(1L)).thenReturn(false);

        mockMvc.perform(get("/member/attendance/qr")
                        .param("token", "center-token")
                        .session(session(SessionUser.ROLE_MEMBER)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "id=\"qr-attendance-form\""
                )))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "/member/attendance/qr/check-in"
                )))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "name=\"_csrf\""
                )));
    }

    @Test
    void adminCannotUseMemberSelfCheckIn() throws Exception {
        mockMvc.perform(post("/member/attendance/check-in")
                        .with(csrf())
                        .session(session(SessionUser.ROLE_ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousApiRequestReturnsUnauthorizedJson() throws Exception {
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().json("""
                        {
                          "success": false,
                          "error": {
                            "code": "UNAUTHORIZED"
                          }
                        }
                        """));
    }

    @Test
    void memberApiRequestReturnsForbiddenJson() throws Exception {
        mockMvc.perform(get("/api/members").session(session(SessionUser.ROLE_MEMBER)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().json("""
                        {
                          "success": false,
                          "error": {
                            "code": "FORBIDDEN"
                          }
                        }
                        """));
    }

    @Test
    void invalidRoleSessionIsTreatedAsAnonymous() throws Exception {
        mockMvc.perform(get("/admin/dashboard").session(session("UNKNOWN")))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void authenticatedUserCanLogout() throws Exception {
        MockHttpSession session = session(SessionUser.ROLE_MEMBER);

        mockMvc.perform(post("/logout").with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        assertThrows(
                IllegalStateException.class,
                () -> session.getAttribute(SessionUser.SESSION_KEY)
        );
    }

    private MockHttpSession session(String role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(1L, "tester", "tester@gymrovia.example", role)
        );
        return session;
    }
}
