package vn.edu.group12.qldt.service.auth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Never interpret an email, request parameter, or legacy username as a database ID. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static String maNguoiDung() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof QldtUserDetails user)
                || !user.isEnabled() || !user.isAccountNonLocked()) {
            throw new AccessDeniedException("Vui lòng đăng nhập lại bằng tài khoản chính thức.");
        }
        return user.getMaNguoiDung();
    }
}
