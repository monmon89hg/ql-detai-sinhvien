package vn.edu.group12.qldt.service.auth;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.Assert;

/** Email is the login name; the immutable database ID is used by business services. */
public final class QldtUserDetails extends User {

    private static final long serialVersionUID = 1L;
    private final String maNguoiDung;

    public QldtUserDetails(String maNguoiDung, String email, String passwordHash,
            boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        super(email, passwordHash, enabled, true, true, enabled, authorities);
        Assert.hasText(maNguoiDung, "Database user ID is required");
        this.maNguoiDung = maNguoiDung;
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }
}
