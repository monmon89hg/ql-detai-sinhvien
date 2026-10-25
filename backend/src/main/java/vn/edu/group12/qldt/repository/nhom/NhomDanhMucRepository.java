package vn.edu.group12.qldt.repository.nhom;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import vn.edu.group12.qldt.domain.nhom.NhomBaoCaoModels.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
public class NhomDanhMucRepository {
    private final JdbcTemplate jdbc;

    public NhomDanhMucRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<NguoiDung> nguoiDung(String ma, boolean khoa) {
        var rows =
                jdbc.query(
                        "SELECT ma_nguoi_dung, ho_ten, loai_nguoi_dung, trang_thai FROM nguoi_dung "
                                + "WHERE ma_nguoi_dung = ?"
                                + (khoa ? " FOR UPDATE" : ""),
                        (r, n) ->
                                new NguoiDung(
                                        r.getString(1),
                                        r.getString(2),
                                        r.getString(3),
                                        r.getString(4),
                                        new HashSet<>()),
                        ma);
        if (rows.isEmpty()) return Optional.empty();
        var user = rows.getFirst();
        user.vaiTro()
                .addAll(
                        jdbc.queryForList(
                                "SELECT ma_vai_tro FROM nguoi_dung_vai_tro WHERE ma_nguoi_dung = ?",
                                String.class,
                                ma));
        return Optional.of(user);
    }

    public List<Dot> danhSachDot(boolean dangMo, LocalDateTime now) {
        return jdbc.query(
                "SELECT * FROM dot_dang_ky WHERE trang_thai <> 'NHAP' "
                        + (dangMo
                                ? "AND trang_thai = 'CONG_BO' AND bat_dau_sv <= ? AND ket_thuc_sv"
                                        + " >= ? "
                                : "")
                        + "ORDER BY bat_dau_sv DESC, ma_dot LIMIT 200",
                (r, n) -> dot(r),
                dangMo ? new Object[] {now, now} : new Object[] {});
    }

    // Đọc theo mã tự nhiên của V2, không phụ thuộc Entity TV2 chưa được tích hợp.
    public Optional<Dot> dot(String ma, boolean khoa) {
        return jdbc
                .query(
                        "SELECT * FROM dot_dang_ky WHERE ma_dot = ?" + (khoa ? " FOR SHARE" : ""),
                        (r, n) -> dot(r),
                        ma)
                .stream()
                .findFirst();
    }

    public Optional<DeTai> deTai(String ma, boolean khoa) {
        return jdbc
                .query(
                        "SELECT"
                            + " t.ma_de_tai,t.ten_de_tai,t.ma_dot,d.ten_dot,b.ten_bo_mon,t.trang_thai"
                            + " FROM de_tai t JOIN dot_dang_ky d ON d.ma_dot=t.ma_dot JOIN bo_mon b"
                            + " ON b.ma_bo_mon=t.ma_bo_mon WHERE t.ma_de_tai=?"
                                + (khoa ? " FOR SHARE" : ""),
                        (r, n) -> deTai(r),
                        ma)
                .stream()
                .findFirst();
    }

    public List<DeTai> deTaiCoTheDangKy(
            String tuKhoa, String maDot, LocalDateTime now, int offset) {
        return jdbc.query(
                """
                SELECT t.ma_de_tai,t.ten_de_tai,t.ma_dot,d.ten_dot,b.ten_bo_mon,t.trang_thai
                FROM de_tai t JOIN dot_dang_ky d ON d.ma_dot=t.ma_dot
                JOIN bo_mon b ON b.ma_bo_mon=t.ma_bo_mon
                WHERE t.trang_thai='CONG_BO' AND d.trang_thai='CONG_BO'
                  AND d.bat_dau_sv<=? AND d.ket_thuc_sv>=?
                  AND (?='' OR t.ma_dot=?)
                  AND (?='' OR LOCATE(?, t.ma_de_tai)>0 OR LOCATE(?,t.ten_de_tai)>0)
                ORDER BY t.ma_dot,t.ma_de_tai LIMIT 21 OFFSET ?
                """,
                (r, n) -> deTai(r),
                now,
                now,
                maDot,
                maDot,
                tuKhoa,
                tuKhoa,
                tuKhoa,
                offset);
    }

