package vn.edu.group12.qldt.config.nhom;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.storage.nhom.*;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

@Configuration
public class NhomBaoCaoConfiguration {
    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock nghiepVuClock() {
        return Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(S3Client.class)
    public S3Client baoCaoR2Client(Environment env) {
        String endpoint = env.getProperty("R2_ENDPOINT", "");
        String key = env.getProperty("R2_ACCESS_KEY_ID", "");
        String secret = env.getProperty("R2_SECRET_ACCESS_KEY", "");
        if (endpoint.isBlank()
                || key.isBlank()
                || secret.isBlank()
                || env.getProperty("R2_BUCKET", "").isBlank()) return null;
        URI uri = URI.create(endpoint);
        if (!"https".equals(uri.getScheme())
                || uri.getHost() == null
                || uri.getUserInfo() != null
                || uri.getQuery() != null
                || uri.getFragment() != null) {
            throw new IllegalStateException("R2_ENDPOINT phải là địa chỉ HTTPS của kho R2.");
        }
        return taoClient(uri, key, secret, env.getProperty("R2_REGION", "auto"));
    }

    public static S3Client taoClient(URI endpoint, String key, String secret, String region) {
        return S3Client.builder()
                .endpointOverride(endpoint)
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(key, secret)))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .chunkedEncodingEnabled(false)
                                .build())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .overrideConfiguration(
                        c ->
                                c.apiCallAttemptTimeout(Duration.ofSeconds(15))
                                        .apiCallTimeout(Duration.ofSeconds(30)))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ReportStorageService.class)
    public ReportStorageService reportStorage(
            Environment env, org.springframework.beans.factory.ObjectProvider<S3Client> clients) {
        S3Client client = clients.getIfAvailable();
        if (client != null)
            return new R2ReportStorage(client, env.getRequiredProperty("R2_BUCKET"));
        return new ReportStorageService() {
            private NghiepVuNhomException loi() {
                return NghiepVuNhomException.luuTru(
                        "Kho PDF chưa được cấu hình. Vui lòng liên hệ quản trị viên thiết lập"
                                + " Cloudflare R2.",
                        null);
            }

            public void put(String key, byte[] bytes) {
                throw loi();
            }

            public byte[] get(String key, int maxBytes) {
                throw loi();
            }

            public void delete(String key) {
                throw loi();
            }
        };
    }
}
