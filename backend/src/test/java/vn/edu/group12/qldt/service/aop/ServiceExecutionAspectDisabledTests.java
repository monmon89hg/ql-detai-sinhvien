package vn.edu.group12.qldt.service.aop;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import vn.edu.group12.qldt.aop.ServiceExecutionAspect;

/** Kiểm tra có thể tắt riêng AOP bằng cấu hình mà không ảnh hưởng Service. */
@SpringJUnitConfig(classes = ServiceExecutionAspectDisabledTests.CauHinh.class)
@TestPropertySource(properties = "app.aop.service.enabled=false")
class ServiceExecutionAspectDisabledTests {
    @Autowired
    private ServiceExecutionAspectTests.NghiepVuGiaLap service;

    @Test
    void tatAopServiceVanTraVeKetQuaGoc() {
        assertThat(AopUtils.isAopProxy(service)).isFalse();
        assertThat(service.xuLy("OK")).isEqualTo("OK");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Import(ServiceExecutionAspect.class)
    static class CauHinh {
        @Bean
        ServiceExecutionAspectTests.NghiepVuGiaLap nghiepVuGiaLap() {
            return new ServiceExecutionAspectTests.NghiepVuGiaLap();
        }
    }
}
