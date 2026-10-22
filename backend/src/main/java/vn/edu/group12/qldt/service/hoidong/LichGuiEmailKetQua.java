package vn.edu.group12.qldt.service.hoidong;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
    prefix = "app.ket-qua.email",
    name = "enabled",
    havingValue = "true"
)
public class LichGuiEmailKetQua {

    private final GuiEmailKetQuaService guiEmailKetQuaService;

    public LichGuiEmailKetQua(
            GuiEmailKetQuaService guiEmailKetQuaService) {
        this.guiEmailKetQuaService = guiEmailKetQuaService;
    }

    // Mỗi lượt lấy một email; service giữ khóa bản ghi trong lúc gửi.
    @Scheduled(fixedDelayString = "${app.ket-qua.email.poll-ms:10000}")
    public void xuLyHangCho() {
        guiEmailKetQuaService.guiMotEmail();
    }
}