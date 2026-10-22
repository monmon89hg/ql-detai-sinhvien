package vn.edu.group12.qldt.service.nhom;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.repository.nhom.*;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class DangKyDeTaiService {
    private final NhomBaoCaoRepository repo;
    private final NhomDanhMucRepository danhMuc;
    private final QuyenNhomService quyen;
    private final NhomService nhomService;
    private final Clock clock;

    public DangKyDeTaiService(
            NhomBaoCaoRepository repo,
            NhomDanhMucRepository danhMuc,
            QuyenNhomService quyen,
            NhomService nhomService,
            Clock clock) {
        this.repo = repo;
        this.danhMuc = danhMuc;
        this.quyen = quyen;
        this.nhomService = nhomService;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void dangKy(String maDeTai) {
        var u = quyen.sinhVien();
        var maNhom =
                repo.nhomHienTai(u.maNguoiDung())
                        .orElseThrow(() -> NghiepVuNhomException.duLieu("Bạn chưa có nhóm sinh viên."));
        var n = nhomService.khoa(maNhom);
        quyen.nhomTruong(u, n);
        if (repo.dangKy(maNhom).isPresent())
            throw NghiepVuNhomException.xungDot(
                    "Nhóm đã có đăng ký đề tài, kể cả đăng ký đã bị từ chối. Không thể tạo đăng ký"
                            + " thứ hai.");
        if (!"DA_CHOT".equals(n.trangThai()))
            throw NghiepVuNhomException.duLieu("Nhóm phải chốt danh sách trước khi đăng ký đề tài.");
        nhomService.duThanhVien(n);
        if (n.soThanhVienChot() != n.soThanhVien())
            throw NghiepVuNhomException.duLieu("Danh sách thành viên chốt chưa hợp lệ.");
        nhomService.dotDangMo(n.maDot());
        if (maDeTai == null || maDeTai.isBlank() || maDeTai.length() > 30)
            throw NghiepVuNhomException.duLieu("Vui lòng chọn mã đề tài hợp lệ.");
        var t =
                danhMuc.deTai(maDeTai, true)
                        .orElseThrow(() -> NghiepVuNhomException.thieu("Không tìm thấy đề tài."));
        if (!n.maDot().equals(t.maDot()) || !"CONG_BO".equals(t.trangThai()))
            throw NghiepVuNhomException.duLieu("Chỉ đăng ký đề tài đã công bố trong đúng đợt của nhóm.");
        try {
            repo.dangKy(maNhom, maDeTai, u.maNguoiDung(), LocalDateTime.now(clock));
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuNhomException(
                    NghiepVuNhomException.Loai.XUNG_DOT,
                    "Nhóm đã có đăng ký hoặc dữ liệu tham chiếu vừa thay đổi.",
                    ex);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void duyet(String maNhom) {
        xuLy(maNhom, null);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void tuChoi(String maNhom, String lyDo) {
        quyen.truongKhoa();
        if (lyDo == null || lyDo.isBlank() || lyDo.strip().length() > 1000)
            throw NghiepVuNhomException.duLieu("Lý do từ chối phải có từ 1 đến 1000 ký tự.");
        xuLy(maNhom, lyDo.strip());
    }

    private void xuLy(String maNhom, String lyDo) {
        var u = quyen.truongKhoa();
        var n = nhomService.khoa(maNhom);
        var dk =
                repo.dangKy(maNhom)
                        .orElseThrow(() -> NghiepVuNhomException.thieu("Không tìm thấy đăng ký đề tài."));
        if (!"CHO_DUYET".equals(dk.trangThai()))
            throw NghiepVuNhomException.xungDot("Đăng ký đã được xử lý.");
        if (lyDo == null) {
            nhomService.duThanhVien(n);
            if (!"DA_CHOT".equals(n.trangThai()) || n.soThanhVienChot() != n.soThanhVien())
                throw NghiepVuNhomException.duLieu("Nhóm chưa có danh sách chốt hợp lệ.");
        }
        repo.xuLyDangKy(maNhom, u.maNguoiDung(), lyDo, LocalDateTime.now(clock));
    }
}
