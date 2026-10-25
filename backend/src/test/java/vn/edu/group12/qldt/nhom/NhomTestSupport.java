package vn.edu.group12.qldt.nhom;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import vn.edu.group12.qldt.service.auth.QldtUserDetails;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.storage.nhom.ReportStorageService;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

final class NhomTestSupport {
    static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 22, 15, 0);
    static final Clock CLOCK =
            Clock.fixed(
                    NOW.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant(),
                    ZoneId.of("Asia/Ho_Chi_Minh"));
    static final String PASSWORD = UUID.randomUUID().toString();
    static final String HASH = new BCryptPasswordEncoder(4).encode(PASSWORD);

    @TestConfiguration(proxyBeanMethods = false)
    static class Config {
        @Bean
        @Primary
        Clock testClock() {
            return CLOCK;
        }

        @Bean
        @Primary
        FakeStorage testStorage() {
            return new FakeStorage();
        }
    }

    static class FakeStorage implements ReportStorageService {
        final Map<String, byte[]> objects = new ConcurrentHashMap<>();
        boolean failAfterPut;
        final AtomicInteger deleteFailures = new AtomicInteger();

        void reset() {
            objects.clear();
            failAfterPut = false;
            deleteFailures.set(0);
        }

        public void put(String key, byte[] bytes) {
            ReportStorageService.kiemTraKhoa(key);
            objects.put(key, bytes.clone());
            if (failAfterPut)
                throw NghiepVuNhomException.luuTru("Kho thử nghiệm mất kết nối sau khi nhận tệp.", null);
        }

        public byte[] get(String key, int limit) {
            ReportStorageService.kiemTraKhoa(key);
            if (!objects.containsKey(key))
                throw NghiepVuNhomException.thieu(
                        "Không tìm thấy tệp PDF trong kho lưu trữ. Metadata được giữ nguyên.");
            return objects.get(key).clone();
        }

        public void delete(String key) {
            if (deleteFailures.getAndDecrement() > 0)
                throw NghiepVuNhomException.luuTru("Lỗi dọn tệp thử nghiệm.", null);
            objects.remove(key);
        }
    }

    static class Fixture {
        final JdbcTemplate jdbc;
        final String prefix = "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        final String dot = prefix + "DOT",
                topic = prefix + "DT",
                dean = prefix + "TK",
                lecturer = prefix + "GV";

        Fixture(JdbcTemplate jdbc) {
            this.jdbc = jdbc;
            String url = Objects.requireNonNull(System.getenv("NHOM_TEST_DB_URL"));
            if (!url.matches(
                    "jdbc:mysql://(127\\.0\\.0\\.1|localhost):[0-9]+/nhom_test_[A-Za-z0-9_]+(\\?.*)?"))
                throw new IllegalStateException(
                        "Chỉ dùng schema nhom_test_ trên MySQL local riêng.");
            user(dean, "CAN_BO", "TRUONG_KHOA");
            user(lecturer, "GV", "GIANG_VIEN");
            for (int i = 1; i <= 15; i++) user(sv(i), "SV", "SINH_VIEN");
            jdbc.update(
                    "INSERT INTO bo_mon(ma_bo_mon,ten_bo_mon) VALUES(?,?)",
                    prefix + "BM",
                    "Bộ môn thử nghiệm");
            jdbc.update(
                    """
                    INSERT INTO dot_dang_ky(ma_dot,ten_dot,loai_de_tai,bat_dau_gv,ket_thuc_gv,bat_dau_sv,ket_thuc_sv,han_nop_bao_cao,trang_thai,ma_nguoi_tao)
                    VALUES(?,?,'MON_HOC',?,?,?,?,?,'CONG_BO',?)
                    """,
                    dot,
                    "Đợt thử " + prefix,
                    NOW.minusDays(10),
                    NOW.minusDays(5),
                    NOW.minusDays(4),
                    NOW.plusDays(2),
                    NOW.plusDays(10),
                    dean);
            topic(topic, dot, "CONG_BO");
        }

        String sv(int n) {
            return prefix + "SV" + n;
        }

        void user(String ma, String type, String role) {
            jdbc.update(
                    "INSERT INTO"
                        + " nguoi_dung(ma_nguoi_dung,ho_ten,email,mat_khau_hash,loai_nguoi_dung)"
                        + " VALUES(?,?,?,?,?)",
                    ma,
                    "Người thử " + ma,
                    ma + "@example.invalid",
                    HASH,
                    type);
            jdbc.update(
                    "INSERT INTO nguoi_dung_vai_tro(ma_nguoi_dung,ma_vai_tro) VALUES(?,?)",
                    ma,
                    role);
        }

        void topic(String ma, String round, String state) {
            jdbc.update(
                    "INSERT INTO"
                        + " de_tai(ma_de_tai,ma_dot,ma_bo_mon,ten_de_tai,noi_dung,ma_nguoi_de_xuat,trang_thai,cong_bo_luc)"
                        + " VALUES(?,?,?,?,?,?,?,?)",
                    ma,
                    round,
                    prefix + "BM",
                    "Đề tài thử " + ma,
                    "Nội dung thử",
                    lecturer,
                    state,
                    "CONG_BO".equals(state) ? NOW.minusDays(1) : null);
        }

        long count(String sql, Object... args) {
            return jdbc.queryForObject(sql, Long.class, args);
        }
    }

    static QldtUserDetails testDetails(String ma, String role) {
        return new QldtUserDetails(
                ma,
                ma + "@example.invalid",
                HASH,
                true,
                AuthorityUtils.createAuthorityList("ROLE_" + role));
    }

    static RequestPostProcessor testUser(String ma, String role) {
        return SecurityMockMvcRequestPostProcessors.user(testDetails(ma, role));
    }

    static <T> T as(String user, String role, Supplier<T> action) {
        var old = SecurityContextHolder.getContext();
        var ctx = SecurityContextHolder.createEmptyContext();
        var principal = testDetails(user, role);
        ctx.setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        principal, "", principal.getAuthorities()));
        SecurityContextHolder.setContext(ctx);
        try {
            return action.get();
        } finally {
            SecurityContextHolder.setContext(old);
        }
    }

    static void as(String user, String role, Runnable action) {
        as(
                user,
                role,
                () -> {
                    action.run();
                    return null;
                });
    }

    static MockMultipartFile file(byte[] bytes) {
        return new MockMultipartFile("tepBaoCao", "báo-cáo.pdf", "application/pdf", bytes);
    }

    static byte[] pdf(String text) {
        return pdfSized(0, text);
    }

    static byte[] pdfSized(int target, String text) {
        int padding = Math.max(0, target - 600);
        for (int lan = 0; lan < 5; lan++) {
            var out = new ByteArrayOutputStream();
            var offsets = new ArrayList<Integer>();
            write(out, "%PDF-1.4\n");
            String[] objects = {
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 100 100] /Contents 4 0 R >>",
                "<< /Length "
                        + (padding + text.length() + 2)
                        + " >>\nstream\n%"
                        + text
                        + "\n"
                        + " ".repeat(padding)
                        + "\nendstream"
            };
            for (int i = 0; i < objects.length; i++) {
                offsets.add(out.size());
                write(out, (i + 1) + " 0 obj\n" + objects[i] + "\nendobj\n");
            }
            int xref = out.size();
            write(out, "xref\n0 5\n0000000000 65535 f \n");
            for (int offset : offsets)
                write(out, String.format(Locale.ROOT, "%010d 00000 n \n", offset));
            write(out, "trailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
            if (target == 0 || out.size() == target) return out.toByteArray();
            padding += target - out.size();
        }
        throw new IllegalStateException("Không tạo được PDF có kích thước yêu cầu.");
    }

    private static void write(ByteArrayOutputStream out, String text) {
        out.writeBytes(text.getBytes(StandardCharsets.US_ASCII));
    }
}
