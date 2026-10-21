
package vn.edu.group12.qldt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import vn.edu.group12.qldt.config.preview.PreviewSecurityConfig;
import vn.edu.group12.qldt.controller.DashboardController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(PreviewSecurityConfig.class)
@ActiveProfiles("preview")
@TestPropertySource(properties = {
    "PREVIEW_USERNAME=preview-test",
    "PREVIEW_PASSWORD=local-test-only"
})
class PreviewSecurityTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void anonymousUserMustLogin() throws Exception {
        mvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void previewUserCanViewDashboard() throws Exception {
        mvc.perform(get("/dashboard")
                .with(user("preview-test").roles("PREVIEW")))
            .andExpect(status().isOk());
    }

    @Test
    void previewUserCannotCreateCouncil() throws Exception {
        mvc.perform(post("/hoi-dong/tao")
                .with(user("preview-test").roles("PREVIEW"))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void previewUserCannotAccessUnapprovedRoute() throws Exception {
        mvc.perform(get("/admin/users")
                .with(user("preview-test").roles("PREVIEW")))
            .andExpect(status().isForbidden());
    }
}
