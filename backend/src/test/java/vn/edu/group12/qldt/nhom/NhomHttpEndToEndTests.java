package vn.edu.group12.qldt.nhom;

import static org.assertj.core.api.Assertions.*;

import static vn.edu.group12.qldt.nhom.NhomTestSupport.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import vn.edu.group12.qldt.service.nhom.PdfValidator;

import java.io.ByteArrayOutputStream;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@org.junit.jupiter.api.Tag("mysql")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.flyway.enabled=true",
            "spring.jpa.hibernate.ddl-auto=none",
            "spring.jpa.show-sql=false"
        })
@ActiveProfiles("staging")
@Import(Config.class)
@EnabledIfEnvironmentVariable(
        named = "NHOM_TEST_DB_URL",
        matches = "jdbc:mysql://(127\\.0\\.0\\.1|localhost):[0-9]+/nhom_test_.*")
class NhomHttpEndToEndTests extends NhomMySqlTestBase {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired FakeStorage storage;

    @Test
    void httpQuaDangNhapCsrfMvcServiceMySqlVaKhoPdf() throws Exception {
        var f = new Fixture(jdbc);
        storage.reset();
        try (var leader = new Session(f.sv(1));
                var sv2 = new Session(f.sv(2));
                var sv3 = new Session(f.sv(3));
                var outsider = new Session(f.sv(9));
                var dean = new Session(f.dean)) {
            var created =
                    leader.post(
                            "/nhom/tao",
                            Map.of(
                                    "tenNhom",
                                    "Nhóm HTTP " + f.prefix,
                                    "maDot",
                                    f.dot,
                                    "maNguoiTao",
                                    f.sv(9)),
                            "/nhom/tao");
            assertThat(created.statusCode()).isEqualTo(302);
            String ma =
                    jdbc.queryForObject(
                            "SELECT ma_nhom FROM nhom_sinh_vien WHERE ma_nguoi_tao=?",
                            String.class,
                            f.sv(1));
            assertThat(f.count("SELECT COUNT(*) FROM thanh_vien_nhom WHERE nhom_co_truong=?", ma))
                    .isEqualTo(1);
            for (var item : List.of(Map.entry(sv2, 2), Map.entry(sv3, 3))) {
                assertThat(
                                item.getKey()
                                        .post("/nhom/" + ma + "/yeu-cau", Map.of(), "/nhom/" + ma)
                                        .statusCode())
                        .isEqualTo(302);
                assertThat(
                                f.count(
                                        "SELECT COUNT(*) FROM thanh_vien_nhom WHERE ma_nhom=? AND"
                                                + " ma_sinh_vien=? AND trang_thai='CHO_DUYET'",
                                        ma,
                                        f.sv(item.getValue())))
                        .isEqualTo(1);
                assertThat(
                                leader.post(
                                                "/nhom/"
                                                        + ma
                                                        + "/yeu-cau/"
                                                        + f.sv(item.getValue())
                                                        + "/duyet",
                                                Map.of(),
                                                "/nhom/cua-toi")
                                        .statusCode())
                        .isEqualTo(302);
            }
            assertThat(
                            f.count(
                                    "SELECT COUNT(*) FROM thanh_vien_nhom WHERE ma_nhom=? AND"
                                            + " trang_thai='THAM_GIA'",
                                    ma))
                    .isEqualTo(3);
            assertThat(leader.post("/nhom/" + ma + "/chot", Map.of(), "/nhom/cua-toi").statusCode())
                    .isEqualTo(302);
            assertThat(
                            f.count(
                                    "SELECT COUNT(*) FROM thanh_vien_nhom WHERE ma_nhom=? AND"
                                            + " la_thanh_vien_chot=TRUE",
                                    ma))
                    .isEqualTo(3);
            assertThat(
                            sv2.post("/dang-ky-de-tai", Map.of("maDeTai", f.topic), "/nhom/tao")
                                    .statusCode())
                    .isEqualTo(403);
            assertThat(
                            leader.post(
                                            "/dang-ky-de-tai",
                                            Map.of("maDeTai", f.topic, "maNhom", "gia-ma-nhom"),
                                            "/dang-ky-de-tai?maDot=" + f.dot)
                                    .statusCode())
                    .isEqualTo(302);
            assertThat(
                            f.count(
                                    "SELECT COUNT(*) FROM dang_ky_de_tai WHERE ma_nhom=? AND"
                                            + " ma_nguoi_dang_ky=?",
                                    ma,
                                    f.sv(1)))
                    .isEqualTo(1);
            assertThat(
                            dean.post(
                                            "/dang-ky-de-tai/" + ma + "/duyet",
                                            Map.of(),
                                            "/dang-ky-de-tai/duyet?tuKhoa=" + ma)
                                    .statusCode())
                    .isEqualTo(302);
            assertThat(
                            jdbc.queryForObject(
                                    "SELECT trang_thai FROM dang_ky_de_tai WHERE ma_nhom=?",
                                    String.class,
                                    ma))
                    .isEqualTo("DA_DUYET");
            byte[] bytes = pdf("Từ HTTP đến MySQL và kho PDF");
            // Gửi CSRF như biểu mẫu trình duyệt: token nằm trong phần multipart.
            var upload = leader.upload(bytes, true, false);
            assertThat(upload.statusCode()).as(upload.body()).isEqualTo(302);
            String key =
                    jdbc.queryForObject(
                            "SELECT object_key FROM nop_bao_cao WHERE ma_nhom=? AND phien_ban=1",
                            String.class,
                            ma);
            assertThat(storage.objects.get(key)).isEqualTo(bytes);
            assertThat(
                            jdbc.queryForObject(
                                    "SELECT sha256 FROM nop_bao_cao WHERE ma_nhom=? AND"
                                            + " phien_ban=1",
                                    String.class,
                                    ma))
                    .isEqualTo(PdfValidator.sha256(bytes));
            var download = sv2.download("/bao-cao/" + ma + "/1/tai");
            assertThat(download.statusCode()).isEqualTo(200);
            assertThat(download.body()).isEqualTo(bytes);
            assertThat(download.headers().firstValue("Content-Type")).contains("application/pdf");
            assertThat(download.headers().firstValue("Cache-Control"))
                    .contains("private, no-store");
            assertThat(outsider.download("/bao-cao/" + ma + "/1/tai").statusCode()).isEqualTo(403);
            assertThat(sv2.upload(pdf("Không phải trưởng"), true, true).statusCode())
                    .isEqualTo(403);
            assertThat(leader.upload("Đổi tên TXT thành PDF".getBytes(), true, true).statusCode())
                    .isEqualTo(400);
            assertThat(leader.upload(pdf("Thiếu CSRF"), false, false).statusCode()).isEqualTo(403);
            var oversized = leader.upload(new byte[PdfValidator.MAX_BYTES + 1], true, true);
            assertThat(oversized.statusCode()).as(oversized.body()).isEqualTo(413);
            assertThat(oversized.body()).contains("20 MiB");
            assertThat(f.count("SELECT COUNT(*) FROM nop_bao_cao WHERE ma_nhom=?", ma))
                    .isEqualTo(1);
            storage.objects.remove(key);
            assertThat(leader.download("/bao-cao/" + ma + "/1/tai").statusCode()).isEqualTo(404);
            assertThat(f.count("SELECT COUNT(*) FROM nop_bao_cao WHERE ma_nhom=?", ma))
                    .isEqualTo(1);
        }
    }

