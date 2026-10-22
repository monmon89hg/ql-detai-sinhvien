package vn.edu.group12.qldt.service.hoidong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import vn.edu.group12.qldt.entity.hoidong.EmailThongBaoKetQua;
import vn.edu.group12.qldt.entity.hoidong.KetQua;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiEmailKetQua;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiKetQua;
import vn.edu.group12.qldt.repository.hoidong.EmailThongBaoKetQuaRepository;
import vn.edu.group12.qldt.repository.hoidong.KetQuaRepository;

class GuiEmailKetQuaServiceTests {

    private final EmailThongBaoKetQuaRepository emailRepository =
            mock(EmailThongBaoKetQuaRepository.class);
    private final KetQuaRepository ketQuaRepository =
            mock(KetQuaRepository.class);
    private final JavaMailSender mailSender =
            mock(JavaMailSender.class);

    private GuiEmailKetQuaService service;

    @BeforeEach
    void setUp() {
        service = new GuiEmailKetQuaService(
                emailRepository,
                ketQuaRepository,
                mailSender,
                "noreply@example.test");
    }

    @Test
    void guiDiemDaCongBoChoDungSinhVien() {
        EmailThongBaoKetQua email = taoEmailCho();
        KetQua ketQua = taoKetQuaDaCongBo();

        when(emailRepository.khoaEmailCanGui())
                .thenReturn(Optional.of(email));
        when(ketQuaRepository.findById("N01"))
                .thenReturn(Optional.of(ketQua));

        service.guiMotEmail();

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage thu = captor.getValue();
        assertEquals("sv01@example.test", thu.getTo()[0]);
        assertTrue(thu.getText().contains("8.75"));
        assertEquals(TrangThaiEmailKetQua.DA_GUI,
                email.getTrangThai());
        assertEquals(1, email.getSoLanThu());
    }

    @Test
    void loiSmtpDuocGhiLaiDeThuGuiLai() {
        EmailThongBaoKetQua email = taoEmailCho();
        KetQua ketQua = taoKetQuaDaCongBo();

        when(emailRepository.khoaEmailCanGui())
                .thenReturn(Optional.of(email));
        when(ketQuaRepository.findById("N01"))
                .thenReturn(Optional.of(ketQua));
        doThrow(new MailSendException("SMTP unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        service.guiMotEmail();

        assertEquals(TrangThaiEmailKetQua.LOI,
                email.getTrangThai());
        assertEquals(1, email.getSoLanThu());
        assertTrue(email.getLoiCuoi().contains("SMTP unavailable"));
    }

    @Test
    void khongCoEmailChoThiKhongGui() {
        when(emailRepository.khoaEmailCanGui())
                .thenReturn(Optional.empty());

        service.guiMotEmail();

        verifyNoInteractions(mailSender, ketQuaRepository);
    }

    private EmailThongBaoKetQua taoEmailCho() {
        EmailThongBaoKetQua email = new EmailThongBaoKetQua();
        email.setMaNhom("N01");
        email.setMaSinhVien("SV01");
        email.setDiaChiEmail("sv01@example.test");
        return email;
    }

    private KetQua taoKetQuaDaCongBo() {
        KetQua ketQua = mock(KetQua.class);
        when(ketQua.getTrangThai())
                .thenReturn(TrangThaiKetQua.CONG_BO);
        when(ketQua.getDiemCuoi())
                .thenReturn(new BigDecimal("8.75"));
        return ketQua;
    }
}