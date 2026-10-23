package vn.edu.group12.qldt.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import vn.edu.group12.qldt.service.auth.CustomUserDetailsService;

/** One database authentication mechanism and one RBAC policy for every environment. */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public DaoAuthenticationProvider databaseAuthenticationProvider(
            CustomUserDetailsService users, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            DaoAuthenticationProvider databaseAuthenticationProvider) throws Exception {
        http
            .authenticationProvider(databaseAuthenticationProvider)
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                // Let the container render an error without a login/403 redirect loop.
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET, "/login", "/error", "/favicon.ico",
                        "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers(HttpMethod.HEAD, "/css/**", "/js/**", "/images/**").permitAll()

                // TV1: account/department administration and notification management.
                .requestMatchers(HttpMethod.GET, "/admin/users", "/admin/users/new",
                        "/admin/users/{id}/edit", "/admin/bo-mon", "/admin/bo-mon/new",
                        "/admin/bo-mon/{id}/edit").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/users", "/admin/users/{id}",
                        "/admin/users/{id}/trang-thai", "/admin/bo-mon",
                        "/admin/bo-mon/{id}", "/admin/bo-mon/{id}/trang-thai").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/thong-bao/quan-ly",
                        "/thong-bao/quan-ly/new", "/thong-bao/quan-ly/{id}/edit")
                    .hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers(HttpMethod.POST, "/thong-bao/quan-ly",
                        "/thong-bao/quan-ly/{id}", "/thong-bao/quan-ly/{id}/cong-bo",
                        "/thong-bao/quan-ly/{id}/an").hasAnyRole("ADMIN", "TRUONG_KHOA")

                // TV2: preserve the existing staging policy; no implicit ADMIN inheritance.
                .requestMatchers(HttpMethod.GET, "/de-tai/de-xuat")
                    .hasAnyRole("GIANG_VIEN", "TRUONG_BO_MON")
                .requestMatchers(HttpMethod.POST, "/de-tai/de-xuat")
                    .hasAnyRole("GIANG_VIEN", "TRUONG_BO_MON")
                .requestMatchers(HttpMethod.GET, "/de-tai/duyet", "/dot-dang-ky/tao",
                        "/dot-dang-ky/sua/{id}").hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.POST, "/de-tai/{id}/duyet",
                        "/de-tai/{id}/tu-choi", "/de-tai/{id}/cong-bo",
                        "/de-tai/{id}/phan-cong", "/de-tai/phan-cong/{id}/xoa",
                        "/dot-dang-ky/tao", "/dot-dang-ky/sua/{id}",
                        "/dot-dang-ky/{id}/cong-bo", "/dot-dang-ky/{id}/dong")
                    .hasRole("TRUONG_KHOA")

                // TV4: assignment/ownership checks still run inside the real services.
                .requestMatchers(HttpMethod.GET, "/hoi-dong")
                    .hasAnyRole("TRUONG_KHOA", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/hoi-dong/tao")
                    .hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.GET, "/hoi-dong/{id}")
                    .hasAnyRole("TRUONG_KHOA", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/hoi-dong/tao", "/hoi-dong/{id}/thanh-vien",
                        "/hoi-dong/{id}/thanh-vien/{lecturer}/xoa",
                        "/hoi-dong/{id}/san-sang", "/hoi-dong/{id}/phan-cong")
                    .hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.GET, "/cham-diem", "/cham-diem/{id}")
                    .hasAnyRole("GIANG_VIEN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/ket-qua/tong-hop")
                    .hasAnyRole("GIANG_VIEN", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/cham-diem/{id}/luu-nhap",
                        "/cham-diem/{id}/nop", "/ket-qua/{id}/tong-hop").hasRole("GIANG_VIEN")
                .requestMatchers(HttpMethod.GET, "/ket-qua/quan-ly").hasAnyRole("TRUONG_KHOA","ADMIN")
                .requestMatchers(HttpMethod.POST, "/ket-qua/{id}/cong-bo").hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.GET, "/ket-qua/cua-toi")
                    .hasAnyRole("SINH_VIEN", "ADMIN")

                // TV3: nhóm sinh viên, đăng ký đề tài và nộp báo cáo.
                // Kiểm tra quyền nhóm trưởng, thành viên và giảng viên tiếp tục ở tầng Service.
                .requestMatchers(HttpMethod.GET, "/nhom/tao", "/nhom/cua-toi", "/bao-cao/nop")
                    .hasRole("SINH_VIEN")
                .requestMatchers(HttpMethod.GET, "/dang-ky-de-tai/duyet")
                    .hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.GET, "/nhom", "/nhom/{maNhom}",
                        "/dang-ky-de-tai", "/bao-cao", "/bao-cao/{maNhom}/{phienBan}/tai")
                    .authenticated()
                .requestMatchers(HttpMethod.POST, "/dang-ky-de-tai/{maNhom}/duyet",
                        "/dang-ky-de-tai/{maNhom}/tu-choi")
                    .hasRole("TRUONG_KHOA")
                .requestMatchers(HttpMethod.POST, "/nhom/tao",
                        "/nhom/{maNhom}/yeu-cau",
                        "/nhom/{maNhom}/yeu-cau/{maSinhVien}/duyet",
                        "/nhom/{maNhom}/yeu-cau/{maSinhVien}/tu-choi",
                        "/nhom/{maNhom}/thanh-vien/{maSinhVien}/xoa",
                        "/nhom/{maNhom}/chot", "/dang-ky-de-tai", "/bao-cao/nop")
                    .hasRole("SINH_VIEN")

                .requestMatchers(HttpMethod.GET, "/", "/dashboard", "/thong-bao",
                        "/de-tai", "/de-tai/chi-tiet/{id}", "/dot-dang-ky")
                    .hasAnyRole("ADMIN", "TRUONG_KHOA", "TRUONG_BO_MON", "GIANG_VIEN", "SINH_VIEN")
                // Mọi tuyến đường chưa được xét duyệt đều bị từ chối.
                .anyRequest().denyAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("matKhau")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
