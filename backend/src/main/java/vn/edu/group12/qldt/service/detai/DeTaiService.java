package vn.edu.group12.qldt.service.detai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
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
}
