package vn.edu.group12.qldt.nhom;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import vn.edu.group12.qldt.config.nhom.NhomBaoCaoConfiguration;
import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.storage.nhom.ReportStorageService;

class BaoCaoStorageConfigurationTests {
    @Test
    void thieuCauHinhThiUploadVaDownloadFailClosed() {
        new ApplicationContextRunner()
                .withUserConfiguration(NhomBaoCaoConfiguration.class)
                .withPropertyValues(
                        "R2_ENDPOINT=", "R2_BUCKET=", "R2_ACCESS_KEY_ID=", "R2_SECRET_ACCESS_KEY=")
                .run(
                        context -> {
                            var storage = context.getBean(ReportStorageService.class);
                            assertThatThrownBy(
                                            () ->
                                                    storage.put(
                                                            "bao-cao/test.pdf",
                                                            NhomTestSupport.pdf("thử")))
                                    .isInstanceOfSatisfying(
                                            NghiepVuNhomException.class,
                                            e -> {
                                                assertThat(e.getLoai())
                                                        .isEqualTo(NghiepVuNhomException.Loai.LUU_TRU);
                                                assertThat(e.getMessage())
                                                        .contains("chưa được cấu hình");
                                            });
                            assertThatThrownBy(() -> storage.get("bao-cao/test.pdf", 10000))
                                    .isInstanceOf(NghiepVuNhomException.class);
                        });
    }

    @Test
    void khongChoEndpointKhongCoTlsTrongCauHinhThat() {
        new ApplicationContextRunner()
                .withUserConfiguration(NhomBaoCaoConfiguration.class)
                .withPropertyValues(
                        "R2_ENDPOINT=http://example.invalid",
                        "R2_BUCKET=test",
                        "R2_ACCESS_KEY_ID=test",
                        "R2_SECRET_ACCESS_KEY=test")
                .run(context -> assertThat(context).hasFailed());
    }
}
