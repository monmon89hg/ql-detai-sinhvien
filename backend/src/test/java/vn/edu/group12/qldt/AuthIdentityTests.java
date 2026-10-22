package vn.edu.group12.qldt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.edu.group12.qldt.service.auth.CurrentUser;
import vn.edu.group12.qldt.support.SecurityTestUsers;

import static org.assertj.core.api.Assertions.*;

class AuthIdentityTests {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void emailAndDatabaseIdStayDistinct() {
        var auth = SecurityTestUsers.authentication("DB_GV01", "GIANG_VIEN");
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertThat(auth.getName()).isEqualTo("db_gv01@example.test");
        assertThat(CurrentUser.maNguoiDung()).isEqualTo("DB_GV01");
    }

    @Test
    void oldSessionCannotBeMistakenForAnOfficialDatabaseIdentity() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("QA_SV01", null,
                        AuthorityUtils.createAuthorityList("ROLE_SINH_VIEN")));
        assertThatThrownBy(CurrentUser::maNguoiDung).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void anonymousOrUnauthenticatedIdentityHasNoDatabaseId() {
        assertThatThrownBy(CurrentUser::maNguoiDung).isInstanceOf(AccessDeniedException.class);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.unauthenticated(
                SecurityTestUsers.user("DB_GV01", "GIANG_VIEN"), null));
        assertThatThrownBy(CurrentUser::maNguoiDung).isInstanceOf(AccessDeniedException.class);
    }
}
