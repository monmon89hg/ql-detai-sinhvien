package vn.edu.group12.qldt.support;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import vn.edu.group12.qldt.service.auth.QldtUserDetails;

/** Synthetic identities only. This helper never provisions users or passwords in a database. */
public final class SecurityTestUsers {
    private SecurityTestUsers() {
    }

    public static QldtUserDetails user(String id, String... roles) {
        return new QldtUserDetails(id, id.toLowerCase() + "@example.test", UUID.randomUUID().toString(),
                true, Arrays.stream(roles).map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
    }

    public static Authentication authentication(String id, String... roles) {
        QldtUserDetails user = user(id, roles);
        return UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
    }
}
