package vn.edu.group12.qldt;

import java.util.List;
import java.util.Set;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import vn.edu.group12.qldt.config.SecurityConfig;
import vn.edu.group12.qldt.config.auth.PasswordConfig;
import vn.edu.group12.qldt.service.auth.CustomUserDetailsService;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;
import vn.edu.group12.qldt.repository.admin.NguoiDungVaiTroRepository;
import vn.edu.group12.qldt.controller.hoidong.HoiDongController;
import vn.edu.group12.qldt.dto.hoidong.NhomPhanCongLuaChon;
import vn.edu.group12.qldt.entity.hoidong.HoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiHoiDong;
import vn.edu.group12.qldt.service.hoidong.HoiDongService;
import vn.edu.group12.qldt.service.hoidong.PhanCongHoiDongService;
import vn.edu.group12.qldt.service.hoidong.ThanhVienHoiDongService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import vn.edu.group12.qldt.service.hoidong.NguoiDungHienThiService;
import static org.hamcrest.Matchers.not;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.group12.qldt.dto.hoidong.HoiDongSuaForm;
import vn.edu.group12.qldt.dto.hoidong.DanhSachHoiDong;
import vn.edu.group12.qldt.dto.hoidong.DotDangKyLuaChon;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.mockito.ArgumentMatchers.argThat;

// Render the real templates and serve the real assets without accessing MySQL.
@WebMvcTest(HoiDongController.class)
@Import({SecurityConfig.class, PasswordConfig.class, CustomUserDetailsService.class})
@ActiveProfiles("hoi-dong-local")
class FrontendIntegrationTests {

    @MockitoBean
    private vn.edu.group12.qldt.service.hoidong.MoChamService moChamService;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private HoiDongService hoiDongService;

    @MockitoBean
    private ThanhVienHoiDongService thanhVienHoiDongService;

    @MockitoBean
    private PhanCongHoiDongService phanCongHoiDongService;

    @MockitoBean
    private NguoiDungRepository nguoiDungRepository;

    @MockitoBean
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @MockitoBean
    private NguoiDungHienThiService nguoiDungHienThiService;

    @Test
    void staticAssetsKeepTheirPublicUrls() throws Exception {
        for (String url : List.of("/css/base.css", "/css/layout.css",
                "/css/components.css", "/js/common.js", "/images/ute_logo.png")) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
    }

    @Test
    void editButtonOnlyAppearsForDeanAndDraftCouncil() throws Exception {
        HoiDong hoiDong = council();
        hoiDong.setTrangThai(TrangThaiHoiDong.NHAP);
        when(hoiDongService.layChiTiet("HD_TEST")).thenReturn(hoiDong);
        mvc.perform(get("/hoi-dong/HD_TEST").with(user("TK").roles("TRUONG_KHOA")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/hoi-dong/HD_TEST/sua")));
        mvc.perform(get("/hoi-dong/HD_TEST").with(user("ADMIN").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("/hoi-dong/HD_TEST/sua"))));
        hoiDong.setTrangThai(TrangThaiHoiDong.SAN_SANG);
        mvc.perform(get("/hoi-dong/HD_TEST").with(user("TK").roles("TRUONG_KHOA")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("/hoi-dong/HD_TEST/sua"))));
    }

