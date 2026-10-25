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
import java.util.Set;

@Repository
public class NhomBaoCaoRepository {
    private final JdbcTemplate jdbc;
    private static final String NHOM =
            """
            SELECT n.ma_nhom,n.ten_nhom,n.ma_dot,d.ten_dot,n.trang_thai,
              (SELECT COUNT(*) FROM thanh_vien_nhom v WHERE v.ma_nhom=n.ma_nhom AND v.trang_thai='THAM_GIA') so_tv,
              (SELECT COUNT(*) FROM thanh_vien_nhom v WHERE v.nhom_co_truong=n.ma_nhom) so_truong,
              (SELECT COUNT(*) FROM thanh_vien_nhom v WHERE v.ma_nhom=n.ma_nhom
                AND v.trang_thai='THAM_GIA' AND v.la_thanh_vien_chot=TRUE) so_chot,
              tv.ma_sinh_vien, u.ho_ten
            FROM nhom_sinh_vien n JOIN dot_dang_ky d ON d.ma_dot=n.ma_dot
            LEFT JOIN thanh_vien_nhom tv ON tv.nhom_co_truong=n.ma_nhom
            LEFT JOIN nguoi_dung u ON u.ma_nguoi_dung=tv.ma_sinh_vien
            """;

    public NhomBaoCaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Nhom> danhSachNhom(String tuKhoa, String maDot, String trangThai, int offset) {
        return jdbc.query(
                NHOM
                        + """
                        WHERE (?='' OR n.ma_dot=?) AND (?='' OR n.trang_thai=?)
                          AND (?='' OR LOCATE(?,n.ma_nhom)>0 OR LOCATE(?,n.ten_nhom)>0)
                        ORDER BY n.created_at DESC,n.ma_nhom LIMIT 21 OFFSET ?
                        """,
                (r, n) -> nhom(r),
                maDot,
                maDot,
                trangThai,
                trangThai,
                tuKhoa,
                tuKhoa,
                tuKhoa,
                offset);
    }

    // Trạng thái đang chờ xét duyệt của sinh viên đăng nhập, không lấy yêu cầu người khác.
    public Set<String> nhomDaGuiYeuCau(String maSinhVien) {
        return new HashSet<>(jdbc.query(
                "SELECT ma_nhom FROM thanh_vien_nhom WHERE ma_sinh_vien=? AND trang_thai='CHO_DUYET'",
                (r, i) -> r.getString(1), maSinhVien));
    }

    public Optional<Nhom> nhom(String ma) {
        return jdbc.query(NHOM + " WHERE n.ma_nhom=?", (r, n) -> nhom(r), ma).stream().findFirst();
    }

    public Optional<String> nhomHienTai(String maSinhVien) {
        return jdbc
                .query(
                        "SELECT ma_nhom FROM thanh_vien_nhom WHERE sv_dang_tham_gia=?",
                        (r, n) -> r.getString(1),
                        maSinhVien)
                .stream()
                .findFirst();
    }

    public Optional<String> nhomBaoCaoGanNhat(String maSinhVien) {
        return jdbc
                .query(
                        """
                        SELECT n.ma_nhom FROM thanh_vien_nhom v
                        JOIN nhom_sinh_vien n ON n.ma_nhom=v.ma_nhom
                        JOIN dang_ky_de_tai dk ON dk.ma_nhom=n.ma_nhom
                        WHERE v.ma_sinh_vien=? AND v.la_thanh_vien_chot=TRUE
                          AND v.trang_thai IN ('THAM_GIA','KET_THUC')
                        ORDER BY n.created_at DESC,n.ma_nhom DESC LIMIT 1
                        """,
                        (r, i) -> r.getString(1),
                        maSinhVien)
                .stream()
                .findFirst();
    }

    public boolean khoaNhom(String ma) {
        return !jdbc.query(
                        "SELECT ma_nhom FROM nhom_sinh_vien WHERE ma_nhom=? FOR UPDATE",
                        (r, n) -> r.getString(1),
                        ma)
                .isEmpty();
    }

    public void taoNhom(String ma, String ten, String maDot, String actor) {
        jdbc.update(
                "INSERT INTO nhom_sinh_vien(ma_nhom,ten_nhom,ma_dot,ma_nguoi_tao) VALUES(?,?,?,?)",
                ma,
                ten,
                maDot,
                actor);
    }

