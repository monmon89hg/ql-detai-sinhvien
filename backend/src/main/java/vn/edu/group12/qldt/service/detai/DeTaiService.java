package vn.edu.group12.qldt.service.detai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.group12.qldt.dto.detai.DeTaiDto;
import vn.edu.group12.qldt.entity.detai.DeTai;
import vn.edu.group12.qldt.entity.detai.DotDangKy;
import vn.edu.group12.qldt.repository.detai.DeTaiRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA', 'TRUONG_BO_MON', 'GIANG_VIEN', 'SINH_VIEN')")
public class DeTaiService {

    @Autowired
    private DeTaiRepository deTaiRepository;

    @Autowired
    private DotDangKyService dotDangKyService;

    @Autowired
    private DeTaiGiangVienService deTaiGiangVienService;

    public List<DeTai> getAllDeTai() {
        return deTaiRepository.findAll();
    }

    public List<DeTai> filterDeTai(String maDot, String maBoMon, String trangThai, String keyword) {
        return deTaiRepository.filterDeTai(maDot, maBoMon, trangThai, keyword);
    }

    public DeTai getDeTaiByMa(String maDeTai) {
        return deTaiRepository.findById(maDeTai)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề tài với mã: " + maDeTai));
    }

    // Giảng viên đề xuất đề tài trong Giai đoạn 1
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_BO_MON')")
    @Transactional
    public DeTai deXuatDeTai(DeTaiDto dto, String maGiangVien) {
        DotDangKy dot = dotDangKyService.getDotDangKyByMa(dto.getMaDot());

        // Kiểm tra thời gian: Chỉ được đề xuất trong Giai đoạn 1 của đợt
        if (!dotDangKyService.isGiaiDoan1(dot)) {
            throw new IllegalArgumentException("Đợt đăng ký hiện không trong Giai đoạn 1! Giảng viên không thể đề xuất đề tài.");
        }

        // Tự động sinh mã đề tài nếu chưa có
        String maDeTai = dto.getMaDeTai();
        if (maDeTai == null || maDeTai.isBlank()) {
            maDeTai = "DT" + System.currentTimeMillis() % 1000000;
        }

        if (deTaiRepository.existsByMaDeTai(maDeTai)) {
            throw new IllegalArgumentException("Mã đề tài '" + maDeTai + "' đã tồn tại!");
        }

        DeTai deTai = new DeTai();
        deTai.setMaDeTai(maDeTai);
        deTai.setMaDot(dto.getMaDot());
        deTai.setMaBoMon(dto.getMaBoMon() != null && !dto.getMaBoMon().isBlank() ? dto.getMaBoMon() : "DEMO_CNPM");
        deTai.setTenDeTai(dto.getTenDeTai());
        deTai.setNoiDung(dto.getNoiDung());
        deTai.setYeuCau(dto.getYeuCau());
        deTai.setMaNguoiDeXuat(maGiangVien);
        deTai.setTrangThai("CHO_DUYET");

        DeTai saved = deTaiRepository.save(deTai);

        // Tự động phân công GV đề xuất làm Giảng viên hướng dẫn 1
        deTaiGiangVienService.phanCongNguoiDeXuat(saved.getMaDeTai());

        return saved;
    }

