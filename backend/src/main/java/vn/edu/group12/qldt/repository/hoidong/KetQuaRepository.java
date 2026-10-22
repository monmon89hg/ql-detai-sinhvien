package vn.edu.group12.qldt.repository.hoidong;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.group12.qldt.entity.hoidong.KetQua;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.group12.qldt.dto.hoidong.NguoiNhanKetQua;
import vn.edu.group12.qldt.dto.hoidong.DotDangKyLuaChon;
import vn.edu.group12.qldt.dto.hoidong.KetQuaTraCuuHienThi;

public interface KetQuaRepository extends JpaRepository<KetQua, String> {

    // Khóa chính là mã nhóm; JpaRepository đã có findById và save.
    // Lấy thành viên đã chốt, gồm cả sinh viên đã kết thúc tham gia nhóm.
    @Query(
        value = """
            SELECT
                tv.ma_sinh_vien AS maSinhVien,
                nd.ho_ten AS hoTen,
                nd.email AS email
            FROM thanh_vien_nhom tv
            JOIN nguoi_dung nd
                ON nd.ma_nguoi_dung = tv.ma_sinh_vien
            WHERE tv.ma_nhom = :maNhom
                AND tv.trang_thai IN ('THAM_GIA', 'KET_THUC')
                AND tv.la_thanh_vien_chot = 1
            ORDER BY tv.ma_sinh_vien
            """,
        nativeQuery = true
    )
    List<NguoiNhanKetQua> layNguoiNhanTheoNhom(
        @Param("maNhom") String maNhom
    );

    // Chỉ liệt kê đợt có kết quả đã công bố của chính sinh viên.
    @Query(value = """
        SELECT DISTINCT
            d.ma_dot AS maDot,
            d.ten_dot AS tenDot
        FROM thanh_vien_nhom tv
        JOIN nhom_sinh_vien n ON n.ma_nhom = tv.ma_nhom
        JOIN dot_dang_ky d ON d.ma_dot = n.ma_dot
        JOIN ket_qua k ON k.ma_nhom = n.ma_nhom
        WHERE tv.ma_sinh_vien = :maSinhVien
        AND tv.la_thanh_vien_chot = 1
        AND tv.trang_thai IN ('THAM_GIA', 'KET_THUC')
        AND k.trang_thai = 'CONG_BO'
        ORDER BY d.ten_dot, d.ma_dot
        """, nativeQuery = true)
    List<DotDangKyLuaChon> layDotCoKetQuaCuaSinhVien(
        @Param("maSinhVien") String maSinhVien
    );

    // Lọc theo đợt, đồng thời giới hạn kết quả vào nhóm của sinh viên đó.
    @Query(value = """
        SELECT
            d.ma_dot AS maDot,
            d.ten_dot AS tenDot,
            n.ma_nhom AS maNhom,
            n.ten_nhom AS tenNhom,
            dt.ma_de_tai AS maDeTai,
            dt.ten_de_tai AS tenDeTai,
            k.diem_cuoi AS diemCuoi,
            k.nhan_xet_tong_hop AS nhanXetTongHop,
            k.cong_bo_luc AS congBoLuc
        FROM thanh_vien_nhom tv
        JOIN nhom_sinh_vien n ON n.ma_nhom = tv.ma_nhom
        JOIN dot_dang_ky d ON d.ma_dot = n.ma_dot
        JOIN dang_ky_de_tai dk ON dk.ma_nhom = n.ma_nhom
        JOIN de_tai dt ON dt.ma_de_tai = dk.ma_de_tai
        JOIN ket_qua k ON k.ma_nhom = n.ma_nhom
        WHERE tv.ma_sinh_vien = :maSinhVien
        AND tv.la_thanh_vien_chot = 1
        AND tv.trang_thai IN ('THAM_GIA', 'KET_THUC')
        AND n.ma_dot = :maDot
        AND k.trang_thai = 'CONG_BO'
        ORDER BY k.cong_bo_luc DESC, n.ma_nhom
        """, nativeQuery = true)
    List<KetQuaTraCuuHienThi> traCuuKetQuaCuaSinhVien(
        @Param("maSinhVien") String maSinhVien,
        @Param("maDot") String maDot
    );

}