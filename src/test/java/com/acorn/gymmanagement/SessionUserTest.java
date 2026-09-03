package com.acorn.gymmanagement;

import com.acorn.gymmanagement.security.SessionUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionUserTest {

    @Test
    void supportedRolesAreValid() {
        assertTrue(sessionUser(SessionUser.ROLE_ADMIN).hasValidRole());
        assertTrue(sessionUser(SessionUser.ROLE_TRAINER).hasValidRole());
        assertTrue(sessionUser(SessionUser.ROLE_MEMBER).hasValidRole());
    }

    @Test
    void unsupportedRoleIsInvalid() {
        SessionUser user = sessionUser("UNKNOWN");

        assertFalse(user.hasValidRole());
        assertThrows(IllegalStateException.class, user::authority);
    }

    @Test
    void roleIsConvertedToSpringSecurityAuthority() {
        assertEquals("ROLE_ADMIN", sessionUser(SessionUser.ROLE_ADMIN).authority());
        assertEquals("ROLE_TRAINER", sessionUser(SessionUser.ROLE_TRAINER).authority());
        assertEquals("ROLE_MEMBER", sessionUser(SessionUser.ROLE_MEMBER).authority());
    }

    @Test
    void eachRoleCanAccessOnlyItsOwnArea() {
        SessionUser admin = sessionUser(SessionUser.ROLE_ADMIN);
        SessionUser trainer = sessionUser(SessionUser.ROLE_TRAINER);
        SessionUser member = sessionUser(SessionUser.ROLE_MEMBER);

        assertTrue(admin.canAccess("/admin/members"));
        assertTrue(admin.canAccess("/admin"));
        assertFalse(admin.canAccess("/member/home"));
        assertTrue(trainer.canAccess("/trainer/home"));
        assertFalse(trainer.canAccess("/admin/dashboard"));
        assertTrue(member.canAccess("/member/home"));
        assertFalse(member.canAccess("/admin/members"));
    }

    @Test
    void eachRoleHasItsOwnDefaultRedirect() {
        assertEquals("/admin/dashboard", sessionUser(SessionUser.ROLE_ADMIN).defaultRedirectPath());
        assertEquals("/trainer/home", sessionUser(SessionUser.ROLE_TRAINER).defaultRedirectPath());
        assertEquals("/member/home", sessionUser(SessionUser.ROLE_MEMBER).defaultRedirectPath());
    }

    private SessionUser sessionUser(String role) {
        return new SessionUser(1L, "tester", "tester@gymrovia.example", role);
    }
}
