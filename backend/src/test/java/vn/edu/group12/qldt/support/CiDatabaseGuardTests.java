package vn.edu.group12.qldt.support;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CiDatabaseGuardTests {
    @Test
    void isolatedLocalDatabaseIsAllowedBeforeContextRefresh() {
        try (GenericApplicationContext context = context(Map.of())) {
            assertThatCode(() -> new CiDatabaseGuard().initialize(context)).doesNotThrowAnyException();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "jdbc:mysql://qa.example.invalid:3306/qldt_ci",
            "jdbc:mysql://127.0.0.1:3306/qldt_local_demo",
            "jdbc:mysql://127.0.0.1:3306/qldt_ci_extra",
            "jdbc:mysql:loadbalance://127.0.0.1:3306,qa.example.invalid:3306/qldt_ci"
    })
    void nonCiDatabaseIsRejectedBeforeOpeningAnyConnection(String url) {
        assertRejected(Map.of("spring.datasource.url", url));
    }

    @Test
    void separateFlywayUrlIsRejected() {
        assertRejected(Map.of("spring.flyway.url", "jdbc:mysql://qa.example.invalid/qldt_ci"));
    }

    @Test
    void schemaUpdatesAreRejected() {
        assertRejected(Map.of("spring.jpa.hibernate.ddl-auto", "update"));
    }

    @Test
    void disabledFlywayIsRejected() {
        assertRejected(Map.of("spring.flyway.enabled", "false"));
    }

    @Test
    void enabledEmailDeliveryIsRejected() {
        assertRejected(Map.of("app.ket-qua.email.enabled", "true"));
    }

    @Test
    void enabledFlywayCleanIsRejected() {
        assertRejected(Map.of("spring.flyway.clean-disabled", "false"));
    }

    private void assertRejected(Map<String, Object> overrides) {
        try (GenericApplicationContext context = context(overrides)) {
            assertThatThrownBy(() -> new CiDatabaseGuard().initialize(context))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private GenericApplicationContext context(Map<String, Object> overrides) {
        Map<String, Object> properties = new HashMap<>(Map.of(
                "spring.datasource.url", "jdbc:mysql://127.0.0.1:3306/qldt_ci",
                "spring.jpa.hibernate.ddl-auto", "validate",
                "spring.flyway.enabled", "true",
                "spring.flyway.clean-disabled", "true",
                "app.ket-qua.email.enabled", "false"));
        properties.putAll(overrides);
        GenericApplicationContext context = new GenericApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("ci-test", properties));
        return context;
    }
}
