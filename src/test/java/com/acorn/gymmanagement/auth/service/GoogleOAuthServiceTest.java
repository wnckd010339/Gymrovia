package com.acorn.gymmanagement.auth.service;

import com.acorn.gymmanagement.auth.form.GoogleSignupForm;
import com.acorn.gymmanagement.auth.mapper.AuthMapper;
import com.acorn.gymmanagement.auth.model.GoogleMemberRegistration;
import com.acorn.gymmanagement.auth.model.OAuthAuthenticatedUser;
import com.acorn.gymmanagement.auth.model.PendingGoogleSignup;
import com.acorn.gymmanagement.common.exception.BusinessException;
import com.acorn.gymmanagement.member.model.MemberGender;
import com.acorn.gymmanagement.security.SessionUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTest {

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private GoogleOAuthService googleOAuthService;

    private PendingGoogleSignup pendingSignup;
    private GoogleSignupForm form;

    @BeforeEach
    void setUp() {
        pendingSignup = new PendingGoogleSignup(
                "google-sub-123",
                "member@gmail.com",
                "홍길동"
        );

        form = new GoogleSignupForm();
        form.setName("홍길동");
        form.setPhone("01012345678");
        form.setBirthDate(LocalDate.of(1995, 1, 1));
        form.setGender(MemberGender.MALE);
        form.setTrainerRequested(false);
        form.setTermsAgreed(true);
    }

    @Test
    void 신규Google회원가입에성공한다() {
        when(authMapper.existsUserByEmail("member@gmail.com"))
                .thenReturn(false);

        when(authMapper.insertGoogleUser(any()))
                .thenAnswer(invocation -> {
                    GoogleMemberRegistration registration =
                            invocation.getArgument(0);

                    registration.setUserId(10L);
                    return 1;
                });

        when(authMapper.insertGoogleOAuthAccount(any()))
                .thenReturn(1);

        when(authMapper.insertGoogleMember(any()))
                .thenReturn(1);

        SessionUser result =
                googleOAuthService.registerGoogleMember(
                        pendingSignup,
                        form
                );

        assertEquals(10L, result.userId());
        assertEquals("member@gmail.com", result.email());
        assertEquals(SessionUser.ROLE_MEMBER, result.role());

        verify(authMapper).insertGoogleUser(any());
        verify(authMapper).insertGoogleOAuthAccount(any());
        verify(authMapper).insertGoogleMember(any());
    }

    @Test
    void 이메일이이미존재하면가입할수없다() {
        when(authMapper.existsUserByEmail("member@gmail.com"))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> googleOAuthService.registerGoogleMember(
                        pendingSignup,
                        form
                )
        );

        verify(authMapper, never())
                .insertGoogleUser(any());
    }

    @Test
    void Google인증정보가없으면가입할수없다() {
        assertThrows(
                BusinessException.class,
                () -> googleOAuthService.registerGoogleMember(
                        null,
                        form
                )
        );

        verifyNoInteractions(authMapper);
    }

    @Test
    void 기존Google회원로그인에성공하면마지막로그인시간을갱신한다() {
        OAuthAuthenticatedUser user = new OAuthAuthenticatedUser(
                10L,
                "member@gmail.com",
                "MEMBER",
                "ACTIVE",
                20L
        );
        when(authMapper.findOAuthUser("GOOGLE", "google-sub-123"))
                .thenReturn(Optional.of(user));

        Optional<OAuthAuthenticatedUser> result =
                googleOAuthService.findActiveUser("google-sub-123");

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().userId());
        verify(authMapper).updateLastLoginAt(10L);
    }

    @Test
    void 등록되지않은Google계정은신규가입대상이다() {
        when(authMapper.findOAuthUser("GOOGLE", "new-google-sub"))
                .thenReturn(Optional.empty());

        Optional<OAuthAuthenticatedUser> result =
                googleOAuthService.findActiveUser("new-google-sub");

        assertTrue(result.isEmpty());
        verify(authMapper, never()).updateLastLoginAt(anyLong());
    }

    @Test
    void 비활성Google계정은로그인할수없다() {
        OAuthAuthenticatedUser user = new OAuthAuthenticatedUser(
                10L,
                "member@gmail.com",
                "MEMBER",
                "SUSPENDED",
                20L
        );
        when(authMapper.findOAuthUser("GOOGLE", "google-sub-123"))
                .thenReturn(Optional.of(user));

        assertThrows(
                BusinessException.class,
                () -> googleOAuthService.findActiveUser("google-sub-123")
        );
        verify(authMapper, never()).updateLastLoginAt(anyLong());
    }

    @Test
    void 회원정보가완성되지않은Google계정은로그인할수없다() {
        OAuthAuthenticatedUser user = new OAuthAuthenticatedUser(
                10L,
                "member@gmail.com",
                "MEMBER",
                "ACTIVE",
                null
        );
        when(authMapper.findOAuthUser("GOOGLE", "google-sub-123"))
                .thenReturn(Optional.of(user));

        assertThrows(
                BusinessException.class,
                () -> googleOAuthService.findActiveUser("google-sub-123")
        );
        verify(authMapper, never()).updateLastLoginAt(anyLong());
    }
}
