package vn.edu.group12.qldt.service.hoidong;

import vn.edu.group12.qldt.service.auth.CurrentUser;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import vn.edu.group12.qldt.dto.hoidong.HoiDongForm;
import vn.edu.group12.qldt.entity.hoidong.HoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiHoiDong;
import vn.edu.group12.qldt.repository.hoidong.HoiDongRepository;

import vn.edu.group12.qldt.dto.hoidong.DotDangKyLuaChon;

import jakarta.persistence.EntityNotFoundException;
import vn.edu.group12.qldt.repository.hoidong.ThanhVienHoiDongRepository;
import jakarta.validation.constraints.NotBlank;

import org.springframework.transaction.annotation.Isolation;

import vn.edu.group12.qldt.entity.hoidong.ThanhVienHoiDong;
import vn.edu.group12.qldt.enums.hoidong.ChucDanhHoiDong;

@Service
@Validated
public class HoiDongService {

    private final HoiDongRepository hoiDongRepository;
    private final ThanhVienHoiDongRepository thanhVienHoiDongRepository;

    public HoiDongService(
            HoiDongRepository hoiDongRepository,
            ThanhVienHoiDongRepository thanhVienHoiDongRepository) {
        this.hoiDongRepository = hoiDongRepository;
        this.thanhVienHoiDongRepository = thanhVienHoiDongRepository;
    }


    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(readOnly = true)
    public List<HoiDong> layDanhSach() {
        return hoiDongRepository.findAll(
            Sort.by(Sort.Direction.ASC, "maHoiDong")
        );
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(readOnly = true)
    public HoiDong layChiTiet(String maHoiDong) {
        return hoiDongRepository.findById(maHoiDong)
            .orElseThrow(() -> new EntityNotFoundException(
                "Hội đồng không tồn tại."
            ));
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(readOnly = true)
    public List<DotDangKyLuaChon> layDanhSachDot() {
        return hoiDongRepository.layDanhSachDot();
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional
    public HoiDong taoHoiDong(@NotNull @Valid HoiDongForm form) {
        String maHoiDong = form.getMaHoiDong().trim();
        String tenHoiDong = form.getTenHoiDong().trim();
        String maDot = form.getMaDot().trim();

        String maNguoiTao = CurrentUser.maNguoiDung();

        if (hoiDongRepository.demTruongKhoaHoatDong(maNguoiTao) == 0) {
            throw new AccessDeniedException(
                "Tài khoản không có quyền tạo hội đồng."
            );
        }

        if (hoiDongRepository.existsById(maHoiDong)) {
            throw new IllegalArgumentException(
                "Mã hội đồng đã tồn tại."
            );
        }

        if (hoiDongRepository.demDotTheoMa(maDot) == 0) {
            throw new IllegalArgumentException(
                "Đợt đăng ký không tồn tại."
            );
        }

        String diaDiem = form.getDiaDiem();

        HoiDong hoiDong = new HoiDong();
        hoiDong.setMaHoiDong(maHoiDong);
        hoiDong.setTenHoiDong(tenHoiDong);
        hoiDong.setMaDot(maDot);
        hoiDong.setDiaDiem(
            diaDiem == null || diaDiem.isBlank()
                ? null
                : diaDiem.trim()
        );
        hoiDong.setTrangThai(TrangThaiHoiDong.NHAP);
        hoiDong.setMaNguoiTao(maNguoiTao);

        return hoiDongRepository.saveAndFlush(hoiDong);
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public HoiDong chuyenSangSanSang(@NotBlank String maHoiDong) {

        String maNguoiThaoTac = CurrentUser.maNguoiDung();

        if (hoiDongRepository.demTruongKhoaHoatDong(maNguoiThaoTac) == 0) {
            throw new AccessDeniedException(
                "Tài khoản không có quyền xác nhận hội đồng sẵn sàng."
            );
        }

        HoiDong hoiDong = hoiDongRepository.timVaKhoaTheoMa(maHoiDong)
            .orElseThrow(() -> new EntityNotFoundException(
                "Hội đồng không tồn tại."
            ));

        if (hoiDong.getTrangThai() != TrangThaiHoiDong.NHAP) {
            throw new IllegalArgumentException(
                "Chỉ được xác nhận sẵn sàng khi hội đồng đang ở trạng thái nháp."
            );
        }

        List<ThanhVienHoiDong> danhSachThanhVien =
            thanhVienHoiDongRepository
                .findById_MaHoiDongOrderById_MaGiangVienAsc(maHoiDong);

        if (danhSachThanhVien.size() < 3 || danhSachThanhVien.size() > 5) {
            throw new IllegalArgumentException(
                "Hội đồng phải có từ 3 đến 5 thành viên."
            );
        }

        boolean coChuTich = thanhVienHoiDongRepository
            .existsById_MaHoiDongAndChucDanh(
                maHoiDong,
                ChucDanhHoiDong.CHU_TICH
            );

        boolean coThuKy = thanhVienHoiDongRepository
            .existsById_MaHoiDongAndChucDanh(
                maHoiDong,
                ChucDanhHoiDong.THU_KY
            );

        if (!coChuTich || !coThuKy) {
            throw new IllegalArgumentException(
                "Hội đồng phải có một chủ tịch và một thư ký."
            );
        }

        for (ThanhVienHoiDong thanhVien : danhSachThanhVien) {
            String maGiangVien = thanhVien.getId().getMaGiangVien();

            if (thanhVienHoiDongRepository
                    .demGiangVienHopLe(maGiangVien) == 0) {
                throw new IllegalArgumentException(
                    "Giảng viên " + maGiangVien
                        + " không còn đủ điều kiện tham gia hội đồng."
                );
            }

            if (thanhVienHoiDongRepository
                    .demXungDotHuongDan(maHoiDong, maGiangVien) > 0) {
                throw new IllegalArgumentException(
                    "Giảng viên " + maGiangVien
                        + " đang hướng dẫn đề tài được giao cho hội đồng."
                );
            }
        }

        hoiDong.setTrangThai(TrangThaiHoiDong.SAN_SANG);

        return hoiDongRepository.saveAndFlush(hoiDong);
    }

}