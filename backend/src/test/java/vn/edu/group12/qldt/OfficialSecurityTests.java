package vn.edu.group12.qldt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import vn.edu.group12.qldt.config.SecurityConfig;
import vn.edu.group12.qldt.config.auth.PasswordConfig;
import vn.edu.group12.qldt.controller.DashboardController;
import vn.edu.group12.qldt.controller.admin.*;
import vn.edu.group12.qldt.controller.auth.AuthController;
import vn.edu.group12.qldt.controller.detai.*;
import vn.edu.group12.qldt.controller.hoidong.*;
import vn.edu.group12.qldt.entity.admin.NguoiDung;
import vn.edu.group12.qldt.entity.admin.NguoiDungVaiTro;
import vn.edu.group12.qldt.entity.admin.VaiTro;
import vn.edu.group12.qldt.repository.admin.*;
import vn.edu.group12.qldt.service.admin.*;
import vn.edu.group12.qldt.service.auth.CustomUserDetailsService;
import vn.edu.group12.qldt.service.auth.QldtUserDetails;
import vn.edu.group12.qldt.service.detai.*;
import vn.edu.group12.qldt.service.hoidong.*;
import vn.edu.group12.qldt.support.SecurityTestUsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;

import vn.edu.group12.qldt.dto.hoidong.NhomKetQuaHienThi;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiem;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiemId;
import vn.edu.group12.qldt.enums.hoidong.LoaiPhieuCham;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiPhieuCham;
import vn.edu.group12.qldt.dto.hoidong.DotDangKyLuaChon;
import vn.edu.group12.qldt.dto.hoidong.KetQuaTraCuuHienThi;

import static org.hamcrest.Matchers.not;

/** Real filter chain, login provider, BCrypt, UserDetailsService, controllers and templates; mock persistence/services. */
@WebMvcTest({AuthController.class, DashboardController.class, NguoiDungController.class,
        BoMonController.class, ThongBaoController.class, DeTaiController.class, DotDangKyController.class,
        HoiDongController.class, ChamDiemHoiDongController.class, KetQuaHoiDongController.class,
        TraCuuKetQuaController.class})
@Import({SecurityConfig.class, PasswordConfig.class, CustomUserDetailsService.class})
class OfficialSecurityTests {
    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @MockitoBean NguoiDungRepository users;
    @MockitoBean NguoiDungVaiTroRepository roles;
    @MockitoBean BoMonRepository departments;
    @MockitoBean VaiTroRepository roleDefinitions;
    @MockitoBean NguoiDungService nguoiDungService;
    @MockitoBean BoMonService boMonService;
    @MockitoBean ThongBaoService thongBaoService;
    @MockitoBean DeTaiService deTaiService;
    @MockitoBean DeTaiGiangVienService deTaiGiangVienService;
    @MockitoBean DotDangKyService dotDangKyService;
    @MockitoBean HoiDongService hoiDongService;
    @MockitoBean ThanhVienHoiDongService thanhVienHoiDongService;
    @MockitoBean PhanCongHoiDongService phanCongHoiDongService;
    @MockitoBean ChamDiemHoiDongService chamDiemHoiDongService;
    @MockitoBean AdminXemChamDiemService adminXemChamDiemService;
    @MockitoBean TongHopKetQuaService tongHopKetQuaService;
    @MockitoBean TraCuuKetQuaService traCuuKetQuaService;
    @MockitoBean AdminTraCuuKetQuaService adminTraCuuKetQuaService;
    @MockitoBean NguoiDungHienThiService nguoiDungHienThiService;

