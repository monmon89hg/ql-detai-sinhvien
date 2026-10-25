package vn.edu.group12.qldt.repository.hoidong;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.group12.qldt.entity.hoidong.ThanhVienHoiDong;
import vn.edu.group12.qldt.entity.hoidong.ThanhVienHoiDongId;
import vn.edu.group12.qldt.enums.hoidong.ChucDanhHoiDong;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.group12.qldt.dto.hoidong.ThanhVienHoiDongHienThi;
import vn.edu.group12.qldt.dto.hoidong.GiangVienLuaChon;

public interface ThanhVienHoiDongRepository
        extends JpaRepository<ThanhVienHoiDong, ThanhVienHoiDongId> {

    List<ThanhVienHoiDong>
        findById_MaHoiDongOrderById_MaGiangVienAsc(String maHoiDong);

    long countById_MaHoiDong(String maHoiDong);

    boolean existsById_MaHoiDongAndChucDanh(
        String maHoiDong,
        ChucDanhHoiDong chucDanh
    );

    // Xác nhận đúng giảng viên giữ chức danh trong hội đồng cụ thể.
    boolean existsById_MaHoiDongAndId_MaGiangVienAndChucDanh(
        String maHoiDong,
        String maGiangVien,
        ChucDanhHoiDong chucDanh
    );

    @Query(
        value = """
            SELECT
                tv.ma_giang_vien AS maGiangVien,
                nd.ho_ten AS hoTen,
                tv.chuc_danh AS chucDanh
            FROM thanh_vien_hoi_dong tv
            JOIN nguoi_dung nd
                ON nd.ma_nguoi_dung = tv.ma_giang_vien
            WHERE tv.ma_hoi_dong = :maHoiDong
            ORDER BY
                CASE tv.chuc_danh
                    WHEN 'CHU_TICH' THEN 1
                    WHEN 'THU_KY' THEN 2
                    ELSE 3
                END,
                tv.ma_giang_vien
            """,
        nativeQuery = true
    )
    List<ThanhVienHoiDongHienThi> layDanhSachHienThi(
        @Param("maHoiDong") String maHoiDong
    );

    @Query(
        value = """
            SELECT
                nd.ma_nguoi_dung AS maGiangVien,
                nd.ho_ten AS hoTen
            FROM nguoi_dung nd
            JOIN nguoi_dung_vai_tro ndvt
                ON nd.ma_nguoi_dung = ndvt.ma_nguoi_dung
            WHERE nd.loai_nguoi_dung = 'GV'
            AND nd.trang_thai = 'HOAT_DONG'
            AND ndvt.ma_vai_tro = 'GIANG_VIEN'
            ORDER BY nd.ho_ten, nd.ma_nguoi_dung
            """,
        nativeQuery = true
    )
    List<GiangVienLuaChon> layDanhSachGiangVien();

    @Query(
        value = """
            SELECT COUNT(*)
            FROM nguoi_dung nd
            JOIN nguoi_dung_vai_tro ndvt
                ON nd.ma_nguoi_dung = ndvt.ma_nguoi_dung
            WHERE nd.ma_nguoi_dung = :maGiangVien
            AND nd.loai_nguoi_dung = 'GV'
            AND nd.trang_thai = 'HOAT_DONG'
            AND ndvt.ma_vai_tro = 'GIANG_VIEN'
            """,
        nativeQuery = true
    )
    long demGiangVienHopLe(
        @Param("maGiangVien") String maGiangVien
    );

    @Query(
        value = """
            SELECT COUNT(*)
            FROM hoi_dong_de_tai hddt
            JOIN dang_ky_de_tai dk
                ON dk.ma_nhom = hddt.ma_nhom
            JOIN de_tai_giang_vien dtgv
                ON dtgv.ma_de_tai = dk.ma_de_tai
            WHERE hddt.ma_hoi_dong = :maHoiDong
            AND dtgv.ma_giang_vien = :maGiangVien
            AND dtgv.loai_phan_cong IN ('HUONG_DAN','PHAN_BIEN')
            """,
        nativeQuery = true
    )
    long demXungDotHuongDan(
        @Param("maHoiDong") String maHoiDong,
        @Param("maGiangVien") String maGiangVien
    );

}
