package vn.edu.group12.qldt.service.hoidong;

import vn.edu.group12.qldt.service.auth.CurrentUser;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.dto.hoidong.NhomChamDiemHienThi;
import vn.edu.group12.qldt.repository.hoidong.PhieuChamDiemRepository;
import vn.edu.group12.qldt.repository.hoidong.ThanhVienHoiDongRepository;

import vn.edu.group12.qldt.dto.hoidong.PhieuChamDiemForm;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiem;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiemId;
import vn.edu.group12.qldt.enums.hoidong.LoaiPhieuCham;
import vn.edu.group12.qldt.entity.hoidong.HoiDongDeTai;
import vn.edu.group12.qldt.repository.hoidong.HoiDongDeTaiRepository;
import vn.edu.group12.qldt.repository.hoidong.HoiDongRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.validation.annotation.Validated;

import vn.edu.group12.qldt.enums.hoidong.TrangThaiPhieuCham;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Validated
@PreAuthorize("hasRole('GIANG_VIEN')")
public class ChamDiemHoiDongService {

    private final PhieuChamDiemRepository phieuChamDiemRepository;
    private final ThanhVienHoiDongRepository thanhVienHoiDongRepository;
    private final HoiDongRepository hoiDongRepository;
    private final HoiDongDeTaiRepository hoiDongDeTaiRepository;

    public ChamDiemHoiDongService(
            PhieuChamDiemRepository phieuChamDiemRepository,
            ThanhVienHoiDongRepository thanhVienHoiDongRepository,
            HoiDongRepository hoiDongRepository,
            HoiDongDeTaiRepository hoiDongDeTaiRepository) {

        this.phieuChamDiemRepository = phieuChamDiemRepository;
        this.thanhVienHoiDongRepository = thanhVienHoiDongRepository;
        this.hoiDongRepository = hoiDongRepository;
        this.hoiDongDeTaiRepository = hoiDongDeTaiRepository;
    }

    // Lấy danh sách theo tài khoản đăng nhập, không nhận mã GV từ trình duyệt.
    @Transactional(readOnly = true)
    public List<NhomChamDiemHienThi> layDanhSachNhomDuocCham() {
        String maGiangVien = layMaGiangVienDangNhap();

        return phieuChamDiemRepository
            .layDanhSachNhomDuocCham(maGiangVien);
    }

    // Lấy thông tin nhóm để hiển thị trên trang chấm.
    @Transactional(readOnly = true)
    public NhomChamDiemHienThi layThongTinNhom(String maNhom) {
        String maGiangVien = layMaGiangVienDangNhap();

        return kiemTraQuyenCham(maNhom, maGiangVien);
    }

    // Nếu đã có phiếu thì đưa điểm và nhận xét cũ lên form.
    @Transactional(readOnly = true)
    public PhieuChamDiemForm layFormChamDiem(String maNhom) {
        String maGiangVien = layMaGiangVienDangNhap();

        kiemTraQuyenCham(maNhom, maGiangVien);

        PhieuChamDiemId id = new PhieuChamDiemId(
            maNhom,
            maGiangVien
        );

        PhieuChamDiem phieu = phieuChamDiemRepository
            .findById(id)
            .orElse(null);

        PhieuChamDiemForm form = new PhieuChamDiemForm();

        if (phieu != null) {
            // Khóa chính không chứa loại phiếu nên cần kiểm tra loại.
            if (phieu.getLoaiPhieu() != LoaiPhieuCham.HOI_DONG) {
                throw new IllegalArgumentException(
                    "Bạn đã có phiếu chấm thuộc loại khác cho nhóm này."
                );
            }

            form.setDiem(phieu.getDiem());
            form.setNhanXet(phieu.getNhanXet());
        }

        return form;
    }

