package vn.edu.group12.qldt;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import vn.edu.group12.qldt.config.SecurityConfig;
import vn.edu.group12.qldt.config.auth.PasswordConfig;
import vn.edu.group12.qldt.controller.hoidong.KetQuaHoiDongController;
import vn.edu.group12.qldt.dto.hoidong.NhomKetQuaHienThi;
import vn.edu.group12.qldt.dto.hoidong.PhieuChamYeuCau;
import vn.edu.group12.qldt.entity.hoidong.*;
import vn.edu.group12.qldt.enums.hoidong.*;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;
import vn.edu.group12.qldt.repository.admin.NguoiDungVaiTroRepository;
import vn.edu.group12.qldt.repository.hoidong.*;
import vn.edu.group12.qldt.service.auth.CustomUserDetailsService;
import vn.edu.group12.qldt.service.hoidong.*;
import vn.edu.group12.qldt.support.SecurityTestUsers;

/** HTTP, real templates and service transaction proxies; no surrounding test transaction. */
@WebMvcTest(KetQuaHoiDongController.class)
@Import({SecurityConfig.class, PasswordConfig.class, CustomUserDetailsService.class,
    MoChamService.class, TinhDiemTrungBinhService.class, KiemTraDuPhieuChamService.class,
    TongHopKetQuaService.class, KetQuaChiTietIntegrationTests.Transactions.class})
class KetQuaChiTietIntegrationTests {
    @TestConfiguration
    @EnableTransactionManagement(proxyTargetClass = true)
    static class Transactions {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:ket_qua_detail;DB_CLOSE_DELAY=-1", "sa", "");
        }
        @Bean PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Autowired MockMvc mvc;
    @MockitoBean NguoiDungRepository users;
    @MockitoBean NguoiDungVaiTroRepository roles;
    @MockitoBean DanhGiaRepository danhGia;
    @MockitoBean HoiDongRepository hoiDong;
    @MockitoBean HoiDongDeTaiRepository phanCong;
    @MockitoBean ThanhVienHoiDongRepository thanhVien;
    @MockitoBean PhieuChamDiemRepository phieu;
    @MockitoBean KetQuaRepository ketQua;
    @MockitoBean TaoEmailThongBaoKetQuaService email;
    @MockitoBean NguoiDungHienThiService tenNguoiDung;
    @MockitoBean XuatExcelKetQuaService excel;
    private NhomKetQuaHienThi nhom;

