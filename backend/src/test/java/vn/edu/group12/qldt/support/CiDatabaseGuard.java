package vn.edu.group12.qldt.support;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/** Từ chối cấu hình database không an toàn trước khi khởi tạo DataSource/Flyway. */
public final class CiDatabaseGuard
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        Environment environment = context.getEnvironment();
        String url = environment.getRequiredProperty("spring.datasource.url");
        if (!url.matches("jdbc:mysql://127\\.0\\.0\\.1:[0-9]{1,5}/qldt_ci(?:\\?[^#]*)?")
                || environment.containsProperty("spring.flyway.url")
                || !"validate".equals(environment.getProperty("spring.jpa.hibernate.ddl-auto"))
                || !environment.getProperty("spring.flyway.enabled", Boolean.class, false)
                || !environment.getProperty("spring.flyway.clean-disabled", Boolean.class, false)
                || environment.getProperty("app.ket-qua.email.enabled", Boolean.class, true)) {
            throw new IllegalStateException(
                    "Test CI chỉ được dùng MySQL loopback/schema qldt_ci, Flyway bật và chặn clean, "
                    + "JPA validate và không gửi email");
        }
    }
}