    // Lưu nháp cho phép chưa nhập đủ điểm và nhận xét.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void luuNhap(
            @NotBlank String maNhom,
            @NotNull @Valid PhieuChamDiemForm form) {

        String maGiangVien = layMaGiangVienDangNhap();

        // Khóa dữ liệu và kiểm tra lại quyền trước khi ghi.
        khoaVaKiemTraQuyenCham(maNhom, maGiangVien);

        PhieuChamDiemId id = new PhieuChamDiemId(
            maNhom,
            maGiangVien
        );

        PhieuChamDiem phieu = phieuChamDiemRepository
            .findById(id)
            .orElse(null);

        if (phieu == null) {
            // Chưa có phiếu: tạo phiếu hội đồng cho chính giảng viên này.
            phieu = new PhieuChamDiem();
            phieu.setId(id);
            phieu.setLoaiPhieu(LoaiPhieuCham.HOI_DONG);
            phieu.setMaNhomHoiDong(maNhom);
            phieu.setMaDeTaiPhanCong(null);
        } else {
            // Không ghi đè phiếu phản biện hoặc phiếu chấm độc lập.
            if (phieu.getLoaiPhieu() != LoaiPhieuCham.HOI_DONG) {
                throw new IllegalArgumentException(
                    "Bạn đã có phiếu chấm thuộc loại khác cho nhóm này."
                );
            }

            // Phiếu đã nộp không được sửa qua chức năng lưu nháp.
            if (phieu.getTrangThai() != TrangThaiPhieuCham.NHAP) {
                throw new IllegalArgumentException(
                    "Phiếu đã nộp, không thể lưu nháp hoặc chỉnh sửa."
                );
            }
        }

        phieu.setDiem(form.getDiem());

        // Bỏ khoảng trắng ở đầu và cuối nhận xét nếu có.
        String nhanXet = form.getNhanXet();
        phieu.setNhanXet(
            nhanXet == null ? null : nhanXet.strip()
        );

        phieu.setTrangThai(TrangThaiPhieuCham.NHAP);
        phieu.setNopLuc(null);

        phieuChamDiemRepository.saveAndFlush(phieu);
    }

    // Nộp chính thức: phải có điểm và nhận xét đầy đủ.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void nopPhieu(
            @NotBlank String maNhom,
            @NotNull @Valid PhieuChamDiemForm form) {

        String maGiangVien = layMaGiangVienDangNhap();

        // Các trường được phép trống khi lưu nháp phải đủ khi nộp.
        if (form.getDiem() == null) {
            throw new IllegalArgumentException(
                "Bạn phải nhập điểm trước khi nộp phiếu."
            );
        }

        String nhanXet = form.getNhanXet();

        if (nhanXet == null || nhanXet.isBlank()) {
            throw new IllegalArgumentException(
                "Bạn phải nhập nhận xét trước khi nộp phiếu."
            );
        }

        // Kiểm tra quyền và giữ khóa đến khi transaction kết thúc.
        khoaVaKiemTraQuyenCham(maNhom, maGiangVien);

        PhieuChamDiemId id = new PhieuChamDiemId(
            maNhom,
            maGiangVien
        );

        PhieuChamDiem phieu = phieuChamDiemRepository
            .findById(id)
            .orElse(null);

        if (phieu == null) {
            // Cho phép nộp trực tiếp mà không bắt buộc lưu nháp trước.
            phieu = new PhieuChamDiem();
            phieu.setId(id);
            phieu.setLoaiPhieu(LoaiPhieuCham.HOI_DONG);
            phieu.setMaNhomHoiDong(maNhom);
            phieu.setMaDeTaiPhanCong(null);
        } else {
            if (phieu.getLoaiPhieu() != LoaiPhieuCham.HOI_DONG) {
                throw new IllegalArgumentException(
                    "Bạn đã có phiếu chấm thuộc loại khác cho nhóm này."
                );
            }

            // Chặn nộp lại hoặc thay đổi nội dung phiếu đã nộp.
            if (phieu.getTrangThai() != TrangThaiPhieuCham.NHAP) {
                throw new IllegalArgumentException(
                    "Phiếu đã được nộp, không thể nộp lại hoặc chỉnh sửa."
                );
            }
        }

        // Lấy nội dung hiện tại trên form khi người dùng bấm Nộp.
        phieu.setDiem(form.getDiem());
        phieu.setNhanXet(nhanXet.strip());
        phieu.setTrangThai(TrangThaiPhieuCham.DA_NOP);

        // Thời điểm nộp do backend xác định, không nhận từ trình duyệt.
        phieu.setNopLuc(
            LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        );

        phieuChamDiemRepository.saveAndFlush(phieu);
    }

    // Trả về trạng thái phiếu của chính giảng viên đang đăng nhập.
    @Transactional(readOnly = true)
    public TrangThaiPhieuCham layTrangThaiPhieu(String maNhom) {
        String maGiangVien = layMaGiangVienDangNhap();

        kiemTraQuyenCham(maNhom, maGiangVien);

        PhieuChamDiemId id = new PhieuChamDiemId(
            maNhom,
            maGiangVien
        );

        PhieuChamDiem phieu = phieuChamDiemRepository
            .findById(id)
            .orElse(null);

        // Chưa có bản ghi: giao diện cho phép nhập phiếu mới.
        if (phieu == null) {
            return TrangThaiPhieuCham.NHAP;
        }

        if (phieu.getLoaiPhieu() != LoaiPhieuCham.HOI_DONG) {
            throw new IllegalArgumentException(
                "Bạn đã có phiếu chấm thuộc loại khác cho nhóm này."
            );
        }

        return phieu.getTrangThai();
    }

    // Chỉ gọi từ hàm lưu/nộp đang chạy trong transaction ghi.
    private void khoaVaKiemTraQuyenCham(
            String maNhom,
            String maGiangVien) {

        // Kiểm tra quyền trước khi giữ khóa dữ liệu.
        kiemTraQuyenCham(maNhom, maGiangVien);

        HoiDongDeTai phanCong = hoiDongDeTaiRepository
            .findById(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Nhóm chưa được phân công hội đồng."
            ));

        String maHoiDong = phanCong.getMaHoiDong();

        // Giữ cùng thứ tự khóa với chức năng phân công:
        // hội đồng trước, đăng ký của nhóm sau.
        hoiDongRepository.timVaKhoaTheoMa(maHoiDong)
            .orElseThrow(() -> new IllegalArgumentException(
                "Hội đồng không còn tồn tại."
            ));

        hoiDongDeTaiRepository.khoaDangKyTheoMaNhom(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Đăng ký đề tài của nhóm không còn tồn tại."
            ));

        // Trong lúc chờ khóa, trạng thái hoặc phân công có thể đã đổi.
        NhomChamDiemHienThi nhom = kiemTraQuyenCham(
            maNhom,
            maGiangVien
        );

        if (!maHoiDong.equals(nhom.getMaHoiDong())) {
            throw new IllegalArgumentException(
                "Hội đồng của nhóm đã thay đổi. Vui lòng tải lại trang."
            );
        }
    }

    // Dùng chung khi mở trang và khi xử lý lưu/nộp ở các bước tiếp theo.
    private NhomChamDiemHienThi kiemTraQuyenCham(
            String maNhom,
            String maGiangVien) {

        if (maNhom == null || maNhom.isBlank()) {
            throw new IllegalArgumentException(
                "Mã nhóm không được để trống."
            );
        }

        if (phieuChamDiemRepository
                .demPhanCongHuongDan(maNhom, maGiangVien) > 0) {

            throw new AccessDeniedException(
                "Bạn không được chấm đề tài mà mình hướng dẫn."
            );
        }

        // Truy vấn đã kiểm tra thành viên hội đồng, trạng thái và cùng đợt.
        return phieuChamDiemRepository
            .layDanhSachNhomDuocCham(maGiangVien)
            .stream()
            .filter(nhom -> maNhom.equals(nhom.getMaNhom()))
            .findFirst()
            .orElseThrow(() -> new AccessDeniedException(
                "Nhóm không thuộc phạm vi được chấm hoặc chưa ở trạng thái chấm."
            ));
    }

    private String layMaGiangVienDangNhap() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        // Chặn yêu cầu chưa đăng nhập hoặc đang dùng tài khoản anonymous.
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new AccessDeniedException(
                "Bạn cần đăng nhập bằng tài khoản giảng viên."
            );
        }

        String maGiangVien = CurrentUser.maNguoiDung();

        // Kiểm tra lại database vì tài khoản có thể bị khóa sau khi đăng nhập.
        if (thanhVienHoiDongRepository
                .demGiangVienHopLe(maGiangVien) == 0) {

            throw new AccessDeniedException(
                "Tài khoản giảng viên không còn hoạt động hoặc không có quyền chấm."
            );
        }

        return maGiangVien;
    }
}