package vn.edu.group12.qldt.service.detai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.group12.qldt.dto.detai.DeTaiDto;
import vn.edu.group12.qldt.entity.detai.DeTai;
import vn.edu.group12.qldt.entity.detai.DotDangKy;
import vn.edu.group12.qldt.repository.detai.DeTaiRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
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

    public List<DeTai> filterDeTai(Long dotId, Long boMonId, String trangThai, String keyword) {
        return deTaiRepository.filterDeTai(dotId, boMonId, trangThai, keyword);
    }

    public DeTai getDeTaiById(Long id) {
        return deTaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề tài với ID: " + id));
    }

    // Giảng viên đề xuất đề tài trong Giai đoạn 1
    @Transactional
    public DeTai deXuatDeTai(DeTaiDto dto, Long giangVienId) {
        DotDangKy dot = dotDangKyService.getDotDangKyById(dto.getDotDangKyId());

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
        deTai.setDotDangKyId(dto.getDotDangKyId());
        deTai.setBoMonId(dto.getBoMonId() != null ? dto.getBoMonId() : 1L);
        deTai.setTenDeTai(dto.getTenDeTai());
        deTai.setNoiDung(dto.getNoiDung());
        deTai.setYeuCau(dto.getYeuCau());
        deTai.setNguoiDeXuatId(giangVienId);
        deTai.setTrangThai("CHO_DUYET");

        DeTai saved = deTaiRepository.save(deTai);

        // Tự động phân công GV đề xuất làm Giảng viên hướng dẫn 1
        deTaiGiangVienService.phanCong(saved.getId(), giangVienId, "HUONG_DAN", giangVienId);

        return saved;
    }

    // Trưởng khoa duyệt đề tài
    @Transactional
    public DeTai duyetDeTai(Long deTaiId, Long nguoiDuyetId) {
        DeTai deTai = getDeTaiById(deTaiId);
        deTai.setTrangThai("DA_DUYET");
        deTai.setNguoiDuyetId(nguoiDuyetId);
        deTai.setDuyetLuc(LocalDateTime.now());
        deTai.setLyDoTuChoi(null);
        return deTaiRepository.save(deTai);
    }

    // Trưởng khoa từ chối đề tài (BẮT BUỘC có lý do)
    @Transactional
    public DeTai tuChoiDeTai(Long deTaiId, String lyDoTuChoi, Long nguoiDuyetId) {
        if (lyDoTuChoi == null || lyDoTuChoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Bắt buộc phải nhập lý do từ chối đề tài!");
        }
        DeTai deTai = getDeTaiById(deTaiId);
        deTai.setTrangThai("TU_CHOI");
        deTai.setLyDoTuChoi(lyDoTuChoi.trim());
        deTai.setNguoiDuyetId(nguoiDuyetId);
        deTai.setDuyetLuc(LocalDateTime.now());
        return deTaiRepository.save(deTai);
    }

    // Công bố danh sách đề tài (Chỉ công bố đề tài đã duyệt và đã có GVHD)
    @Transactional
    public DeTai congBoDeTai(Long deTaiId) {
        DeTai deTai = getDeTaiById(deTaiId);
        if (!"DA_DUYET".equals(deTai.getTrangThai())) {
            throw new IllegalArgumentException("Chỉ được công bố đề tài đã ở trạng thái 'DA_DUYET'!");
        }

        // Ràng buộc: Đề tài phải có ít nhất 1 GVHD trước khi công bố
        long countGvhd = deTaiGiangVienService.countGvhd(deTaiId);
        if (countGvhd < 1) {
            throw new IllegalArgumentException("Đề tài chưa có Giảng viên hướng dẫn nên không thể công bố!");
        }

        deTai.setTrangThai("CONG_BO");
        deTai.setCongBoLuc(LocalDateTime.now());
        return deTaiRepository.save(deTai);
    }

    // Danh sách đề tài cho Sinh viên xem và chọn (Chỉ lấy đề tài ĐÃ CÔNG BỐ trong Giai đoạn 2)
    public List<DeTai> getDeTaiChoSinhVien(Long dotId) {
        DotDangKy dot = dotDangKyService.getDotDangKyById(dotId);
        if (!dotDangKyService.isGiaiDoan2(dot)) {
            throw new IllegalArgumentException("Đợt đăng ký hiện chưa hoặc đã qua Giai đoạn 2! Sinh viên chỉ được xem và chọn đề tài trong Giai đoạn 2.");
        }
        return deTaiRepository.findByDotDangKyIdAndTrangThai(dotId, "CONG_BO");
    }
}
