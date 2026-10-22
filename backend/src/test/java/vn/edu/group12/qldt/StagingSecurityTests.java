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

/** Security-filter tests only. No real database access or business mutation. */
@WebMvcTest(DashboardController.class)
@Import(StagingSecurityConfig.class)
@ActiveProfiles("staging")
class StagingSecurityTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DataSource dataSource;

    @Test
    void anonymousVisitorMustSignIn() throws Exception {
        mvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void signedInStudentCanSeeDashboard() throws Exception {
        mvc.perform(get("/dashboard")
                .with(user("STUDENT_TEST").roles("SINH_VIEN")))
            .andExpect(status().isOk());
    }

    @Test
    void studentCannotManageRegistrationPeriods() throws Exception {
        mvc.perform(post("/dot-dang-ky/tao")
                .with(user("STUDENT_TEST").roles("SINH_VIEN"))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void deanCanReachAuthorizedRegistrationPostRoute() throws Exception {
        // No period controller in this MVC slice: 404 proves authorization passed.
        mvc.perform(post("/dot-dang-ky/tao")
                .with(user("DEAN_TEST").roles("TRUONG_KHOA"))
                .with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    void evenDeanNeedsCsrfToWrite() throws Exception {
        mvc.perform(post("/dot-dang-ky/tao")
                .with(user("DEAN_TEST").roles("TRUONG_KHOA")))
            .andExpect(status().isForbidden());
    }

    @Test
    void lecturerCanReachProposalRouteButStudentCannot() throws Exception {
        mvc.perform(post("/de-tai/de-xuat")
                .with(user("LECTURER_TEST").roles("GIANG_VIEN"))
                .with(csrf()))
            .andExpect(status().isNotFound());
        mvc.perform(post("/de-tai/de-xuat")
                .with(user("STUDENT_TEST").roles("SINH_VIEN"))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void lecturerCannotApproveTopicsOrManageCouncil() throws Exception {
        mvc.perform(post("/de-tai/DT01/duyet")
                .with(user("LECTURER_TEST").roles("GIANG_VIEN"))
                .with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/hoi-dong")
                .with(user("LECTURER_TEST").roles("GIANG_VIEN")))
            .andExpect(status().isForbidden());
    }

    @Test
    void resultActionsRequireTheirRespectiveRoles() throws Exception {
        // MVC slice không nạp controller kết quả: 404 nghĩa là đã qua lớp lọc quyền.
        mvc.perform(post("/ket-qua/N01/tong-hop")
                .with(user("LECTURER_TEST").roles("GIANG_VIEN"))
                .with(csrf()))
            .andExpect(status().isNotFound());

        mvc.perform(post("/ket-qua/N01/tong-hop")
                .with(user("STUDENT_TEST").roles("SINH_VIEN"))
                .with(csrf()))
            .andExpect(status().isForbidden());

        mvc.perform(post("/ket-qua/N01/cong-bo")
                .with(user("DEAN_TEST").roles("TRUONG_KHOA"))
                .with(csrf()))
            .andExpect(status().isNotFound());

        mvc.perform(post("/ket-qua/N01/cong-bo")
                .with(user("LECTURER_TEST").roles("GIANG_VIEN"))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void unknownWriteEndpointIsClosedByDefault() throws Exception {
        mvc.perform(post("/unregistered-action")
                .with(user("DEAN_TEST").roles("TRUONG_KHOA"))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void roleBoundReadPageIsProtected() throws Exception {
        mvc.perform(get("/de-tai/duyet")
                .with(user("STUDENT_TEST").roles("SINH_VIEN")))
            .andExpect(status().isForbidden());
    }
}
