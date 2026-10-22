package vn.edu.group12.qldt.service.admin;

import vn.edu.group12.qldt.entity.admin.NguoiDung;
import vn.edu.group12.qldt.entity.admin.NguoiDungVaiTro;
import vn.edu.group12.qldt.entity.admin.NguoiDungVaiTroId;
import vn.edu.group12.qldt.entity.admin.VaiTro;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;
import vn.edu.group12.qldt.repository.admin.NguoiDungVaiTroRepository;
import vn.edu.group12.qldt.repository.admin.VaiTroRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class NguoiDungVaiTroService {

    private final NguoiDungRepository nguoiDungRepository;
    private final VaiTroRepository vaiTroRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final JdbcTemplate jdbcTemplate;

    public NguoiDungVaiTroService(
            NguoiDungRepository nguoiDungRepository,
            VaiTroRepository vaiTroRepository,
            NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void ganVaiTro(
            String maNguoiDung,
            String maVaiTroNhap,
            NguoiDung nguoiCap
    ) {
        kiemTraNguoiCapLaAdmin(nguoiCap);

        NguoiDung nguoiDung = timNguoiDung(maNguoiDung);
        String maVaiTro = chuanHoaMa(maVaiTroNhap);
        VaiTro vaiTro = timVaiTro(maVaiTro);

        if (!"HOAT_DONG".equals(nguoiDung.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Không thể cấp vai trò cho tài khoản đang bị khóa"
            );
        }

        kiemTraTuongThich(nguoiDung, maVaiTro);

        if (nguoiDungVaiTroRepository
                .existsById_MaNguoiDungAndId_MaVaiTro(
                        maNguoiDung,
                        maVaiTro
                )) {
            return;
        }

        NguoiDungVaiTro lienKet = new NguoiDungVaiTro();
        lienKet.setId(new NguoiDungVaiTroId(maNguoiDung, maVaiTro));
        lienKet.setNguoiDung(nguoiDung);
        lienKet.setVaiTro(vaiTro);
        lienKet.setNguoiCap(nguoiCap);

        nguoiDungVaiTroRepository.save(lienKet);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void thuHoiVaiTro(
            String maNguoiDung,
            String maVaiTroNhap,
            NguoiDung nguoiCap
    ) {
        kiemTraNguoiCapLaAdmin(nguoiCap);

        NguoiDung nguoiDung = timNguoiDung(maNguoiDung);
        String maVaiTro = chuanHoaMa(maVaiTroNhap);
        NguoiDungVaiTroId id =
                new NguoiDungVaiTroId(maNguoiDung, maVaiTro);

        NguoiDungVaiTro lienKet = nguoiDungVaiTroRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Tài khoản chưa được cấp vai trò này"
                        ));

        if ("ADMIN".equals(maVaiTro)
                && "HOAT_DONG".equals(nguoiDung.getTrangThai())
                && nguoiDungVaiTroRepository
                        .countNguoiDungHoatDongCoVaiTro("ADMIN") <= 1) {
            throw new IllegalArgumentException(
                    "Không thể thu hồi ADMIN hoạt động cuối cùng"
            );
        }

        kiemTraKhongConNghiepVuDangHoatDong(nguoiDung, maVaiTro);

        nguoiDungVaiTroRepository.delete(lienKet);
    }

    private void kiemTraNguoiCapLaAdmin(NguoiDung nguoiCap) {
        if (nguoiCap == null
                || !"HOAT_DONG".equals(nguoiCap.getTrangThai())
                || !nguoiDungVaiTroRepository
                        .existsById_MaNguoiDungAndId_MaVaiTro(
                                nguoiCap.getMaNguoiDung(),
                                "ADMIN"
                        )) {
            throw new IllegalArgumentException(
                    "Người cấp/thu hồi vai trò phải là ADMIN đang hoạt động"
            );
        }
    }

    private NguoiDung timNguoiDung(String maNguoiDung) {
        return nguoiDungRepository.findById(maNguoiDung)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy tài khoản"
                        ));
    }

    private VaiTro timVaiTro(String maVaiTro) {
        return vaiTroRepository.findById(maVaiTro)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy vai trò"
                        ));
    }

    private void kiemTraTuongThich(
            NguoiDung nguoiDung,
            String maVaiTro
    ) {
        if ("SV".equals(nguoiDung.getLoaiNguoiDung())
                && "GIANG_VIEN".equals(maVaiTro)) {
            throw new IllegalArgumentException(
                    "Tài khoản SV không được có vai trò GIANG_VIEN"
            );
        }

        if ("GV".equals(nguoiDung.getLoaiNguoiDung())
                && "SINH_VIEN".equals(maVaiTro)) {
            throw new IllegalArgumentException(
                    "Tài khoản GV không được có vai trò SINH_VIEN"
            );
        }
    }

    private void kiemTraKhongConNghiepVuDangHoatDong(
            NguoiDung nguoiDung,
            String maVaiTro
    ) {
        if ("SINH_VIEN".equals(maVaiTro)) {
            Long soNhomDangThamGia = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM thanh_vien_nhom tvn
                    JOIN nhom_sinh_vien n
                      ON n.ma_nhom = tvn.ma_nhom
                    WHERE tvn.ma_sinh_vien = ?
                      AND tvn.trang_thai = 'THAM_GIA'
                      AND n.trang_thai IN (
                          'DANG_LAP', 'DA_CHOT', 'DANG_THUC_HIEN'
                      )
                    """, Long.class, nguoiDung.getMaNguoiDung());

            if (soNhomDangThamGia != null && soNhomDangThamGia > 0) {
                throw new IllegalArgumentException(
                        "Không thể thu hồi SINH_VIEN khi tài khoản còn "
                                + "tham gia nhóm đang hoạt động"
                );
            }
        }

        if ("GIANG_VIEN".equals(maVaiTro)) {
            Long soPhanCongDangHoatDong = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM de_tai_giang_vien dgv
                    JOIN de_tai dt
                      ON dt.ma_de_tai = dgv.ma_de_tai
                    WHERE dgv.ma_giang_vien = ?
                      AND dt.trang_thai IN (
                          'NHAP', 'CHO_DUYET', 'DA_DUYET', 'CONG_BO'
                      )
                    """, Long.class, nguoiDung.getMaNguoiDung());

            if (soPhanCongDangHoatDong != null
                    && soPhanCongDangHoatDong > 0) {
                throw new IllegalArgumentException(
                        "Không thể thu hồi GIANG_VIEN khi còn phân công "
                                + "đề tài đang hoạt động"
                );
            }
        }
    }

    private String chuanHoaMa(String ma) {
        if (ma == null || ma.isBlank()) {
            throw new IllegalArgumentException("Mã không được để trống");
        }
        return ma.trim().toUpperCase(Locale.ROOT);
    }
}