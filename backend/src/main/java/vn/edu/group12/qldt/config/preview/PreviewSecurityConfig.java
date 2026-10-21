
package vn.edu.group12.qldt.config.preview; 

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("preview")
public class PreviewSecurityConfig {

    @Bean
    public SecurityFilterChain previewSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/css/**", "/js/**", "/images/**",
                "/favicon.ico"
            ).permitAll()
            .requestMatchers(HttpMethod.GET,
                "/", "/dashboard",
                "/hoi-dong", "/hoi-dong/**",
                "/de-tai", "/de-tai/**",
                "/dot-dang-ky", "/dot-dang-ky/**"
            ).authenticated()
            .anyRequest().denyAll()
        ).formLogin(form -> form
            .defaultSuccessUrl("/dashboard", true)
            .permitAll()
        ).logout(logout -> logout.permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder previewPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService previewUserDetailsService(
            @Value("${PREVIEW_USERNAME}") String username,
            @Value("${PREVIEW_PASSWORD}") String password,
            PasswordEncoder encoder) {

        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                "Preview credentials must not be empty"
            );
        }

        return new InMemoryUserDetailsManager(
            User.withUsername(username)
                .password(encoder.encode(password))
                .roles("PREVIEW")
                .build()
        );
    }
}