    // Trưởng khoa duyệt đề tài
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional
    public DeTai duyetDeTai(String maDeTai, String maNguoiDuyet) {
        DeTai deTai = getDeTaiByMa(maDeTai);
        if (!"CHO_DUYET".equals(deTai.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể duyệt đề tài khi đang ở trạng thái 'CHO_DUYET'! Trạng thái hiện tại: " + deTai.getTrangThai());
        }
        deTai.setTrangThai("DA_DUYET");
        deTai.setMaNguoiDuyet(maNguoiDuyet);
        deTai.setDuyetLuc(LocalDateTime.now());
        deTai.setLyDoTuChoi(null);
        return deTaiRepository.save(deTai);
    }

    // Trưởng khoa từ chối đề tài (BẮT BUỘC có lý do)
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional
    public DeTai tuChoiDeTai(String maDeTai, String lyDoTuChoi, String maNguoiDuyet) {
        if (lyDoTuChoi == null || lyDoTuChoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Bắt buộc phải nhập lý do từ chối đề tài!");
        }
        DeTai deTai = getDeTaiByMa(maDeTai);
        if (!"CHO_DUYET".equals(deTai.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể từ chối đề tài khi đang ở trạng thái 'CHO_DUYET'! Trạng thái hiện tại: " + deTai.getTrangThai());
        }
        deTai.setTrangThai("TU_CHOI");
        deTai.setLyDoTuChoi(lyDoTuChoi.trim());
        deTai.setMaNguoiDuyet(maNguoiDuyet);
        deTai.setDuyetLuc(LocalDateTime.now());
        return deTaiRepository.save(deTai);
    }

    // Công bố danh sách đề tài (Chỉ công bố đề tài đã duyệt và đã có GVHD)
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional
    public DeTai congBoDeTai(String maDeTai) {
        DeTai deTai = getDeTaiByMa(maDeTai);
        if (!"DA_DUYET".equals(deTai.getTrangThai())) {
            throw new IllegalStateException("Chỉ được công bố đề tài đã ở trạng thái 'DA_DUYET'! Trạng thái hiện tại: " + deTai.getTrangThai());
        }

        DotDangKy dot = dotDangKyService.getDotDangKyByMa(deTai.getMaDot());
        if ("DONG".equals(dot.getTrangThai())) {
            throw new IllegalStateException("Không thể công bố đề tài thuộc đợt đăng ký đã đóng!");
        }

        // Ràng buộc: Đề tài phải có ít nhất 1 GVHD trước khi công bố
        long countGvhd = deTaiGiangVienService.countGvhd(maDeTai);
        if (countGvhd < 1) {
            throw new IllegalStateException("Đề tài chưa có Giảng viên hướng dẫn nên không thể công bố!");
        }

        deTai.setTrangThai("CONG_BO");
        deTai.setCongBoLuc(LocalDateTime.now());
        return deTaiRepository.save(deTai);
    }

    // Danh sách đề tài cho Sinh viên xem và chọn (Chỉ lấy đề tài ĐÃ CÔNG BỐ trong Giai đoạn 2)
    public List<DeTai> getDeTaiChoSinhVien(String maDot) {
        DotDangKy dot = dotDangKyService.getDotDangKyByMa(maDot);
        if (!dotDangKyService.isGiaiDoan2(dot)) {
            throw new IllegalArgumentException("Đợt đăng ký hiện chưa hoặc đã qua Giai đoạn 2! Sinh viên chỉ được xem và chọn đề tài trong Giai đoạn 2.");
        }
        return deTaiRepository.findByMaDotAndTrangThai(maDot, "CONG_BO");
    }

    // Giảng viên đề xuất hoặc Trưởng khoa/Admin chỉnh sửa thông tin đề tài
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_BO_MON', 'TRUONG_KHOA', 'ADMIN')")
    @Transactional
    public DeTai capNhatDeTai(String maDeTai, DeTaiDto dto, String maNguoiCapNhat) {
        DeTai deTai = getDeTaiByMa(maDeTai);

        boolean laTruongKhoaHoacAdmin = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRUONG_KHOA") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!laTruongKhoaHoacAdmin && !deTai.getMaNguoiDeXuat().equals(maNguoiCapNhat)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa đề tài do người khác đề xuất!");
        }

        if ("NGUNG_NHAN".equals(deTai.getTrangThai())) {
            throw new IllegalStateException("Đề tài đã ngừng nhận, không thể chỉnh sửa!");
        }

        long soDangKy = deTaiRepository.countDangKyByMaDeTai(maDeTai);
        if ("CONG_BO".equals(deTai.getTrangThai()) && soDangKy > 0) {
            throw new IllegalStateException("Đề tài đã có sinh viên/nhóm đăng ký! Không thể chỉnh sửa, vui lòng dùng cơ chế Ngừng nhận đề tài.");
        }

        if (dto.getTenDeTai() != null && !dto.getTenDeTai().isBlank()) {
            deTai.setTenDeTai(dto.getTenDeTai().trim());
        }
        if (dto.getNoiDung() != null && !dto.getNoiDung().isBlank()) {
            deTai.setNoiDung(dto.getNoiDung().trim());
        }
        if (dto.getYeuCau() != null) {
            deTai.setYeuCau(dto.getYeuCau().trim());
        }
        if (dto.getMaBoMon() != null && !dto.getMaBoMon().isBlank()) {
            deTai.setMaBoMon(dto.getMaBoMon().trim());
        }

        // Nếu đề tài đang bị từ chối mà giảng viên sửa lại nội dung, tự động chuyển về CHO_DUYET để Trưởng khoa xem lại
        if ("TU_CHOI".equals(deTai.getTrangThai())) {
            deTai.setTrangThai("CHO_DUYET");
            deTai.setLyDoTuChoi(null);
        } else if ("DA_DUYET".equals(deTai.getTrangThai()) && !laTruongKhoaHoacAdmin) {
            deTai.setTrangThai("CHO_DUYET");
        }

        return deTaiRepository.save(deTai);
    }

