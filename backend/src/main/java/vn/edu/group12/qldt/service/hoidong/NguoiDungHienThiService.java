package vn.edu.group12.qldt.service.hoidong;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.entity.admin.NguoiDung;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;
import vn.edu.group12.qldt.service.auth.CurrentUser;

@Service
public class NguoiDungHienThiService {

    private final NguoiDungRepository nguoiDungRepository;

    public NguoiDungHienThiService(
            NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Transactional(readOnly = true)
    public String layHoTenHienTai() {
        String maNguoiDung = CurrentUser.maNguoiDung();

        return nguoiDungRepository.findById(maNguoiDung)
            .map(NguoiDung::getHoTen)
            .filter(hoTen -> !hoTen.isBlank())
            .orElse("Người dùng");
    }
}