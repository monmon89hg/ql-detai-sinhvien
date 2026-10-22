package vn.edu.group12.qldt.config.staging;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Bat @PreAuthorize o staging mac dinh.
 * QA open chi tam tat method-security interceptor; cac kiem tra nghiep vu
 * hoac phan quyen truc tiep trong Service van co hieu luc.
 */
@Configuration
@Profile("staging & !qa-open & !preview & !hoi-dong-local")
@EnableMethodSecurity
public class StagingMethodSecurityConfig {
}
