package vn.edu.group12.qldt;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import vn.edu.group12.qldt.config.staging.StagingSecurityConfig;
import vn.edu.group12.qldt.controller.DashboardController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Chi kiem tra HTTP security filter QA, khong can DB that.
 * 404 nghia la da vuot qua filter nhung MVC test slice khong import route do.
 */
@WebMvcTest(DashboardController.class)
@Import(StagingSecurityConfig.class)
@ActiveProfiles({"staging", "qa-open"})
class QaOpenSecurityTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DataSource dataSource;

    @Test
    void anonymousUserMustStillSignIn() throws Exception {
        mvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void studentCanReachRoleProtectedReadRoutesInQaMode() throws Exception {
        mvc.perform(get("/hoi-dong")
                .with(user("QA_SV01").roles("SINH_VIEN")))
            .andExpect(status().isNotFound());

        mvc.perform(get("/de-tai/duyet")
                .with(user("QA_SV01").roles("SINH_VIEN")))
            .andExpect(status().isNotFound());
    }

    @Test
    void studentCanReachPostRouteWithCsrfInQaMode() throws Exception {
        mvc.perform(post("/dot-dang-ky/tao")
                .with(user("QA_SV01").roles("SINH_VIEN"))
                .with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    void csrfStillProtectsMutatingRequests() throws Exception {
        mvc.perform(post("/dot-dang-ky/tao")
                .with(user("QA_SV01").roles("SINH_VIEN")))
            .andExpect(status().isForbidden());
    }
}
