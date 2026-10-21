package vn.edu.group12.qldt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.group12.qldt.dto.DotDangKyDto;
import vn.edu.group12.qldt.model.DotDangKy;
import vn.edu.group12.qldt.repository.DotDangKyRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DotDangKyService {

    @Autowired
    private DotDangKyRepository dotDangKyRepository;

    public List<DotDangKy> getAllDotDangKy() {
        return dotDangKyRepository.findAllByOrderByCreatedAtDesc();
    }

    public DotDangKy getDotDangKyById(Long id) {
        return dotDangKyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký với ID: " + id));
    }

    @Transactional
    public DotDangKy createDotDangKy(DotDangKyDto dto, Long nguoiTaoId) {
        validateBusinessRules(dto);

        if (dotDangKyRepository.existsByMaDot(dto.getMaDot())) {
            throw new IllegalArgumentException("Mã đợt đăng ký '" + dto.getMaDot() + "' đã tồn tại!");
        }

        DotDangKy dot = new DotDangKy();
        mapDtoToEntity(dto, dot);
        dot.setNguoiTaoId(nguoiTaoId);
        dot.setTrangThai("NHAP");

        return dotDangKyRepository.save(dot);
    }

    @Transactional
    public DotDangKy updateDotDangKy(Long id, DotDangKyDto dto) {
        validateBusinessRules(dto);

        DotDangKy dot = getDotDangKyById(id);
        if (!dot.getMaDot().equals(dto.getMaDot()) && dotDangKyRepository.existsByMaDot(dto.getMaDot())) {
            throw new IllegalArgumentException("Mã đợt đăng ký '" + dto.getMaDot() + "' đã tồn tại!");
        }

        mapDtoToEntity(dto, dot);
        if (dto.getTrangThai() != null && !dto.getTrangThai().isBlank()) {
            dot.setTrangThai(dto.getTrangThai());
        }
        return dotDangKyRepository.save(dot);
    }

    @Transactional
    public void congBoDot(Long id) {
        DotDangKy dot = getDotDangKyById(id);
        dot.setTrangThai("CONG_BO");
        dotDangKyRepository.save(dot);
    }

    @Transactional
    public void dongDot(Long id) {
        DotDangKy dot = getDotDangKyById(id);
        dot.setTrangThai("DONG");
        dotDangKyRepository.save(dot);
    }

    // Kiểm tra ràng buộc thời gian 2 giai đoạn và loại đề tài
    private void validateBusinessRules(DotDangKyDto dto) {
        if (dto.getBatDauGv() == null || dto.getKetThucGv() == null ||
            dto.getBatDauSv() == null || dto.getKetThucSv() == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thời gian bắt đầu và kết thúc của 2 giai đoạn!");
        }

        // Ràng buộc quy trình 2 giai đoạn: bat_dau_gv < ket_thuc_gv <= bat_dau_sv < ket_thuc_sv
        if (!dto.getBatDauGv().isBefore(dto.getKetThucGv())) {
            throw new IllegalArgumentException("Giai đoạn 1: Thời gian bắt đầu giảng viên phải trước thời gian kết thúc!");
        }
        if (dto.getKetThucGv().isAfter(dto.getBatDauSv())) {
            throw new IllegalArgumentException("Giai đoạn 1 (kết thúc GV) phải trước hoặc bằng thời gian bắt đầu Giai đoạn 2 (SV)!");
        }
        if (!dto.getBatDauSv().isBefore(dto.getKetThucSv())) {
            throw new IllegalArgumentException("Giai đoạn 2: Thời gian bắt đầu sinh viên phải trước thời gian kết thúc!");
        }

        // Ràng buộc loại đề tài
        String loai = dto.getLoaiDeTai();
        if ("TLCN".equals(loai) || "KLTN".equals(loai)) {
            if (dto.getHanNopDiemPb() == null) {
                throw new IllegalArgumentException("Đề tài TLCN và KLTN bắt buộc phải có hạn nộp điểm phản biện!");
            }
            if (!dto.getHanNopDiemPb().isAfter(dto.getKetThucSv())) {
                throw new IllegalArgumentException("Hạn nộp điểm phản biện phải sau thời gian kết thúc đăng ký của sinh viên!");
            }
        } else {
            dto.setHanNopDiemPb(null);
        }

        if ("KLTN".equals(loai)) {
            if (dto.getNgayBaoCaoHd() == null) {
                throw new IllegalArgumentException("Khóa luận tốt nghiệp (KLTN) bắt buộc phải có ngày báo cáo hội đồng!");
            }
        } else {
            dto.setNgayBaoCaoHd(null);
        }

        if (dto.getHanNopBaoCao() != null && !dto.getHanNopBaoCao().isAfter(dto.getKetThucSv())) {
            throw new IllegalArgumentException("Hạn nộp báo cáo phải sau thời gian kết thúc đăng ký của sinh viên!");
        }
    }

    private void mapDtoToEntity(DotDangKyDto dto, DotDangKy dot) {
        dot.setMaDot(dto.getMaDot());
        dot.setTenDot(dto.getTenDot());
        dot.setLoaiDeTai(dto.getLoaiDeTai());
        dot.setBatDauGv(dto.getBatDauGv());
        dot.setKetThucGv(dto.getKetThucGv());
        dot.setBatDauSv(dto.getBatDauSv());
        dot.setKetThucSv(dto.getKetThucSv());
        dot.setHanNopBaoCao(dto.getHanNopBaoCao());
        dot.setHanNopDiemPb(dto.getHanNopDiemPb());
        dot.setNgayBaoCaoHd(dto.getNgayBaoCaoHd());
    }

    // Helper kiểm tra giai đoạn hiện tại để hiển thị giao diện
    public String getGiaiDoanHienTai(DotDangKy dot) {
        LocalDateTime now = LocalDateTime.now();
        if ("DONG".equals(dot.getTrangThai())) {
            return "Đã đóng";
        }
        if (now.isBefore(dot.getBatDauGv())) {
            return "Chưa mở (Sắp diễn ra)";
        }
        if (!now.isAfter(dot.getKetThucGv())) {
            return "Giai đoạn 1: GV đề xuất đề tài";
        }
        if (now.isBefore(dot.getBatDauSv())) {
            return "Chờ mở Giai đoạn 2 (SV)";
        }
        if (!now.isAfter(dot.getKetThucSv())) {
            return "Giai đoạn 2: SV đăng ký đề tài";
        }
        return "Hết hạn đăng ký";
    }

    public boolean isGiaiDoan1(DotDangKy dot) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(dot.getBatDauGv()) && !now.isAfter(dot.getKetThucGv());
    }

    public boolean isGiaiDoan2(DotDangKy dot) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(dot.getBatDauSv()) && !now.isAfter(dot.getKetThucSv());
    }
}