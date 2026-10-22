package vn.edu.group12.qldt.dto.admin;

import java.time.LocalDateTime;

public record ThongBaoDanhSachDto(
        String maThongBao,
        String tieuDe,
        String noiDung,
        String tenNguoiDang,
        String maDot,
        String phamVi,
        String trangThai,
        LocalDateTime congBoLuc
) {
}   