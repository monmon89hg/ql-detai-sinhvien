package vn.edu.group12.qldt.repository.hoidong;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.group12.qldt.entity.hoidong.HoiDong;

import java.util.List;

import vn.edu.group12.qldt.dto.hoidong.DotDangKyLuaChon;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;

public interface HoiDongRepository
        extends JpaRepository<HoiDong, String> {

        List<HoiDong> findByMaDotOrderByMaHoiDongAsc(String maDot);

        @Query(value = """
                SELECT ma_dot AS maDot, ten_dot AS tenDot
                FROM dot_dang_ky
                ORDER BY created_at DESC, ma_dot ASC
                """, nativeQuery = true)
        List<DotDangKyLuaChon> layDanhSachDotMoiNhatTruoc();

        @Query(value = """
                SELECT hd.ma_hoi_dong
                FROM hoi_dong hd
                WHERE (:maDot IS NULL OR hd.ma_dot = :maDot)
                  AND hd.trang_thai = 'NHAP'
                  AND NOT EXISTS (
                      SELECT 1 FROM thanh_vien_hoi_dong tv
                      WHERE tv.ma_hoi_dong = hd.ma_hoi_dong)
                  AND NOT EXISTS (
                      SELECT 1 FROM hoi_dong_de_tai pc
                      WHERE pc.ma_hoi_dong = hd.ma_hoi_dong)
                """, nativeQuery = true)
        List<String> layMaHoiDongCoTheXoa(@Param("maDot") String maDot);

        @Query(value = """
                SELECT COUNT(*) FROM thanh_vien_hoi_dong
                WHERE ma_hoi_dong = :maHoiDong
                """, nativeQuery = true)
        long demThanhVien(@Param("maHoiDong") String maHoiDong);

        @Query(value = """
                SELECT COUNT(*) FROM hoi_dong_de_tai
                WHERE ma_hoi_dong = :maHoiDong
                """, nativeQuery = true)
        long demNhomDaPhanCong(@Param("maHoiDong") String maHoiDong);

        @Query(
                value = """
                SELECT COUNT(*)
                FROM dot_dang_ky
                WHERE ma_dot = :maDot
                """,
                nativeQuery = true
        )
        long demDotTheoMa(@Param("maDot") String maDot);

        @Query(
                value = """
                SELECT COUNT(*)
                FROM nguoi_dung nd
                JOIN nguoi_dung_vai_tro ndvt
                        ON nd.ma_nguoi_dung = ndvt.ma_nguoi_dung
                WHERE nd.ma_nguoi_dung = :maNguoiDung
                AND nd.trang_thai = 'HOAT_DONG'
                AND ndvt.ma_vai_tro = 'TRUONG_KHOA'
                """,
                nativeQuery = true
        )
        long demTruongKhoaHoatDong(
                @Param("maNguoiDung") String maNguoiDung
        );

        @Query(
                value = """
                        SELECT
                        ma_dot AS maDot,
                        ten_dot AS tenDot
                        FROM dot_dang_ky
                        ORDER BY ma_dot
                        """,
                nativeQuery = true
        )
        List<DotDangKyLuaChon> layDanhSachDot();

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                SELECT hd
                FROM HoiDong hd
                WHERE hd.maHoiDong = :maHoiDong
                """)
        Optional<HoiDong> timVaKhoaTheoMa(
                @Param("maHoiDong") String maHoiDong
        );

        // Cập nhật mã bằng SQL thay vì thay @Id của một Entity đang được quản lý.
        // V7 giữ nguyên các liên kết qua ON UPDATE CASCADE.
        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query(value = """
                UPDATE hoi_dong
                SET ma_hoi_dong = :maMoi,
                    ten_hoi_dong = :tenHoiDong,
                    dia_diem = :diaDiem,
                    version = version + 1
                WHERE ma_hoi_dong = :maCu
                    AND trang_thai = 'NHAP'
                    AND version = :version
                """, nativeQuery = true)
        int suaThongTin(
                @Param("maCu") String maCu,
                @Param("maMoi") String maMoi,
                @Param("tenHoiDong") String tenHoiDong,
                @Param("diaDiem") String diaDiem,
                @Param("version") Long version
        );

}
