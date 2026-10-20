package vn.edu.group12.qldt.service;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.dto.ThanhVienHoiDongHienThi;
import vn.edu.group12.qldt.repository.HoiDongRepository;
import vn.edu.group12.qldt.repository.ThanhVienHoiDongRepository;
import vn.edu.group12.qldt.dto.GiangVienLuaChon;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.validation.annotation.Validated;

import vn.edu.group12.qldt.dto.ThanhVienHoiDongForm;
import vn.edu.group12.qldt.entity.HoiDong;
import vn.edu.group12.qldt.entity.ThanhVienHoiDong;
import vn.edu.group12.qldt.entity.ThanhVienHoiDongId;
import vn.edu.group12.qldt.enums.ChucDanhHoiDong;
import vn.edu.group12.qldt.enums.TrangThaiHoiDong;

@Service
@Validated
public class ThanhVienHoiDongService {

    private final HoiDongRepository hoiDongRepository;
    private final ThanhVienHoiDongRepository thanhVienHoiDongRepository;

    public ThanhVienHoiDongService(
            HoiDongRepository hoiDongRepository,
            ThanhVienHoiDongRepository thanhVienHoiDongRepository) {
        this.hoiDongRepository = hoiDongRepository;
        this.thanhVienHoiDongRepository = thanhVienHoiDongRepository;
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(readOnly = true)
    public List<ThanhVienHoiDongHienThi> layDanhSach(String maHoiDong) {
        if (!hoiDongRepository.existsById(maHoiDong)) {
            throw new EntityNotFoundException(
                "Hội đồng không tồn tại."
            );
        }

        return thanhVienHoiDongRepository.layDanhSachHienThi(maHoiDong);
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(readOnly = true)
    public List<GiangVienLuaChon> layDanhSachGiangVien() {
        return thanhVienHoiDongRepository.layDanhSachGiangVien();
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ThanhVienHoiDong themThanhVien(
            @NotBlank String maHoiDong,
            @NotNull @Valid ThanhVienHoiDongForm form) {

        String maNguoiThaoTac = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

        if (hoiDongRepository.demTruongKhoaHoatDong(maNguoiThaoTac) == 0) {
            throw new AccessDeniedException(
                "Tài khoản không có quyền thêm thành viên."
            );
        }

        HoiDong hoiDong = hoiDongRepository.timVaKhoaTheoMa(maHoiDong)
            .orElseThrow(() -> new EntityNotFoundException(
                "Hội đồng không tồn tại."
            ));

        if (hoiDong.getTrangThai() != TrangThaiHoiDong.NHAP) {
            throw new IllegalArgumentException(
                "Chỉ được thêm thành viên khi hội đồng ở trạng thái nháp."
            );
        }

        String maGiangVien = form.getMaGiangVien().trim();
        ChucDanhHoiDong chucDanh = form.getChucDanh();

        ThanhVienHoiDongId id =
            new ThanhVienHoiDongId(maHoiDong, maGiangVien);

        if (thanhVienHoiDongRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "Giảng viên đã là thành viên của hội đồng."
            );
        }

        if (thanhVienHoiDongRepository.countById_MaHoiDong(maHoiDong) >= 5) {
            throw new IllegalArgumentException(
                "Hội đồng chỉ được có tối đa 5 thành viên."
            );
        }

        if (thanhVienHoiDongRepository.demGiangVienHopLe(maGiangVien) == 0) {
            throw new IllegalArgumentException(
                "Giảng viên không tồn tại hoặc không đủ điều kiện tham gia."
            );
        }

        if ((chucDanh == ChucDanhHoiDong.CHU_TICH
                || chucDanh == ChucDanhHoiDong.THU_KY)
                && thanhVienHoiDongRepository
                    .existsById_MaHoiDongAndChucDanh(maHoiDong, chucDanh)) {

            throw new IllegalArgumentException(
                chucDanh == ChucDanhHoiDong.CHU_TICH
                    ? "Hội đồng đã có chủ tịch."
                    : "Hội đồng đã có thư ký."
            );
        }

        if (thanhVienHoiDongRepository
                .demXungDotHuongDan(maHoiDong, maGiangVien) > 0) {
            throw new IllegalArgumentException(
                "Giảng viên đang hướng dẫn đề tài được giao cho hội đồng."
            );
        }

        ThanhVienHoiDong thanhVien = new ThanhVienHoiDong();
        thanhVien.setId(id);
        thanhVien.setChucDanh(chucDanh);

        return thanhVienHoiDongRepository.saveAndFlush(thanhVien);
    }

    @PreAuthorize("hasRole('TRUONG_KHOA')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void xoaThanhVien(
            @NotBlank String maHoiDong,
            @NotBlank String maGiangVien) {

        String maNguoiThaoTac = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

        if (hoiDongRepository.demTruongKhoaHoatDong(maNguoiThaoTac) == 0) {
            throw new AccessDeniedException(
                "Tài khoản không có quyền xóa thành viên."
            );
        }

        HoiDong hoiDong = hoiDongRepository.timVaKhoaTheoMa(maHoiDong)
            .orElseThrow(() -> new EntityNotFoundException(
                "Hội đồng không tồn tại."
            ));

        if (hoiDong.getTrangThai() != TrangThaiHoiDong.NHAP) {
            throw new IllegalArgumentException(
                "Chỉ được xóa thành viên khi hội đồng ở trạng thái nháp."
            );
        }

        ThanhVienHoiDongId id =
            new ThanhVienHoiDongId(maHoiDong, maGiangVien);

        ThanhVienHoiDong thanhVien = thanhVienHoiDongRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Giảng viên không phải thành viên của hội đồng."
            ));

        thanhVienHoiDongRepository.delete(thanhVien);
        thanhVienHoiDongRepository.flush();
    }

}