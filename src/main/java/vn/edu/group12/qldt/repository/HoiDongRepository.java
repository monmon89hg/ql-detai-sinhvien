package vn.edu.group12.qldt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.group12.qldt.entity.HoiDong;

import java.util.List;

import vn.edu.group12.qldt.dto.DotDangKyLuaChon;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;

public interface HoiDongRepository
        extends JpaRepository<HoiDong, String> {

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

}