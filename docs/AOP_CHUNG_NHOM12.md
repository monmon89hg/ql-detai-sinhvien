# AOP dùng chung cho toàn Nhóm 12 — WEPR

## 1. Mục tiêu và phạm vi

Một `@Aspect` duy nhất theo dõi **public method** trên các class `@Service` thuộc `vn.edu.group12.qldt.service..*`:
- TV1: `service.admin`, `service.auth` (tài khoản, xác thực, thông báo).
- TV2: `service.detai` (đề tài, đợt đăng ký, phân công giảng viên).
- TV3: `service.nhom` (nhóm, đăng ký, báo cáo PDF/R2).
- TV4: `service.hoidong` (hội đồng, chấm điểm, kết quả, email).

AOP chỉ đo thời gian và ghi log, **không phải** kiểm soát quyền, không thay thế `@Transactional` hoặc `@PreAuthorize`, không kiểm tra điều kiện nghiệp vụ, và **không thay đổi CSDL**. Không cần migration.

## 2. Dòng chảy và các khái niệm cần vấn đáp

1. Controller gọi một Bean `@Service` do Spring quản lý.
2. Spring AOP tạo proxy, Advice `@Around` chạy, lưu `System.nanoTime()`.
3. `joinPoint.proceed()` thực thi phương thức gốc **đúng một lần** (cùng tham số).
4. Thành công: trả nguyên object kết quả và ghi `status=SUCCESS` hoặc `SLOW` nếu quá ngưỡng.
5. Lỗi: ghi **chỉ loại exception** và thời gian, ném lại chính exception gốc. Rollback/HTTP error vẫn do mã cũ quyết định.

- **Aspect:** `ServiceExecutionAspect` chứa logic ghi thời gian.
- **Join point:** điểm thực thi một phương thức Service được Spring proxy.
- **Pointcut:** `execution(public * vn.edu.group12.qldt.service..*.*(..)) && @within(org.springframework.stereotype.Service)`; không hard-code theo TV3.
- **Advice:** `@Around` — ôm trước/sau lời gọi.
- **Proceed:** chạy method thực sự.
- **Proxy:** đối tượng trung gian Spring tạo, chỉ bắt được lời gọi đi qua proxy.

> Giới hạn: gọi nội bộ `this.method()` trong cùng class sẽ không đi qua Spring proxy; `private`, `static`, `final` method không được giám sát theo cơ chế này. Đây là **Spring AOP proxy**, không phải AspectJ bytecode weaving. Những tác vụ tự kích hoạt bên trong Bean cũng có thể không đi qua proxy.

## 3. Bảo mật và vận hành

Log mẫu:

```
AOP_SERVICE method=NhomService.tao(..) status=SUCCESS durationMs=4.125
AOP_SERVICE method=BaoCaoService.nop(..) status=ERROR durationMs=2.842 errorType=NghiepVuNhomException
AOP_SERVICE method=DeTaiService.filterDeTai(..) status=SLOW durationMs=1234.335
```

**Không ghi** giá trị tham số, giá trị trả về, mật khẩu, session, token, tên file gốc, nội dung PDF, email, mã sinh viên hoặc `exception.getMessage()`. `status=ERROR` chỉ là nhận diện lời gọi ném exception; không khẳng định log là hệ thống audit nghiệp vụ đã lưu thành công. `status=SLOW` là lời gọi **thành công nhưng chậm**.

Mặc định AOP bật và log một dòng INFO cho mỗi lời gọi được bắt. Trước khi bật production với lưu lượng lớn, cân nhắc tắt hoặc cấu hình logging để tránh quá nhiều bản ghi; không nên dùng làm benchmark hiệu năng chuẩn.

Biến môi trường tùy chọn (không phải secret):

```
AOP_SERVICE_LOG_ENABLED=true
AOP_SERVICE_SLOW_MS=1000
```

Nếu tắt: `AOP_SERVICE_LOG_ENABLED=false`. Spring tự bỏ Bean Aspect, các Service hoạt động bình thường.

## 4. Test và demo sau khi áp dụng patch

Tạo nhánh trên `develop` mới nhất, áp dụng patch, chạy:

```powershell
cd D:\ql-detai-sinhvien
sh backend/mvnw -B -ntp -f backend/pom.xml -Dtest=ServiceExecutionAspectTests,ServiceExecutionAspectDisabledTests test
```

