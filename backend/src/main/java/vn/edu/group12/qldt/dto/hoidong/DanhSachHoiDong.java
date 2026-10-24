package vn.edu.group12.qldt.dto.hoidong;

import java.util.List;
import java.util.Set;

import vn.edu.group12.qldt.entity.hoidong.HoiDong;

public record DanhSachHoiDong(
        List<DotDangKyLuaChon> danhSachDot,
        String maDotDuocChon,
        List<HoiDong> danhSachHoiDong,
        Set<String> maHoiDongCoTheXoa) {
}
