package vn.edu.group12.qldt.config.staging;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Tai khoan QA dung MySQL that. Neu bat them profile qa-open, moi tai khoan
 * dang nhap duoc vao cac route hien co de kiem tra giao dien/tich hop.
 * Mac dinh staging van phan quyen theo vai tro nhu cu.
 */
@Configuration
@Profile("staging & !preview & !hoi-dong-local")
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
    public SecurityFilterChain stagingSecurityFilterChain(
            HttpSecurity http, Environment environment) throws Exception {

        boolean qaOpen = environment.acceptsProfiles(Profiles.of("qa-open"));

        if (qaOpen) {
            // QA: tai khoan da dang nhap co the truy cap cac route da co Controller.
            // KHONG mo truy cap an danh va KHONG tat CSRF.
            http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico")
                    .permitAll()
                .anyRequest().authenticated()
            );
        } else {
            // Staging thong thuong: bao toan phan quyen theo role.
            http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/hoi-dong", "/hoi-dong/**")
                    .hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.POST, "/hoi-dong", "/hoi-dong/**")
                    .hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.GET, "/cham-diem", "/cham-diem/**")
                    .hasRole("GIANG_VIEN")
                .requestMatchers(HttpMethod.POST, "/cham-diem", "/cham-diem/**")
                    .hasRole("GIANG_VIEN")
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
                .requestMatchers(HttpMethod.GET, "/**").authenticated()
                .anyRequest().denyAll()
            );
        }

        http.formLogin(form -> form
            .defaultSuccessUrl("/dashboard", true)
            .permitAll()
        ).logout(logout -> logout.permitAll());

        return http.build();
    }
}
