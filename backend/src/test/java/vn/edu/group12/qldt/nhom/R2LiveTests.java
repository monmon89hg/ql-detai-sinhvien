package vn.edu.group12.qldt.nhom;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import vn.edu.group12.qldt.config.nhom.NhomBaoCaoConfiguration;
import vn.edu.group12.qldt.service.nhom.PdfValidator;
import vn.edu.group12.qldt.storage.nhom.R2ReportStorage;

import java.net.URI;
import java.util.UUID;

@org.junit.jupiter.api.Tag("external")
@EnabledIfEnvironmentVariable(named = "R2_ENDPOINT", matches = "https://.+")
class R2LiveTests {
    @Test
    void putGetShaVaDeleteTrenR2DaDuocCauHinh() {
        for (String name : new String[] {"R2_BUCKET", "R2_ACCESS_KEY_ID", "R2_SECRET_ACCESS_KEY"})
            assumeTrue(
                    System.getenv(name) != null && !System.getenv(name).isBlank(),
                    "Thiếu biến cấu hình R2: " + name);
        var uri = URI.create(System.getenv("R2_ENDPOINT"));
        try (var client =
                NhomBaoCaoConfiguration.taoClient(
                        uri,
                        System.getenv("R2_ACCESS_KEY_ID"),
                        System.getenv("R2_SECRET_ACCESS_KEY"),
                        System.getenv().getOrDefault("R2_REGION", "auto"))) {
            var storage = new R2ReportStorage(client, System.getenv("R2_BUCKET"));
            String key = "bao-cao/verification/" + UUID.randomUUID() + ".pdf";
            byte[] bytes = NhomTestSupport.pdf("Kiểm tra R2");
            try {
                storage.put(key, bytes);
                byte[] read = storage.get(key, bytes.length);
                assertThat(read).isEqualTo(bytes);
                assertThat(PdfValidator.sha256(read)).isEqualTo(PdfValidator.sha256(bytes));
            } finally {
                storage.delete(key);
            }
        }
    }
}
