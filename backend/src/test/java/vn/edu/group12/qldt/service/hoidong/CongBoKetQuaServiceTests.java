package vn.edu.group12.qldt.service.hoidong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import static vn.edu.group12.qldt.support.SecurityTestUsers.authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import vn.edu.group12.qldt.dto.hoidong.KetQuaTinhDiem;
import vn.edu.group12.qldt.entity.hoidong.HoiDong;
import vn.edu.group12.qldt.entity.hoidong.HoiDongDeTai;
import vn.edu.group12.qldt.entity.hoidong.KetQua;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiKetQua;
import vn.edu.group12.qldt.repository.hoidong.HoiDongDeTaiRepository;
import vn.edu.group12.qldt.repository.hoidong.HoiDongRepository;
import vn.edu.group12.qldt.repository.hoidong.KetQuaRepository;
import vn.edu.group12.qldt.repository.hoidong.ThanhVienHoiDongRepository;

@ExtendWith(MockitoExtension.class)
class CongBoKetQuaServiceTests {

    @Mock private HoiDongDeTaiRepository hoiDongDeTaiRepository;
    @Mock private HoiDongRepository hoiDongRepository;
    @Mock private ThanhVienHoiDongRepository thanhVienHoiDongRepository;
    @Mock private KetQuaRepository ketQuaRepository;
    @Mock private TinhDiemTrungBinhService tinhDiemTrungBinhService;
    @Mock
    private TaoEmailThongBaoKetQuaService taoEmailThongBaoKetQuaService;

    @Mock private vn.edu.group12.qldt.repository.hoidong.DanhGiaRepository danhGiaRepository;

    @InjectMocks
    private TongHopKetQuaService tongHopKetQuaService;

    @BeforeEach
    void dangNhapTruongKhoa() {
        // Giả lập trưởng khoa CB02; không dùng mật khẩu thật.
        SecurityContextHolder.getContext().setAuthentication(
            authentication("CB02", "TRUONG_KHOA")
        );
    }

    @AfterEach
    void xoaTaiKhoanGiaLap() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void congBoCapNhatBaBangCungThoiDiem() {
        chuanBiKetQuaDaTongHop();
        choDiemHienTaiKhongDoi();

        when(hoiDongDeTaiRepository.hoanThanhDangKyKhiDangCham(
                eq("N01"), any(LocalDateTime.class)))
            .thenReturn(1);
        when(hoiDongDeTaiRepository.hoanThanhNhomKhiDangThucHien(
                eq("N01"), any(LocalDateTime.class)))
            .thenReturn(1);
        when(ketQuaRepository.saveAndFlush(any(KetQua.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        KetQua ketQua = tongHopKetQuaService.congBo("N01");

        assertEquals(TrangThaiKetQua.CONG_BO, ketQua.getTrangThai());
        assertEquals("CB02", ketQua.getMaNguoiCongBo());
        assertNotNull(ketQua.getCongBoLuc());

        // Hai bảng còn lại phải nhận đúng thời điểm trên kết quả.
        verify(hoiDongDeTaiRepository).hoanThanhDangKyKhiDangCham(
            "N01", ketQua.getCongBoLuc()
        );
        verify(hoiDongDeTaiRepository).hoanThanhNhomKhiDangThucHien(
            "N01", ketQua.getCongBoLuc()
        );
        // Công bố thành công phải tạo hàng chờ thông báo cho nhóm.
        verify(taoEmailThongBaoKetQuaService).taoChoNhom("N01");
    }

    @Test
    void diemDaDoiThiKhongCongBo() {
        chuanBiKetQuaDaTongHop();

        when(tinhDiemTrungBinhService.tinhDiem("N01"))
            .thenReturn(new KetQuaTinhDiem(
                4,
                new BigDecimal("32.00"),
                new BigDecimal("8.00")
            ));

        assertThrows(
            IllegalArgumentException.class,
            () -> tongHopKetQuaService.congBo("N01")
        );

        verify(ketQuaRepository, never()).saveAndFlush(any(KetQua.class));
    }

    @Test
    void khongHoanThanhDuocNhomThiKhongLuuCongBo() {
        chuanBiKetQuaDaTongHop();
        choDiemHienTaiKhongDoi();

        when(hoiDongDeTaiRepository.hoanThanhDangKyKhiDangCham(
                eq("N01"), any(LocalDateTime.class)))
            .thenReturn(1);
        // UPDATE nhóm trả về 0: nhóm không ở trạng thái cho phép.
        when(hoiDongDeTaiRepository.hoanThanhNhomKhiDangThucHien(
                eq("N01"), any(LocalDateTime.class)))
            .thenReturn(0);

        assertThrows(
            IllegalArgumentException.class,
            () -> tongHopKetQuaService.congBo("N01")
        );

        verify(ketQuaRepository, never()).saveAndFlush(any(KetQua.class));
    }

    @Test
    void taiKhoanKhongConQuyenTruongKhoaBiChan() {
        // Mock trả 0: tài khoản không còn vai trò trưởng khoa hợp lệ.
        when(hoiDongRepository.demTruongKhoaHoatDong("CB02"))
            .thenReturn(0L);

        assertThrows(
            AccessDeniedException.class,
            () -> tongHopKetQuaService.congBo("N01")
        );

        verify(ketQuaRepository, never()).saveAndFlush(any(KetQua.class));
    }

    private void chuanBiKetQuaDaTongHop() {
        HoiDongDeTai phanCong = new HoiDongDeTai();
        phanCong.setMaNhom("N01");
        phanCong.setMaHoiDong("HD01");

        HoiDong hoiDong = new HoiDong();
        hoiDong.setMaHoiDong("HD01");

        KetQua ketQua = new KetQua();
        ketQua.setMaNhom("N01");
        ketQua.setSoPhieu(4);
        ketQua.setTongDiem(new BigDecimal("33.00"));
        ketQua.setDiemCuoi(new BigDecimal("8.25"));
        ketQua.setTrangThai(TrangThaiKetQua.DA_TONG_HOP);
        ketQua.setTongHopLuc(LocalDateTime.of(2020, 1, 1, 0, 0));

        when(hoiDongRepository.demTruongKhoaHoatDong("CB02"))
            .thenReturn(1L);
        when(hoiDongDeTaiRepository.findById("N01"))
            .thenReturn(Optional.of(phanCong));
        when(hoiDongRepository.timVaKhoaTheoMa("HD01"))
            .thenReturn(Optional.of(hoiDong));
        when(hoiDongDeTaiRepository.khoaDangKyTheoMaNhom("N01"))
            .thenReturn(Optional.of("N01"));
        when(hoiDongDeTaiRepository.timMaHoiDongHienTai("N01"))
            .thenReturn(Optional.of("HD01"));
        when(hoiDongDeTaiRepository.timTrangThaiDangKyTheoMaNhom("N01"))
            .thenReturn(Optional.of("DANG_CHAM"));
        when(ketQuaRepository.findById("N01"))
            .thenReturn(Optional.of(ketQua));
    }

    private void choDiemHienTaiKhongDoi() {
        when(tinhDiemTrungBinhService.tinhDiem("N01"))
            .thenReturn(new KetQuaTinhDiem(
                4,
                new BigDecimal("33.00"),
                new BigDecimal("8.25")
            ));
    }
}