Trên Windows PowerShell nếu không có `sh`, dùng:

```powershell
.\backend\mvnw.cmd -B -ntp -f backend\pom.xml '-Dtest=ServiceExecutionAspectTests,ServiceExecutionAspectDisabledTests' test
```

Sau đó chạy toàn bộ CI-compatible test:

```powershell
.\backend\mvnw.cmd -B -ntp -f backend\pom.xml clean verify
```

MySQL integration CI phải chạy trên database thử nghiệm riêng qua profile `-Pci-mysql`; không trỏ vào Railway production và không cần kết nối R2 thật. Để demo AOP, chạy app trong staging/test environment và thực hiện thao tác tạo nhóm, tìm đề tài hoặc xem hội đồng. Ở logs app, tìm `AOP_SERVICE`.

Kiểm tra đủ: (1) test proxy thật PASS; (2) test trả kết quả/throw exception gốc PASS; (3) pointcut khớp 4 phân hệ PASS; (4) test tắt AOP PASS; (5) CI build/test MySQL không regress; (6) nếu deploy staging, HTTP/CSRF/role vẫn đúng. Không deploy thẳng production khi chưa nghiệm thu.

## 5. Phân biệt kết quả và mức bằng chứng

- File này mô tả **bản triển khai mới được đề xuất**, không khẳng định nó đã chạy trên Render.
- Unit test kiểm tra AOP ở môi trường Spring độc lập, không cần MySQL/R2.
- Chỉ ghi “hoàn thành” trong Word sau khi build, unit test và CI kiểm tra thành công trên branch đã merge.

## 6. Phần trình bày 30–45 giây với thầy

“Nhóm em triển khai một Aspect dùng chung cho các Service của bốn phân hệ. Pointcut chọn các public method của lớp được đánh dấu `@Service`. Advice `@Around` dùng `System.nanoTime()` để đo thời gian thực thi, gọi `proceed()` đúng một lần, rồi ghi SUCCESS, SLOW hoặc ERROR. Khi có exception, Aspect ném lại nguyên exception, nên quy tắc nghiệp vụ, transaction và xử lý mã lỗi HTTP cũ không thay đổi. Log không chứa mật khẩu, token, nội dung file hay dữ liệu cá nhân. Nhóm có kiểm thử proxy thực sự, lỗi, bảo mật log và cấu hình tắt AOP.”

## 7. Câu hỏi có thể bị hỏi xoáy

- **Tại sao không để log trong từng Service?** Vì sẽ trùng code và khó áp dụng thống nhất cho 4 phân hệ. AOP tách việc theo dõi thời gian khỏi nghiệp vụ.
- **AOP có thay Spring Security không?** Không; quyền vẫn do FilterChain, `@PreAuthorize` và kiểm tra quyền tại Service đảm nhận.
- **AOP có tự rollback MySQL/R2 không?** Không; `@Transactional` xử lý transaction MySQL, R2 bù trừ như code TV3. Aspect chỉ ghi log rồi ném lại exception.
- **AOP có bắt được mọi method trong hệ thống không?** Không, chỉ public method của Bean `@Service` ở package đã chỉ định, và chỉ khi lời gọi đi qua proxy Spring.
- **Tại sao không log giá trị input và exception message?** Vì dễ lộ mật khẩu, PDF, dữ liệu người dùng và token; chỉ log method name, loại lỗi và thời gian.
- **`@Around` với `@After` khác nhau thế nào?** `@Around` chủ động gọi `proceed()` để bao thời gian trước/sau method; `@After` chỉ chạy sau khi join point kết thúc.
- **Vì sao không có commit thẳng develop?** Vì AOP là thay đổi chung có rủi ro tác động đến tất cả Service; cần nhánh + PR + CI nghiệm thu trước.

## 8. Tài liệu đối chiếu

- Repo `monmon89hg/ql-detai-sinhvien`, nhánh `develop`, mốc commit `f2c84ca27b94e7bc10855e31017b02d24ef8ac5d` (24/10/2026 theo giờ VN).
- Spring Boot 4.0 Migration Guide — `spring-boot-starter-aop` đổi tên thành `spring-boot-starter-aspectj`.
- Spring Framework Reference — AspectJ-style `@Around` và Spring AOP Proxy.
