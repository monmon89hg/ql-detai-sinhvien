package vn.edu.group12.qldt.config.hoidong;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("hoi-dong-local")
@EnableMethodSecurity
public class HoiDongLocalSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JdbcUserDetailsManager userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager manager =
            new JdbcUserDetailsManager(dataSource);

        manager.setUsersByUsernameQuery("""
            SELECT
                ma_nguoi_dung,
                mat_khau_hash,
                (trang_thai = 'HOAT_DONG') AS enabled
            FROM nguoi_dung
            WHERE ma_nguoi_dung = ?
            """);

        manager.setAuthoritiesByUsernameQuery("""
            SELECT
                ma_nguoi_dung,
                CONCAT('ROLE_', ma_vai_tro) AS authority
            FROM nguoi_dung_vai_tro
            WHERE ma_nguoi_dung = ?
            """);

        return manager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()
                .requestMatchers(
                    "/hoi-dong",
                    "/hoi-dong/**"
                ).hasRole("TRUONG_KHOA")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/dashboard", true)
            );

        return http.build();
    }
}