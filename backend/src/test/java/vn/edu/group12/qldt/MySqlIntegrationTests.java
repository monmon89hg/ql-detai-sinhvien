package vn.edu.group12.qldt;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import vn.edu.group12.qldt.entity.admin.BoMon;
import vn.edu.group12.qldt.repository.admin.BoMonRepository;
import vn.edu.group12.qldt.service.auth.QldtUserDetails;
import vn.edu.group12.qldt.support.CiDatabaseGuard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("mysql")
@SpringBootTest
@AutoConfigureMockMvc(print = org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint.NONE)
@ActiveProfiles({"ci-mysql", "staging"})
@ContextConfiguration(initializers = CiDatabaseGuard.class)
class MySqlIntegrationTests {

    @Autowired private DataSource dataSource;
    @Autowired private Flyway flyway;
    @Autowired private EntityManager entityManager;
    @Autowired private BoMonRepository boMonRepository;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PasswordEncoder encoder;
    @Autowired private MockMvc mvc;

    @Test
    void contextUsesRealMySql84AndDedicatedSchema() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("MySQL");
            assertThat(connection.getMetaData().getDatabaseProductVersion()).startsWith("8.4.");
            assertThat(connection.getCatalog()).isEqualTo("qldt_ci");
        }
    }

    @Test
    void flywayValidatesAllMigrationsIncludingV1ThroughV6() {
        flyway.validate();
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(Arrays.stream(flyway.info().applied())
                .map(migration -> migration.getVersion().toString()).toList())
                .contains("1", "2", "3", "4", "5", "6");
    }

    @Test
    void everyJpaEntityCanReadItsMappedColumns() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                entityManager.getMetamodel().getEntities().forEach(entity ->
                        entityManager.createQuery("select e from " + entity.getName() + " e")
                                .setMaxResults(1).getResultList()));
    }

    @Test
    void repositoryWriteRollsBackWhenTransactionFails() {
        String code = code();
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> {
                    BoMon department = new BoMon();
                    department.setMaBoMon(code);
                    department.setTenBoMon("Bộ môn kiểm thử rollback");
                    boMonRepository.saveAndFlush(department);
                    assertThat(boMonRepository.existsById(code)).isTrue();
                    throw new IllegalStateException("Chủ động hủy giao dịch kiểm thử");
                })).isInstanceOf(IllegalStateException.class)
                .hasMessage("Chủ động hủy giao dịch kiểm thử");
        assertThat(boMonRepository.existsById(code)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "SINH_VIEN", "GIANG_VIEN"})
    @Transactional
    void databaseLoginPreservesIdentityRolesCsrfAndLogout(String role) throws Exception {
        String userCode = code();
        String email = userCode + "@ci.invalid";
        String password = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO nguoi_dung
                    (ma_nguoi_dung, ho_ten, email, mat_khau_hash, loai_nguoi_dung, trang_thai)
                VALUES (?, ?, ?, ?, ?, 'HOAT_DONG')
                """, userCode, "Tài khoản kiểm thử CI", email, encoder.encode(password),
                "SINH_VIEN".equals(role) ? "SV" : "GIANG_VIEN".equals(role) ? "GV" : "CAN_BO");
        jdbc.update("INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro) VALUES (?, ?)",
                userCode, role);
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/login").with(csrf())
                        .param("email", email).param("matKhau", password))
                .andExpect(authenticated().withUsername(email)).andExpect(redirectedUrl("/dashboard"))
                .andReturn().getRequest().getSession(false);
        SecurityContext security = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        QldtUserDetails principal = (QldtUserDetails) security.getAuthentication().getPrincipal();
        assertThat(principal.getMaNguoiDung()).isEqualTo(userCode);
        assertThat(principal.getPassword()).isNull();
        mvc.perform(get("/dashboard").session(session)).andExpect(status().isOk());

        String departmentCode = code();
        mvc.perform(post("/admin/bo-mon").session(session)
                        .param("maBoMon", departmentCode).param("tenBoMon", "Bộ môn kiểm thử CI"))
                .andExpect(status().isForbidden());
        var write = post("/admin/bo-mon").session(session).with(csrf())
                .param("maBoMon", departmentCode).param("tenBoMon", "Bộ môn kiểm thử CI");
        if ("ADMIN".equals(role)) {
            mvc.perform(write).andExpect(redirectedUrl("/admin/bo-mon"));
            assertThat(boMonRepository.existsById(departmentCode)).isTrue();
        } else {
            mvc.perform(get("/admin/bo-mon/new").session(session)).andExpect(status().isForbidden());
            mvc.perform(write).andExpect(status().isForbidden());
            assertThat(boMonRepository.existsById(departmentCode)).isFalse();
        }
        mvc.perform(post("/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?logout"));
        assertThat(session.isInvalid()).isTrue();
    }

    private String code() {
        return ("CI_" + UUID.randomUUID().toString().substring(0, 12)).toUpperCase(Locale.ROOT);
    }
}
