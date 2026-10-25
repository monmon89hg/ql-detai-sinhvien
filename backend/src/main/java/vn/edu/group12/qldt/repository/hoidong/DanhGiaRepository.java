package vn.edu.group12.qldt.repository.hoidong;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Các điểm khóa dữ liệu dùng chung giữa đánh giá, phân công và báo cáo. */
@Repository
public class DanhGiaRepository {
    private final JdbcTemplate jdbc;

    public DanhGiaRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record DauVao(String maDeTai, String loaiDeTai, String trangThaiDangKy,
            String trangThaiNhom, String trangThaiDot, LocalDateTime hanPhanBien,
            Integer phienBanBaoCao, long soThanhVien, long soThanhVienChot, String maDot) {}
    public record EmailHienThi(String maSinhVien, String diaChiEmail, String trangThai,
            int soLanThu, String loiCuoi) {
        public String getThongBaoLoi() {
            if (!"LOI".equals(trangThai)) return "";
            String loi=loiCuoi==null ? "" : loiCuoi.toLowerCase(java.util.Locale.ROOT);
            if (loi.contains("authentication") || loi.contains("535")) return "Xác thực với máy chủ gửi thất bại.";
            if (loi.contains("timeout") || loi.contains("connect")) return "Không kết nối được máy chủ gửi.";
            if (loi.contains("recipient") || loi.contains("550")) return "Địa chỉ nhận hoặc quyền gửi thư bị từ chối.";
            return "Máy chủ gửi chưa tiếp nhận thư. Cần kiểm tra nhật ký dịch vụ email.";
        }
    }

    // Bên gọi khóa hội đồng trước (nếu có). Thứ tự tiếp theo khớp thao tác nộp báo cáo.
    public void khoaDauVao(String maNhom) {
        var ids = jdbc.query("""
            SELECT dk.ma_de_tai, n.ma_dot FROM dang_ky_de_tai dk
            JOIN nhom_sinh_vien n ON n.ma_nhom=dk.ma_nhom WHERE dk.ma_nhom=?
            """, (r,i) -> new String[]{r.getString(1),r.getString(2)}, maNhom);
        if (ids.isEmpty()) return; // Service kiểm tra bản ghi tồn tại và báo lỗi nghiệp vụ.
        jdbc.queryForList("SELECT ma_de_tai FROM de_tai WHERE ma_de_tai=? FOR UPDATE", ids.getFirst()[0]);
        jdbc.queryForList("SELECT ma_nhom FROM nhom_sinh_vien WHERE ma_nhom=? FOR UPDATE", maNhom);
        jdbc.queryForList("SELECT ma_dot FROM dot_dang_ky WHERE ma_dot=? FOR UPDATE", ids.getFirst()[1]);
        jdbc.queryForList("SELECT ma_nhom FROM dang_ky_de_tai WHERE ma_nhom=? FOR UPDATE", maNhom);
    }

    public java.util.Optional<DauVao> dauVao(String maNhom) {
        return jdbc.query("""
            SELECT dt.ma_de_tai, dot.loai_de_tai, dk.trang_thai dk_trang_thai,
                n.trang_thai nhom_trang_thai, dot.trang_thai dot_trang_thai, dot.han_nop_diem_pb,
                (SELECT MAX(b.phien_ban) FROM nop_bao_cao b
                    WHERE b.ma_nhom=n.ma_nhom AND b.trang_thai='HOP_LE') phien_ban,
                (SELECT COUNT(*) FROM thanh_vien_nhom v
                    WHERE v.ma_nhom=n.ma_nhom AND v.trang_thai='THAM_GIA') so_tv,
                (SELECT COUNT(*) FROM thanh_vien_nhom v
                    WHERE v.ma_nhom=n.ma_nhom AND v.trang_thai='THAM_GIA'
                    AND v.la_thanh_vien_chot=1) so_chot, dot.ma_dot
            FROM dang_ky_de_tai dk JOIN nhom_sinh_vien n ON n.ma_nhom=dk.ma_nhom
            JOIN de_tai dt ON dt.ma_de_tai=dk.ma_de_tai AND dt.ma_dot=n.ma_dot
            JOIN dot_dang_ky dot ON dot.ma_dot=n.ma_dot WHERE n.ma_nhom=?
            """, (r,i) -> new DauVao(r.getString(1),r.getString(2),r.getString(3),r.getString(4),
                r.getString(5),r.getObject(6,LocalDateTime.class),r.getObject(7,Integer.class),
                r.getLong(8),r.getLong(9),r.getString(10)), maNhom).stream().findFirst();
    }

