package vn.edu.group12.qldt.nhom;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

abstract class NhomMySqlTestBase {
    @DynamicPropertySource
    static void nguonThuNghiem(DynamicPropertyRegistry properties) {
        String url = System.getenv("NHOM_TEST_DB_URL");
        if (url == null
                || !url.matches(
                        "jdbc:mysql://(127\\.0\\.0\\.1|localhost):[0-9]+/nhom_test_[A-Za-z0-9_]+(\\?.*)?")) {
            throw new IllegalStateException(
                    "Kiểm thử Nhóm/đăng ký/báo cáo chỉ được dùng schema nhom_test_ trên MySQL local riêng.");
        }
        properties.add("spring.datasource.url", () -> url);
        properties.add("spring.datasource.username", () -> batBuoc("NHOM_TEST_DB_USERNAME"));
        properties.add("spring.datasource.password", () -> batBuoc("NHOM_TEST_DB_PASSWORD"));
    }

    private static String batBuoc(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank())
            throw new IllegalStateException("Thiếu biến cấu hình kiểm thử: " + name);
        return value;
    }
}
