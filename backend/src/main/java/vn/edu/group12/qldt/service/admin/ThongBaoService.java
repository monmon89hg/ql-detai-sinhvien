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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ThongBaoService {

    private static final Set<String> PHAM_VI_HOP_LE =
            Set.of("TAT_CA", "CA_NHAN");

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
        thongBao.setMaNguoiNhanIds(
                chuanHoaMaNguoiNhan(phamVi, form.getMaNguoiNhan())
        );
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
        String phamVi = chuanHoaPhamVi(form.getPhamVi());
        thongBao.setPhamVi(phamVi);
        thongBao.setMaNguoiNhanIds(
                chuanHoaMaNguoiNhan(phamVi, form.getMaNguoiNhan())
        );

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
            String maNguoiDung,
            Pageable pageable
    ) {
        if (maNguoiDung == null || maNguoiDung.isBlank()) {
            throw new IllegalArgumentException("Không xác định được tài khoản nhận thông báo");
        }

        LocalDateTime bayGio =
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        return thongBaoRepository.findInboxForUser(maNguoiDung, bayGio, pageable)
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
        form.setMaNguoiNhan(new ArrayList<>(thongBao.getMaNguoiNhanIds()));

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

    private Set<String> chuanHoaMaNguoiNhan(
            String phamVi,
            Collection<String> maNguoiNhanNhap
    ) {
        if ("TAT_CA".equals(phamVi)) {
            return Set.of();
        }

        Set<String> maNguoiNhans = maNguoiNhanNhap == null
                ? Set.of()
                : maNguoiNhanNhap.stream()
                        .filter(ma -> ma != null && !ma.isBlank())
                        .map(String::trim)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        if (maNguoiNhans.isEmpty()) {
            throw new IllegalArgumentException(
                    "Hãy chọn ít nhất một người nhận cho thông báo cá nhân"
            );
        }

        Set<String> maNguoiNhanHopLe = nguoiDungRepository
                .findActiveNotificationRecipientsByIds(maNguoiNhans)
                .stream()
                .map(NguoiDung::getMaNguoiDung)
                .collect(Collectors.toSet());

        if (!maNguoiNhanHopLe.equals(maNguoiNhans)) {
            throw new IllegalArgumentException(
                    "Có người nhận không tồn tại, đã khóa hoặc không thuộc nhóm được nhận thông báo"
            );
        }

        return maNguoiNhanHopLe;
    }

    private String chuanHoaCoTheTrong(String giaTri) {
        if (giaTri == null || giaTri.isBlank()) {
            return null;
        }
        return giaTri.trim().toUpperCase(Locale.ROOT);
    }
}
