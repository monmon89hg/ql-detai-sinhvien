package vn.edu.group12.qldt.service.hoidong;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.dto.hoidong.NguoiNhanKetQua;
import vn.edu.group12.qldt.entity.hoidong.EmailThongBaoKetQua;
import vn.edu.group12.qldt.entity.hoidong.EmailThongBaoKetQuaId;
import vn.edu.group12.qldt.repository.hoidong.EmailThongBaoKetQuaRepository;
import vn.edu.group12.qldt.repository.hoidong.KetQuaRepository;

@Service
public class TaoEmailThongBaoKetQuaService {

    private final KetQuaRepository ketQuaRepository;
    private final EmailThongBaoKetQuaRepository emailRepository;

    public TaoEmailThongBaoKetQuaService(
            KetQuaRepository ketQuaRepository,
            EmailThongBaoKetQuaRepository emailRepository) {
        this.ketQuaRepository = ketQuaRepository;
        this.emailRepository = emailRepository;
    }

    // Phải chạy trong cùng transaction với thao tác công bố kết quả.
    @Transactional(propagation = Propagation.MANDATORY)
    public void taoChoNhom(String maNhom) {
        List<NguoiNhanKetQua> nguoiNhan =
                ketQuaRepository.layNguoiNhanTheoNhom(maNhom);

        if (nguoiNhan.isEmpty()) {
            throw new IllegalStateException(
                    "Nhóm không có sinh viên hợp lệ để nhận thông báo kết quả.");
        }

        for (NguoiNhanKetQua sinhVien : nguoiNhan) {
            if (sinhVien.getEmail() == null
                    || sinhVien.getEmail().isBlank()) {
                throw new IllegalStateException(
                        "Sinh viên " + sinhVien.getMaSinhVien()
                                + " chưa có địa chỉ email.");
            }

            EmailThongBaoKetQuaId id = new EmailThongBaoKetQuaId(
                    maNhom, sinhVien.getMaSinhVien());

            // Khóa chính ngăn tạo hai email cho cùng sinh viên và kết quả.
            if (emailRepository.existsById(id)) {
                continue;
            }

            EmailThongBaoKetQua email = new EmailThongBaoKetQua();
            email.setMaNhom(maNhom);
            email.setMaSinhVien(sinhVien.getMaSinhVien());
            email.setDiaChiEmail(sinhVien.getEmail());
            emailRepository.save(email);
        }
    }
}