    @Test
    void loginUsesTv1TemplateAndCsrf() throws Exception {
        mvc.perform(get("/login"))
            .andExpect(status().isOk()).andExpect(view().name("auth/login"))
            .andExpect(content().string(containsString("name=\"email\"")))
            .andExpect(content().string(containsString("name=\"matKhau\"")))
            .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void validEmailAndBcryptPasswordCreateAnOfficialSession() throws Exception {
        String password = account("HOAT_DONG", "ADMIN");
        MockHttpSession before = new MockHttpSession();
        String oldId = before.getId();
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/login").session(before).with(csrf())
                .param("email", "  ADMIN@example.test  ").param("matKhau", password))
            .andExpect(redirectedUrl("/dashboard")).andExpect(authenticated().withUsername("admin@example.test"))
            .andReturn().getRequest().getSession(false);
        assertThat(session.getId()).isNotEqualTo(oldId);
        SecurityContext context = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        QldtUserDetails user = (QldtUserDetails) context.getAuthentication().getPrincipal();
        assertThat(user.getMaNguoiDung()).isEqualTo("DB_ADMIN");
        assertThat(user.getPassword()).isNull(); // Credentials erased after authentication.
        mvc.perform(get("/dashboard").session(session)).andExpect(status().isOk());
        verify(users).findByEmailIgnoreCase("ADMIN@example.test");
        verify(roles).findAllById_MaNguoiDung("DB_ADMIN");
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        account("HOAT_DONG", "ADMIN");
        mvc.perform(post("/login").with(csrf()).param("email", "admin@example.test")
                .param("matKhau", UUID.randomUUID().toString()))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
    }

