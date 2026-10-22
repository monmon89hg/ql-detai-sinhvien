package vn.edu.group12.qldt.service.hoidong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.edu.group12.qldt.dto.hoidong.KetQuaTinhDiem;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiem;

@ExtendWith(MockitoExtension.class)
class TinhDiemTrungBinhServiceTests {

    @Mock
    private KiemTraDuPhieuChamService kiemTraDuPhieuChamService;

    @InjectMocks
    private TinhDiemTrungBinhService tinhDiemTrungBinhService;

    @Test
    void congDiemThapPhanChinhXac() {
        when(kiemTraDuPhieuChamService.layPhieuDaNopKhiDayDu("N01"))
            .thenReturn(List.of(phieu("0.10"), phieu("0.20")));

        KetQuaTinhDiem ketQua = tinhDiemTrungBinhService.tinhDiem("N01");

        assertEquals(2, ketQua.soPhieu());
        assertEquals(new BigDecimal("0.30"), ketQua.tongDiem());
        assertEquals(new BigDecimal("0.15"), ketQua.diemCuoi());
    }

    @Test
    void lamTronDiemTrungBinhDenHaiChuSo() {
        // 16.67 / 2 = 8.335, làm tròn HALF_UP thành 8.34.
        when(kiemTraDuPhieuChamService.layPhieuDaNopKhiDayDu("N02"))
            .thenReturn(List.of(phieu("8.33"), phieu("8.34")));

        KetQuaTinhDiem ketQua = tinhDiemTrungBinhService.tinhDiem("N02");

        assertEquals(new BigDecimal("16.67"), ketQua.tongDiem());
        assertEquals(new BigDecimal("8.34"), ketQua.diemCuoi());
    }

    @Test
    void khongTinhKhiThieuPhieuBatBuoc() {
        // Bộ kiểm tra báo thiếu phiếu thì phép tính phải dừng.
        when(kiemTraDuPhieuChamService.layPhieuDaNopKhiDayDu("N03"))
            .thenThrow(new IllegalArgumentException("Thiếu phiếu chấm."));

        IllegalArgumentException loi = assertThrows(
            IllegalArgumentException.class,
            () -> tinhDiemTrungBinhService.tinhDiem("N03")
        );

        assertEquals("Thiếu phiếu chấm.", loi.getMessage());
    }

    @Test
    void tuChoiDiemNgoaiKhoangChoPhep() {
        when(kiemTraDuPhieuChamService.layPhieuDaNopKhiDayDu("N04"))
            .thenReturn(List.of(phieu("10.01")));

        assertThrows(
            IllegalArgumentException.class,
            () -> tinhDiemTrungBinhService.tinhDiem("N04")
        );
    }

    private PhieuChamDiem phieu(String diem) {
        PhieuChamDiem phieu = new PhieuChamDiem();
        phieu.setDiem(new BigDecimal(diem));
        return phieu;
    }
}