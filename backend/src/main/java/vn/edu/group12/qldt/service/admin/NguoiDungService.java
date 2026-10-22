package vn.edu.group12.qldt.service.admin;

import vn.edu.group12.qldt.dto.admin.NguoiDungCapNhatForm;
import vn.edu.group12.qldt.dto.admin.NguoiDungDanhSachDto;
import vn.edu.group12.qldt.dto.admin.NguoiDungForm;
import vn.edu.group12.qldt.entity.admin.BoMon;
import vn.edu.group12.qldt.entity.admin.NguoiDung;
import vn.edu.group12.qldt.entity.admin.NguoiDungVaiTro;
import vn.edu.group12.qldt.entity.admin.NguoiDungVaiTroId;
import vn.edu.group12.qldt.entity.admin.VaiTro;
import vn.edu.group12.qldt.repository.admin.BoMonRepository;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;
import vn.edu.group12.qldt.repository.admin.NguoiDungVaiTroRepository;
import vn.edu.group12.qldt.repository.admin.VaiTroRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class NguoiDungService {

    private static final Set<String> LOAI_NGUOI_DUNG_HOP_LE =
            Set.of("SV", "GV", "CAN_BO");

    private static final Set<String> TRANG_THAI_HOP_LE =
            Set.of("HOAT_DONG", "KHOA");

    private final NguoiDungRepository nguoiDungRepository;
    private final VaiTroRepository vaiTroRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final BoMonRepository boMonRepository;
    private final PasswordEncoder passwordEncoder;

    public NguoiDungService(
            NguoiDungRepository nguoiDungRepository,
            VaiTroRepository vaiTroRepository,
            NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
            BoMonRepository boMonRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.boMonRepository = boMonRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")   
    public String taoTaiKhoan(NguoiDungForm form, NguoiDung nguoiCap) {
        if (nguoiCap == null) {
            throw new IllegalArgumentException(
                    "Không xác định được cán bộ cấp vai trò"
            );
        }

        String maNguoiDung = chuanHoaMa(form.getMaNguoiDung());
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        String loaiNguoiDung =
                form.getLoaiNguoiDung().trim().toUpperCase(Locale.ROOT);

        if (nguoiDungRepository.existsById(maNguoiDung)) {
            throw new IllegalArgumentException("Mã người dùng đã tồn tại");
        }

        if (nguoiDungRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        if (!LOAI_NGUOI_DUNG_HOP_LE.contains(loaiNguoiDung)) {
            throw new IllegalArgumentException("Loại người dùng không hợp lệ");
        }

        BoMon boMon = timBoMonHoatDong(form.getMaBoMon());

        if ("GV".equals(loaiNguoiDung) && boMon == null) {
            throw new IllegalArgumentException(
                    "Giảng viên phải được gán vào một bộ môn"
            );
        }

        kiemTraDoDaiMatKhau(form.getMatKhau());

        List<VaiTro> vaiTros = timVaiTros(form.getMaVaiTros());
        kiemTraLoaiVaVaiTro(loaiNguoiDung, vaiTros);

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setMaNguoiDung(maNguoiDung);
        nguoiDung.setHoTen(form.getHoTen().trim());
        nguoiDung.setEmail(email);
        nguoiDung.setMatKhauHash(passwordEncoder.encode(form.getMatKhau()));
        nguoiDung.setLoaiNguoiDung(loaiNguoiDung);
        nguoiDung.setBoMon(boMon);
        nguoiDung.setSoDienThoai(
                chuanHoaCoTheTrong(form.getSoDienThoai())
        );
        nguoiDung.setLopHanhChinh(
                chuanHoaCoTheTrong(form.getLopHanhChinh())
        );
        nguoiDung.setTrangThai("HOAT_DONG");
        nguoiDung.setBuocDoiMatKhau(true);

        nguoiDungRepository.save(nguoiDung);

        for (VaiTro vaiTro : vaiTros) {
            NguoiDungVaiTro ganVaiTro = new NguoiDungVaiTro();
            ganVaiTro.setId(new NguoiDungVaiTroId(
                    nguoiDung.getMaNguoiDung(),
                    vaiTro.getMaVaiTro()
            ));
            ganVaiTro.setNguoiDung(nguoiDung);
            ganVaiTro.setVaiTro(vaiTro);
            ganVaiTro.setNguoiCap(nguoiCap);

            nguoiDungVaiTroRepository.save(ganVaiTro);
        }

        return maNguoiDung;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<NguoiDungDanhSachDto> layDanhSach(Pageable pageable) {
        return nguoiDungRepository.findAll(pageable)
                .map(nguoiDung -> new NguoiDungDanhSachDto(
                        nguoiDung.getMaNguoiDung(),
                        nguoiDung.getHoTen(),
                        nguoiDung.getEmail(),
                        nguoiDung.getLoaiNguoiDung(),
                        nguoiDung.getBoMon() == null
                                ? null
                                : nguoiDung.getBoMon().getTenBoMon(),
                        nguoiDung.getTrangThai()
                ));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void capNhatThongTin(
            String maNguoiDung,
            NguoiDungCapNhatForm form
    ) {
        NguoiDung nguoiDung = timNguoiDung(maNguoiDung);
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);

        nguoiDungRepository.findByEmailIgnoreCase(email)
                .ifPresent(nguoiDungCungEmail -> {
                    if (!nguoiDungCungEmail.getMaNguoiDung()
                            .equals(nguoiDung.getMaNguoiDung())) {
                        throw new IllegalArgumentException(
                                "Email đã được sử dụng"
                        );
                    }
                });

        BoMon boMon = timBoMonHoatDong(form.getMaBoMon());

        if ("GV".equals(nguoiDung.getLoaiNguoiDung()) && boMon == null) {
            throw new IllegalArgumentException(
                    "Giảng viên phải được gán vào một bộ môn"
            );
        }

        nguoiDung.setHoTen(form.getHoTen().trim());
        nguoiDung.setEmail(email);
        nguoiDung.setBoMon(boMon);
        nguoiDung.setSoDienThoai(
                chuanHoaCoTheTrong(form.getSoDienThoai())
        );
        nguoiDung.setLopHanhChinh(
                chuanHoaCoTheTrong(form.getLopHanhChinh())
        );

        nguoiDungRepository.save(nguoiDung);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void capNhatTrangThai(
            String maNguoiDung,
            String trangThaiMoi
    ) {
        String trangThai = trangThaiMoi.trim().toUpperCase(Locale.ROOT);

        if (!TRANG_THAI_HOP_LE.contains(trangThai)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }

        NguoiDung nguoiDung = timNguoiDung(maNguoiDung);

        boolean khoaAdminHoatDong =
                "KHOA".equals(trangThai)
                && "HOAT_DONG".equals(nguoiDung.getTrangThai())
                && nguoiDungVaiTroRepository
                        .existsById_MaNguoiDungAndId_MaVaiTro(
                                maNguoiDung,
                                "ADMIN"
                        );

        if (khoaAdminHoatDong
                && nguoiDungVaiTroRepository
                        .countNguoiDungHoatDongCoVaiTro("ADMIN") <= 1) {
            throw new IllegalArgumentException(
                    "Không thể khóa ADMIN hoạt động cuối cùng"
            );
        }

        nguoiDung.setTrangThai(trangThai);
        nguoiDungRepository.save(nguoiDung);
    }


    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public NguoiDungCapNhatForm layFormCapNhat(String maNguoiDung) {
        NguoiDung nguoiDung = timNguoiDung(maNguoiDung);

        NguoiDungCapNhatForm form = new NguoiDungCapNhatForm();
        form.setHoTen(nguoiDung.getHoTen());
        form.setEmail(nguoiDung.getEmail());
        form.setMaBoMon(
                nguoiDung.getBoMon() == null
                        ? null
                        : nguoiDung.getBoMon().getMaBoMon()
        );
        form.setSoDienThoai(nguoiDung.getSoDienThoai());
        form.setLopHanhChinh(nguoiDung.getLopHanhChinh());

        return form;
    }

    private NguoiDung timNguoiDung(String maNguoiDung) {
        return nguoiDungRepository.findById(maNguoiDung)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy tài khoản"
                        ));
    }

    private BoMon timBoMonHoatDong(String maBoMonNhap) {
        String maBoMon = chuanHoaCoTheTrong(maBoMonNhap);

        if (maBoMon == null) {
            return null;
        }

        BoMon boMon = boMonRepository.findById(
                        maBoMon.toUpperCase(Locale.ROOT)
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy bộ môn"));

        if (!Boolean.TRUE.equals(boMon.getHoatDong())) {
            throw new IllegalArgumentException(
                    "Bộ môn đã ngừng hoạt động"
            );
        }

        return boMon;
    }

    private List<VaiTro> timVaiTros(List<String> maVaiTrosNhap) {
        if (maVaiTrosNhap == null || maVaiTrosNhap.isEmpty()) {
            throw new IllegalArgumentException(
                    "Hãy chọn ít nhất một vai trò"
            );
        }

        Set<String> maVaiTros = new LinkedHashSet<>();

        for (String maVaiTro : maVaiTrosNhap) {
            if (maVaiTro == null || maVaiTro.isBlank()) {
                continue;
            }
            maVaiTros.add(maVaiTro.trim().toUpperCase(Locale.ROOT));
        }

        if (maVaiTros.isEmpty()) {
            throw new IllegalArgumentException(
                    "Hãy chọn ít nhất một vai trò"
            );
        }

        List<VaiTro> vaiTros = new ArrayList<>();

        for (String maVaiTro : maVaiTros) {
            VaiTro vaiTro = vaiTroRepository.findById(maVaiTro)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Vai trò không tồn tại: " + maVaiTro
                            ));
            vaiTros.add(vaiTro);
        }

        return vaiTros;
    }

    private void kiemTraLoaiVaVaiTro(
            String loaiNguoiDung,
            List<VaiTro> vaiTros
    ) {
        Set<String> maVaiTros = new LinkedHashSet<>();

        for (VaiTro vaiTro : vaiTros) {
            maVaiTros.add(vaiTro.getMaVaiTro());
        }

        if ("SV".equals(loaiNguoiDung)
                && maVaiTros.contains("GIANG_VIEN")) {
            throw new IllegalArgumentException(
                    "Tài khoản sinh viên không được có vai trò GIANG_VIEN"
            );
        }

        if ("GV".equals(loaiNguoiDung)
                && maVaiTros.contains("SINH_VIEN")) {
            throw new IllegalArgumentException(
                    "Tài khoản giảng viên không được có vai trò SINH_VIEN"
            );
        }
    }

    private void kiemTraDoDaiMatKhau(String matKhau) {
        if (matKhau.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Mật khẩu quá dài đối với mã hóa BCrypt"
            );
        }
    }

    private String chuanHoaMa(String giaTri) {
        return giaTri.trim().toUpperCase(Locale.ROOT);
    }

    private String chuanHoaCoTheTrong(String giaTri) {
        if (giaTri == null || giaTri.isBlank()) {
            return null;
        }
        return giaTri.trim();
    }
}