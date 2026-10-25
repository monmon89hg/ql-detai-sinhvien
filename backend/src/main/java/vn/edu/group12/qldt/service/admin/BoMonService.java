package vn.edu.group12.qldt.service.admin;

import vn.edu.group12.qldt.dto.admin.BoMonCapNhatForm;
import vn.edu.group12.qldt.dto.admin.BoMonDanhSachDto;
import vn.edu.group12.qldt.dto.admin.BoMonForm;
import vn.edu.group12.qldt.entity.admin.BoMon;
import vn.edu.group12.qldt.repository.admin.BoMonRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class BoMonService {

    private final BoMonRepository boMonRepository;

    public BoMonService(BoMonRepository boMonRepository) {
        this.boMonRepository = boMonRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<BoMonDanhSachDto> layDanhSach(Pageable pageable) {
        return boMonRepository.findAll(pageable)
                .map(this::sangDto);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public String taoBoMon(BoMonForm form) {
        String maBoMon = form.getMaBoMon()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (boMonRepository.existsById(maBoMon)) {
            throw new IllegalArgumentException("Mã bộ môn đã tồn tại");
        }

        BoMon boMon = new BoMon();
        boMon.setMaBoMon(maBoMon);
        boMon.setTenBoMon(form.getTenBoMon().trim());
        boMon.setMoTa(chuanHoaMoTa(form.getMoTa()));
        boMon.setHoatDong(true);

        boMonRepository.save(boMon);
        return maBoMon;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void capNhatBoMon(
            String maBoMon,
            BoMonCapNhatForm form
    ) {
        BoMon boMon = timBoMon(maBoMon);
        boMon.setTenBoMon(form.getTenBoMon().trim());
        boMon.setMoTa(chuanHoaMoTa(form.getMoTa()));

        boMonRepository.save(boMon);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void capNhatTrangThai(String maBoMon, boolean hoatDong) {
        BoMon boMon = timBoMon(maBoMon);
        boMon.setHoatDong(hoatDong);
        boMonRepository.save(boMon);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public BoMonCapNhatForm layFormCapNhat(String maBoMon) {
        BoMon boMon = timBoMon(maBoMon);

        BoMonCapNhatForm form = new BoMonCapNhatForm();
        form.setTenBoMon(boMon.getTenBoMon());
        form.setMoTa(boMon.getMoTa());

        return form;
    }

    private BoMon timBoMon(String maBoMon) {
        String maDaChuanHoa = maBoMon.trim().toUpperCase(Locale.ROOT);

        return boMonRepository.findById(maDaChuanHoa)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy bộ môn"));
    }

    private BoMonDanhSachDto sangDto(BoMon boMon) {
        return new BoMonDanhSachDto(
                boMon.getMaBoMon(),
                boMon.getTenBoMon(),
                boMon.getMoTa(),
                boMon.getHoatDong()
        );
    }

    private String chuanHoaMoTa(String moTa) {
        if (moTa == null || moTa.isBlank()) {
            return null;
        }
        return moTa.trim();
    }
}