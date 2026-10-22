package vn.edu.group12.qldt.nhom;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import static vn.edu.group12.qldt.nhom.NhomTestSupport.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.domain.nhom.NhomBaoCaoModels.*;
import vn.edu.group12.qldt.dto.nhom.TaoNhomForm;
import vn.edu.group12.qldt.repository.nhom.*;
import vn.edu.group12.qldt.service.nhom.*;

import java.util.Optional;
import java.util.Set;

class NhomServiceTests {
    NhomBaoCaoRepository repo = mock(NhomBaoCaoRepository.class);
    NhomDanhMucRepository danhMuc = mock(NhomDanhMucRepository.class);
    QuyenNhomService quyen = mock(QuyenNhomService.class);
    NhomService service = new NhomService(repo, danhMuc, quyen, CLOCK);
    NguoiDung leader = new NguoiDung("SV1", "Sinh viên", "SV", "HOAT_DONG", Set.of("SINH_VIEN"));

    @BeforeEach
    void setup() {
        when(quyen.sinhVien()).thenReturn(leader);
    }

    @Test
    void sinhMaTuNhienVaTaoTruong() {
        when(danhMuc.dot("D", true))
                .thenReturn(
                        Optional.of(
                                new Dot(
                                        "D",
                                        "Đợt",
                                        "CONG_BO",
                                        NOW.minusDays(1),
                                        NOW.plusDays(1),
                                        NOW.plusDays(2))));
        var form = new TaoNhomForm();
        form.setTenNhom(" Nhóm thử ");
        form.setMaDot("D");
        String ma = service.tao(form);
        assertThat(ma).hasSize(30).startsWith("N");
        verify(repo).taoNhom(ma, "Nhóm thử", "D", "SV1");
        verify(repo).themThanhVien(ma, "SV1", true, NOW);
    }

    @Test
    void duoiBaThanhVienLaLoiDuLieuCoKieu() {
        assertThatThrownBy(
                        () ->
                                service.duThanhVien(
                                        new Nhom(
                                                "N",
                                                "N",
                                                "D",
                                                "D",
                                                "DANG_LAP",
                                                2,
                                                1,
                                                0,
                                                "SV1",
                                                "SV1")))
                .isInstanceOfSatisfying(
                        NghiepVuNhomException.class,
                        e -> assertThat(e.getLoai()).isEqualTo(NghiepVuNhomException.Loai.DU_LIEU));
    }

    @Test
    void nhomDaChotKhongDuocSua() {
        assertThatThrownBy(
                        () ->
                                service.dangLap(
                                        new Nhom(
                                                "N", "N", "D", "D", "DA_CHOT", 3, 1, 3, "SV1",
                                                "SV1")))
                .isInstanceOfSatisfying(
                        NghiepVuNhomException.class,
                        e -> assertThat(e.getLoai()).isEqualTo(NghiepVuNhomException.Loai.XUNG_DOT));
    }

    @Test
    void daCoThanhVienThiKhongGhiNhomMoi() {
        when(repo.nhomHienTai("SV1")).thenReturn(Optional.of("N"));
        var f = new TaoNhomForm();
        f.setTenNhom("Nhóm");
        f.setMaDot("D");
        assertThatThrownBy(() -> service.tao(f)).isInstanceOf(NghiepVuNhomException.class);
        verify(repo, never()).taoNhom(any(), any(), any(), any());
    }

    @Test
    void quyenNhomTruongKhongPhuThuocRoleClient() {
        var that = new QuyenNhomService(danhMuc);
        assertThatThrownBy(
                        () ->
                                that.nhomTruong(
                                        leader,
                                        new Nhom(
                                                "N",
                                                "N",
                                                "D",
                                                "D",
                                                "DANG_LAP",
                                                3,
                                                1,
                                                0,
                                                "SV2",
                                                "SV2")))
                .isInstanceOfSatisfying(
                        NghiepVuNhomException.class,
                        e -> assertThat(e.getLoai()).isEqualTo(NghiepVuNhomException.Loai.CAM_TRUY_CAP));
    }
}
