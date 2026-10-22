package vn.edu.group12.qldt.dto.admin;

public record NguoiDungDanhSachDto(
        String maNguoiDung,
        String hoTen,
        String email,
        String loaiNguoiDung,
        String tenBoMon,
        String trangThai
) {
}