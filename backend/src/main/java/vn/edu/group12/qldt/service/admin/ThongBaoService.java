package vn.edu.group12.qldt.service.admin;

import vn.edu.group12.qldt.dto.admin.ThongBaoDanhSachDto;
import vn.edu.group12.qldt.dto.admin.ThongBaoForm;
import vn.edu.group12.qldt.entity.admin.NguoiDung;
import vn.edu.group12.qldt.entity.admin.ThongBao;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;
import vn.edu.group12.qldt.repository.admin.ThongBaoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class ThongBaoService {

    private static final Set<String> PHAM_VI_HOP_LE =
            Set.of("TAT_CA", "SINH_VIEN", "GIANG_VIEN");

    private final ThongBaoRepository thongBaoRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public ThongBaoService(
            ThongBaoRepository thongBaoRepository,
            NguoiDungRepository nguoiDungRepository
    ) {
        this.thongBaoRepository = thongBaoRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public Page<ThongBaoDanhSachDto> layDanhSachQuanTri(
            Pageable pageable
    ) {
        return thongBaoRepository.findAllByOrderByCongBoLucDesc(pageable)
                .map(this::sangDto);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String taoBanNhap(
            ThongBaoForm form,
            NguoiDung nguoiDang
    ) {
        if (nguoiDang == null
                || !"HOAT_DONG".equals(nguoiDang.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Người đăng không hợp lệ hoặc đã bị khóa"
            );
        }

        String maThongBao =
                form.getMaThongBao().trim().toUpperCase(Locale.ROOT);

        if (thongBaoRepository.existsById(maThongBao)) {
            throw new IllegalArgumentException(
                    "Mã thông báo đã tồn tại"
            );
        }

        String phamVi = chuanHoaPhamVi(form.getPhamVi());

        ThongBao thongBao = new ThongBao();
        thongBao.setMaThongBao(maThongBao);
        thongBao.setTieuDe(form.getTieuDe().trim());
        thongBao.setNoiDung(form.getNoiDung().trim());
        thongBao.setNguoiDang(nguoiDang);
        thongBao.setMaDot(chuanHoaCoTheTrong(form.getMaDot()));
        thongBao.setPhamVi(phamVi);
        thongBao.setTrangThai("NHAP");
        thongBao.setCongBoLuc(null);

        thongBaoRepository.save(thongBao);
        return maThongBao;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public void capNhatBanNhap(
            String maThongBao,
            ThongBaoForm form
    ) {
        ThongBao thongBao = timThongBao(maThongBao);

        if (!"NHAP".equals(thongBao.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Chỉ được sửa thông báo ở trạng thái NHAP"
            );
        }

        thongBao.setTieuDe(form.getTieuDe().trim());
        thongBao.setNoiDung(form.getNoiDung().trim());
        thongBao.setMaDot(chuanHoaCoTheTrong(form.getMaDot()));
        thongBao.setPhamVi(chuanHoaPhamVi(form.getPhamVi()));

        thongBaoRepository.save(thongBao);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public void congBo(String maThongBao) {
        ThongBao thongBao = timThongBao(maThongBao);
        String trangThai = thongBao.getTrangThai();

        if (!"NHAP".equals(trangThai) && !"AN".equals(trangThai)) {
            throw new IllegalArgumentException(
                    "Thông báo hiện không thể công bố"
            );
        }

        thongBao.setTrangThai("CONG_BO");
        thongBao.setCongBoLuc(
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        );

        thongBaoRepository.save(thongBao);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public void anThongBao(String maThongBao) {
        ThongBao thongBao = timThongBao(maThongBao);
        thongBao.setTrangThai("AN");
        thongBaoRepository.save(thongBao);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<ThongBaoDanhSachDto> layThongBaoChoNguoiDung(
            Collection<String> maVaiTros,
            Pageable pageable
    ) {
        Set<String> phamViDuocXem = new LinkedHashSet<>();
        phamViDuocXem.add("TAT_CA");

        if (maVaiTros != null) {
            if (maVaiTros.contains("SINH_VIEN")) {
                phamViDuocXem.add("SINH_VIEN");
            }
            if (maVaiTros.contains("GIANG_VIEN")) {
                phamViDuocXem.add("GIANG_VIEN");
            }
        }

        LocalDateTime bayGio =
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        return thongBaoRepository
                .findAllByTrangThaiAndPhamViInAndCongBoLucLessThanEqualOrderByCongBoLucDesc(
                        "CONG_BO",
                        phamViDuocXem,
                        bayGio,
                        pageable
                )
                .map(this::sangDto);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public ThongBaoForm layFormCapNhat(String maThongBao) {
        ThongBao thongBao = timThongBao(maThongBao);

        if (!"NHAP".equals(thongBao.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Chỉ được sửa thông báo ở trạng thái NHAP"
            );
        }

        ThongBaoForm form = new ThongBaoForm();
        form.setMaThongBao(thongBao.getMaThongBao());
        form.setTieuDe(thongBao.getTieuDe());
        form.setNoiDung(thongBao.getNoiDung());
        form.setMaDot(thongBao.getMaDot());
        form.setPhamVi(thongBao.getPhamVi());

        return form;
    }

    private ThongBao timThongBao(String maThongBao) {
        return thongBaoRepository.findById(maThongBao)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy thông báo"
                        ));
    }

    private ThongBaoDanhSachDto sangDto(ThongBao thongBao) {
        return new ThongBaoDanhSachDto(
                thongBao.getMaThongBao(),
                thongBao.getTieuDe(),
                thongBao.getNoiDung(),
                thongBao.getNguoiDang().getHoTen(),
                thongBao.getMaDot(),
                thongBao.getPhamVi(),
                thongBao.getTrangThai(),
                thongBao.getCongBoLuc()
        );
    }

    private String chuanHoaPhamVi(String giaTri) {
        if (giaTri == null || giaTri.isBlank()) {
            throw new IllegalArgumentException(
                    "Phạm vi thông báo không được để trống"
            );
        }

        String phamVi = giaTri.trim().toUpperCase(Locale.ROOT);

        if (!PHAM_VI_HOP_LE.contains(phamVi)) {
            throw new IllegalArgumentException(
                    "Phạm vi thông báo không hợp lệ"
            );
        }

        return phamVi;
    }

    private String chuanHoaCoTheTrong(String giaTri) {
        if (giaTri == null || giaTri.isBlank()) {
            return null;
        }
        return giaTri.trim().toUpperCase(Locale.ROOT);
    }
}