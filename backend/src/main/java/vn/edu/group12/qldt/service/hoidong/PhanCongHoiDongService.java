package vn.edu.group12.qldt.service.hoidong;

import vn.edu.group12.qldt.service.auth.CurrentUser;

import java.util.List;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Isolation;

import vn.edu.group12.qldt.dto.hoidong.PhanCongHoiDongForm;
import vn.edu.group12.qldt.entity.hoidong.HoiDong;
import vn.edu.group12.qldt.entity.hoidong.HoiDongDeTai;
import vn.edu.group12.qldt.entity.hoidong.ThanhVienHoiDong;
import vn.edu.group12.qldt.enums.hoidong.ChucDanhHoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiHoiDong;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import vn.edu.group12.qldt.dto.hoidong.NhomPhanCongLuaChon;
import vn.edu.group12.qldt.repository.hoidong.HoiDongDeTaiRepository;
import vn.edu.group12.qldt.repository.hoidong.HoiDongRepository;
import vn.edu.group12.qldt.repository.hoidong.ThanhVienHoiDongRepository;

@Service
@Validated
public class PhanCongHoiDongService {

    private final HoiDongRepository hoiDongRepository;
    private final HoiDongDeTaiRepository hoiDongDeTaiRepository;
    private final ThanhVienHoiDongRepository thanhVienHoiDongRepository;
    private final vn.edu.group12.qldt.repository.hoidong.DanhGiaRepository danhGiaRepository;

    public PhanCongHoiDongService(
            HoiDongRepository hoiDongRepository,
            HoiDongDeTaiRepository hoiDongDeTaiRepository,
            ThanhVienHoiDongRepository thanhVienHoiDongRepository,
            vn.edu.group12.qldt.repository.hoidong.DanhGiaRepository danhGiaRepository) {

        this.hoiDongRepository = hoiDongRepository;
        this.danhGiaRepository = danhGiaRepository;
        this.hoiDongDeTaiRepository = hoiDongDeTaiRepository;
        this.thanhVienHoiDongRepository = thanhVienHoiDongRepository;
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(readOnly = true)
    public List<NhomPhanCongLuaChon> layDanhSachCoThePhanCong(
            @NotBlank String maHoiDong) {

        kiemTraHoiDongTonTai(maHoiDong);

        return hoiDongDeTaiRepository
            .layDanhSachNhomCoThePhanCong(maHoiDong);
    }

    @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<NhomPhanCongLuaChon> layDanhSachDaPhanCong(
            @NotBlank String maHoiDong) {

        kiemTraHoiDongTonTai(maHoiDong);

        return hoiDongDeTaiRepository
            .layDanhSachNhomDaPhanCong(maHoiDong);
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public HoiDongDeTai phanCong(
            @NotBlank String maHoiDong,
            @NotNull @Valid PhanCongHoiDongForm form) {

        // Lấy tài khoản đăng nhập từ hệ thống bảo mật.
        String maNguoiPhanCong = CurrentUser.maNguoiDung();

        if (hoiDongRepository
                .demTruongKhoaHoatDong(maNguoiPhanCong) == 0) {
            throw new AccessDeniedException(
                "Tài khoản không có quyền phân công hội đồng."
            );
        }

        // Khóa hội đồng trong transaction để tránh thay đổi đồng thời.
        HoiDong hoiDong = hoiDongRepository
            .timVaKhoaTheoMa(maHoiDong)
            .orElseThrow(() -> new EntityNotFoundException(
                "Hội đồng không tồn tại."
            ));

        if (hoiDong.getTrangThai() != TrangThaiHoiDong.SAN_SANG) {
            throw new IllegalArgumentException(
                "Chỉ được phân công khi hội đồng ở trạng thái sẵn sàng."
            );
        }

        String maNhom = form.getMaNhom().trim();
        danhGiaRepository.khoaDauVao(maNhom);

        // Khóa đăng ký: hai hội đồng không thể cùng phân công một nhóm.
        hoiDongDeTaiRepository.khoaDangKyTheoMaNhom(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Nhóm chưa có đăng ký đề tài."
            ));

        if (hoiDongDeTaiRepository.existsById(maNhom)) {
            throw new IllegalArgumentException(
                "Nhóm đã được phân công cho một hội đồng."
            );
        }

        List<ThanhVienHoiDong> thanhVien = thanhVienHoiDongRepository
            .findById_MaHoiDongOrderById_MaGiangVienAsc(maHoiDong);

        boolean coChuTich = thanhVien.stream().anyMatch(
            tv -> tv.getChucDanh() == ChucDanhHoiDong.CHU_TICH
        );

        boolean coThuKy = thanhVien.stream().anyMatch(
            tv -> tv.getChucDanh() == ChucDanhHoiDong.THU_KY
        );

        if (thanhVien.size() < 3 || thanhVien.size() > 5
                || !coChuTich || !coThuKy) {
            throw new IllegalArgumentException(
                "Hội đồng phải có 3–5 thành viên, gồm chủ tịch và thư ký."
            );
        }

        // Tài khoản có thể bị khóa sau khi hội đồng đã sẵn sàng.
        for (ThanhVienHoiDong tv : thanhVien) {
            String maGiangVien = tv.getId().getMaGiangVien();

            if (thanhVienHoiDongRepository
                    .demGiangVienHopLe(maGiangVien) == 0) {
                throw new IllegalArgumentException(
                    "Giảng viên " + maGiangVien
                        + " không còn đủ điều kiện tham gia hội đồng."
                );
            }
        }

        // Kiểm tra lại ở backend, không tin mã nhóm gửi từ trình duyệt.
        // Truy vấn đã lọc cùng đợt KLTN, trạng thái hợp lệ
        // và không có giảng viên hướng dẫn trong hội đồng.
        boolean nhomHopLe = hoiDongDeTaiRepository
            .layDanhSachNhomCoThePhanCong(maHoiDong)
            .stream()
            .anyMatch(nhom -> maNhom.equals(nhom.getMaNhom()));

        if (!nhomHopLe) {
            throw new IllegalArgumentException(
                "Nhóm không đủ điều kiện phân công: hãy kiểm tra đợt, "
                    + "trạng thái đăng ký và xung đột giảng viên hướng dẫn."
            );
        }

        HoiDongDeTai phanCong = new HoiDongDeTai();
        phanCong.setMaNhom(maNhom);
        phanCong.setMaHoiDong(maHoiDong);
        phanCong.setMaNguoiPhanCong(maNguoiPhanCong);
        phanCong.setPhanCongLuc(
            LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        );

        return hoiDongDeTaiRepository.saveAndFlush(phanCong);
    }

    private void kiemTraHoiDongTonTai(String maHoiDong) {
        if (!hoiDongRepository.existsById(maHoiDong)) {
            throw new EntityNotFoundException(
                "Hội đồng không tồn tại."
            );
        }
    }
}