    public List<ThanhVien> thanhVien(String nhom, String trangThai) {
        return jdbc.query(
                """
                SELECT v.ma_nhom,v.ma_sinh_vien,u.ho_ten,v.trang_thai,v.la_nhom_truong,
                  v.la_thanh_vien_chot,v.yeu_cau_luc
                FROM thanh_vien_nhom v JOIN nguoi_dung u ON u.ma_nguoi_dung=v.ma_sinh_vien
                WHERE v.ma_nhom=? AND v.trang_thai=? ORDER BY v.la_nhom_truong DESC,v.yeu_cau_luc,v.ma_sinh_vien
                """,
                (r, n) -> thanhVien(r),
                nhom,
                trangThai);
    }

    public Optional<ThanhVien> thanhVien(String nhom, String sv, boolean khoa) {
        return jdbc
                .query(
                        """
                        SELECT v.ma_nhom,v.ma_sinh_vien,u.ho_ten,v.trang_thai,v.la_nhom_truong,
                          v.la_thanh_vien_chot,v.yeu_cau_luc
                        FROM thanh_vien_nhom v JOIN nguoi_dung u ON u.ma_nguoi_dung=v.ma_sinh_vien
                        WHERE v.ma_nhom=? AND v.ma_sinh_vien=?
                        """
                                + (khoa ? " FOR UPDATE" : ""),
                        (r, n) -> thanhVien(r),
                        nhom,
                        sv)
                .stream()
                .findFirst();
    }

    public void themThanhVien(String nhom, String sv, boolean truong, LocalDateTime now) {
        jdbc.update(
                """
                INSERT INTO thanh_vien_nhom(ma_nhom,ma_sinh_vien,trang_thai,la_nhom_truong,yeu_cau_luc,tham_gia_luc)
                VALUES(?,?,?,?,?,?)
                """,
                nhom,
                sv,
                truong ? "THAM_GIA" : "CHO_DUYET",
                truong,
                now,
                truong ? now : null);
    }

    public void duyetThanhVien(String nhom, String sv, boolean chapNhan, LocalDateTime now) {
        jdbc.update(
                """
                UPDATE thanh_vien_nhom SET trang_thai=?,tham_gia_luc=?,version=version+1
                WHERE ma_nhom=? AND ma_sinh_vien=? AND trang_thai='CHO_DUYET'
                """,
                chapNhan ? "THAM_GIA" : "TU_CHOI",
                chapNhan ? now : null,
                nhom,
                sv);
    }

    public void ketThucThanhVien(String nhom, String sv, LocalDateTime now) {
        jdbc.update(
                """
                UPDATE thanh_vien_nhom SET trang_thai='KET_THUC',ket_thuc_luc=?,version=version+1
                WHERE ma_nhom=? AND ma_sinh_vien=? AND trang_thai='THAM_GIA' AND la_nhom_truong=FALSE
                """,
                now,
                nhom,
                sv);
    }

    public void chotNhom(String nhom, LocalDateTime now) {
        jdbc.update(
                "UPDATE nhom_sinh_vien SET trang_thai='DA_CHOT',chot_luc=?,version=version+1 WHERE"
                        + " ma_nhom=?",
                now,
                nhom);
        jdbc.update(
                "UPDATE thanh_vien_nhom SET la_thanh_vien_chot=TRUE,version=version+1 WHERE"
                        + " ma_nhom=? AND trang_thai='THAM_GIA'",
                nhom);
        jdbc.update(
                "UPDATE thanh_vien_nhom SET trang_thai='TU_CHOI',version=version+1 WHERE ma_nhom=?"
                        + " AND trang_thai='CHO_DUYET'",
                nhom);
    }

    public void dangKy(String nhom, String deTai, String actor, LocalDateTime now) {
        jdbc.update(
                "INSERT INTO dang_ky_de_tai(ma_nhom,ma_de_tai,ma_nguoi_dang_ky,dang_ky_luc)"
                        + " VALUES(?,?,?,?)",
                nhom,
                deTai,
                actor,
                now);
    }

    private static final String DANG_KY =
            """
            SELECT dk.ma_nhom,dk.ma_de_tai,t.ten_de_tai,dk.trang_thai,dk.ly_do_tu_choi,dk.dang_ky_luc
            FROM dang_ky_de_tai dk JOIN de_tai t ON t.ma_de_tai=dk.ma_de_tai
            """;

    public Optional<DangKy> dangKy(String nhom) {
        return jdbc.query(DANG_KY + " WHERE dk.ma_nhom=?", (r, n) -> dangKy(r), nhom).stream()
                .findFirst();
    }