    @Test
    void editFormKeepsRoundReadOnlyAndValidationKeepsSubmittedValues() throws Exception {
        HoiDong hoiDong = council();
        hoiDong.setTrangThai(TrangThaiHoiDong.NHAP);
        ReflectionTestUtils.setField(hoiDong, "version", 2L);
        when(hoiDongService.layDeSua("HD_TEST")).thenReturn(hoiDong);
        mvc.perform(get("/hoi-dong/HD_TEST/sua").with(user("TK").roles("TRUONG_KHOA")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("value=\"DOT_TEST\" readonly")))
            .andExpect(content().string(not(containsString("name=\"maDot\""))))
            .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(post("/hoi-dong/HD_TEST/sua").with(user("TK").roles("TRUONG_KHOA"))
                .with(csrf()).param("maHoiDong", "NEW").param("tenHoiDong", " ").param("version", "2"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Vui lòng nhập tên hội đồng.")))
            .andExpect(content().string(containsString("value=\"NEW\"")));
        verify(hoiDongService, never()).suaThongTin(any(), any());
    }

    @Test
    void validEditRedirectsToNewCodeAndIgnoresUneditableFields() throws Exception {
        when(hoiDongService.layDeSua("HD_TEST")).thenReturn(council());
        HoiDong daLuu = council();
        daLuu.setMaHoiDong("NEW");
        when(hoiDongService.suaThongTin(eq("HD_TEST"), any(HoiDongSuaForm.class))).thenReturn(daLuu);
        mvc.perform(post("/hoi-dong/HD_TEST/sua").with(user("TK").roles("TRUONG_KHOA"))
                .with(csrf()).param("maHoiDong", "NEW").param("tenHoiDong", "Tên mới")
                .param("version", "2").param("maDot", "FAKE").param("trangThai", "DA_KHOA"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/hoi-dong/NEW"));
        verify(hoiDongService).suaThongTin(eq("HD_TEST"), argThat(form ->
            "NEW".equals(form.getMaHoiDong()) && form.getVersion().equals(2L)));
    }

    @Test
    void editRequiresDeanRoleAndCsrf() throws Exception {
        for (String role : List.of("ADMIN", "GIANG_VIEN", "SINH_VIEN", "TRUONG_BO_MON")) {
            mvc.perform(get("/hoi-dong/HD_TEST/sua").with(user("U").roles(role)))
                .andExpect(status().isForbidden());
            mvc.perform(post("/hoi-dong/HD_TEST/sua").with(user("U").roles(role)).with(csrf()))
                .andExpect(status().isForbidden());
        }
        mvc.perform(post("/hoi-dong/HD_TEST/sua").with(user("TK").roles("TRUONG_KHOA")))
            .andExpect(status().isForbidden());
        verify(hoiDongService, never()).suaThongTin(any(), any());
    }

    @Test
    void councilListRendersSharedFragments() throws Exception {
        when(hoiDongService.traCuuDanhSach(null)).thenReturn(danhSach("", Set.of()));

        when(nguoiDungHienThiService.layHoTenHienTai())
            .thenReturn("Đặng Minh Phú");

        mvc.perform(get("/hoi-dong").with(user("TEST_TK").roles("TRUONG_KHOA")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("<option value=\"\" selected=\"selected\">Tất cả đợt</option>")))
            .andExpect(content().string(containsString("HD_TEST")))
            .andExpect(content().string(containsString("workflow-status--waiting")))
            .andExpect(content().string(containsString(">Sẵn sàng</span>")))
            .andExpect(content().string(not(containsString(">SAN_SANG</td>"))))
            .andExpect(content().string(containsString("/css/components.css")))
            .andExpect(content().string(containsString("/images/ute_logo.png")))
            .andExpect(content().string(containsString("Đặng Minh Phú")));
    }

    @Test
    void adminCanViewCouncilDetailWithoutActionForms() throws Exception {
        when(hoiDongService.layChiTiet("HD_TEST")).thenReturn(council());
        when(phanCongHoiDongService.layDanhSachDaPhanCong("HD_TEST"))
            .thenReturn(List.of());
        when(thanhVienHoiDongService.layDanhSach("HD_TEST"))
            .thenReturn(List.of());

        mvc.perform(get("/hoi-dong/HD_TEST")
                .with(user("TEST_ADMIN").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("HD_TEST")))
            .andExpect(content().string(containsString("workflow-status--waiting")))
            .andExpect(content().string(containsString(">Sẵn sàng</span>")))
            .andExpect(content().string(
                not(containsString("/hoi-dong/HD_TEST/phan-cong"))))
            .andExpect(content().string(
                not(containsString("/hoi-dong/HD_TEST/thanh-vien"))));
    }

    @Test
    void councilCreationFormRendersFromFrontendDirectory() throws Exception {
        mvc.perform(get("/hoi-dong/tao").with(user("TEST_TK").roles("TRUONG_KHOA")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("name=\"maHoiDong\"")))
            .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void creatingACouncilInAnOlderRoundReturnsToThatRound() throws Exception {
        mvc.perform(post("/hoi-dong/tao").with(user("TK").roles("TRUONG_KHOA")).with(csrf())
                .param("maHoiDong", "HD_NEW").param("tenHoiDong", "Hội đồng mới").param("maDot", "OLDER"))
            .andExpect(redirectedUrl("/hoi-dong?maDot=OLDER"));
        verify(hoiDongService).taoHoiDong(any());
    }

    @Test
    void assignmentFormRendersOptionsAndCsrfToken() throws Exception {
        NhomPhanCongLuaChon group = mock(NhomPhanCongLuaChon.class);
        when(group.getMaNhom()).thenReturn("NHOM_TEST");
        when(group.getTenNhom()).thenReturn("Test group");
        when(group.getMaDeTai()).thenReturn("DT_TEST");
        when(group.getTenDeTai()).thenReturn("Test topic");
        when(hoiDongService.layChiTiet("HD_TEST")).thenReturn(council());
        when(phanCongHoiDongService.layDanhSachCoThePhanCong("HD_TEST"))
            .thenReturn(List.of(group));

        mvc.perform(get("/hoi-dong/HD_TEST")
                .with(user("TEST_TK").roles("TRUONG_KHOA")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/hoi-dong/HD_TEST/phan-cong")))
            .andExpect(content().string(containsString("value=\"NHOM_TEST\"")))
            .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void assignmentPostKeepsItsRouteAndRedirect() throws Exception {
        mvc.perform(post("/hoi-dong/HD_TEST/phan-cong")
                .with(user("TEST_TK").roles("TRUONG_KHOA"))
                .with(csrf()).param("maNhom", "NHOM_TEST"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/hoi-dong/HD_TEST"));

        verify(phanCongHoiDongService).phanCong(eq("HD_TEST"), any());
    }

    @Test
    void assignmentPostStillRequiresCsrf() throws Exception {
        mvc.perform(post("/hoi-dong/HD_TEST/phan-cong")
                .with(user("TEST_TK").roles("TRUONG_KHOA"))
                .param("maNhom", "NHOM_TEST"))
            .andExpect(status().isForbidden());

        verify(phanCongHoiDongService, never()).phanCong(any(), any());
    }

    @Test
    void councilPagesStillRequireTheDeanRole() throws Exception {
        mvc.perform(get("/hoi-dong").with(user("TEST_GV").roles("GIANG_VIEN")))
            .andExpect(status().isForbidden());
    }

    @Test
    void filterSelectsTheRoundAndDeleteFormsOnlyAppearForEligibleCouncilsAndDean() throws Exception {
        when(hoiDongService.traCuuDanhSach("DOT_TEST")).thenReturn(danhSach("DOT_TEST", Set.of("HD_TEST")));
        mvc.perform(get("/hoi-dong").param("maDot", "DOT_TEST").with(user("TK").roles("TRUONG_KHOA")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("value=\"DOT_TEST\"")))
            .andExpect(content().string(containsString("selected=\"selected\"")))
            .andExpect(content().string(containsString("/hoi-dong/HD_TEST/xoa")))
            .andExpect(content().string(containsString("name=\"version\" value=\"2\"")))
            .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(get("/hoi-dong").param("maDot", "DOT_TEST").with(user("ADMIN").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("/hoi-dong/HD_TEST/xoa"))));
        when(hoiDongService.traCuuDanhSach("DOT_TEST")).thenReturn(danhSach("DOT_TEST", Set.of()));
        mvc.perform(get("/hoi-dong").param("maDot", "DOT_TEST").with(user("TK").roles("TRUONG_KHOA")))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("/hoi-dong/HD_TEST/xoa"))));
    }

    @Test
    void emptySelectedRoundAndInvalidSelectionRenderAnEmptyPageOr404() throws Exception {
        var selected = danhSach("DOT_TEST", Set.of());
        when(hoiDongService.traCuuDanhSach("DOT_TEST")).thenReturn(new DanhSachHoiDong(
            selected.danhSachDot(), "DOT_TEST", List.of(), Set.of()));
        mvc.perform(get("/hoi-dong").param("maDot", "DOT_TEST").with(user("TK").roles("TRUONG_KHOA")))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Chưa có hội đồng nào trong đợt đăng ký này.")));
        doThrow(new jakarta.persistence.EntityNotFoundException("Đợt không tồn tại"))
            .when(hoiDongService).traCuuDanhSach("UNKNOWN");
        mvc.perform(get("/hoi-dong").param("maDot", "UNKNOWN").with(user("TK").roles("TRUONG_KHOA")))
            .andExpect(status().isNotFound());
    }

    @Test
    void deletionPreservesRoundFilterAndDisplaysBusinessFailures() throws Exception {
        mvc.perform(post("/hoi-dong/HD_TEST/xoa").with(user("TK").roles("TRUONG_KHOA"))
                .with(csrf()).param("version", "2").param("maDot", "DOT_TEST"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/hoi-dong?maDot=DOT_TEST"))
            .andExpect(flash().attribute("successMessage", "Xóa hội đồng thành công."));
        verify(hoiDongService).xoaHoiDong("HD_TEST", 2L);
        doThrow(new IllegalArgumentException("Hội đồng đã có thành viên"))
            .when(hoiDongService).xoaHoiDong("BLOCKED", 0L);
        mvc.perform(post("/hoi-dong/BLOCKED/xoa").with(user("TK").roles("TRUONG_KHOA"))
                .with(csrf()).param("version", "0").param("maDot", ""))
            .andExpect(redirectedUrl("/hoi-dong?maDot="))
            .andExpect(flash().attribute("errorMessage", "Hội đồng đã có thành viên"));
    }

    @Test
    void deletionRequiresDeanCsrfAndAValidVersion() throws Exception {
        for (String role : List.of("ADMIN", "GIANG_VIEN", "SINH_VIEN", "TRUONG_BO_MON")) {
            mvc.perform(post("/hoi-dong/HD_TEST/xoa").with(user("U").roles(role))
                    .with(csrf()).param("version", "2"))
                .andExpect(status().isForbidden());
        }
        mvc.perform(post("/hoi-dong/HD_TEST/xoa").with(user("TK").roles("TRUONG_KHOA"))
                .param("version", "2"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/hoi-dong/HD_TEST/xoa").with(user("TK").roles("TRUONG_KHOA"))
                .with(csrf()).param("version", "-1"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/hoi-dong/HD_TEST/xoa").with(user("TK").roles("TRUONG_KHOA")).with(csrf()))
            .andExpect(status().isBadRequest());
        verify(hoiDongService, never()).xoaHoiDong(any(), any());
    }

    private DanhSachHoiDong danhSach(String maDot, Set<String> maCoTheXoa) {
        DotDangKyLuaChon dot = new DotDangKyLuaChon() {
            public String getMaDot() { return "DOT_TEST"; }
            public String getTenDot() { return "Đợt kiểm thử"; }
        };
        HoiDong hoiDong = council();
        if (maCoTheXoa.contains("HD_TEST")) {
            hoiDong.setTrangThai(TrangThaiHoiDong.NHAP);
        }
        ReflectionTestUtils.setField(hoiDong, "version", 2L);
        return new DanhSachHoiDong(List.of(dot), maDot, List.of(hoiDong), maCoTheXoa);
    }

    private HoiDong council() {
        HoiDong council = new HoiDong();
        council.setMaHoiDong("HD_TEST");
        council.setTenHoiDong("Test council");
        council.setMaDot("DOT_TEST");
        council.setMaNguoiTao("TEST_TK");
        council.setTrangThai(TrangThaiHoiDong.SAN_SANG);
        return council;
    }
}
