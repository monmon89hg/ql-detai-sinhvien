package vn.edu.group12.qldt.nhom;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static vn.edu.group12.qldt.nhom.NhomTestSupport.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@org.junit.jupiter.api.Tag("mysql")
@SpringBootTest(
        properties = {
            "spring.flyway.enabled=true",
            "spring.jpa.hibernate.ddl-auto=none",
            "spring.jpa.show-sql=false"
        })
@AutoConfigureMockMvc
@ActiveProfiles("staging")
@Import(Config.class)
@EnabledIfEnvironmentVariable(
        named = "NHOM_TEST_DB_URL",
        matches = "jdbc:mysql://(127\\.0\\.0\\.1|localhost):[0-9]+/nhom_test_.*")
class NhomStagingTests extends NhomMySqlTestBase {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    Fixture f;

    @BeforeEach
    void setup() {
        f = new Fixture(jdbc);
    }

    @Test
    void anDanhBiChuyenDangNhap() throws Exception {
        mvc.perform(get("/nhom")).andExpect(status().is3xxRedirection());
    }

    @Test
    void stagingChoSinhVienTaoNhomVaVanBatCsrf() throws Exception {
        mvc.perform(
                        post("/nhom/tao")
                                .param("tenNhom", "Nhóm staging")
                                .param("maDot", f.dot)
                                .with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/nhom/tao")
                                .param("tenNhom", "Nhóm staging")
                                .param("maDot", f.dot)
                                .with(testUser(f.sv(1), "SINH_VIEN"))
                                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/nhom/cua-toi"));
        assertThat(f.count("SELECT COUNT(*) FROM nhom_sinh_vien WHERE ma_nguoi_tao=?", f.sv(1)))
                .isEqualTo(1);
    }

    @Test
    void sinhVienKhongDuocVaoTrangDuyetVaGiangVienKhongTaoNhom() throws Exception {
        mvc.perform(get("/dang-ky-de-tai/duyet").with(testUser(f.sv(1), "SINH_VIEN")))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/nhom/tao")
                                .param("tenNhom", "Không hợp lệ")
                                .param("maDot", f.dot)
                                .with(testUser(f.lecturer, "GIANG_VIEN"))
                                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void truongKhoaVaoDuocTrangDuyet() throws Exception {
        mvc.perform(
                        get("/dang-ky-de-tai/duyet")
                                .param("tuKhoa", f.prefix)
                                .with(testUser(f.dean, "TRUONG_KHOA")))
                .andExpect(status().isOk())
                .andExpect(view().name("registrations/review"));
    }
}
