package vn.edu.group12.qldt.config.nhom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class NhomBaoCaoSecurityConfiguration {
    // Chỉ bổ sung bảo vệ route Nhóm/đăng ký/báo cáo; giữ nguyên các chain và profile của thành viên khác.
    @Bean
    @Order(0)
    public SecurityFilterChain nhomBaoCaoSecurityFilterChain(HttpSecurity http, Environment env)
            throws Exception {
        http.securityMatcher(
                "/nhom",
                "/nhom/**",
                "/dang-ky-de-tai",
                "/dang-ky-de-tai/**",
                "/bao-cao",
                "/bao-cao/**");
        if (env.acceptsProfiles(Profiles.of("qa-open"))) {
            http.authorizeHttpRequests(a -> a.anyRequest().authenticated());
        } else {
            http.authorizeHttpRequests(
                    a ->
                            a.requestMatchers(
                                            "/dang-ky-de-tai/duyet",
                                            "/dang-ky-de-tai/*/duyet",
                                            "/dang-ky-de-tai/*/tu-choi")
                                    .hasRole("TRUONG_KHOA")
                                    .requestMatchers(HttpMethod.POST, "/**")
                                    .hasRole("SINH_VIEN")
                                    .requestMatchers("/nhom/tao", "/nhom/cua-toi", "/bao-cao/nop")
                                    .hasRole("SINH_VIEN")
                                    .anyRequest()
                                    .authenticated());
        }
        http.exceptionHandling(
                e ->
                        e.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                                .accessDeniedHandler(
                                        (request, response, exception) -> {
                                            response.setStatus(403);
                                            response.setContentType("text/plain;charset=UTF-8");
                                            response.getWriter()
                                                    .write(
                                                            "Không có quyền truy cập hoặc phiên gửi"
                                                                + " biểu mẫu đã hết hạn. Vui lòng"
                                                                + " tải lại trang và thử lại.");
                                        }));
        return http.build();
    }
}
