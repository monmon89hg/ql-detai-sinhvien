package vn.edu.group12.qldt.service.nhom;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.domain.nhom.NhomBaoCaoModels.*;
import vn.edu.group12.qldt.repository.nhom.NhomDanhMucRepository;
import vn.edu.group12.qldt.service.auth.CurrentUser;

@Service
public class QuyenNhomService {
    private final NhomDanhMucRepository danhMuc;

    public QuyenNhomService(NhomDanhMucRepository danhMuc) {
        this.danhMuc = danhMuc;
    }

    public NguoiDung hienTai() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            throw new NghiepVuNhomException(
                    NghiepVuNhomException.Loai.KHONG_DANG_NHAP, "Vui lòng đăng nhập để tiếp tục.");
        }
        return taiKhoan(CurrentUser.maNguoiDung(), false);
    }

    public NguoiDung taiKhoan(String ma, boolean khoa) {
        var u =
                danhMuc.nguoiDung(ma, khoa)
                        .orElseThrow(
                                () ->
                                        NghiepVuNhomException.cam(
                                                "Tài khoản không tồn tại hoặc không còn quyền truy"
                                                        + " cập."));
        if (!"HOAT_DONG".equals(u.trangThai())) throw NghiepVuNhomException.cam("Tài khoản đã bị khóa.");
        return u;
    }

    public NguoiDung sinhVien() {
        var u = hienTai();
        if (!u.laSinhVien())
            throw NghiepVuNhomException.cam("Chỉ sinh viên đang hoạt động được thực hiện thao tác này.");
        return u;
    }

    public NguoiDung truongKhoa() {
        var u = hienTai();
        if (!u.laTruongKhoa())
            throw NghiepVuNhomException.cam("Chỉ trưởng khoa được xét duyệt đăng ký đề tài.");
        return u;
    }

    public void nhomTruong(NguoiDung u, Nhom n) {
        if (!u.laSinhVien() || !u.maNguoiDung().equals(n.maNhomTruong()) || n.soNhomTruong() != 1) {
            throw NghiepVuNhomException.cam(
                    "Chỉ nhóm trưởng chính thức của nhóm được thực hiện thao tác này.");
        }
    }
}
