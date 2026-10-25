package vn.edu.group12.qldt.service.nhom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.domain.nhom.NhomBaoCaoModels.*;
import vn.edu.group12.qldt.repository.nhom.*;
import vn.edu.group12.qldt.storage.nhom.ReportStorageService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BaoCaoService {
    private static final Logger log = LoggerFactory.getLogger(BaoCaoService.class);
    private final NhomBaoCaoRepository repo;
    private final NhomDanhMucRepository danhMuc;
    private final QuyenNhomService quyen;
    private final NhomService nhomService;
    private final ReportStorageService storage;
    private final PdfValidator validator;
    private final Clock clock;

    public BaoCaoService(
            NhomBaoCaoRepository repo,
            NhomDanhMucRepository danhMuc,
            QuyenNhomService quyen,
            NhomService nhomService,
            ReportStorageService storage,
            PdfValidator validator,
            Clock clock) {
        this.repo = repo;
        this.danhMuc = danhMuc;
        this.quyen = quyen;
        this.nhomService = nhomService;
        this.storage = storage;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BaoCao nop(MultipartFile file) {
        var u = quyen.sinhVien();
        String ma =
                repo.nhomHienTai(u.maNguoiDung())
                        .orElseThrow(() -> NghiepVuNhomException.duLieu("Bạn chưa có nhóm sinh viên."));
        var n = nhomService.khoa(ma);
        quyen.nhomTruong(u, n);
        var dk =
                repo.dangKy(ma).orElseThrow(() -> NghiepVuNhomException.duLieu("Nhóm chưa đăng ký đề tài."));
        var dot =
                danhMuc.dot(n.maDot(), true)
                        .orElseThrow(() -> NghiepVuNhomException.thieu("Không tìm thấy đợt đăng ký."));
        if (!duocNop(n, dk, dot, u, LocalDateTime.now(clock)))
            throw NghiepVuNhomException.duLieu(
                    "Chỉ nhóm trưởng của nhóm đã được duyệt, có danh sách chốt hợp lệ và còn hạn"
                            + " nộp mới được gửi PDF.");
        var pdf = validator.kiemTra(file);
        int phienBan = repo.phienBanTiepTheo(ma);
        if (phienBan > 1) {
            var truoc = repo.baoCao(ma, phienBan - 1);
            // Gửi lặp cùng nội dung không tạo thêm phiên bản hoặc object.
            if (truoc.isPresent()
                    && "HOP_LE".equals(truoc.get().trangThai())
                    && pdf.sha256().equals(truoc.get().sha256())
                    && u.maNguoiDung().equals(truoc.get().maNguoiNop())) {
                try {
                    var daLuu = storage.get(truoc.get().objectKey(), PdfValidator.MAX_BYTES);
                    if (daLuu.length == truoc.get().kichThuocByte()
                            && pdf.sha256().equals(PdfValidator.sha256(daLuu))) return truoc.get();
                } catch (NghiepVuNhomException ex) {
                    // Metadata seed có thể thiếu object; nộp thành phiên bản mới, giữ nguyên lịch
                    // sử.
                    if (ex.getLoai() != NghiepVuNhomException.Loai.KHONG_TIM_THAY) throw ex;
                }
            }
        }
        String key = "bao-cao/" + UUID.randomUUID() + ".pdf";
        // Đăng ký bù trừ trước PUT, kể cả khi PUT thành công nhưng phản hồi bị mất.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) donTep(key);
                        else if (status == STATUS_UNKNOWN)
                            log.error(
                                    "Chưa xác định kết quả commit; cần đối soát object {} với"
                                            + " metadata.",
                                    key);
                    }
                });
        storage.put(key, pdf.bytes());
        var bc =
                new BaoCao(
                        ma,
                        phienBan,
                        u.maNguoiDung(),
                        pdf.tenFile(),
                        key,
                        "application/pdf",
                        pdf.bytes().length,
                        pdf.sha256(),
                        LocalDateTime.now(clock),
                        "HOP_LE");
        repo.themBaoCao(bc);
        return bc;
    }

    public static boolean duocNop(Nhom n, DangKy dk, Dot d, NguoiDung u, LocalDateTime now) {
        return u.laSinhVien()
                && u.maNguoiDung().equals(n.maNhomTruong())
                && n.soNhomTruong() == 1
                && n.soThanhVien() >= 3
                && n.soThanhVien() <= 5
                && n.soThanhVienChot() == n.soThanhVien()
                && Set.of("DA_CHOT", "DANG_THUC_HIEN").contains(n.trangThai())
                && Set.of("DA_DUYET", "DANG_THUC_HIEN").contains(dk.trangThai())
                && !"NHAP".equals(d.trangThai())
                && !now.isBefore(d.batDauSv())
                && d.hanNopBaoCao() != null
                && !now.isAfter(d.hanNopBaoCao());
    }

    public TepTai tai(String maNhom, int phienBan) {
        var u = quyen.hienTai();
        boolean coQuyen = u.laTruongKhoa();
        if (!coQuyen && u.laSinhVien()) {
            coQuyen =
                    repo.thanhVien(maNhom, u.maNguoiDung(), false)
                            .map(
                                    v ->
                                            v.laThanhVienChot()
                                                    && Set.of("THAM_GIA", "KET_THUC")
                                                            .contains(v.trangThai()))
                            .orElse(false);
        }
        if (!coQuyen && u.vaiTro().contains("GIANG_VIEN") && "GV".equals(u.loaiNguoiDung()))
            coQuyen = danhMuc.giangVienDuocDoc(u.maNguoiDung(), maNhom);
        if (!coQuyen) throw NghiepVuNhomException.cam("Bạn không có quyền tải báo cáo của nhóm này.");
        var bc =
                repo.baoCao(maNhom, phienBan)
                        .orElseThrow(() -> NghiepVuNhomException.thieu("Không tìm thấy phiên bản báo cáo."));
        if (!"HOP_LE".equals(bc.trangThai()))
            throw NghiepVuNhomException.thieu("Phiên bản báo cáo đã được thu hồi.");
        ReportStorageService.kiemTraKhoa(bc.objectKey());
        var bytes = storage.get(bc.objectKey(), PdfValidator.MAX_BYTES);
        if (bytes.length != bc.kichThuocByte() || !PdfValidator.sha256(bytes).equals(bc.sha256()))
            throw NghiepVuNhomException.luuTru(
                    "PDF lưu trữ không khớp kích thước hoặc SHA-256. Vui lòng liên hệ quản trị"
                            + " viên.",
                    null);
        return new TepTai(bc, bytes);
    }

    private void donTep(String key) {
        for (int lan = 0; lan < 3; lan++) {
            try {
                storage.delete(key);
                return;
            } catch (RuntimeException ex) {
                if (lan == 2)
                    log.error(
                            "Dọn PDF thất bại sau rollback; cần đối soát object {}. Loại lỗi: {}",
                            key,
                            ex.getClass().getSimpleName());
            }
        }
    }
}
