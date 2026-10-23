package vn.edu.group12.qldt.service.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.TestPropertySource;

import vn.edu.group12.qldt.aop.ServiceExecutionAspect;
import vn.edu.group12.qldt.service.admin.NguoiDungService;
import vn.edu.group12.qldt.service.detai.DeTaiService;
import vn.edu.group12.qldt.service.hoidong.HoiDongService;
import vn.edu.group12.qldt.service.nhom.NhomService;

/** Kiểm thử AOP độc lập, không cần chạy MySQL, R2, SMTP hoặc server HTTP. */
@SpringJUnitConfig(classes = ServiceExecutionAspectTests.CauHinh.class)
@TestPropertySource(properties = "app.aop.service.slow-threshold-ms=60000")
@ExtendWith(OutputCaptureExtension.class)
class ServiceExecutionAspectTests {

    @org.springframework.beans.factory.annotation.Autowired
    private NghiepVuGiaLap service;

    @Test
    void aspectCanThiepDuocMethodServiceVaKhongLoDuLieu(CapturedOutput output) {
        assertThat(AopUtils.isAopProxy(service)).isTrue();
        // SpringJUnitConfig không khởi chạy hệ thống cấu hình logging của Spring Boot.
        // Vì vậy, bật INFO trực tiếp trên logger của Aspect trong phạm vi phép thử.
        ch.qos.logback.classic.Logger aspectLogger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ServiceExecutionAspect.class);
        Level previousLevel = aspectLogger.getLevel();
        try {
            aspectLogger.setLevel(Level.INFO);
            String secret = "matKhau=KHONG_DUOC_GHI_LOG";
            assertThat(service.xuLy(secret)).isEqualTo(secret);
            assertThat(output.getAll())
                    .contains("AOP_SERVICE", "NghiepVuGiaLap.xuLy", "status=SUCCESS")
                    .doesNotContain(secret);
        } finally {
            // Trả lại mức log trước test, tránh ảnh hưởng tới các test khác.
            aspectLogger.setLevel(previousLevel);
        }
    }

    @Test
    void aspectGiuNguyenExceptionGocVaKhongLogNoiDungNhayCam(CapturedOutput output) {
        IllegalStateException original = new IllegalStateException("THONG_TIN_RIENG_TU");
        assertThatThrownBy(() -> service.gayLoi(original)).isSameAs(original);
        assertThat(output.getAll())
                .contains("AOP_SERVICE", "status=ERROR", "errorType=IllegalStateException")
                .doesNotContain("THONG_TIN_RIENG_TU");
    }

    @Test
    void pointcutBaoPhuBonPhanHeTV1TV2TV3TV4() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(ServiceExecutionAspect.SERVICE_METHODS);
        for (Class<?> type : List.of(
                NguoiDungService.class, DeTaiService.class, NhomService.class, HoiDongService.class)) {
            assertThat(Arrays.stream(type.getDeclaredMethods())
                            .filter(m -> Modifier.isPublic(m.getModifiers()))
                            .anyMatch(m -> pointcut.matches(m, type)))
                    .as("Pointcut phải khớp Service thuộc " + type.getPackageName())
                    .isTrue();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Import(ServiceExecutionAspect.class)
    static class CauHinh {
        @Bean
        NghiepVuGiaLap nghiepVuGiaLap() {
            return new NghiepVuGiaLap();
        }
    }

    @Service
    static class NghiepVuGiaLap {
        public String xuLy(String value) {
            return value;
        }

        public void gayLoi(RuntimeException original) {
            throw original;
        }
    }
}
