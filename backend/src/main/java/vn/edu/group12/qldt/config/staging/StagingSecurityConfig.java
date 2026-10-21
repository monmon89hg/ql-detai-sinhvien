package vn.edu.group12.qldt.config.staging;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Shared integration-testing environment with real MySQL users and CSRF.
 * New mutation routes are blocked until they receive an explicit role rule.
 */
@Configuration
@Profile("staging & !preview & !hoi-dong-local")
@EnableMethodSecurity
public class StagingSecurityConfig {

    @Bean
    public PasswordEncoder stagingPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager stagingUserDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        manager.setUsersByUsernameQuery("""
            SELECT ma_nguoi_dung, mat_khau_hash,
                   (trang_thai = 'HOAT_DONG') AS enabled
            FROM nguoi_dung
            WHERE ma_nguoi_dung = ?
            """);
        manager.setAuthoritiesByUsernameQuery("""
            SELECT ma_nguoi_dung, CONCAT('ROLE_', ma_vai_tro) AS authority
            FROM nguoi_dung_vai_tro
            WHERE ma_nguoi_dung = ?
            """);
        return manager;
    }

    @Bean
    public SecurityFilterChain stagingSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico")
                    .permitAll()
                // Existing council and assessment routes: enforce their roles for reads and writes.
                .requestMatchers(HttpMethod.GET, "/hoi-dong", "/hoi-dong/**")
                    .hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.POST, "/hoi-dong", "/hoi-dong/**")
                    .hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.GET, "/cham-diem", "/cham-diem/**")
                    .hasRole("GIANG_VIEN")
                .requestMatchers(HttpMethod.POST, "/cham-diem", "/cham-diem/**")
                    .hasRole("GIANG_VIEN")
                // Proposal belongs to teaching staff, approvals and period setup to the dean.
                .requestMatchers(HttpMethod.GET, "/de-tai/de-xuat")
                    .hasAnyRole("GIANG_VIEN", "TRUONG_BO_MON")
                .requestMatchers(HttpMethod.POST, "/de-tai/de-xuat")
                    .hasAnyRole("GIANG_VIEN", "TRUONG_BO_MON")
                .requestMatchers(HttpMethod.GET, "/de-tai/duyet",
                    "/dot-dang-ky/tao", "/dot-dang-ky/sua/**")
                    .hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.POST, "/de-tai/**", "/dot-dang-ky/**")
                    .hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.GET, "/admin/**")
                    .hasRole("ADMIN")
                // Browse other pages as an authenticated tester; no anonymous data access.
                .requestMatchers(HttpMethod.GET, "/**").authenticated()
                // Future forms/APIs must register explicit write permissions first.
                .anyRequest().denyAll()
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout.permitAll());
        // Keep CSRF enabled for browser form submissions.
        return http.build();
    }
}
