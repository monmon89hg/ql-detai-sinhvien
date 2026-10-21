package vn.edu.group12.qldt.repository.hoidong;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.group12.qldt.dto.hoidong.NhomPhanCongLuaChon;
import vn.edu.group12.qldt.entity.hoidong.HoiDongDeTai;

import java.util.Optional;

public interface HoiDongDeTaiRepository
        extends JpaRepository<HoiDongDeTai, String> {

    List<HoiDongDeTai> findByMaHoiDongOrderByMaNhomAsc(
        String maHoiDong
    );

    @Query(
        value = """
            SELECT
                nh.ma_nhom AS maNhom,
                nh.ten_nhom AS tenNhom,
                dt.ma_de_tai AS maDeTai,
                dt.ten_de_tai AS tenDeTai
            FROM dang_ky_de_tai dk
            JOIN nhom_sinh_vien nh
                ON nh.ma_nhom = dk.ma_nhom
            JOIN de_tai dt
                ON dt.ma_de_tai = dk.ma_de_tai
            JOIN hoi_dong hd
                ON hd.ma_dot = nh.ma_dot
                AND hd.ma_dot = dt.ma_dot
            JOIN dot_dang_ky dot
                ON dot.ma_dot = hd.ma_dot
            WHERE hd.ma_hoi_dong = :maHoiDong
                AND hd.trang_thai = 'SAN_SANG'
                AND dot.loai_de_tai = 'KLTN'
                AND dk.trang_thai IN ('DA_DUYET', 'DANG_THUC_HIEN')
                AND nh.trang_thai IN ('DA_CHOT', 'DANG_THUC_HIEN')
                AND NOT EXISTS (
                    SELECT 1
                    FROM hoi_dong_de_tai pc
                    WHERE pc.ma_nhom = dk.ma_nhom
                )
                AND NOT EXISTS (
                    SELECT 1
                    FROM thanh_vien_hoi_dong tv
                    JOIN de_tai_giang_vien gv
                        ON gv.ma_giang_vien = tv.ma_giang_vien
                    WHERE tv.ma_hoi_dong = hd.ma_hoi_dong
                        AND gv.ma_de_tai = dk.ma_de_tai
                        AND gv.loai_phan_cong = 'HUONG_DAN'
                )
            ORDER BY nh.ma_nhom
            """,
        nativeQuery = true
    )
    List<NhomPhanCongLuaChon> layDanhSachNhomCoThePhanCong(
        @Param("maHoiDong") String maHoiDong
    );

    @Query(
        value = """
            SELECT
                nh.ma_nhom AS maNhom,
                nh.ten_nhom AS tenNhom,
                dt.ma_de_tai AS maDeTai,
                dt.ten_de_tai AS tenDeTai
            FROM hoi_dong_de_tai pc
            JOIN nhom_sinh_vien nh
                ON nh.ma_nhom = pc.ma_nhom
            JOIN dang_ky_de_tai dk
                ON dk.ma_nhom = pc.ma_nhom
            JOIN de_tai dt
                ON dt.ma_de_tai = dk.ma_de_tai
            WHERE pc.ma_hoi_dong = :maHoiDong
            ORDER BY nh.ma_nhom
            """,
        nativeQuery = true
    )
    List<NhomPhanCongLuaChon> layDanhSachNhomDaPhanCong(
        @Param("maHoiDong") String maHoiDong
    );

    @Query(
        value = """
            SELECT dk.ma_nhom
            FROM dang_ky_de_tai dk
            WHERE dk.ma_nhom = :maNhom
            FOR UPDATE
            """,
        nativeQuery = true
    )
    Optional<String> khoaDangKyTheoMaNhom(
        @Param("maNhom") String maNhom
    );

}