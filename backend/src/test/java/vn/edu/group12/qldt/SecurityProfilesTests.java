package vn.edu.group12.qldt;

import java.util.UUID;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import vn.edu.group12.qldt.service.auth.CustomUserDetailsService;
import vn.edu.group12.qldt.service.detai.DeTaiService;
import vn.edu.group12.qldt.service.hoidong.HoiDongService;
import vn.edu.group12.qldt.support.SecurityTestUsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Full Boot/JPA startup on an isolated in-memory database. Not a MySQL compatibility test. */
class SecurityProfilesTests {
    @ParameterizedTest(name = "one official security configuration: {0}")
    @ValueSource(strings = {"", "local", "staging", "hoi-dong-local", "local,hoi-dong-local",
            "staging,hoi-dong-local", "staging,qa-open", "preview"})
    void everyEnvironmentUsesTheSameAuthenticationAndMethodSecurity(String profiles) throws Exception {
        // Command-line overrides take precedence over application.properties and environment values.
        // A shared MySQL URL can never be used by this test.
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(QldtApplication.class).run(
                "--spring.profiles.active=" + profiles,
                "--spring.datasource.url=jdbc:h2:mem:security_" + UUID.randomUUID() + ";MODE=MySQL",
                "--spring.datasource.driver-class-name=org.h2.Driver",
                "--spring.datasource.username=sa", "--spring.datasource.password=",
                "--spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "--spring.jpa.hibernate.ddl-auto=create-drop", "--spring.jpa.show-sql=false",
                "--spring.flyway.enabled=false", "--app.ket-qua.email.enabled=false",
                "--spring.main.allow-bean-definition-overriding=false", "--server.port=0",
                "--logging.level.root=WARN")) {
            assertThat(context.getBeansOfType(SecurityFilterChain.class)).hasSize(1);
            assertThat(context.getBean(FilterChainProxy.class).getFilterChains()).hasSize(1);
            assertThat(context.getBeansOfType(PasswordEncoder.class)).hasSize(1);
            assertThat(context.getBeansOfType(UserDetailsService.class)).hasSize(1);
            assertThat(context.getBean(UserDetailsService.class)).isInstanceOf(CustomUserDetailsService.class);
            assertThat(context.getBeansOfType(DaoAuthenticationProvider.class)).hasSize(1);

            SecurityContextHolder.getContext().setAuthentication(SecurityTestUsers.authentication("SV", "SINH_VIEN"));
            assertThatThrownBy(() -> context.getBean(HoiDongService.class).layDanhSach())
                .isInstanceOf(AccessDeniedException.class);
            assertThatThrownBy(() -> context.getBean(DeTaiService.class).congBoDeTai("DT01"))
                .isInstanceOf(AccessDeniedException.class);
            SecurityContextHolder.clearContext();

            MockMvc mvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) context)
                    .apply(springSecurity()).build();
            mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("auth/login"));
            mvc.perform(get("/admin/users").with(user("student").roles("SINH_VIEN")))
                .andExpect(status().isForbidden());
            mvc.perform(post("/hoi-dong/tao").with(csrf()).with(user("student").roles("SINH_VIEN")))
                .andExpect(status().isForbidden());
            mvc.perform(get("/unknown").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
