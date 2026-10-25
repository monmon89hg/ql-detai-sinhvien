package vn.edu.group12.qldt.service.hoidong;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.dto.hoidong.NhomKetQuaHienThi;
import vn.edu.group12.qldt.repository.hoidong.HoiDongDeTaiRepository;
import jakarta.persistence.EntityNotFoundException;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiem;
import vn.edu.group12.qldt.repository.hoidong.PhieuChamDiemRepository;

@Service
public class AdminXemChamDiemService {

    private final HoiDongDeTaiRepository hoiDongDeTaiRepository;
    private final PhieuChamDiemRepository phieuChamDiemRepository;

    public AdminXemChamDiemService(
            HoiDongDeTaiRepository hoiDongDeTaiRepository,
            PhieuChamDiemRepository phieuChamDiemRepository) {
        this.hoiDongDeTaiRepository = hoiDongDeTaiRepository;
        this.phieuChamDiemRepository = phieuChamDiemRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    @Transactional(readOnly = true)
    public List<NhomKetQuaHienThi> layDanhSachNhom() {
        return hoiDongDeTaiRepository.layDanhSachNhomChoTruongKhoa();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    @Transactional(readOnly = true)
    public NhomKetQuaHienThi layThongTinNhom(String maNhom) {
        return hoiDongDeTaiRepository.layDanhSachNhomChoTruongKhoa()
            .stream()
            .filter(nhom -> nhom.getMaNhom().equals(maNhom))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                "Không tìm thấy đăng ký đề tài hợp lệ."
            ));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    @Transactional(readOnly = true)
    public List<PhieuChamDiem> layPhieuChamTheoNhom(String maNhom) {
        if (hoiDongDeTaiRepository.layDanhSachNhomChoTruongKhoa().stream()
                .noneMatch(n -> maNhom.equals(n.getMaNhom()))) {
            throw new EntityNotFoundException("Không tìm thấy đăng ký đề tài hợp lệ.");
        }

        return phieuChamDiemRepository
            .findById_MaNhomOrderById_MaGiangVienAsc(maNhom);
    }
}