    @BeforeEach
    void setup() {
        nhom = mock(NhomKetQuaHienThi.class);
        when(nhom.getMaNhom()).thenReturn("N01");
        when(nhom.getTenNhom()).thenReturn("Nhóm kiểm thử");
        when(nhom.getTrangThaiDangKy()).thenReturn("DANG_CHAM");
        when(nhom.getLoaiDeTai()).thenReturn("MON_HOC");
        when(hoiDong.demTruongKhoaHoatDong("TK")).thenReturn(1L);
        when(thanhVien.demGiangVienHopLe("GV01")).thenReturn(1L);
        when(phanCong.layDanhSachNhomChoTruongKhoa()).thenReturn(List.of(nhom));
        when(phanCong.layDanhSachNhomChoChuTich("GV01")).thenReturn(List.of(nhom));
        when(danhGia.dauVao("N01")).thenReturn(Optional.of(new DanhGiaRepository.DauVao(
            "DT01", "MON_HOC", "DANG_CHAM", "DANG_THUC_HIEN", "CONG_BO", null,
            1, 3, 3, "DOT01")));
        when(danhGia.duocTongHopNgoaiHoiDong("N01", "GV01")).thenReturn(true);
        when(phanCong.timTrangThaiDangKyTheoMaNhom("N01")).thenReturn(Optional.of("DANG_CHAM"));
        when(phieu.timLoaiDotTheoMaNhom("N01")).thenReturn(Optional.of("MON_HOC"));
        var requiredMarker = marker();
        when(phieu.layPhieuChamYeuCau("N01")).thenReturn(List.of(requiredMarker));
        when(phieu.findById_MaNhomOrderById_MaGiangVienAsc("N01")).thenReturn(List.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DA_DUYET", "DANG_THUC_HIEN"})
    void deanCanViewBeforeOpeningWithoutCalculatingScore(String state) throws Exception {
        when(nhom.getTrangThaiDangKy()).thenReturn(state);
        mvc.perform(get("/ket-qua/N01/chi-tiet").with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Nhóm chưa mở chấm")))
            .andExpect(content().string(containsString("GV01")))
            .andExpect(content().string(containsString("/bao-cao/N01/1/tai")))
            .andExpect(content().string(not(containsString("Điểm trung bình:"))));
        verify(phieu, never()).timLoaiDotTheoMaNhom(anyString());
    }

    @Test
    void deanCanViewMissingBallotsWithoutRollbackFailure() throws Exception {
        mvc.perform(get("/ket-qua/N01/chi-tiet").with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Chưa có phiếu đã nộp hợp lệ của giảng viên GV01")))
            .andExpect(content().string(not(containsString("Điểm trung bình:"))))
            .andExpect(content().string(not(containsString("Xác nhận tổng hợp"))));
    }

    @Test
    void draftBallotRemainsVisibleWithoutAnAverageOrAggregationButton() throws Exception {
        var ballot = ballot();
        ballot.setTrangThai(TrangThaiPhieuCham.NHAP);
        when(phieu.findById_MaNhomOrderById_MaGiangVienAsc("N01")).thenReturn(List.of(ballot));
        mvc.perform(get("/ket-qua/N01/chi-tiet").with(user(SecurityTestUsers.user("GV01", "GIANG_VIEN"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Nháp")))
            .andExpect(content().string(containsString("Chưa có phiếu đã nộp hợp lệ")))
            .andExpect(content().string(not(containsString("Điểm trung bình:"))))
            .andExpect(content().string(not(containsString("Xác nhận tổng hợp"))));
    }

    @Test
    void missingAssignmentAndReportStillAllowDetail() throws Exception {
        when(phieu.layPhieuChamYeuCau("N01")).thenReturn(List.of());
        when(danhGia.dauVao("N01")).thenReturn(Optional.of(new DanhGiaRepository.DauVao(
            "DT01", "MON_HOC", "DANG_CHAM", "DANG_THUC_HIEN", "CONG_BO", null,
            null, 3, 3, "DOT01")));
        mvc.perform(get("/ket-qua/N01/chi-tiet").with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("đúng một giảng viên")))
            .andExpect(content().string(not(containsString("/bao-cao/N01/"))));
    }

    @Test
    void completeBallotsStillDisplayAverageAndAllowAssignedLecturerToAggregate() throws Exception {
        when(phieu.findById_MaNhomOrderById_MaGiangVienAsc("N01")).thenReturn(List.of(ballot()));
        mvc.perform(get("/ket-qua/N01/chi-tiet").with(user(SecurityTestUsers.user("GV01", "GIANG_VIEN"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Điểm trung bình:")))
            .andExpect(content().string(containsString("8.25")))
            .andExpect(content().string(containsString("Xác nhận tổng hợp")));
    }

    @Test
    void invalidScoreDisplaysReasonWithoutAnAverage() throws Exception {
        var ballot = ballot();
        ballot.setDiem(new BigDecimal("10.01"));
        when(phieu.findById_MaNhomOrderById_MaGiangVienAsc("N01")).thenReturn(List.of(ballot));
        mvc.perform(get("/ket-qua/N01/chi-tiet").with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Phiếu chấm có điểm không hợp lệ.")))
            .andExpect(content().string(not(containsString("Điểm trung bình:"))));
    }

    @Test
    void lecturerOutsideAssignmentStillCannotViewDetail() throws Exception {
        mvc.perform(get("/ket-qua/N01/chi-tiet").with(user(SecurityTestUsers.user("GV02", "GIANG_VIEN"))))
            .andExpect(status().isForbidden());
        verify(phieu, never()).timLoaiDotTheoMaNhom(anyString());
    }

    @Test
    void databaseFailureIsNotHiddenAsMissingBallots() {
        when(phieu.timLoaiDotTheoMaNhom("N01"))
            .thenThrow(new DataAccessResourceFailureException("Database unavailable"));
        assertThatThrownBy(() -> mvc.perform(get("/ket-qua/N01/chi-tiet")
                .with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA")))))
            .hasRootCauseInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    void missingBallotsStillPreventAggregationAndPublication() throws Exception {
        mvc.perform(post("/ket-qua/N01/tong-hop").with(csrf())
                .with(user(SecurityTestUsers.user("GV01", "GIANG_VIEN"))))
            .andExpect(redirectedUrl("/ket-qua/tong-hop"))
            .andExpect(flash().attribute("errorMessage", containsString("Chưa có phiếu đã nộp hợp lệ")));
        var result = new KetQua();
        result.setTrangThai(TrangThaiKetQua.DA_TONG_HOP);
        when(ketQua.findById("N01")).thenReturn(Optional.of(result));
        mvc.perform(post("/ket-qua/N01/cong-bo").with(csrf())
                .with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(redirectedUrl("/ket-qua/quan-ly"))
            .andExpect(flash().attribute("errorMessage", containsString("Chưa có phiếu đã nộp hợp lệ")));
        verify(ketQua, never()).saveAndFlush(any());
        verify(phanCong, never()).hoanThanhDangKyKhiDangCham(anyString(), any());
        verifyNoInteractions(email);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DA_DUYET", "DANG_CHAM", "DA_TONG_HOP", "CONG_BO"})
    void adminManagementPageHasOnlyCenteredReadOnlyPermission(String state) throws Exception {
        if (state.equals("DA_TONG_HOP") || state.equals("CONG_BO")) {
            when(nhom.getTrangThaiKetQua()).thenReturn(state);
        } else {
            when(nhom.getTrangThaiDangKy()).thenReturn(state);
        }
        var response = mvc.perform(get("/ket-qua/quan-ly")
                .with(user(SecurityTestUsers.user("ADMIN", "ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Chỉ xem")))
            .andExpect(content().string(not(containsString("Xem chi tiết"))))
            .andExpect(content().string(not(containsString("/ket-qua/N01/mo-cham"))))
            .andReturn().getResponse().getContentAsString();
        assertThat(response).containsPattern("<th[^>]*class=\"result-permission\"[^>]*>Quyền</th>");
        assertThat(response).containsPattern("<td[^>]*class=\"result-permission\"[^>]*>\\s*Chỉ xem\\s*</td>");
    }

    @Test
    void deanManagementPageKeepsDetailAndOpeningActions() throws Exception {
        when(nhom.getTrangThaiDangKy()).thenReturn("DA_DUYET");
        mvc.perform(get("/ket-qua/quan-ly").with(user(SecurityTestUsers.user("TK", "TRUONG_KHOA"))))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/ket-qua/N01/chi-tiet")))
            .andExpect(content().string(containsString("/ket-qua/N01/mo-cham")));
    }

    private PhieuChamYeuCau marker() {
        var marker = mock(PhieuChamYeuCau.class);
        when(marker.getMaGiangVien()).thenReturn("GV01");
        when(marker.getLoaiPhieu()).thenReturn("CHAM_DOC_LAP");
        return marker;
    }

    private PhieuChamDiem ballot() {
        var ballot = new PhieuChamDiem();
        ballot.setId(new PhieuChamDiemId("N01", "GV01"));
        ballot.setLoaiPhieu(LoaiPhieuCham.CHAM_DOC_LAP);
        ballot.setTrangThai(TrangThaiPhieuCham.DA_NOP);
        ballot.setDiem(new BigDecimal("8.25"));
        ballot.setNhanXet("Đã đánh giá");
        ballot.setNopLuc(LocalDateTime.of(2026, 10, 25, 10, 0));
        return ballot;
    }
}
