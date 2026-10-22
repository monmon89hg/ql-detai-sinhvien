package vn.edu.group12.qldt.service.detai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.group12.qldt.dto.detai.DotDangKyDto;
import vn.edu.group12.qldt.entity.detai.DotDangKy;
import vn.edu.group12.qldt.repository.detai.DotDangKyRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA', 'TRUONG_BO_MON', 'GIANG_VIEN', 'SINH_VIEN')")
public class DotDangKyService {

    @Autowired
    private DotDangKyRepository dotDangKyRepository;

    public List<DotDangKy> getAllDotDangKy() {
        return dotDangKyRepository.findAllByOrderByCreatedAtDesc();
    }

    public DotDangKy getDotDangKyByMa(String maDot) {
        return dotDangKyRepository.findById(maDot)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký với mã: " + maDot));
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional
    public DotDangKy createDotDangKy(DotDangKyDto dto, String maNguoiTao) {
        validateBusinessRules(dto);

        if (dotDangKyRepository.existsByMaDot(dto.getMaDot())) {
            throw new IllegalArgumentException("Mã đợt đăng ký '" + dto.getMaDot() + "' đã tồn tại!");
        }

        DotDangKy dot = new DotDangKy();
        mapDtoToEntity(dto, dot);
        dot.setMaNguoiTao(maNguoiTao);
        dot.setTrangThai("NHAP");

        return dotDangKyRepository.save(dot);
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional
    public DotDangKy updateDotDangKy(String maDot, DotDangKyDto dto) {
        validateBusinessRules(dto);

        DotDangKy dot = getDotDangKyByMa(maDot);

        mapDtoToEntity(dto, dot);
        if (dto.getTrangThai() != null && !dto.getTrangThai().isBlank()) {
            dot.setTrangThai(dto.getTrangThai());
        }
        return dotDangKyRepository.save(dot);
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional
    public void congBoDot(String maDot) {
        DotDangKy dot = getDotDangKyByMa(maDot);
        dot.setTrangThai("CONG_BO");
        dotDangKyRepository.save(dot);
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional
    public void dongDot(String maDot) {
        DotDangKy dot = getDotDangKyByMa(maDot);
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

    // Kiểm tra đợt có đang ở Giai đoạn 1 (Giảng viên đề xuất đề tài) không
    public boolean isGiaiDoan1(DotDangKy dot) {
        LocalDateTime now = LocalDateTime.now();
        return (now.isEqual(dot.getBatDauGv()) || now.isAfter(dot.getBatDauGv()))
                && (now.isEqual(dot.getKetThucGv()) || now.isBefore(dot.getKetThucGv()));
    }

    // Kiểm tra đợt có đang ở Giai đoạn 2 (Sinh viên xem & chọn đề tài) không
    public boolean isGiaiDoan2(DotDangKy dot) {
        LocalDateTime now = LocalDateTime.now();
        return (now.isEqual(dot.getBatDauSv()) || now.isAfter(dot.getBatDauSv()))
                && (now.isEqual(dot.getKetThucSv()) || now.isBefore(dot.getKetThucSv()));
    }

    // Hiển thị trạng thái tiến trình giai đoạn của đợt
    public String getGiaiDoanHienTai(DotDangKy dot) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(dot.getBatDauGv())) {
            return "Chưa bắt đầu (Sắp diễn ra)";
        } else if (isGiaiDoan1(dot)) {
            return "Giai đoạn 1: GV đề xuất đề tài";
        } else if (now.isAfter(dot.getKetThucGv()) && now.isBefore(dot.getBatDauSv())) {
            return "Thời gian chờ công bố (TK xét duyệt)";
        } else if (isGiaiDoan2(dot)) {
            return "Giai đoạn 2: SV chọn đề tài";
        } else {
            return "Đã kết thúc đăng ký";
        }
    }
}
