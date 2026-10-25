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
                var dean = new Session(f.dean);
                var lecturer = new Session(f.lecturer)) {
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
            // Giảng viên chưa được phân công không thể truy cập nhóm theo mã đoán được.
            assertThat(lecturer.get("/bao-cao?maNhom=" + ma).statusCode()).isEqualTo(403);
            // Cho phép giảng viên hướng dẫn truy cập báo cáo của nhóm được phân công.
            jdbc.update(
                    "INSERT INTO de_tai_giang_vien(ma_de_tai,ma_giang_vien,loai_phan_cong,ma_nguoi_phan_cong,phan_cong_luc) VALUES(?,?,'HUONG_DAN',?,?)",
                    f.topic, f.lecturer, f.dean, NOW);
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
                // Từ chối thành viên đang chờ phải dùng modal chung (không POST khi Hủy).
                assertThat(leader.get("/nhom/cua-toi").body())
                        .contains("data-sv-confirm-button=\"Xác nhận từ chối\"");
                // Hồi quy: khi đang chờ, trạng thái phải hiện ở danh sách và chi tiết.
                assertThat(item.getKey().get("/nhom").body())
                        .contains("Đã gửi yêu cầu tham gia", "Chi tiết");
                assertThat(item.getKey().get("/nhom/" + ma).body())
                        .contains("Đã gửi yêu cầu tham gia.");
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
            // Modal xác nhận TV3: xóa thành viên và chốt nhóm, không dùng popup native.
            String trangQuanLyNhom = leader.get("/nhom/cua-toi").body();
            assertThat(trangQuanLyNhom)
                    .contains("data-sv-confirm-button=\"Xác nhận xóa\"",
                            "data-sv-confirm-button=\"Xác nhận chốt nhóm\"");
            String jsXacNhan = leader.get("/js/nhom-dang-ky-bao-cao.js").body();
            assertThat(jsXacNhan)
                    .contains("data-sv-action-dialog", "form.requestSubmit(")
                    .doesNotContain("window.confirm(");
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
            // Modal xác nhận đăng ký phải được render, không dùng xác nhận native.
            String trangDangKyTruocKhiGui = leader.get("/dang-ky-de-tai?maDot=" + f.dot).body();
            assertThat(trangDangKyTruocKhiGui)
                    .contains("data-sv-register-confirm", "data-sv-register-dialog",
                            "data-sv-register-submit", "data-sv-register-code",
                            "data-sv-register-title", "name=\"maDeTai\"")
                    .doesNotContain("data-sv-confirm=\"Gửi đăng ký đề tài này cho nhóm?\"");
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
            // Trưởng khoa có bảng duyệt nhóm/báo cáo riêng, gồm bộ lọc và phân trang.
            String trangTraCuuBaoCao = dean.get("/bao-cao?tuKhoa=" + ma).body();
            assertThat(trangTraCuuBaoCao)
                    .contains("Danh sách báo cáo theo nhóm", ma,
                                "Xem", "Chưa có PDF hợp lệ", "name=\"trangThaiNop\"");
            String trangNopTheoNhom = dean.get("/bao-cao?maNhom=" + ma).body();
            assertThat(trangNopTheoNhom).contains("Thông tin nộp báo cáo", ma);
            // Hồi quy: modal từ chối có đúng action từng nhóm, CSRF và giới hạn nhập.
            String trangDuyet = dean.get("/dang-ky-de-tai/duyet?tuKhoa=" + ma).body();
            // Quyết định duyệt có dữ liệu xác nhận, độc lập với modal từ chối.
            assertThat(trangDuyet).contains("data-sv-confirm-button=\"Xác nhận duyệt\"");
            assertThat(trangDuyet)
                    .contains("data-sv-reject-dialog", "data-sv-reject-open",
                            "data-sv-reject-form", "name=\"lyDoTuChoi\"",
                            "maxlength=\"1000\"", "name=\"_csrf\"");
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
            // GV chỉ thấy nhóm có liên quan và có thể xem/tải PDF qua đường dẫn thật.
            assertThat(lecturer.get("/dashboard").body()).contains("Báo cáo nhóm");
            assertThat(lecturer.get("/bao-cao").body())
                    .contains("Chọn nhóm được phân công", "name=\"maNhom\"", ma);
            assertThat(lecturer.get("/bao-cao?maNhom=" + ma).body())
                    .contains("Tải PDF", "Lịch sử các phiên bản");
            assertThat(lecturer.download("/bao-cao/" + ma + "/1/tai").statusCode())
                    .isEqualTo(200);
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
            // Thu hồi chỉ đổi trạng thái trên schema kiểm thử: UI ẩn nút và backend trả 404.
            jdbc.update("UPDATE nop_bao_cao SET trang_thai='THU_HOI' WHERE ma_nhom=? AND phien_ban=1", ma);
            String danhSachThuHoi = leader.get("/bao-cao").body();
            assertThat(danhSachThuHoi).contains("Không thể tải (đã thu hồi)")
                    .doesNotContain("/bao-cao/" + ma + "/1/tai");
            assertThat(leader.download("/bao-cao/" + ma + "/1/tai").statusCode())
                    .isEqualTo(404);
            jdbc.update("UPDATE nop_bao_cao SET trang_thai='HOP_LE' WHERE ma_nhom=? AND phien_ban=1", ma);
            storage.objects.remove(key);
            assertThat(leader.download("/bao-cao/" + ma + "/1/tai").statusCode()).isEqualTo(404);
            assertThat(f.count("SELECT COUNT(*) FROM nop_bao_cao WHERE ma_nhom=?", ma))
                    .isEqualTo(1);
        }
    }

    @Test
    void lyDoTuChoiDuocHienThiAnToanKhiLocDangKy() throws Exception {
        var f = new Fixture(jdbc);
        String maNhom = f.prefix + "RJ";
        jdbc.update(
                "INSERT INTO nhom_sinh_vien(ma_nhom,ten_nhom,ma_dot,ma_nguoi_tao,trang_thai,chot_luc) "
                        + "VALUES(?,?,?,?,'DA_CHOT',?)",
                maNhom, "Nhóm kiểm thử lý do", f.dot, f.sv(1), NOW);
        // Tạo nhóm hợp lệ với ba thành viên chính thức; chỉ trên schema MySQL test.
        for (int i = 1; i <= 3; i++) {
            jdbc.update(
                    "INSERT INTO thanh_vien_nhom(ma_nhom,ma_sinh_vien,trang_thai,"
                            + "la_nhom_truong,la_thanh_vien_chot,yeu_cau_luc,tham_gia_luc) "
                            + "VALUES(?,?,'THAM_GIA',?,TRUE,?,?)",
                    maNhom, f.sv(i), i == 1, NOW.minusDays(1), NOW.minusDays(1));
        }
        jdbc.update(
                "INSERT INTO dang_ky_de_tai(ma_nhom,ma_de_tai,ma_nguoi_dang_ky,dang_ky_luc) "
                        + "VALUES(?,?,?,?)",
                maNhom, f.topic, f.sv(1), NOW);
        String lyDo = "Phạm vi khảo sát chưa đủ <script>alert(1)</script>";
        try (var dean = new Session(f.dean)) {
            var response = dean.post(
                    "/dang-ky-de-tai/" + maNhom + "/tu-choi",
                    Map.of("lyDoTuChoi", lyDo),
                    "/dang-ky-de-tai/duyet?tuKhoa=" + maNhom);
            assertThat(response.statusCode()).isEqualTo(302);
            assertThat(jdbc.queryForObject(
                    "SELECT ly_do_tu_choi FROM dang_ky_de_tai WHERE ma_nhom=?",
                    String.class, maNhom)).isEqualTo(lyDo);
            var page = dean.get(
                    "/dang-ky-de-tai/duyet?trangThai=TU_CHOI&tuKhoa=" + maNhom);
            assertThat(page.statusCode()).isEqualTo(200);
            assertThat(page.body())
                    .contains(maNhom, "Xem lý do", "sv-rejection-history", "&lt;script&gt;alert(1)&lt;/script&gt;")
                    .doesNotContain("<script>alert(1)</script>");
            // Quyết định bị từ chối chỉ được xem lại, không xuất hiện form duyệt lại.
            assertThat(page.body()).doesNotContain("data-sv-reject-open");
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
