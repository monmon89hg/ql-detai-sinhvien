package vn.edu.group12.qldt.service.nhom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.domain.nhom.NhomBaoCaoModels.*;
import vn.edu.group12.qldt.repository.nhom.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class NhomBaoCaoViewService {
    private final NhomBaoCaoRepository repo;
    private final NhomDanhMucRepository danhMuc;
    private final QuyenNhomService quyen;
    private final Clock clock;

    public NhomBaoCaoViewService(
            NhomBaoCaoRepository repo, NhomDanhMucRepository danhMuc, QuyenNhomService quyen, Clock clock) {
        this.repo = repo;
        this.danhMuc = danhMuc;
        this.quyen = quyen;
        this.clock = clock;
    }

    public Map<String, Object> danhSachNhom(String keyword, String round, String status, int page) {
        var u = quyen.hienTai();
        var m = model(u);
        keyword = loc(keyword, 255);
        round = loc(round, 30);
        status = loc(status, 20);
        m.put("tuKhoa", keyword);
        m.put("maDot", round);
        m.put("trangThai", status);
        m.put("danhSachDot", danhMuc.danhSachDot(false, now()));
        trang(m, "danhSachNhom", repo.danhSachNhom(keyword, round, status, offset(page)), page);
        // Chỉ truy vấn một lần cho sinh viên hiện tại; không tạo N+1 query trên từng dòng.
        m.put("nhomDaGuiYeuCau", u.laSinhVien()
                ? repo.nhomDaGuiYeuCau(u.maNguoiDung()) : Set.of());
        return m;
    }

    public Map<String, Object> formTaoNhom() {
        var m = model(quyen.sinhVien());
        m.put("danhSachDot", danhMuc.danhSachDot(true, now()));
        return m;
    }

    public Map<String, Object> chiTietNhom(String ma) {
        var u = quyen.hienTai();
        var m = model(u);
        var n =
                repo.nhom(ma)
                        .orElseThrow(() -> NghiepVuNhomException.thieu("Không tìm thấy nhóm sinh viên."));
        m.put("nhom", n);
        m.put("danhSachThanhVien", repo.thanhVien(ma, "THAM_GIA"));
        String trangThaiYeuCau = u.laSinhVien()
                ? repo.thanhVien(ma, u.maNguoiDung(), false).map(ThanhVien::trangThai).orElse(null)
                : null;
        m.put("trangThaiYeuCauCuaToi", trangThaiYeuCau);
        boolean duoc =
                u.laSinhVien()
                        && "DANG_LAP".equals(n.trangThai())
                        && n.soThanhVien() < 5
                        && repo.nhomHienTai(u.maNguoiDung()).isEmpty()
                        && trangThaiYeuCau == null
                        && danhMuc.dot(n.maDot(), false).map(d -> d.dangMo(now())).orElse(false);
        m.put("coTheGuiYeuCau", duoc);
        return m;
    }

    public Map<String, Object> nhomCuaToi() {
        var u = quyen.sinhVien();
        var m = model(u);
        var n = nhomHienTai(u);
        m.put("nhomCuaToi", n);
        boolean truong = n != null && u.maNguoiDung().equals(n.maNhomTruong());
        m.put("laNhomTruong", truong);
        m.put("danhSachThanhVien", n == null ? List.of() : repo.thanhVien(n.maNhom(), "THAM_GIA"));
        m.put("danhSachYeuCau", truong ? repo.thanhVien(n.maNhom(), "CHO_DUYET") : List.of());
        return m;
    }

    public Map<String, Object> dangKy(String keyword, String round, int page) {
        var u = quyen.hienTai();
        var m = model(u);
        var n = nhomHienTai(u);
        var dk = n == null ? null : repo.dangKy(n.maNhom()).orElse(null);
        keyword = loc(keyword, 255);
        round = loc(round, 30);
        m.put("nhomCuaToi", n);
        m.put("dangKyHienTai", dk);
        boolean truong = n != null && u.laSinhVien() && u.maNguoiDung().equals(n.maNhomTruong());
        m.put("laNhomTruong", truong);
        m.put(
                "duDieuKienDangKy",
                truong
                        && dk == null
                        && "DA_CHOT".equals(n.trangThai())
                        && n.soThanhVien() >= 3
                        && n.soThanhVien() <= 5
                        && n.soNhomTruong() == 1
                        && n.soThanhVienChot() == n.soThanhVien()
                        && danhMuc.dot(n.maDot(), false).map(d -> d.dangMo(now())).orElse(false));
        m.put("tuKhoa", keyword);
        m.put("maDot", round);
        m.put("danhSachDot", danhMuc.danhSachDot(false, now()));
        trang(
                m,
                "danhSachDeTai",
                danhMuc.deTaiCoTheDangKy(keyword, round, now(), offset(page)),
                page);
        return m;
    }

    public Map<String, Object> duyetDangKy(String keyword, String status, int page) {
        var m = model(quyen.truongKhoa());
        keyword = loc(keyword, 255);
        status = loc(status, 20);
        m.put("tuKhoa", keyword);
        m.put("trangThai", status);
        trang(m, "danhSachDangKy", repo.danhSachDangKy(keyword, status, offset(page)), page);
        return m;
    }

    public Map<String, Object> baoCao(String maNhom, int page) {
        var u = quyen.hienTai();
        var m = model(u);
        Nhom n;
        if (maNhom == null || maNhom.isBlank()) {
            n = nhomHienTai(u);
            if (n == null && u.laSinhVien())
                n = repo.nhomBaoCaoGanNhat(u.maNguoiDung()).flatMap(repo::nhom).orElse(null);
        } else {
            boolean duoc =
                    u.laTruongKhoa()
                            || (u.laSinhVien()
                                    && repo.thanhVien(maNhom, u.maNguoiDung(), false)
                                            .map(
                                                    v ->
                                                            v.laThanhVienChot()
                                                                    && Set.of(
                                                                                    "THAM_GIA",
                                                                                    "KET_THUC")
                                                                            .contains(
                                                                                    v.trangThai()))
                                            .orElse(false))
                            || ("GV".equals(u.loaiNguoiDung())
                                    && u.vaiTro().contains("GIANG_VIEN")
                                    && danhMuc.giangVienDuocDoc(u.maNguoiDung(), maNhom));
            if (!duoc)
                throw NghiepVuNhomException.cam("Bạn không có quyền xem lịch sử báo cáo của nhóm này.");
            n =
                    repo.nhom(maNhom)
                            .orElseThrow(
                                    () -> NghiepVuNhomException.thieu("Không tìm thấy nhóm sinh viên."));
        }
        m.put("nhomCuaToi", n);
        var dk = n == null ? null : repo.dangKy(n.maNhom()).orElse(null);
        var dot = n == null ? null : danhMuc.dot(n.maDot(), false).orElse(null);
        m.put("tenDeTai", dk == null ? null : dk.tenDeTai());
        m.put("trangThaiDangKy", dk == null ? null : dk.trangThai());
        m.put("hanNopBaoCao", dot == null ? null : dot.hanNopBaoCao());
        m.put(
                "coTheNopBaoCao",
                n != null
                        && dk != null
                        && dot != null
                        && BaoCaoService.duocNop(n, dk, dot, u, now()));
        trang(
                m,
                "danhSachBaoCao",
                n == null ? List.of() : repo.danhSachBaoCao(n.maNhom(), offset(page)),
                page);
        return m;
    }

    private Nhom nhomHienTai(NguoiDung u) {
        return repo.nhomHienTai(u.maNguoiDung()).flatMap(repo::nhom).orElse(null);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private Map<String, Object> model(NguoiDung u) {
        var m = new LinkedHashMap<String, Object>();
        m.put("currentUserName", u.hoTen());
        return m;
    }

    private String loc(String value, int max) {
        String s = value == null ? "" : value.strip();
        if (s.length() > max) throw NghiepVuNhomException.duLieu("Bộ lọc vượt quá độ dài cho phép.");
        return s;
    }

    private int offset(int page) {
        if (page < 0 || page > 100000) throw NghiepVuNhomException.duLieu("Số trang không hợp lệ.");
        return page * 20;
    }

    private <T> void trang(Map<String, Object> m, String key, List<T> rows, int page) {
        m.put(key, rows.size() > 20 ? List.copyOf(rows.subList(0, 20)) : rows);
        m.put("trang", page);
        m.put("coTrangSau", rows.size() > 20);
    }
}
