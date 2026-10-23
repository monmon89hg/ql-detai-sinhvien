package vn.edu.group12.qldt.service.hoidong;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.entity.hoidong.EmailThongBaoKetQua;
import vn.edu.group12.qldt.entity.hoidong.KetQua;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiEmailKetQua;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiKetQua;
import vn.edu.group12.qldt.repository.hoidong.EmailThongBaoKetQuaRepository;
import vn.edu.group12.qldt.repository.hoidong.KetQuaRepository;

@Service
@ConditionalOnProperty(
    prefix = "app.ket-qua.email",
    name = "enabled",
    havingValue = "true"
)
public class GuiEmailKetQuaService {

    private final EmailThongBaoKetQuaRepository emailRepository;
    private final KetQuaRepository ketQuaRepository;
    private final JavaMailSender mailSender;
    private final String diaChiGui;

    public GuiEmailKetQuaService(
            EmailThongBaoKetQuaRepository emailRepository,
            KetQuaRepository ketQuaRepository,
            JavaMailSender mailSender,
            @Value("${app.ket-qua.email.from}") String diaChiGui) {
        this.emailRepository = emailRepository;
        this.ketQuaRepository = ketQuaRepository;
        this.mailSender = mailSender;
        this.diaChiGui = diaChiGui;
    }

    // Khóa một bản ghi trong lúc gửi để các lượt xử lý không chọn trùng.
    @Transactional
    public void guiMotEmail() {
        LocalDateTime nguongThuLai =
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                        .minusMinutes(5);

        Optional<EmailThongBaoKetQua> emailCanGui =
                emailRepository.khoaEmailCanGui(nguongThuLai);

        if (emailCanGui.isEmpty()) {
            return;
        }

        EmailThongBaoKetQua email = emailCanGui.get();
        KetQua ketQua = ketQuaRepository.findById(email.getMaNhom())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy kết quả của nhóm " + email.getMaNhom()));

        if (ketQua.getTrangThai() != TrangThaiKetQua.CONG_BO) {
            throw new IllegalStateException(
                    "Không được gửi email cho kết quả chưa công bố.");
        }

        SimpleMailMessage thu = new SimpleMailMessage();
        thu.setFrom(diaChiGui);
        thu.setTo(email.getDiaChiEmail());
        thu.setSubject("Thông báo kết quả đề tài - " + email.getMaNhom());
        thu.setText(
                "Kết quả của nhóm " + email.getMaNhom()
                        + " đã được công bố.\n"
                        + "Điểm cuối: "
                        + ketQua.getDiemCuoi().toPlainString()
                        + "/10.\n"
                        + "Vui lòng đăng nhập hệ thống để xem kết quả."
        );

        email.setSoLanThu(email.getSoLanThu() + 1);
        email.setLanThuCuoiLuc(
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

        try {
            mailSender.send(thu);
            email.setTrangThai(TrangThaiEmailKetQua.DA_GUI);
            email.setGuiLuc(
                    LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            email.setLoiCuoi(null);
        } catch (MailException ex) {
            // Lưu lỗi để lượt xử lý sau có thể thử gửi lại.
            email.setTrangThai(TrangThaiEmailKetQua.LOI);
            String moTaLoi = ex.getMessage();
            email.setLoiCuoi(moTaLoi == null
                    ? ex.getClass().getSimpleName()
                    : moTaLoi.substring(0, Math.min(moTaLoi.length(), 1000)));
        }
    }
}