    final class Session implements AutoCloseable {
        final HttpClient client =
                HttpClient.newBuilder()
                        .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

        Session(String user) throws Exception {
            String token = csrf(get("/login").body());
            var result =
                    sendForm(
                            "/login",
                            Map.of("email", user + "@example.invalid", "matKhau", PASSWORD, "_csrf", token));
            assertThat(result.statusCode()).isEqualTo(302);
            assertThat(result.headers().firstValue("Location").orElse("")).endsWith("/dashboard");
        }

        URI uri(String path) {
            return URI.create("http://127.0.0.1:" + port + path);
        }

        HttpResponse<String> get(String path) throws Exception {
            return client.send(
                    HttpRequest.newBuilder(uri(path)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
        }

        HttpResponse<byte[]> download(String path) throws Exception {
            return client.send(
                    HttpRequest.newBuilder(uri(path)).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray());
        }

        HttpResponse<String> post(String path, Map<String, String> values, String source)
                throws Exception {
            var fields = new LinkedHashMap<>(values);
            fields.put("_csrf", csrf(get(source).body()));
            return sendForm(path, fields);
        }

        HttpResponse<String> sendForm(String path, Map<String, String> fields) throws Exception {
            String body =
                    fields.entrySet().stream()
                            .map(
                                    e ->
                                            URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                                                    + "="
                                                    + URLEncoder.encode(
                                                            e.getValue(), StandardCharsets.UTF_8))
                            .collect(Collectors.joining("&"));
            return client.send(
                    HttpRequest.newBuilder(uri(path))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
        }

        HttpResponse<String> upload(byte[] bytes, boolean includeCsrf, boolean header)
                throws Exception {
            String token = includeCsrf ? csrf(get("/nhom/tao").body()) : "";
            String boundary = "nhom" + UUID.randomUUID();
            var body = new ByteArrayOutputStream();
            if (includeCsrf && !header)
                body.writeBytes(
                        ("--"
                                        + boundary
                                        + "\r\n"
                                        + "Content-Disposition: form-data; name=\"_csrf\"\r\n\r\n"
                                        + token
                                        + "\r\n")
                                .getBytes(StandardCharsets.UTF_8));
            body.writeBytes(
                    ("--"
                                    + boundary
                                    + "\r\n"
                                    + "Content-Disposition: form-data; name=\"tepBaoCao\";"
                                    + " filename=\"report.pdf\"\r\n"
                                    + "Content-Type: application/pdf\r\n\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
            body.writeBytes(bytes);
            body.writeBytes(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.US_ASCII));
            var request =
                    HttpRequest.newBuilder(uri("/bao-cao/nop"))
                            .header("Content-Type", "multipart/form-data; boundary=" + boundary);
            if (includeCsrf && header) request.header("X-CSRF-TOKEN", token);
            return client.send(
                    request.POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
        }

        @Override
        public void close() {
            client.close();
        }
    }

    static String csrf(String html) {
        var matcher = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(html);
        assertThat(matcher.find()).as("Biểu mẫu phải có CSRF token").isTrue();
        return matcher.group(1);
    }
}
