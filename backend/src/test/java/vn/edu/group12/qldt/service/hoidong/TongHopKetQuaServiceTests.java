package vn.edu.group12.qldt.service.hoidong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import vn.edu.group12.qldt.enums.hoidong.ChucDanhHoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiHoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiKetQua;
import vn.edu.group12.qldt.repository.hoidong.HoiDongDeTaiRepository;
import vn.edu.group12.qldt.repository.hoidong.HoiDongRepository;
import vn.edu.group12.qldt.repository.hoidong.KetQuaRepository;
import vn.edu.group12.qldt.repository.hoidong.ThanhVienHoiDongRepository;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TongHopKetQuaServiceTests {

    @Mock
    private HoiDongDeTaiRepository hoiDongDeTaiRepository;

    @Mock
    private HoiDongRepository hoiDongRepository;

    @Mock
    private ThanhVienHoiDongRepository thanhVienHoiDongRepository;

    @Mock
    private KetQuaRepository ketQuaRepository;

    @Mock
    private TinhDiemTrungBinhService tinhDiemTrungBinhService;

    @Mock private vn.edu.group12.qldt.repository.hoidong.DanhGiaRepository danhGiaRepository;

    @InjectMocks
    private TongHopKetQuaService tongHopKetQuaService;

    @BeforeEach
    void dangNhapGiangVien() {
        // Giả lập tài khoản GV03 đang đăng nhập.
        SecurityContextHolder.getContext().setAuthentication(
            authentication("GV03", "GIANG_VIEN")
        );
    }

    @AfterEach
    void xoaTaiKhoanGiaLap() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void chuTichTongHopKhiDuPhieu() {
        chuanBiNhomDangCham(true);

        when(tinhDiemTrungBinhService.tinhDiem("N01"))
            .thenReturn(new KetQuaTinhDiem(
                4,
                new BigDecimal("33.00"),
                new BigDecimal("8.25")
            ));

        when(ketQuaRepository.saveAndFlush(any(KetQua.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        KetQua ketQua = tongHopKetQuaService.tongHop(
            "N01",
            "  Nhóm hoàn thành tốt.  "
        );

        assertEquals("N01", ketQua.getMaNhom());
        assertEquals(4, ketQua.getSoPhieu());
        assertEquals(new BigDecimal("33.00"), ketQua.getTongDiem());
        assertEquals(new BigDecimal("8.25"), ketQua.getDiemCuoi());
        assertEquals("Nhóm hoàn thành tốt.", ketQua.getNhanXetTongHop());
        assertEquals("GV03", ketQua.getMaNguoiTongHop());
        assertEquals(TrangThaiKetQua.DA_TONG_HOP, ketQua.getTrangThai());
        assertNull(ketQua.getMaNguoiCongBo());
        assertNull(ketQua.getCongBoLuc());
    }

    @Test
    void thieuPhieuThiKhongLuuKetQua() {
        chuanBiNhomDangCham(true);

        when(tinhDiemTrungBinhService.tinhDiem("N01"))
            .thenThrow(new IllegalArgumentException("Thiếu phiếu chấm."));

        assertThrows(
            IllegalArgumentException.class,
            () -> tongHopKetQuaService.tongHop("N01", null)
        );

        verify(ketQuaRepository, never()).saveAndFlush(any(KetQua.class));
        //verifyNoInteractions(hoiDongRepository);
    }

    @Test
    void nguoiKhongPhaiChuTichKhongDuocTongHop() {
        chuanBiNhomDangCham(false);

        assertThrows(
            AccessDeniedException.class,
            () -> tongHopKetQuaService.tongHop("N01", null)
        );

        verifyNoInteractions(tinhDiemTrungBinhService);
    }

    @Test
    void nhomDaCoKetQuaKhongBiTongHopTrung() {
        chuanBiNhomDangCham(true);
        when(ketQuaRepository.existsById("N01")).thenReturn(true);

        assertThrows(
            IllegalArgumentException.class,
            () -> tongHopKetQuaService.tongHop("N01", null)
        );

        verifyNoInteractions(tinhDiemTrungBinhService);
    }

    private void chuanBiNhomDangCham(boolean laChuTich) {
        HoiDongDeTai phanCong = new HoiDongDeTai();
        phanCong.setMaNhom("N01");
        phanCong.setMaHoiDong("HD01");

        HoiDong hoiDong = new HoiDong();
        hoiDong.setMaHoiDong("HD01");
        hoiDong.setTrangThai(TrangThaiHoiDong.DANG_CHAM);

        when(thanhVienHoiDongRepository.demGiangVienHopLe("GV03"))
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
        when(thanhVienHoiDongRepository
                .existsById_MaHoiDongAndId_MaGiangVienAndChucDanh(
                    "HD01",
                    "GV03",
                    ChucDanhHoiDong.CHU_TICH
                ))
            .thenReturn(laChuTich);
    }
}
