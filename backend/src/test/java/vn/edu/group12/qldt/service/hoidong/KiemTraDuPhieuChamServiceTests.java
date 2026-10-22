package vn.edu.group12.qldt.service.hoidong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.edu.group12.qldt.dto.hoidong.PhieuChamYeuCau;
import vn.edu.group12.qldt.entity.hoidong.HoiDongDeTai;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiem;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiemId;
import vn.edu.group12.qldt.entity.hoidong.ThanhVienHoiDong;
import vn.edu.group12.qldt.entity.hoidong.ThanhVienHoiDongId;
import vn.edu.group12.qldt.enums.hoidong.ChucDanhHoiDong;
import vn.edu.group12.qldt.enums.hoidong.LoaiPhieuCham;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiPhieuCham;
import vn.edu.group12.qldt.repository.hoidong.HoiDongDeTaiRepository;
import vn.edu.group12.qldt.repository.hoidong.PhieuChamDiemRepository;
import vn.edu.group12.qldt.repository.hoidong.ThanhVienHoiDongRepository;

@ExtendWith(MockitoExtension.class)
class KiemTraDuPhieuChamServiceTests {

    private static final String NHOM = "DEMO_N10";
    private static final String HOI_DONG = "DEMO_HD03";

    @Mock
    private PhieuChamDiemRepository phieuChamDiemRepository;

    @Mock
    private HoiDongDeTaiRepository hoiDongDeTaiRepository;

    @Mock
    private ThanhVienHoiDongRepository thanhVienHoiDongRepository;

    @InjectMocks
    private KiemTraDuPhieuChamService service;

    @BeforeEach
    void chuanBiPhanCong() {
        when(phieuChamDiemRepository.timLoaiDotTheoMaNhom(NHOM))
            .thenReturn(Optional.of("KLTN"));

        // Một phản biện và bốn thành viên hội đồng phải nộp phiếu.
        when(phieuChamDiemRepository.layPhieuChamYeuCau(NHOM))
            .thenReturn(List.of(
                new YeuCau("DEMO_GV02", "PHAN_BIEN"),
                new YeuCau("DEMO_GV03", "HOI_DONG"),
                new YeuCau("DEMO_GV04", "HOI_DONG"),
                new YeuCau("DEMO_GV05", "HOI_DONG"),
                new YeuCau("DEMO_GV06", "HOI_DONG")
            ));

        HoiDongDeTai phanCong = new HoiDongDeTai();
        phanCong.setMaNhom(NHOM);
        phanCong.setMaHoiDong(HOI_DONG);

        when(hoiDongDeTaiRepository.findById(NHOM))
            .thenReturn(Optional.of(phanCong));

        when(thanhVienHoiDongRepository
                .findById_MaHoiDongOrderById_MaGiangVienAsc(HOI_DONG))
            .thenReturn(List.of(
                thanhVien("DEMO_GV03", ChucDanhHoiDong.CHU_TICH),
                thanhVien("DEMO_GV04", ChucDanhHoiDong.THU_KY),
                thanhVien("DEMO_GV05", ChucDanhHoiDong.UY_VIEN),
                thanhVien("DEMO_GV06", ChucDanhHoiDong.UY_VIEN)
            ));
    }

    @Test
    void phieuHoiDongConNhapThiKhongDu() {
        when(phieuChamDiemRepository
                .findById_MaNhomOrderById_MaGiangVienAsc(NHOM))
            .thenReturn(List.of(
                phieu("DEMO_GV02", LoaiPhieuCham.PHAN_BIEN,
                    TrangThaiPhieuCham.DA_NOP),
                phieu("DEMO_GV03", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP),
                phieu("DEMO_GV04", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP),
                phieu("DEMO_GV05", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.NHAP),
                phieu("DEMO_GV06", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP)
            ));

        IllegalArgumentException loi = assertThrows(
            IllegalArgumentException.class,
            () -> service.layPhieuDaNopKhiDayDu(NHOM)
        );

        assertTrue(loi.getMessage().contains("DEMO_GV05"));
    }

    @Test
    void tatCaPhieuDaNopThiChoPhepLayDanhSach() {
        when(phieuChamDiemRepository
                .findById_MaNhomOrderById_MaGiangVienAsc(NHOM))
            .thenReturn(List.of(
                phieu("DEMO_GV02", LoaiPhieuCham.PHAN_BIEN,
                    TrangThaiPhieuCham.DA_NOP),
                phieu("DEMO_GV03", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP),
                phieu("DEMO_GV04", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP),
                phieu("DEMO_GV05", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP),
                phieu("DEMO_GV06", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP)
            ));

        List<PhieuChamDiem> ketQua =
            service.layPhieuDaNopKhiDayDu(NHOM);

        assertEquals(5, ketQua.size());
    }

    @Test
    void thieuPhieuPhanBienThiKhongDu() {
        when(phieuChamDiemRepository
                .findById_MaNhomOrderById_MaGiangVienAsc(NHOM))
            .thenReturn(List.of(
                phieu("DEMO_GV03", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP),
                phieu("DEMO_GV04", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP),
                phieu("DEMO_GV05", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP),
                phieu("DEMO_GV06", LoaiPhieuCham.HOI_DONG,
                    TrangThaiPhieuCham.DA_NOP)
            ));

        IllegalArgumentException loi = assertThrows(
            IllegalArgumentException.class,
            () -> service.layPhieuDaNopKhiDayDu(NHOM)
        );

        assertTrue(loi.getMessage().contains("DEMO_GV02"));
    }

    private ThanhVienHoiDong thanhVien(
            String maGiangVien,
            ChucDanhHoiDong chucDanh) {

        ThanhVienHoiDong thanhVien = new ThanhVienHoiDong();
        thanhVien.setId(
            new ThanhVienHoiDongId(HOI_DONG, maGiangVien)
        );
        thanhVien.setChucDanh(chucDanh);
        return thanhVien;
    }

    private PhieuChamDiem phieu(
            String maGiangVien,
            LoaiPhieuCham loai,
            TrangThaiPhieuCham trangThai) {

        PhieuChamDiem phieu = new PhieuChamDiem();
        phieu.setId(new PhieuChamDiemId(NHOM, maGiangVien));
        phieu.setLoaiPhieu(loai);
        phieu.setTrangThai(trangThai);
        phieu.setDiem(new BigDecimal("8.25"));
        phieu.setNhanXet("Nhận xét thử nghiệm.");

        if (trangThai == TrangThaiPhieuCham.DA_NOP) {
            phieu.setNopLuc(LocalDateTime.of(2026, 10, 21, 9, 0));
        }

        return phieu;
    }

    // Dữ liệu giả lập kết quả truy vấn các phiếu bắt buộc.
    private record YeuCau(
            String maGiangVien,
            String loaiPhieu) implements PhieuChamYeuCau {

        @Override
        public String getMaGiangVien() {
            return maGiangVien;
        }

        @Override
        public String getLoaiPhieu() {
            return loaiPhieu;
        }
    }
}