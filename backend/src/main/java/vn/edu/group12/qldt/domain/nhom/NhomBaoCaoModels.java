package vn.edu.group12.qldt.domain.nhom;

import java.time.LocalDateTime;
import java.util.Set;

public final class NhomBaoCaoModels {
    private NhomBaoCaoModels() {}

    public record NguoiDung(
            String maNguoiDung,
            String hoTen,
            String loaiNguoiDung,
            String trangThai,
            Set<String> vaiTro) {
        public boolean laSinhVien() {
            return "SV".equals(loaiNguoiDung) && vaiTro.contains("SINH_VIEN");
        }

        public boolean laTruongKhoa() {
            return vaiTro.contains("TRUONG_KHOA");
        }
    }

    public record Dot(
            String maDot,
            String tenDot,
            String trangThai,
            LocalDateTime batDauSv,
            LocalDateTime ketThucSv,
            LocalDateTime hanNopBaoCao) {
        public boolean dangMo(LocalDateTime now) {
            return "CONG_BO".equals(trangThai)
                    && !now.isBefore(batDauSv)
                    && !now.isAfter(ketThucSv);
        }
    }

    public record Nhom(
            String maNhom,
            String tenNhom,
            String maDot,
            String tenDot,
            String trangThai,
            long soThanhVien,
            long soNhomTruong,
            long soThanhVienChot,
            String maNhomTruong,
            String tenNhomTruong) {}

    public record ThanhVien(
            String maNhom,
            String maSinhVien,
            String hoTen,
            String trangThai,
            boolean laNhomTruong,
            boolean laThanhVienChot,
            LocalDateTime yeuCauLuc) {}

    public record DeTai(
            String maDeTai,
            String tenDeTai,
            String maDot,
            String tenDot,
            String tenBoMon,
            String trangThai) {}

    public record DangKy(
            String maNhom,
            String maDeTai,
            String tenDeTai,
            String trangThai,
            String lyDoTuChoi,
            LocalDateTime dangKyLuc) {}

    /** Nhóm có báo cáo mà giảng viên được quyền truy cập theo phân công thực tế. */
    public record NhomBaoCaoGiangVien(String maNhom, String tenNhom, String tenDeTai) {}

    public record BaoCao(
            String maNhom,
            int phienBan,
            String maNguoiNop,
            String tenFileGoc,
            String objectKey,
            String mimeType,
            long kichThuocByte,
            String sha256,
            LocalDateTime nopLuc,
            String trangThai) {}

    public record TepTai(BaoCao baoCao, byte[] bytes) {}
}