    public boolean duocTongHopNgoaiHoiDong(String maNhom, String maGiangVien) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM dang_ky_de_tai dk
                JOIN de_tai dt ON dt.ma_de_tai=dk.ma_de_tai
                JOIN dot_dang_ky d ON d.ma_dot=dt.ma_dot
                JOIN de_tai_giang_vien gv ON gv.ma_de_tai=dt.ma_de_tai
                WHERE dk.ma_nhom=? AND gv.ma_giang_vien=?
                AND ((d.loai_de_tai IN ('MON_HOC','NCKH') AND gv.loai_phan_cong='CHAM_DOC_LAP')
                    OR (d.loai_de_tai='TLCN' AND gv.loai_phan_cong='PHAN_BIEN')))
            """, Boolean.class, maNhom, maGiangVien));
    }

    public boolean laKhoaLuan(String maNhom) { return dauVao(maNhom).map(d -> "KLTN".equals(d.loaiDeTai())).orElse(false); }

    public int moCham(String maNhom, LocalDateTime luc) {
        return jdbc.update("""
            UPDATE dang_ky_de_tai SET trang_thai='DANG_CHAM',bat_dau_cham_luc=?, version=version+1
            WHERE ma_nhom=? AND trang_thai IN ('DA_DUYET','DANG_THUC_HIEN')
            """, luc,maNhom);
    }

    public int ketThucThanhVien(String maNhom, LocalDateTime luc) {
        return jdbc.update("""
            UPDATE thanh_vien_nhom SET trang_thai='KET_THUC', ket_thuc_luc=?, version=version+1
            WHERE ma_nhom=? AND trang_thai='THAM_GIA'
            """, luc, maNhom);
    }

    public List<String> nhomCuaHoiDong(String maHoiDong) {
        return jdbc.queryForList("SELECT ma_nhom FROM hoi_dong_de_tai WHERE ma_hoi_dong=? ORDER BY ma_nhom",
            String.class, maHoiDong);
    }

    public boolean daCoDuLieuCham(String maHoiDong) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM hoi_dong_de_tai pc
                JOIN dang_ky_de_tai dk ON dk.ma_nhom=pc.ma_nhom
                WHERE pc.ma_hoi_dong=? AND (dk.trang_thai IN ('DANG_CHAM','HOAN_THANH')
                    OR EXISTS(SELECT 1 FROM phieu_cham_diem p WHERE p.ma_nhom=pc.ma_nhom)
                    OR EXISTS(SELECT 1 FROM ket_qua k WHERE k.ma_nhom=pc.ma_nhom)))
            """, Boolean.class, maHoiDong));
    }

    public boolean tatCaDaCongBo(String maHoiDong) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM hoi_dong_de_tai WHERE ma_hoi_dong=?)
            AND NOT EXISTS(SELECT 1 FROM hoi_dong_de_tai pc LEFT JOIN ket_qua k ON k.ma_nhom=pc.ma_nhom
                WHERE pc.ma_hoi_dong=? AND (k.trang_thai IS NULL OR k.trang_thai<>'CONG_BO'))
            """, Boolean.class, maHoiDong, maHoiDong));
    }

    public boolean khoaPhanCongDeTai(String maDeTai) {
        jdbc.queryForList("SELECT ma_de_tai FROM de_tai WHERE ma_de_tai=? FOR UPDATE", maDeTai);
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM dang_ky_de_tai dk WHERE dk.ma_de_tai=?
                AND (dk.trang_thai IN ('DANG_CHAM','HOAN_THANH')
                    OR EXISTS(SELECT 1 FROM phieu_cham_diem p WHERE p.ma_nhom=dk.ma_nhom)
                    OR EXISTS(SELECT 1 FROM ket_qua k WHERE k.ma_nhom=dk.ma_nhom)))
            """, Boolean.class, maDeTai));
    }

    public boolean giangVienHuongDanHopLe(String maGiangVien) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM nguoi_dung nd JOIN nguoi_dung_vai_tro vt
                ON vt.ma_nguoi_dung=nd.ma_nguoi_dung WHERE nd.ma_nguoi_dung=?
                AND nd.loai_nguoi_dung='GV' AND nd.trang_thai='HOAT_DONG'
                AND vt.ma_vai_tro IN ('GIANG_VIEN','TRUONG_BO_MON'))
            """,Boolean.class,maGiangVien));
    }

    public String loaiDeTai(String maDeTai) {
        return jdbc.queryForObject("""
            SELECT d.loai_de_tai FROM de_tai dt JOIN dot_dang_ky d ON d.ma_dot=dt.ma_dot
            WHERE dt.ma_de_tai=?
            """, String.class, maDeTai);
    }

    public boolean xungDotHoiDong(String maDeTai, String maGiangVien) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM dang_ky_de_tai dk
                JOIN hoi_dong_de_tai pc ON pc.ma_nhom=dk.ma_nhom
                JOIN thanh_vien_hoi_dong tv ON tv.ma_hoi_dong=pc.ma_hoi_dong
                WHERE dk.ma_de_tai=? AND tv.ma_giang_vien=?)
            """, Boolean.class, maDeTai, maGiangVien));
    }

    public List<EmailHienThi> emailTheoNhom(String maNhom) {
        return jdbc.query("""
            SELECT ma_sinh_vien,dia_chi_email,trang_thai,so_lan_thu,loi_cuoi
            FROM email_thong_bao_ket_qua WHERE ma_nhom=? ORDER BY ma_sinh_vien
            """, (r,i) -> new EmailHienThi(r.getString(1),r.getString(2),r.getString(3),r.getInt(4),r.getString(5)), maNhom);
    }

    public int thuLaiEmail(String maNhom, String maSinhVien) {
        return jdbc.update("""
            UPDATE email_thong_bao_ket_qua SET trang_thai='CHO_GUI',so_lan_thu=0,
                loi_cuoi=NULL,lan_thu_cuoi_luc=NULL WHERE ma_nhom=? AND ma_sinh_vien=? AND trang_thai='LOI'
            """, maNhom,maSinhVien);
    }
    public record HoiDongThayThe(String maHoiDong, String tenHoiDong) {}
    public List<HoiDongThayThe> hoiDongThayThe(String maNhom) {
        return jdbc.query("""
            SELECT hd.ma_hoi_dong,hd.ten_hoi_dong FROM hoi_dong hd
            JOIN nhom_sinh_vien n ON n.ma_dot=hd.ma_dot
            JOIN dang_ky_de_tai dk ON dk.ma_nhom=n.ma_nhom
            WHERE n.ma_nhom=? AND hd.trang_thai='SAN_SANG'
            AND NOT EXISTS(SELECT 1 FROM thanh_vien_hoi_dong tv JOIN de_tai_giang_vien gv
                ON gv.ma_giang_vien=tv.ma_giang_vien WHERE tv.ma_hoi_dong=hd.ma_hoi_dong
                AND gv.ma_de_tai=dk.ma_de_tai AND gv.loai_phan_cong IN ('HUONG_DAN','PHAN_BIEN'))
            ORDER BY hd.ma_hoi_dong
            """, (r,i) -> new HoiDongThayThe(r.getString(1),r.getString(2)),maNhom);
    }

    public boolean khoaDotDanhGia(String maDot) {
        jdbc.queryForList("SELECT ma_dot FROM dot_dang_ky WHERE ma_dot=? FOR UPDATE",maDot);
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM nhom_sinh_vien n JOIN dang_ky_de_tai dk ON dk.ma_nhom=n.ma_nhom
                WHERE n.ma_dot=? AND (dk.trang_thai IN ('DANG_CHAM','HOAN_THANH')
                OR EXISTS(SELECT 1 FROM phieu_cham_diem p WHERE p.ma_nhom=n.ma_nhom)))
            """,Boolean.class,maDot));
    }

}
