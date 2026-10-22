package vn.edu.group12.qldt.service.nhom;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.domain.nhom.NhomBaoCaoModels.*;
import vn.edu.group12.qldt.dto.nhom.TaoNhomForm;
import vn.edu.group12.qldt.repository.nhom.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NhomService {
    private final NhomBaoCaoRepository repo;
    private final NhomDanhMucRepository danhMuc;
    private final QuyenNhomService quyen;
    private final Clock clock;

    public NhomService(
            NhomBaoCaoRepository repo, NhomDanhMucRepository danhMuc, QuyenNhomService quyen, Clock clock) {
        this.repo = repo;
        this.danhMuc = danhMuc;
        this.quyen = quyen;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String tao(TaoNhomForm form) {
        var u = quyen.sinhVien();
        quyen.taiKhoan(u.maNguoiDung(), true);
        String ten = form.getTenNhom() == null ? "" : form.getTenNhom().strip();
        if (ten.isEmpty() || ten.length() > 100)
            throw NghiepVuNhomException.duLieu("Tên nhóm phải có từ 1 đến 100 ký tự.");
        if (form.getMaDot() == null || form.getMaDot().isBlank() || form.getMaDot().length() > 30)
            throw NghiepVuNhomException.duLieu("Vui lòng chọn đợt đăng ký hợp lệ.");
        chuaCoNhom(u.maNguoiDung());
        dotDangMo(form.getMaDot());
        String ma = "N" + UUID.randomUUID().toString().replace("-", "").substring(0, 29);
        try {
            repo.taoNhom(ma, ten, form.getMaDot(), u.maNguoiDung());
            repo.themThanhVien(ma, u.maNguoiDung(), true, LocalDateTime.now(clock));
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuNhomException(
                    NghiepVuNhomException.Loai.XUNG_DOT,
                    "Không thể tạo nhóm vì dữ liệu vừa thay đổi. Vui lòng kiểm tra nhóm hiện tại.",
                    ex);
        }
        return ma;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void yeuCau(String maNhom) {
        var u = quyen.sinhVien();
        var n = khoa(maNhom);
        quyen.taiKhoan(u.maNguoiDung(), true);
        dangLap(n);
        dotDangMo(n.maDot());
        chuaCoNhom(u.maNguoiDung());
        if (repo.thanhVien(maNhom, u.maNguoiDung(), false).isPresent())
            throw NghiepVuNhomException.xungDot(
                    "Yêu cầu tham gia nhóm đã tồn tại và được giữ trong lịch sử.");
        if (n.soThanhVien() >= 5) throw NghiepVuNhomException.xungDot("Nhóm đã đủ 5 thành viên chính thức.");
        try {
            repo.themThanhVien(maNhom, u.maNguoiDung(), false, LocalDateTime.now(clock));
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuNhomException(NghiepVuNhomException.Loai.XUNG_DOT, "Yêu cầu tham gia đã được gửi.", ex);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void xuLyYeuCau(String maNhom, String maSinhVien, boolean chapNhan) {
        var u = quyen.sinhVien();
        // Khóa nhóm trước, sau đó tài khoản đích: bảo vệ cả suất cuối và một SV ở hai nhóm.
        var n = khoa(maNhom);
        quyen.nhomTruong(u, n);
        dangLap(n);
        dotDangMo(n.maDot());
        var sv = quyen.taiKhoan(maSinhVien, true);
        if (!sv.laSinhVien())
            throw NghiepVuNhomException.duLieu("Tài khoản yêu cầu không còn là sinh viên hợp lệ.");
        var tv =
                repo.thanhVien(maNhom, maSinhVien, true)
                        .orElseThrow(() -> NghiepVuNhomException.thieu("Không tìm thấy yêu cầu tham gia."));
        if (!"CHO_DUYET".equals(tv.trangThai()))
            throw NghiepVuNhomException.xungDot("Yêu cầu đã được xử lý.");
        if (chapNhan) {
            chuaCoNhom(maSinhVien);
            if (n.soThanhVien() >= 5)
                throw NghiepVuNhomException.xungDot("Nhóm đã đủ 5 thành viên chính thức.");
        }
        try {
            repo.duyetThanhVien(maNhom, maSinhVien, chapNhan, LocalDateTime.now(clock));
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuNhomException(
                    NghiepVuNhomException.Loai.XUNG_DOT,
                    "Sinh viên đã tham gia nhóm khác. Yêu cầu chưa được duyệt.",
                    ex);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void xoaThanhVien(String maNhom, String maSinhVien) {
        var u = quyen.sinhVien();
        var n = khoa(maNhom);
        quyen.nhomTruong(u, n);
        dangLap(n);
        var tv =
                repo.thanhVien(maNhom, maSinhVien, true)
                        .orElseThrow(() -> NghiepVuNhomException.thieu("Không tìm thấy thành viên."));
        if (tv.laNhomTruong()) throw NghiepVuNhomException.duLieu("Không được xóa nhóm trưởng của nhóm.");
        if (!"THAM_GIA".equals(tv.trangThai()))
            throw NghiepVuNhomException.xungDot("Sinh viên không còn là thành viên chính thức.");
        repo.ketThucThanhVien(maNhom, maSinhVien, LocalDateTime.now(clock));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void chot(String maNhom) {
        var u = quyen.sinhVien();
        var n = khoa(maNhom);
        quyen.nhomTruong(u, n);
        dangLap(n);
        dotDangMo(n.maDot());
        duThanhVien(n);
        repo.chotNhom(maNhom, LocalDateTime.now(clock));
    }

    public Nhom khoa(String ma) {
        if (!repo.khoaNhom(ma)) throw NghiepVuNhomException.thieu("Không tìm thấy nhóm sinh viên.");
        return lay(ma);
    }

    public Nhom lay(String ma) {
        return repo.nhom(ma)
                .orElseThrow(() -> NghiepVuNhomException.thieu("Không tìm thấy nhóm sinh viên."));
    }

    public Dot dotDangMo(String ma) {
        var d =
                danhMuc.dot(ma, true)
                        .orElseThrow(() -> NghiepVuNhomException.thieu("Không tìm thấy đợt đăng ký."));
        if (!d.dangMo(LocalDateTime.now(clock)))
            throw NghiepVuNhomException.duLieu(
                    "Đợt chưa công bố hoặc không trong thời gian sinh viên đăng ký.");
        return d;
    }

    public void chuaCoNhom(String ma) {
        if (repo.nhomHienTai(ma).isPresent())
            throw NghiepVuNhomException.xungDot("Sinh viên đã có nhóm đang tham gia chính thức.");
    }

    public void dangLap(Nhom n) {
        if (!"DANG_LAP".equals(n.trangThai()))
            throw NghiepVuNhomException.xungDot("Danh sách nhóm đã chốt, không thể thay đổi thành viên.");
    }

    public void duThanhVien(Nhom n) {
        if (n.soThanhVien() < 3 || n.soThanhVien() > 5 || n.soNhomTruong() != 1)
            throw NghiepVuNhomException.duLieu(
                    "Nhóm phải có 3–5 thành viên chính thức và đúng một nhóm trưởng.");
    }
}
