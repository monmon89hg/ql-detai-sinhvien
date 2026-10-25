package vn.edu.group12.qldt.repository.hoidong;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.edu.group12.qldt.dto.hoidong.NhomPhanCongLuaChon;
import vn.edu.group12.qldt.entity.hoidong.HoiDongDeTai;

import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Modifying;
import vn.edu.group12.qldt.dto.hoidong.NhomKetQuaHienThi;

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
                        AND gv.loai_phan_cong IN ('HUONG_DAN','PHAN_BIEN')
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

    // Đọc trạng thái sau khi transaction đã khóa đăng ký của nhóm.
    @Query(
        value = """
            SELECT dk.trang_thai
            FROM dang_ky_de_tai dk
            WHERE dk.ma_nhom = :maNhom
            """,
        nativeQuery = true
    )
    Optional<String> timTrangThaiDangKyTheoMaNhom(
        @Param("maNhom") String maNhom
    );

    // Chỉ hoàn thành đăng ký còn ở giai đoạn chấm.
    @Modifying
    @Query(
        value = """
            UPDATE dang_ky_de_tai
            SET trang_thai = 'HOAN_THANH',
                ket_thuc_luc = :congBoLuc,
                version = version + 1
            WHERE ma_nhom = :maNhom
                AND trang_thai = 'DANG_CHAM'
            """,
        nativeQuery = true
    )
    int hoanThanhDangKyKhiDangCham(
        @Param("maNhom") String maNhom,
        @Param("congBoLuc") LocalDateTime congBoLuc
    );

    // Chỉ hoàn thành nhóm đang thực hiện đề tài.
    @Modifying
    @Query(
        value = """
            UPDATE nhom_sinh_vien
            SET trang_thai = 'HOAN_THANH',
                ket_thuc_luc = :congBoLuc,
                version = version + 1
            WHERE ma_nhom = :maNhom
                AND trang_thai = 'DANG_THUC_HIEN'
            """,
        nativeQuery = true
    )
    int hoanThanhNhomKhiDangThucHien(
        @Param("maNhom") String maNhom,
        @Param("congBoLuc") LocalDateTime congBoLuc
    );

    // Đọc lại mã hội đồng từ database sau khi đã giữ khóa.
    @Query(
        value = """
            SELECT pc.ma_hoi_dong
            FROM hoi_dong_de_tai pc
            WHERE pc.ma_nhom = :maNhom
            """,
        nativeQuery = true
    )
    Optional<String> timMaHoiDongHienTai(
        @Param("maNhom") String maNhom
    );

    String DANH_SACH_DANH_GIA = """
        SELECT nh.ma_nhom AS maNhom, nh.ten_nhom AS tenNhom,
            dt.ma_de_tai AS maDeTai, dt.ten_de_tai AS tenDeTai,
            hd.ma_hoi_dong AS maHoiDong, hd.ten_hoi_dong AS tenHoiDong,
            dot.loai_de_tai AS loaiDeTai, dot.ma_dot AS maDot, dot.ten_dot AS tenDot,
            dk.trang_thai AS trangThaiDangKy, kq.trang_thai AS trangThaiKetQua, kq.diem_cuoi AS diemCuoi
        FROM dang_ky_de_tai dk
        JOIN nhom_sinh_vien nh ON nh.ma_nhom=dk.ma_nhom
        JOIN de_tai dt ON dt.ma_de_tai=dk.ma_de_tai AND dt.ma_dot=nh.ma_dot
        JOIN dot_dang_ky dot ON dot.ma_dot=nh.ma_dot
        LEFT JOIN hoi_dong_de_tai pc ON pc.ma_nhom=dk.ma_nhom
        LEFT JOIN hoi_dong hd ON hd.ma_hoi_dong=pc.ma_hoi_dong AND hd.ma_dot=nh.ma_dot
        LEFT JOIN ket_qua kq ON kq.ma_nhom=dk.ma_nhom
        WHERE dk.trang_thai IN ('DA_DUYET','DANG_THUC_HIEN','DANG_CHAM','HOAN_THANH')
        """;

    @Query(value = DANH_SACH_DANH_GIA + """
        AND dk.trang_thai IN ('DANG_CHAM','HOAN_THANH')
        AND ((dot.loai_de_tai='KLTN' AND EXISTS(SELECT 1 FROM thanh_vien_hoi_dong tv
                WHERE tv.ma_hoi_dong=hd.ma_hoi_dong AND tv.ma_giang_vien=:maGiangVien AND tv.chuc_danh='CHU_TICH'))
            OR (dot.loai_de_tai<>'KLTN' AND EXISTS(SELECT 1 FROM de_tai_giang_vien gv
                WHERE gv.ma_de_tai=dt.ma_de_tai AND gv.ma_giang_vien=:maGiangVien
                AND ((dot.loai_de_tai IN ('MON_HOC','NCKH') AND gv.loai_phan_cong='CHAM_DOC_LAP')
                    OR (dot.loai_de_tai='TLCN' AND gv.loai_phan_cong='PHAN_BIEN')))))
        ORDER BY dot.created_at DESC, nh.ma_nhom
        """, nativeQuery = true)
    List<NhomKetQuaHienThi> layDanhSachNhomChoChuTich(@Param("maGiangVien") String maGiangVien);

    @Query(value = DANH_SACH_DANH_GIA + " ORDER BY dot.created_at DESC, nh.ma_nhom", nativeQuery = true)
    List<NhomKetQuaHienThi> layDanhSachNhomChoTruongKhoa();
    @Query(value = DANH_SACH_DANH_GIA + """
        AND (:maDot='' OR dot.ma_dot=:maDot) AND (:loai='' OR dot.loai_de_tai=:loai)
        AND (:maGiangVien='' OR (dk.trang_thai IN ('DANG_CHAM','HOAN_THANH') AND
((dot.loai_de_tai='KLTN' AND EXISTS(SELECT 1 FROM thanh_vien_hoi_dong tv
                WHERE tv.ma_hoi_dong=hd.ma_hoi_dong AND tv.ma_giang_vien=:maGiangVien AND tv.chuc_danh='CHU_TICH'))
            OR (dot.loai_de_tai<>'KLTN' AND EXISTS(SELECT 1 FROM de_tai_giang_vien gv
                WHERE gv.ma_de_tai=dt.ma_de_tai AND gv.ma_giang_vien=:maGiangVien
                AND ((dot.loai_de_tai IN ('MON_HOC','NCKH') AND gv.loai_phan_cong='CHAM_DOC_LAP')
                    OR (dot.loai_de_tai='TLCN' AND gv.loai_phan_cong='PHAN_BIEN')))))))
        ORDER BY dot.created_at DESC, nh.ma_nhom
        """, nativeQuery = true)
    List<NhomKetQuaHienThi> locDanhSach(@Param("maDot") String maDot, @Param("loai") String loai,
        @Param("maGiangVien") String maGiangVien);

}
