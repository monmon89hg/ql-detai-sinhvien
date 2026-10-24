package vn.edu.group12.qldt.aop;

import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ghi nhận thời gian chạy của các Service dùng chung cho cả bốn phân hệ.
 *
 * <p>Không ghi tham số, giá trị trả về, thông điệp exception, token, email,
 * nội dung PDF hoặc dữ liệu nghiệp vụ vào nhật ký.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@ConditionalOnProperty(prefix = "app.aop.service", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServiceExecutionAspect {
    public static final String SERVICE_METHODS =
            "execution(public * vn.edu.group12.qldt.service..*.*(..)) "
                    + "&& @within(org.springframework.stereotype.Service)";

    private static final Logger log = LoggerFactory.getLogger(ServiceExecutionAspect.class);
    private final long slowThresholdMs;

    public ServiceExecutionAspect(
            @Value("${app.aop.service.slow-threshold-ms:1000}") long slowThresholdMs) {
        this.slowThresholdMs = Math.max(0, slowThresholdMs);
    }

    @Around(SERVICE_METHODS)
    public Object ghiNhanThoiGian(ProceedingJoinPoint joinPoint) throws Throwable {
        final long started = System.nanoTime();
        // Chỉ dùng tên lớp/phương thức, tuyệt đối không nối tham số hay giá trị trả về.
        final String method = joinPoint.getSignature().toShortString();
        try {
            // Gọi đúng một lần và trả nguyên kết quả của nghiệp vụ ban đầu.
            Object result = joinPoint.proceed();
            long elapsedNs = System.nanoTime() - started;
            logSuccess(method, elapsedNs);
            return result;
        } catch (Throwable error) {
            long elapsedNs = System.nanoTime() - started;
            log.warn("AOP_SERVICE method={} status=ERROR durationMs={} errorType={}",
                    method, toMillis(elapsedNs), error.getClass().getSimpleName());
            // Bắt buộc ném lại chính exception gốc để giữ nguyên rollback và mã lỗi HTTP.
            throw error;
        }
    }

    private void logSuccess(String method, long elapsedNs) {
        double elapsedMs = toMillis(elapsedNs);
        if (elapsedMs >= slowThresholdMs) {
            log.warn("AOP_SERVICE method={} status=SLOW durationMs={}", method, elapsedMs);
        } else {
            log.info("AOP_SERVICE method={} status=SUCCESS durationMs={}", method, elapsedMs);
        }
    }

    private static double toMillis(long elapsedNs) {
        // Làm tròn đến microsecond để các lời gọi rất nhanh không chỉ hiện 0 ms.
        return TimeUnit.NANOSECONDS.toMicros(elapsedNs) / 1000.0;
    }
}