    @Test
    void adminCanViewDraftGradingSheetButCannotSaveOrSubmit() throws Exception {
        NhomKetQuaHienThi nhom = mock(NhomKetQuaHienThi.class);
        when(nhom.getMaNhom()).thenReturn("N01");
        when(nhom.getTenNhom()).thenReturn("Nhóm kiểm thử");
        when(nhom.getMaDeTai()).thenReturn("DT01");
        when(nhom.getTenDeTai()).thenReturn("Đề tài kiểm thử");
        when(nhom.getMaHoiDong()).thenReturn("HD01");
        when(nhom.getTenHoiDong()).thenReturn("Hội đồng kiểm thử");

        PhieuChamDiem phieu = new PhieuChamDiem();
        phieu.setId(new PhieuChamDiemId("N01", "GV01"));
        phieu.setLoaiPhieu(LoaiPhieuCham.HOI_DONG);
        phieu.setTrangThai(TrangThaiPhieuCham.NHAP);
        phieu.setDiem(new BigDecimal("8.25"));
        phieu.setNhanXet("Nhận xét bản nháp");

        when(adminXemChamDiemService.layDanhSachNhom())
            .thenReturn(List.of(nhom));
        when(adminXemChamDiemService.layThongTinNhom("N01"))
            .thenReturn(nhom);
        when(adminXemChamDiemService.layPhieuChamTheoNhom("N01"))
            .thenReturn(List.of(phieu));

        var admin = user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"));

        mvc.perform(get("/cham-diem").with(admin))
            .andExpect(status().isOk())
            .andExpect(view().name("assessment/cham-diem/list"))
            .andExpect(content().string(containsString("Xem phiếu")));

        mvc.perform(get("/cham-diem/N01")
                .with(user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(view().name("assessment/cham-diem/admin-detail"))
            .andExpect(content().string(containsString("Nhận xét bản nháp")))
            .andExpect(content().string(containsString("8.25")))
            .andExpect(content().string(
                not(containsString("/cham-diem/N01/luu-nhap"))));

        mvc.perform(post("/cham-diem/N01/luu-nhap").with(csrf())
                .with(user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"))))
            .andExpect(status().isForbidden());

        mvc.perform(post("/cham-diem/N01/nop").with(csrf())
                .with(user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"))))
            .andExpect(status().isForbidden());

        verifyNoInteractions(chamDiemHoiDongService);
    }

    @Test
    void adminCanViewAggregationStatusButCannotAggregate() throws Exception {
        NhomKetQuaHienThi nhom = mock(NhomKetQuaHienThi.class);
        when(nhom.getMaNhom()).thenReturn("N01");
        when(nhom.getTenNhom()).thenReturn("Nhóm kiểm thử");
        when(nhom.getMaDeTai()).thenReturn("DT01");
        when(nhom.getTenDeTai()).thenReturn("Đề tài kiểm thử");
        when(nhom.getMaHoiDong()).thenReturn("HD01");
        when(nhom.getTenHoiDong()).thenReturn("Hội đồng kiểm thử");

        when(tongHopKetQuaService.layDanhSachNhomChoAdmin())
            .thenReturn(List.of(nhom));

        mvc.perform(get("/ket-qua/tong-hop")
                .with(user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(view().name("assessment/ket-qua/tong-hop-list"))
            .andExpect(content().string(containsString("Chưa tổng hợp")))
            .andExpect(content().string(containsString("Chỉ xem")))
            .andExpect(content().string(
                not(containsString("/ket-qua/N01/tong-hop"))));

        mvc.perform(post("/ket-qua/N01/tong-hop").with(csrf())
                .with(user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"))))
            .andExpect(status().isForbidden());

        verify(tongHopKetQuaService).layDanhSachNhomChoAdmin();
        verifyNoMoreInteractions(tongHopKetQuaService);
    }

    @Test
    void adminCanViewPendingPublicationButCannotPublish() throws Exception {
        NhomKetQuaHienThi nhom = mock(NhomKetQuaHienThi.class);
        when(nhom.getMaNhom()).thenReturn("N01");
        when(nhom.getTenNhom()).thenReturn("Nhóm kiểm thử");
        when(nhom.getMaDeTai()).thenReturn("DT01");
        when(nhom.getTenDeTai()).thenReturn("Đề tài kiểm thử");
        when(nhom.getMaHoiDong()).thenReturn("HD01");
        when(nhom.getTenHoiDong()).thenReturn("Hội đồng kiểm thử");
        when(nhom.getTrangThaiKetQua()).thenReturn("DA_TONG_HOP");
        when(nhom.getDiemCuoi()).thenReturn(new BigDecimal("8.25"));

        when(tongHopKetQuaService.layDanhSachNhomChoAdmin())
            .thenReturn(List.of(nhom));

        mvc.perform(get("/ket-qua/quan-ly")
                .with(user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(view().name("assessment/ket-qua/quan-ly-list"))
            .andExpect(content().string(containsString("Chờ công bố")))
            .andExpect(content().string(containsString("8.25")))
            .andExpect(content().string(containsString("Chỉ xem")))
            .andExpect(content().string(
                not(containsString("/ket-qua/N01/cong-bo"))));

        mvc.perform(post("/ket-qua/N01/cong-bo").with(csrf())
                .with(user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"))))
            .andExpect(status().isForbidden());

        verify(tongHopKetQuaService).layDanhSachNhomChoAdmin();
        verifyNoMoreInteractions(tongHopKetQuaService);
    }

    @Test
    void adminCanLookUpPublishedResultsByRoundOnly() throws Exception {
        DotDangKyLuaChon dot = mock(DotDangKyLuaChon.class);
        when(dot.getMaDot()).thenReturn("DOT01");
        when(dot.getTenDot()).thenReturn("Đợt kiểm thử");

        KetQuaTraCuuHienThi ketQua = mock(KetQuaTraCuuHienThi.class);
        when(ketQua.getMaNhom()).thenReturn("N01");
        when(ketQua.getTenNhom()).thenReturn("Nhóm kiểm thử");
        when(ketQua.getMaDeTai()).thenReturn("DT01");
        when(ketQua.getTenDeTai()).thenReturn("Đề tài kiểm thử");
        when(ketQua.getDiemCuoi()).thenReturn(new BigDecimal("8.25"));
        when(ketQua.getNhanXetTongHop()).thenReturn("Đạt yêu cầu");

        when(adminTraCuuKetQuaService.layDotCoKetQuaCongBo())
            .thenReturn(List.of(dot));
        when(adminTraCuuKetQuaService.traCuuTheoDot("DOT01"))
            .thenReturn(List.of(ketQua));

        mvc.perform(get("/ket-qua/cua-toi")
                .param("maDot", "DOT01")
                .with(user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(view().name("assessment/ket-qua/cua-toi"))
            .andExpect(content().string(containsString("N01")))
            .andExpect(content().string(containsString("8.25")))
            .andExpect(content().string(containsString("Đạt yêu cầu")))
            .andExpect(content().string(
                containsString("của tất cả nhóm")));

        mvc.perform(get("/ket-qua/cua-toi")
                .param("maDot", "DOT_KHAC")
                .with(user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"))))
            .andExpect(status().isForbidden());

        verify(adminTraCuuKetQuaService).traCuuTheoDot("DOT01");
        verify(adminTraCuuKetQuaService, never()).traCuuTheoDot("DOT_KHAC");
        verifyNoInteractions(traCuuKetQuaService);
    }

    @Test
    void deanCanInspectGradingAndAggregationButCannotSubmitEither() throws Exception {
        NhomKetQuaHienThi nhom = mock(NhomKetQuaHienThi.class);
        when(nhom.getMaNhom()).thenReturn("N01");
        when(nhom.getTenNhom()).thenReturn("Nhóm kiểm thử");
        when(nhom.getMaDeTai()).thenReturn("DT01");
        when(nhom.getTenDeTai()).thenReturn("Đề tài kiểm thử");
        when(nhom.getMaHoiDong()).thenReturn("HD01");
        when(nhom.getTenHoiDong()).thenReturn("Hội đồng kiểm thử");
        when(nhom.getTrangThaiKetQua()).thenReturn("DA_TONG_HOP");
        when(nhom.getDiemCuoi()).thenReturn(new BigDecimal("8.25"));

        when(adminXemChamDiemService.layDanhSachNhom()).thenReturn(List.of(nhom));
        when(adminXemChamDiemService.layThongTinNhom("N01")).thenReturn(nhom);
        when(adminXemChamDiemService.layPhieuChamTheoNhom("N01")).thenReturn(List.of());
        when(tongHopKetQuaService.layDanhSachNhomChoTruongKhoa()).thenReturn(List.of(nhom));

        mvc.perform(get("/cham-diem")
                .with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Xem phiếu")));
        mvc.perform(get("/cham-diem/N01")
                .with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isOk())
            .andExpect(view().name("assessment/cham-diem/admin-detail"))
            .andExpect(content().string(not(containsString("/cham-diem/N01/nop"))));
        mvc.perform(get("/ket-qua/tong-hop")
                .with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("8.25")))
            .andExpect(content().string(containsString("Chỉ xem")))
            .andExpect(content().string(not(containsString("/ket-qua/N01/tong-hop"))));

        for (String path : List.of("/cham-diem/N01/luu-nhap", "/cham-diem/N01/nop",
                "/ket-qua/N01/tong-hop")) {
            mvc.perform(post(path).with(csrf())
                    .with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA", "GIANG_VIEN"))))
                .andExpect(status().isForbidden());
        }
        verifyNoInteractions(chamDiemHoiDongService);
        verify(tongHopKetQuaService).layDanhSachNhomChoTruongKhoa();
        verifyNoMoreInteractions(tongHopKetQuaService);
    }

    @Test
    void deanCanLookUpPublishedResultsButNotAnUnknownRound() throws Exception {
        DotDangKyLuaChon dot = mock(DotDangKyLuaChon.class);
        when(dot.getMaDot()).thenReturn("DOT01");
        when(dot.getTenDot()).thenReturn("Đợt kiểm thử");
        KetQuaTraCuuHienThi ketQua = mock(KetQuaTraCuuHienThi.class);
        when(ketQua.getMaNhom()).thenReturn("N01");
        when(ketQua.getTenNhom()).thenReturn("Nhóm kiểm thử");
        when(ketQua.getMaDeTai()).thenReturn("DT01");
        when(ketQua.getTenDeTai()).thenReturn("Đề tài kiểm thử");
        when(ketQua.getDiemCuoi()).thenReturn(new BigDecimal("8.25"));

        when(adminTraCuuKetQuaService.layDotCoKetQuaCongBo()).thenReturn(List.of(dot));
        when(adminTraCuuKetQuaService.traCuuTheoDot("DOT01")).thenReturn(List.of(ketQua));

        mvc.perform(get("/ket-qua/cua-toi").param("maDot", "DOT01")
                .with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("N01")))
            .andExpect(content().string(containsString("8.25")));
        mvc.perform(get("/ket-qua/cua-toi").param("maDot", "DOT_KHAC")
                .with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isForbidden());

        verify(adminTraCuuKetQuaService).traCuuTheoDot("DOT01");
        verify(adminTraCuuKetQuaService, never()).traCuuTheoDot("DOT_KHAC");
        verifyNoInteractions(traCuuKetQuaService);
    }

    @Test
    void lecturerCanLookUpPublishedResultsButCannotUseAnUnknownRoundOrManageResults() throws Exception {
        DotDangKyLuaChon dot = mock(DotDangKyLuaChon.class);
        when(dot.getMaDot()).thenReturn("DOT01");
        when(dot.getTenDot()).thenReturn("Đợt kiểm thử");
        KetQuaTraCuuHienThi ketQua = mock(KetQuaTraCuuHienThi.class);
        when(ketQua.getMaNhom()).thenReturn("N01");
        when(ketQua.getTenNhom()).thenReturn("Nhóm kiểm thử");
        when(ketQua.getMaDeTai()).thenReturn("DT01");
        when(ketQua.getTenDeTai()).thenReturn("Đề tài kiểm thử");
        when(ketQua.getDiemCuoi()).thenReturn(new BigDecimal("8.25"));

        when(adminTraCuuKetQuaService.layDotCoKetQuaCongBo()).thenReturn(List.of(dot));
        when(adminTraCuuKetQuaService.traCuuTheoDot("DOT01")).thenReturn(List.of(ketQua));

        mvc.perform(get("/ket-qua/cua-toi").param("maDot", "DOT01")
                .with(user(SecurityTestUsers.user("GV", "GIANG_VIEN"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("N01")))
            .andExpect(content().string(containsString("8.25")))
            .andExpect(content().string(containsString("của tất cả nhóm")));
        mvc.perform(get("/ket-qua/cua-toi").param("maDot", "DOT_KHAC")
                .with(user(SecurityTestUsers.user("GV", "GIANG_VIEN"))))
            .andExpect(status().isForbidden());
        mvc.perform(get("/ket-qua/quan-ly")
                .with(user(SecurityTestUsers.user("GV", "GIANG_VIEN"))))
            .andExpect(status().isForbidden());

        verify(adminTraCuuKetQuaService).traCuuTheoDot("DOT01");
        verify(adminTraCuuKetQuaService, never()).traCuuTheoDot("DOT_KHAC");
        verifyNoInteractions(traCuuKetQuaService);
    }

    @Test
    void lockedAccountIsRejectedEvenWithTheCorrectPassword() throws Exception {
        String password = account("KHOA", "ADMIN");
        mvc.perform(post("/login").with(csrf()).param("email", "admin@example.test").param("matKhau", password))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
    }

    @Test
    void missingAccountOrRoleIsRejected() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("email", "missing@example.test")
                .param("matKhau", UUID.randomUUID().toString()))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
        String password = account("HOAT_DONG");
        mvc.perform(post("/login").with(csrf()).param("email", "admin@example.test").param("matKhau", password))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
    }

    @Test
    void legacyFormFieldsCannotAuthenticate() throws Exception {
        String password = account("HOAT_DONG", "ADMIN");
        mvc.perform(post("/login").with(csrf()).param("username", "admin@example.test").param("password", password))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
    }

    @Test
    void anonymousUsersCannotReachProtectedPages() throws Exception {
        for (String path : List.of("/dashboard", "/admin/users", "/hoi-dong", "/cham-diem", "/ket-qua/cua-toi")) {
            mvc.perform(get(path)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        }
    }

    static Stream<Arguments> forbiddenRoutes() {
        return Stream.of(
            Arguments.of("SINH_VIEN", "GET", "/admin/users"),
            Arguments.of("SINH_VIEN", "POST", "/admin/users/DB_ADMIN/trang-thai"),
            Arguments.of("SINH_VIEN", "GET", "/hoi-dong"),
            Arguments.of("SINH_VIEN", "POST", "/hoi-dong/tao"),
            Arguments.of("SINH_VIEN", "GET", "/cham-diem/N01"),
            Arguments.of("SINH_VIEN", "POST", "/cham-diem/N01/nop"),
            Arguments.of("SINH_VIEN", "POST", "/ket-qua/N01/tong-hop"),
            Arguments.of("SINH_VIEN", "POST", "/ket-qua/N01/cong-bo"),
            Arguments.of("SINH_VIEN", "GET", "/de-tai/duyet"),
            Arguments.of("SINH_VIEN", "POST", "/de-tai/de-xuat"),
            Arguments.of("SINH_VIEN", "GET", "/dot-dang-ky/tao"),
            Arguments.of("SINH_VIEN", "POST", "/thong-bao/quan-ly"),
            Arguments.of("GIANG_VIEN", "POST", "/de-tai/DT01/duyet"),
            Arguments.of("GIANG_VIEN", "POST", "/de-tai/DT01/phan-cong"),
            Arguments.of("GIANG_VIEN", "POST", "/dot-dang-ky/tao"),
            Arguments.of("GIANG_VIEN", "POST", "/hoi-dong/HD01/phan-cong"),
            Arguments.of("GIANG_VIEN", "POST", "/ket-qua/N01/cong-bo"),
            Arguments.of("TRUONG_BO_MON", "POST", "/de-tai/DT01/duyet"),
            Arguments.of("TRUONG_BO_MON", "GET", "/admin/bo-mon"),
            Arguments.of("TRUONG_KHOA", "GET", "/admin/users"),
            Arguments.of("TRUONG_KHOA", "POST", "/cham-diem/N01/nop"),
            Arguments.of("ADMIN", "POST", "/hoi-dong/tao"),
            Arguments.of("UNKNOWN", "GET", "/dashboard")
        );
    }

    @ParameterizedTest
    @MethodSource("forbiddenRoutes")
    void directUrlCannotBypassRoles(String role, String method, String path) throws Exception {
        var request = "POST".equals(method) ? post(path).with(csrf()) : get(path);
        mvc.perform(request.with(user(SecurityTestUsers.user("DB_USER", role))))
            .andExpect(status().isForbidden());
        verifyNoInteractions(nguoiDungService, boMonService, thongBaoService, deTaiService,
                dotDangKyService, hoiDongService, chamDiemHoiDongService, tongHopKetQuaService);
    }

    @Test
    void adminCanRenderRealAccountManagementPage() throws Exception {
        when(nguoiDungService.layDanhSach(any())).thenReturn(Page.empty());
        mvc.perform(get("/admin/users").with(user(SecurityTestUsers.user("DB_ADMIN", "ADMIN"))))
            .andExpect(status().isOk()).andExpect(view().name("admin/users/list"));
        verify(nguoiDungService).layDanhSach(any());
    }

    @Test
    void allowedRolesReachTheirActualControllers() throws Exception {
        mvc.perform(get("/hoi-dong").with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isOk()).andExpect(view().name("assessment/hoi-dong/list"));
        mvc.perform(get("/cham-diem").with(user(SecurityTestUsers.user("GV", "GIANG_VIEN"))))
            .andExpect(status().isOk()).andExpect(view().name("assessment/cham-diem/list"));
        mvc.perform(get("/ket-qua/cua-toi").with(user(SecurityTestUsers.user("SV", "SINH_VIEN"))))
            .andExpect(status().isOk()).andExpect(view().name("assessment/ket-qua/cua-toi"));
        mvc.perform(post("/ket-qua/N01/cong-bo").with(csrf())
                .with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(redirectedUrl("/ket-qua/quan-ly"));
        verify(tongHopKetQuaService).congBo("N01");
    }

    @Test
    void lecturerAndDepartmentHeadProposeUsingDatabaseId() throws Exception {
        for (String role : List.of("GIANG_VIEN", "TRUONG_BO_MON")) {
            mvc.perform(post("/de-tai/de-xuat").with(csrf())
                    .with(user(SecurityTestUsers.user("DB_" + role, role))))
                .andExpect(redirectedUrl("/de-tai"));
            verify(deTaiService).deXuatDeTai(any(), eq("DB_" + role));
        }
    }

    @Test
    void deanCreatesRegistrationRoundUsingDatabaseId() throws Exception {
        mvc.perform(post("/dot-dang-ky/tao").with(csrf())
                .with(user(SecurityTestUsers.user("DB_DEAN", "TRUONG_KHOA"))))
            .andExpect(redirectedUrl("/dot-dang-ky"));
        verify(dotDangKyService).createDotDangKy(any(), eq("DB_DEAN"));
    }

    @Test
    void missingOrInvalidCsrfPreventsLoginAndBusinessWrites() throws Exception {
        mvc.perform(post("/login").param("email", "admin@example.test").param("matKhau", "unused"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/ket-qua/N01/cong-bo").with(user("dean").roles("TRUONG_KHOA")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/ket-qua/N01/cong-bo").with(csrf().useInvalidToken())
                .with(user("dean").roles("TRUONG_KHOA"))).andExpect(status().isForbidden());
        verifyNoInteractions(users, tongHopKetQuaService);
    }

    @Test
    void unreviewedEndpointsAreDeniedForEveryRoleAndHttpVerb() throws Exception {
        for (String role : List.of("ADMIN", "TRUONG_KHOA", "TRUONG_BO_MON", "GIANG_VIEN", "SINH_VIEN")) {
            for (String path : List.of("/unknown", "/dang-ky", "/admin/new-module", "/ket-qua/unknown/action")) {
                mvc.perform(get(path).with(user("test").roles(role))).andExpect(status().isForbidden());
                mvc.perform(post(path).with(csrf()).with(user("test").roles(role))).andExpect(status().isForbidden());
            }
        }
        mvc.perform(delete("/admin/users/DB_ADMIN").with(csrf()).with(user("admin").roles("ADMIN")))
            .andExpect(status().isForbidden());
        mvc.perform(put("/de-tai/DT01/duyet").with(csrf()).with(user("dean").roles("TRUONG_KHOA")))
            .andExpect(status().isForbidden());
    }

    @Test
    void issue9RoutesUseOfficialRoleRules() throws Exception {
        // Các URL của Nhóm đã được xét duyệt; chỉ cho phép đúng vai trò và đúng phương thức.
        mvc.perform(get("/dang-ky-de-tai/duyet").with(user("sv").roles("SINH_VIEN")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/dang-ky-de-tai/N01/duyet").with(csrf())
                .with(user("sv").roles("SINH_VIEN")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/nhom/tao").with(csrf())
                .with(user("tk").roles("TRUONG_KHOA")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/bao-cao/nop").with(csrf())
                .with(user("gv").roles("GIANG_VIEN")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/nhom/N01/xoa-tat-ca").with(csrf())
                .with(user("sv").roles("SINH_VIEN")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/nhom/tao").with(user("sv").roles("SINH_VIEN")))
            .andExpect(status().isForbidden());
        // MVC slice chưa nạp Controller của Issue #9: 404 chứng minh đã qua bộ lọc quyền.
        mvc.perform(get("/nhom").with(user("sv").roles("SINH_VIEN")))
            .andExpect(status().isNotFound());
        mvc.perform(get("/dang-ky-de-tai/duyet").with(user("tk").roles("TRUONG_KHOA")))
            .andExpect(status().isNotFound());
    }

    @Test
    void logoutRequiresCsrfAndInvalidatesTheAuthenticatedSession() throws Exception {
        String password = account("HOAT_DONG", "ADMIN");
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/login").with(csrf())
                .param("email", "admin@example.test").param("matKhau", password))
            .andExpect(authenticated()).andReturn().getRequest().getSession(false);
        String oldId = session.getId();
        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/dashboard").session(session)).andExpect(status().isOk());
        mvc.perform(post("/logout").session(session).with(csrf()))
            .andExpect(redirectedUrl("/login?logout")).andExpect(unauthenticated())
            .andExpect(cookie().maxAge("JSESSIONID", 0));
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/dashboard").cookie(new Cookie("JSESSIONID", oldId)))
            .andExpect(redirectedUrl("/login"));
    }

    private String account(String status, String... roleNames) {
        String password = UUID.randomUUID().toString();
        NguoiDung account = new NguoiDung();
        account.setMaNguoiDung("DB_ADMIN");
        account.setEmail("admin@example.test");
        account.setMatKhauHash(encoder.encode(password));
        account.setTrangThai(status);
        when(users.findByEmailIgnoreCase(any())).thenAnswer(invocation ->
                "admin@example.test".equalsIgnoreCase(invocation.getArgument(0)) ? Optional.of(account) : Optional.empty());
        List<NguoiDungVaiTro> grantedRoles = Stream.of(roleNames).map(name -> {
            VaiTro role = mock(VaiTro.class);
            when(role.getMaVaiTro()).thenReturn(name);
            NguoiDungVaiTro link = new NguoiDungVaiTro();
            link.setVaiTro(role);
            return link;
        }).toList();
        when(roles.findAllById_MaNguoiDung("DB_ADMIN")).thenReturn(grantedRoles);
        return password;
    }
}