    // Cơ chế ngừng nhận đăng ký thay vì xóa vật lý đề tài đã có dữ liệu
    @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'ADMIN', 'GIANG_VIEN', 'TRUONG_BO_MON')")
    @Transactional
    public DeTai ngungNhanDeTai(String maDeTai, String maNguoiThucHien) {
        DeTai deTai = getDeTaiByMa(maDeTai);

        boolean laTruongKhoaHoacAdmin = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRUONG_KHOA") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!laTruongKhoaHoacAdmin && !deTai.getMaNguoiDeXuat().equals(maNguoiThucHien)) {
            throw new AccessDeniedException("Bạn không có quyền chuyển trạng thái đề tài này!");
        }

        if ("NGUNG_NHAN".equals(deTai.getTrangThai())) {
            throw new IllegalStateException("Đề tài này hiện đã ở trạng thái ngừng nhận đăng ký!");
        }

        deTai.setTrangThai("NGUNG_NHAN");
        return deTaiRepository.save(deTai);
    }

    // Cơ chế xóa an toàn / ngừng hủy: Bảo vệ dữ liệu liên quan, chống xóa vật lý
    @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'ADMIN', 'GIANG_VIEN', 'TRUONG_BO_MON')")
    @Transactional
    public String xoaHoacHuyDeTai(String maDeTai, String maNguoiThucHien) {
        DeTai deTai = getDeTaiByMa(maDeTai);

        boolean laTruongKhoaHoacAdmin = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRUONG_KHOA") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!laTruongKhoaHoacAdmin && !deTai.getMaNguoiDeXuat().equals(maNguoiThucHien)) {
            throw new AccessDeniedException("Bạn không có quyền xóa hoặc hủy đề tài này!");
        }

        // Kiểm tra xem đề tài đã có dữ liệu liên quan chưa (đăng ký của sinh viên)
        long soDangKy = deTaiRepository.countDangKyByMaDeTai(maDeTai);
        if (soDangKy > 0 || "CONG_BO".equals(deTai.getTrangThai())) {
            // TUYỆT ĐỐI KHÔNG XÓA VẬT LÝ: Tự động chuyển sang NGUNG_NHAN để bảo toàn dữ liệu lịch sử
            deTai.setTrangThai("NGUNG_NHAN");
            deTaiRepository.save(deTai);
            return "Đề tài đã có dữ liệu liên quan hoặc đã công bố nên hệ thống tự động chuyển sang trạng thái NGỪNG NHẬN thay vì xóa vật lý.";
        }

        // Nếu chưa có sinh viên đăng ký và đang ở giai đoạn đề xuất/nháp: Xóa phân công giảng viên rồi xóa vật lý an toàn
        deTaiGiangVienService.xoaTatCaPhanCongCuaDeTai(maDeTai);
        deTaiRepository.delete(deTai);
        return "Đã xóa đề tài thành công!";
    }
}