    /** Danh sách nhóm cho Trưởng khoa, phân trang 20 dòng (LIMIT 21 để xác định trang sau). */
    public List<NhomBaoCaoTruongKhoa> danhSachBaoCaoTruongKhoa(
            String tuKhoa, String maDot, String trangThaiNop, int offset) {
        return jdbc.query(
                """
                SELECT n.ma_nhom, n.ten_nhom, d.ten_dot,
                       COALESCE(t.ten_de_tai, 'Chưa đăng ký đề tài') AS ten_de_tai,
                       (SELECT COUNT(*) FROM nop_bao_cao bc
                        WHERE bc.ma_nhom=n.ma_nhom AND bc.trang_thai='HOP_LE') AS so_bao_cao_hop_le
                FROM nhom_sinh_vien n
                JOIN dot_dang_ky d ON d.ma_dot=n.ma_dot
                LEFT JOIN dang_ky_de_tai dk ON dk.ma_nhom=n.ma_nhom
                LEFT JOIN de_tai t ON t.ma_de_tai=dk.ma_de_tai
                WHERE (?='' OR n.ma_dot=?)
                  AND (?='' OR LOCATE(?,n.ma_nhom)>0 OR LOCATE(?,n.ten_nhom)>0
                       OR LOCATE(?,COALESCE(t.ten_de_tai,''))>0)
                  AND (?=''
                       OR (?='DA_NOP' AND EXISTS (
                           SELECT 1 FROM nop_bao_cao bc
                           WHERE bc.ma_nhom=n.ma_nhom AND bc.trang_thai='HOP_LE'))
                       OR (?='CHUA_NOP' AND NOT EXISTS (
                           SELECT 1 FROM nop_bao_cao bc
                           WHERE bc.ma_nhom=n.ma_nhom AND bc.trang_thai='HOP_LE')))
                ORDER BY n.created_at DESC, n.ma_nhom
                LIMIT 21 OFFSET ?
                """,
                (r, i) -> new NhomBaoCaoTruongKhoa(
                        r.getString("ma_nhom"), r.getString("ten_nhom"),
                        r.getString("ten_dot"), r.getString("ten_de_tai"),
                        r.getLong("so_bao_cao_hop_le")),
                maDot, maDot, tuKhoa, tuKhoa, tuKhoa, tuKhoa,
                trangThaiNop, trangThaiNop, trangThaiNop, offset);
    }

    /** Chỉ hiển thị nhóm có GVHD hoặc thành viên hội đồng là giảng viên hiện tại. */
    public List<NhomBaoCaoGiangVien> nhomBaoCaoGiangVien(String maGiangVien) {
        return jdbc.query(
                """
                SELECT n.ma_nhom, n.ten_nhom, t.ten_de_tai
                FROM nhom_sinh_vien n
                JOIN dang_ky_de_tai dk ON dk.ma_nhom = n.ma_nhom
                JOIN de_tai t ON t.ma_de_tai = dk.ma_de_tai
                WHERE EXISTS (
                    SELECT 1 FROM de_tai_giang_vien pc
                    WHERE pc.ma_de_tai = dk.ma_de_tai AND pc.ma_giang_vien = ?
                ) OR EXISTS (
                    SELECT 1 FROM hoi_dong_de_tai hd
                    JOIN thanh_vien_hoi_dong tv ON tv.ma_hoi_dong = hd.ma_hoi_dong
                    WHERE hd.ma_nhom = n.ma_nhom AND tv.ma_giang_vien = ?
                )
                ORDER BY n.created_at DESC, n.ma_nhom
                """,
                (r, i) -> new NhomBaoCaoGiangVien(
                        r.getString("ma_nhom"), r.getString("ten_nhom"), r.getString("ten_de_tai")),
                maGiangVien, maGiangVien);
    }

    public boolean giangVienDuocDoc(String maNguoiDung, String maNhom) {
        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        """
                        SELECT EXISTS(SELECT 1 FROM dang_ky_de_tai dk
                          WHERE dk.ma_nhom=? AND (
                            EXISTS(SELECT 1 FROM de_tai_giang_vien pc
                              WHERE pc.ma_de_tai=dk.ma_de_tai AND pc.ma_giang_vien=?)
                            OR EXISTS(SELECT 1 FROM hoi_dong_de_tai hd
                              JOIN thanh_vien_hoi_dong tv ON tv.ma_hoi_dong=hd.ma_hoi_dong
                              WHERE hd.ma_nhom=dk.ma_nhom AND tv.ma_giang_vien=?)))
                        """,
                        Boolean.class,
                        maNhom,
                        maNguoiDung,
                        maNguoiDung));
    }

    private static Dot dot(ResultSet r) throws SQLException {
        return new Dot(
                r.getString("ma_dot"),
                r.getString("ten_dot"),
                r.getString("trang_thai"),
                r.getObject("bat_dau_sv", LocalDateTime.class),
                r.getObject("ket_thuc_sv", LocalDateTime.class),
                r.getObject("han_nop_bao_cao", LocalDateTime.class));
    }

    private static DeTai deTai(ResultSet r) throws SQLException {
        return new DeTai(
                r.getString(1),
                r.getString(2),
                r.getString(3),
                r.getString(4),
                r.getString(5),
                r.getString(6));
    }
}