    public List<DangKy> danhSachDangKy(String tuKhoa, String trangThai, int offset) {
        return jdbc.query(
                DANG_KY
                        + """
                        WHERE (?='' OR dk.trang_thai=?)
                          AND (?='' OR LOCATE(?,dk.ma_nhom)>0 OR LOCATE(?,t.ten_de_tai)>0)
                        ORDER BY dk.dang_ky_luc DESC,dk.ma_nhom LIMIT 21 OFFSET ?
                        """,
                (r, n) -> dangKy(r),
                trangThai,
                trangThai,
                tuKhoa,
                tuKhoa,
                tuKhoa,
                offset);
    }

    public void xuLyDangKy(String nhom, String actor, String lyDo, LocalDateTime now) {
        jdbc.update(
                """
                UPDATE dang_ky_de_tai SET trang_thai=?,ma_nguoi_duyet=?,duyet_luc=?,ly_do_tu_choi=?,version=version+1
                WHERE ma_nhom=? AND trang_thai='CHO_DUYET'
                """,
                lyDo == null ? "DA_DUYET" : "TU_CHOI",
                actor,
                now,
                lyDo,
                nhom);
        if (lyDo == null)
            jdbc.update(
                    "UPDATE nhom_sinh_vien SET trang_thai='DANG_THUC_HIEN',version=version+1 WHERE"
                            + " ma_nhom=?",
                    nhom);
    }

    public int phienBanTiepTheo(String nhom) {
        return jdbc.queryForObject(
                "SELECT COALESCE(MAX(phien_ban),0)+1 FROM nop_bao_cao WHERE ma_nhom=?",
                Integer.class,
                nhom);
    }

    public List<BaoCao> danhSachBaoCao(String nhom, int offset) {
        return jdbc.query(
                "SELECT * FROM nop_bao_cao WHERE ma_nhom=? ORDER BY phien_ban DESC LIMIT 21 OFFSET"
                        + " ?",
                (r, n) -> baoCao(r),
                nhom,
                offset);
    }

    public Optional<BaoCao> baoCao(String nhom, int phienBan) {
        return jdbc
                .query(
                        "SELECT * FROM nop_bao_cao WHERE ma_nhom=? AND phien_ban=?",
                        (r, n) -> baoCao(r),
                        nhom,
                        phienBan)
                .stream()
                .findFirst();
    }

    public void themBaoCao(BaoCao b) {
        jdbc.update(
                """
                INSERT INTO nop_bao_cao(ma_nhom,phien_ban,ma_nguoi_nop,ten_file_goc,object_key,mime_type,kich_thuoc_byte,sha256,nop_luc,trang_thai)
                VALUES(?,?,?,?,?,?,?,?,?,?)
                """,
                b.maNhom(),
                b.phienBan(),
                b.maNguoiNop(),
                b.tenFileGoc(),
                b.objectKey(),
                b.mimeType(),
                b.kichThuocByte(),
                b.sha256(),
                b.nopLuc(),
                b.trangThai());
    }

    private static Nhom nhom(ResultSet r) throws SQLException {
        return new Nhom(
                r.getString(1),
                r.getString(2),
                r.getString(3),
                r.getString(4),
                r.getString(5),
                r.getLong(6),
                r.getLong(7),
                r.getLong(8),
                r.getString(9),
                r.getString(10));
    }

    private static ThanhVien thanhVien(ResultSet r) throws SQLException {
        return new ThanhVien(
                r.getString(1),
                r.getString(2),
                r.getString(3),
                r.getString(4),
                r.getBoolean(5),
                r.getBoolean(6),
                r.getObject(7, LocalDateTime.class));
    }

    private static DangKy dangKy(ResultSet r) throws SQLException {
        return new DangKy(
                r.getString(1),
                r.getString(2),
                r.getString(3),
                r.getString(4),
                r.getString(5),
                r.getObject(6, LocalDateTime.class));
    }

    private static BaoCao baoCao(ResultSet r) throws SQLException {
        return new BaoCao(
                r.getString("ma_nhom"),
                r.getInt("phien_ban"),
                r.getString("ma_nguoi_nop"),
                r.getString("ten_file_goc"),
                r.getString("object_key"),
                r.getString("mime_type"),
                r.getLong("kich_thuoc_byte"),
                r.getString("sha256"),
                r.getObject("nop_luc", LocalDateTime.class),
                r.getString("trang_thai"));
    }
}
