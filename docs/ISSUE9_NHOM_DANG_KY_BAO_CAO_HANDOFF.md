# Bàn giao Nhóm sinh viên, đăng ký đề tài và báo cáo — issue #9

Ngày kiểm chứng local: **22/10/2026**. Base `develop`: `d83b4c68f01bbc17c42cf84cae1fd234e5bc7ed7`.
Nhánh: `feat/nhom-dang-ky-bao-cao`. Liên kết nghiệp vụ: **Refs #9**.

**Bản Codex trước tái cấu trúc** đã báo cáo kiểm thử local thành công; bản đổi tên package/class/test/CI theo quy ước nhóm **chưa được chạy lại MySQL/Maven**. Không coi 99 PASS trước refactor là kết quả kiểm chứng của bản hiện tại. Chưa tuyên bố hoàn tất issue #9.
Thiếu cấu hình R2 thật nên chưa xác minh lưu trữ bền vững trên Cloudflare. Render chưa được kiểm thử hoặc triển khai.
PR **chưa tạo được** vì GitHub từ chối quyền ghi (403); bản commit/patch và mô tả PR đã chuẩn bị để người có quyền xuất bản. Không merge hoặc đóng issue.

## 1. Trạng thái và đối chiếu yêu cầu

| Hạng mục của issue #9 | Trạng thái | Bằng chứng |
|---|---|---|
| Tạo nhóm và tự gán trưởng, rollback đồng thời hai bảng | Đã triển khai, PASS local | NHOM-01/02, lỗi chèn trưởng, hai lần tạo đồng thời |
| Tìm/lọc nhóm; gửi, duyệt/từ chối yêu cầu | Đã triển khai, PASS local | SQL có tham số; test Service, MockMvc và HTTP thật |
| Tối đa 5 người, đúng một trưởng, một SV tham gia chính thức một nhóm | Đã triển khai, PASS local | Khóa hàng + unique index V3; NHOM-03/04 |
| Xóa thành viên trước chốt; chốt riêng 3–5 người | Đã triển khai, PASS local | NHOM-05/06; snapshot và lịch sử `KET_THUC` |
| Chọn đề tài công bố đúng đợt/cửa sổ SV, chỉ trưởng đăng ký | Đã triển khai, PASS local | NHOM-05/07; principal và dữ liệu MySQL |
| Một đăng ký/nhóm, giữ lý do từ chối và quyết định duyệt | Đã triển khai, PASS local | NHOM-08; đăng ký đồng thời; GET/POST review |
| PDF, 20 MiB, SHA-256, version và quyền nhóm | Đã triển khai, PASS với adapter thử nghiệm | NHOM-09…14; HTTP multipart, download đúng bytes |
| Lưu/tải PDF bền vững trên R2 thật | Adapter đã triển khai; **bị chặn bởi cấu hình ngoài môi trường** | `R2LiveTests`: SKIPPED, không được tính là PASS |
| Tích hợp tám trang Base UI với Controller/Service/MySQL | Đã triển khai, PASS local | Render Thymeleaf thật, CSRF, validation, flash và phân quyền |
| Kiểm thử toàn bộ, tạo PR review | Local PASS; PR bị chặn bởi quyền ghi GitHub | Kết quả và link PR ở phần 8/11 |
| Regression sau khi owner merge PR #31 | Đã thực hiện, PASS local | Rebase lên `d83b4c6`; chạy lại đủ `clean verify`, 99 PASS/1 SKIP R2 trên commit gốc trước đổi tên |
| Render staging | **Chưa thực hiện** | Checklist phần 10; chưa triển khai |

Không có chức năng nghiệp vụ Nhóm/đăng ký/báo cáo nào được thay bằng mock hoặc phản hồi thành công giả trong production.
Không có thay đổi schema, migration, dữ liệu Railway, cấu hình Render, TV2 hoặc TV4.

## 2. Phạm vi file thay đổi

Danh sách file trong **bản Codex đã được tổ chức lại theo nghiệp vụ** (một số đường dẫn được đổi tên so với commit gốc):

```text
.github/workflows/nhom-mysql.yml
README.md
backend/pom.xml
backend/src/main/java/vn/edu/group12/qldt/config/bao-cao/NhomBaoCaoConfiguration.java
backend/src/main/java/vn/edu/group12/qldt/config/bao-cao/NhomBaoCaoSecurityConfiguration.java
backend/src/main/java/vn/edu/group12/qldt/controller/bao-cao/BaoCaoController.java
backend/src/main/java/vn/edu/group12/qldt/controller/bao-cao/DangKyDeTaiController.java
backend/src/main/java/vn/edu/group12/qldt/controller/bao-cao/NhomController.java
backend/src/main/java/vn/edu/group12/qldt/controller/bao-cao/NhomExceptionHandler.java
backend/src/main/java/vn/edu/group12/qldt/domain/bao-cao/NghiepVuNhomException.java
backend/src/main/java/vn/edu/group12/qldt/domain/bao-cao/NhomBaoCaoModels.java
backend/src/main/java/vn/edu/group12/qldt/dto/bao-cao/TaoNhomForm.java
backend/src/main/java/vn/edu/group12/qldt/repository/bao-cao/NhomDanhMucRepository.java
backend/src/main/java/vn/edu/group12/qldt/repository/bao-cao/NhomBaoCaoRepository.java
backend/src/main/java/vn/edu/group12/qldt/service/bao-cao/BaoCaoService.java
backend/src/main/java/vn/edu/group12/qldt/service/bao-cao/DangKyDeTaiService.java
backend/src/main/java/vn/edu/group12/qldt/service/bao-cao/NhomService.java
backend/src/main/java/vn/edu/group12/qldt/service/bao-cao/PdfValidator.java
backend/src/main/java/vn/edu/group12/qldt/service/bao-cao/QuyenNhomService.java
backend/src/main/java/vn/edu/group12/qldt/service/bao-cao/NhomBaoCaoViewService.java
backend/src/main/java/vn/edu/group12/qldt/storage/bao-cao/R2ReportStorage.java
backend/src/main/java/vn/edu/group12/qldt/storage/bao-cao/ReportStorageService.java
backend/src/main/resources/application.properties
backend/src/test/java/vn/edu/group12/qldt/bao-cao/NhomServiceTests.java
backend/src/test/java/vn/edu/group12/qldt/bao-cao/PdfValidatorTests.java
backend/src/test/java/vn/edu/group12/qldt/bao-cao/R2LiveTests.java
backend/src/test/java/vn/edu/group12/qldt/bao-cao/R2ReportStorageTests.java
backend/src/test/java/vn/edu/group12/qldt/bao-cao/NhomHttpEndToEndTests.java
backend/src/test/java/vn/edu/group12/qldt/bao-cao/NhomMySqlTestBase.java
backend/src/test/java/vn/edu/group12/qldt/bao-cao/NhomMySqlTests.java
backend/src/test/java/vn/edu/group12/qldt/bao-cao/NhomStagingTests.java
backend/src/test/java/vn/edu/group12/qldt/bao-cao/BaoCaoStorageConfigurationTests.java
backend/src/test/java/vn/edu/group12/qldt/bao-cao/NhomTestSupport.java
docs/ISSUE9_NHOM_DANG_KY_BAO_CAO_HANDOFF.md
frontend/templates/fragments/sidebar.html
frontend/templates/groups/list.html
frontend/templates/registrations/list.html
frontend/templates/registrations/review.html
frontend/templates/reports/list.html
frontend/templates/shared/operation-error.html
```

Sidebar chung chỉ thêm điều kiện hiển thị ba liên kết Nhóm/đăng ký/báo cáo theo vai trò; không đổi layout hoặc menu module khác.
`application.properties` chỉ bổ sung giới hạn/lazy parsing multipart.
`pom.xml` thêm AWS SDK S3, PDFBox và agent Mockito cho Java 21.
Workflow Nhóm/đăng ký/báo cáo mới được cấu hình để chạy MySQL 8.4.7 riêng, dùng mật khẩu ngẫu nhiên của từng job; workflow cũ giữ nguyên.

## 3. Kiến trúc và danh tính

- Ba Controller chỉ bind HTTP, ánh xạ Model/DTO, gọi application service, trả view/redirect/bytes. `TaoNhomForm` giới hạn các field được bind.
- `NhomService`, `DangKyDeTaiService`, `BaoCaoService` giữ các bất biến và mutation trong `@Transactional(READ_COMMITTED)`.
- `QuyenNhomService` lấy người dùng từ `SecurityContextHolder`, kiểm tra trạng thái/loại tài khoản/vai trò hiện tại trong MySQL. Không nhận actor từ browser.
- `NhomBaoCaoViewService` là application query service có transaction read-only, chuẩn bị hợp đồng Model của tám template.
- Hai repository dùng `JdbcTemplate`, tham số SQL và khóa tự nhiên. `NhomBaoCaoModels` là các record đọc dữ liệu; không thêm JPA mapping trùng hoặc `Long id` vào V3. Các lệnh JDBC tham gia transaction Spring trên cùng DataSource; rollback đã được kiểm chứng bằng MySQL thật.
- Lỗi nghiệp vụ dùng `NghiepVuNhomException.Loai`, ánh xạ 400/401/403/404/409/503. Lỗi tệp quá lớn trả 413. Lỗi form tạo nhóm được render với BindingResult hoặc thông báo của Base UI.
- `NhomBaoCaoSecurityConfiguration` chỉ tạo filter chain cho ba prefix Nhóm/đăng ký/báo cáo. CSRF luôn bật. Normal staging áp dụng vai trò HTTP; `qa-open` vẫn yêu cầu đăng nhập và mọi quyền ghi nhạy cảm tiếp tục được xác minh trong Service. Không sửa các file security/profile hiện hữu.

Đăng ký chỉ bind `maDeTai`. Nhóm được truy ra từ membership chính thức của principal rồi kiểm tra trưởng nhóm.
Các test gửi thêm `maNhom`, `maNguoiTao`, `maNguoiDangKy` giả đã xác nhận các giá trị này không quyết định actor/nhóm.

## 4. Truy vấn và tương tranh

| Luồng | Khóa/giao dịch | Ràng buộc cuối cùng |
|---|---|---|
| Tạo nhóm | Khóa hàng tài khoản SV, kiểm tra membership hiện tại, chèn nhóm và trưởng cùng transaction | PK nhóm; unique `sv_dang_tham_gia`, `nhom_co_truong` |
| Gửi yêu cầu | Khóa nhóm rồi tài khoản SV | PK `(ma_nhom,ma_sinh_vien)`; không tạo lại lịch sử đã tồn tại |
| Duyệt thành viên | Khóa nhóm → tài khoản SV đích → membership; đếm chính thức sau khóa | Hai nhóm không thể duyệt cùng SV; suất thứ năm được tuần tự hóa |
| Chốt nhóm | Khóa nhóm; xác nhận 3–5 và một trưởng; cập nhật snapshot và từ chối pending | Danh sách bất biến sau chốt |
| Đăng ký/duyệt | Khóa nhóm; đọc topic/round đúng mã V2, `FOR SHARE` khi cần | PK `dang_ky_de_tai.ma_nhom`; chỉ xử lý `CHO_DUYET` |
| Nộp PDF | Khóa nhóm xuyên suốt cấp `MAX(phien_ban)+1`, PUT và chèn metadata | PK `(ma_nhom,phien_ban)` và unique object key |

Không có `findAll()` rồi lọc trong Java ở Nhóm/đăng ký/báo cáo. Lọc keyword/đợt/trạng thái ở SQL.
Danh sách nhóm/đề tài/đăng ký/báo cáo lấy tối đa 21 dòng để trả 20 dòng và xác định trang kế tiếp.
Tra membership hiện tại theo unique generated column; đếm/thành viên theo PK bắt đầu bằng `ma_nhom`.
Thông tin nhóm/trưởng/số lượng được gộp trong một truy vấn danh sách, không query thêm cho từng dòng.
Danh sách lựa chọn đợt giới hạn 200 dòng. Lịch sử nhóm đã kết thúc chỉ đọc; snapshot vẫn quyết định quyền tải.

## 5. Quyết định tích hợp và khác biệt tài liệu/repository

1. Hai file đính kèm đã được đọc đầy đủ cùng issue #9, README, quy ước UI, V1–V5 và PR #28/#30/#31.
2. Khi bắt đầu, working tree của bản clone sạch. Frontend trên remote vẫn có hidden `maNhom`; chỉ bỏ đúng field này. Không có patch local của người dùng trong working tree để ghi đè.
3. PR #28/#30 đã merge trước khi bắt đầu. Trong khi triển khai, owner merge PR #31 tại `d83b4c68f01bbc17c42cf84cae1fd234e5bc7ed7` (head TV2 `2d2cd21c17b4906bfb98072a7bb9905395fe87f4`). Nhánh Nhóm/đăng ký/báo cáo đã rebase lên develop mới và chạy lại toàn bộ regression: 99 PASS, 0 lỗi, 1 SKIP R2. Xung đột duy nhất ở `application.properties` được giải quyết bằng cách giữ nguyên nội dung develop rồi thêm bốn cấu hình multipart Nhóm/đăng ký/báo cáo. Không sửa code TV2 hoặc tự merge PR #31. Các SQL đọc V2 độc lập mapping vẫn được giữ.
4. Contract yêu cầu duyệt đăng ký thành `DA_DUYET`, nhưng không định nghĩa endpoint “bắt đầu thực hiện”. Implementation giữ `DA_DUYET` và chuyển nhóm sang `DANG_THUC_HIEN` khi duyệt; cho phép nộp khi đăng ký `DA_DUYET` hoặc `DANG_THUC_HIEN`, nhóm đã chốt hợp lệ và còn hạn. `DANG_CHAM`, `HOAN_THANH`, đăng ký bị từ chối/chờ duyệt hoặc hạn chưa cấu hình không được nộp. Quy tắc này cần được reviewer xác nhận trước merge.
5. Đăng ký bị từ chối được giữ nguyên. Không có API mở lại, ghi đè hoặc tạo bản thứ hai khi chính sách đăng ký lại chưa được duyệt.
6. Membership đã từ chối/kết thúc được giữ lịch sử; không tái tạo cùng cặp khóa. Người dùng đã kết thúc membership có thể tạo nhóm mới, vẫn tải báo cáo cũ nếu nằm trong snapshot. `/bao-cao` hiển thị hồ sơ gần nhất khi không còn nhóm hoạt động.
7. Schema không cần thay đổi. `ddl-auto=none` giữ nguyên lựa chọn hiện hành; Flyway V1–V5 chạy thật trên database thử. Kết quả kiểm tra Hibernate `validate` riêng sau PR #31 được ghi dưới phần kiểm thử.

## 6. Kiến trúc PDF và tính nhất quán

`ReportStorageService` tách biệt khỏi `R2ReportStorage` (AWS SDK S3 v2). Adapter fake nằm hoàn toàn trong test.
Production không có adapter filesystem tạm. Thiếu một trong bốn cấu hình R2 thì PUT/GET fail closed, không ghi metadata thành công giả.

Pipeline: xác minh leader/trạng thái/hạn → đọc tối đa 20 MiB + 1 byte → kiểm tra tên/MIME/magic → parse PDF cấu trúc nghiêm ngặt bằng PDFBox → SHA-256 bytes → cấp phiên bản dưới khóa nhóm → PUT object riêng → INSERT metadata → commit.

- Khóa mới `bao-cao/<UUID>.pdf`, không dùng đường dẫn/tên tệp từ client.
- MySQL chỉ giữ metadata theo V3, không giữ bytes hoặc URL công khai. SDK không đặt public ACL; không sinh presigned URL hoặc đưa credentials ra HTML/JS.
- GET yêu cầu user hoạt động và là thành viên snapshot, trưởng khoa hoặc giảng viên được phân công đề tài/hội đồng. Kiểm tra key chống traversal, tồn tại object, size và SHA-256 trước khi trả bytes. Response có attachment, `nosniff`, `private, no-store`.
- Phiên bản `THU_HOI` không tải được. Metadata seed thiếu object trả 404 rõ ràng, không xóa hoặc trả tệp rỗng.
- Gửi lặp cùng nội dung với phiên bản cuối chỉ tái sử dụng bản đó sau khi đọc và kiểm chứng object thật. Nếu metadata seed cùng hash nhưng thiếu object, lưu thành phiên bản mới, giữ nguyên bản cũ.
- Callback rollback được đăng ký trước PUT; xóa object mới khi PUT/INSERT/transaction thất bại. PUT đã nhận bytes nhưng mất phản hồi cũng được dọn. Xóa thử lại tối đa ba lần. Không đụng object/metadata cũ.
- Commit không rõ kết quả không tự xóa object có khả năng đã được tham chiếu. Lỗi dọn tệp ghi log object key để quản trị đối soát.

**Giới hạn còn lại:** MySQL và R2 không có transaction phân tán. Tiến trình bị kill cứng sau PUT hoặc R2 liên tục từ chối DELETE có thể để lại object chưa tham chiếu. Cần đối soát có kiểm tra metadata trước khi xóa; không được xóa hàng loạt prefix theo suy đoán. Không thêm outbox/table mới vì không có phê duyệt schema. Test đã chứng minh bù trừ cho các lỗi có thể quan sát, không chứng minh không có orphan khi máy chủ bị mất hoàn toàn.

Cấu hình SDK theo tài liệu [Cloudflare R2 Java](https://developers.cloudflare.com/r2/examples/aws/aws-sdk-java/): endpoint override, region `auto`, path style, tắt chunked encoding; checksum SDK chỉ dùng khi API yêu cầu. PDFBox 3 dùng [API parser/Loader](https://pdfbox.apache.org/3.0/migration.html).

## 7. Chạy lại local an toàn

Yêu cầu Java 21 và một MySQL **8.4** riêng. Tạo schema trống có tên bắt đầu `nhom_test_`, tài khoản thử chỉ có quyền trên schema đó. Không dùng database Railway/QA hoặc import/reset DEMO.
Bộ fixture tạo mã `T...` ngẫu nhiên và không truncate bảng. Các test Nhóm/đăng ký/báo cáo khóa URL vào `127.0.0.1`/`localhost` và `nhom_test_*` trước khi Spring/Flyway khởi tạo DataSource.

PowerShell, từ thư mục repository:

```powershell
$env:NHOM_TEST_DB_URL = "jdbc:mysql://127.0.0.1:3307/nhom_test_local?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Ho_Chi_Minh"
$env:NHOM_TEST_DB_USERNAME = "nhom_test"
$taskDbPassword = Read-Host "Mật khẩu MySQL thử nghiệm riêng" -AsSecureString
$env:NHOM_TEST_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $taskDbPassword).Password
$env:SPRING_DATASOURCE_URL = $env:NHOM_TEST_DB_URL
$env:SPRING_DATASOURCE_USERNAME = $env:NHOM_TEST_DB_USERNAME
$env:SPRING_DATASOURCE_PASSWORD = $env:NHOM_TEST_DB_PASSWORD
$env:SPRING_FLYWAY_ENABLED = "true"
$env:SPRING_JPA_SHOW_SQL = "false"
.\backend\mvnw.cmd -f backend/pom.xml clean verify
```

Biến `SPRING_DATASOURCE_*` cũng cấu hình context test cũ của repo; các test Nhóm/đăng ký/báo cáo tự pin từ `NHOM_TEST_DB_*`.
Nếu thiếu URL thử, lớp MySQL/HTTP Nhóm/đăng ký/báo cáo sẽ skip; không được coi lượt chạy đó là MySQL PASS.
`R2LiveTests` chỉ chạy khi có biến môi trường R2 thật; credential không được đưa vào tham số Maven/log.


Để chạy web local thủ công, dùng datasource local riêng và profile `local,staging` để bật đăng nhập JDBC hiện có. Tài khoản phải hoạt động và có vai trò trong `nguoi_dung_vai_tro`; các DEMO khóa sẵn không phải tài khoản đăng nhập. Production không nạp fake storage từ test: nếu chưa đặt R2 thì upload phải báo 503 như thiết kế.

```powershell
.\backend\mvnw.cmd -f backend/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local,staging"
```

## 8. Lệnh và kết quả kiểm thử

Môi trường chạy: OpenJDK **21.0.12**, Maven Wrapper **3.9.16**, Spring Boot **4.1.1**, MySQL Community Server **8.4.7** trên loopback, schema `nhom_test_20261022` riêng. Flyway xác nhận đủ **5 migration thành công**.
Đã chạy lại sau khi rebase lên PR #31 đã merge: cùng kết quả 100 test (99 PASS, 1 SKIP).
SDK `2.57.2`, PDFBox `3.0.8`. Không dùng H2 hoặc database mock để mô tả kết quả MySQL.

Lệnh Maven regression để chạy lại trên Windows (kết quả kiểm thử đã ghi phía dưới là kết quả trước khi đổi tên source):

```bash
.\backend\mvnw.cmd -f backend/pom.xml clean verify
```

Wrapper local khởi động MySQL riêng, sinh credential ngẫu nhiên và đặt các biến ở phần 7 trước lệnh này; dừng MySQL sau khi test. Không sửa cấu hình/secret của dự án để phục vụ runtime đó.

| Bộ test | PASS | FAIL/ERROR | SKIP |
|---|---:|---:|---:|
| `NhomServiceTests` | 5 | 0 | 0 |
| `PdfValidatorTests` | 9 | 0 | 0 |
| `BaoCaoStorageConfigurationTests` | 2 | 0 | 0 |
| `R2ReportStorageTests` — SDK thật, máy chủ S3 giả lập | 2 | 0 | 0 |
| `NhomMySqlTests` — Service/repository/MockMvc/concurrency, MySQL thật | 36 | 0 | 0 |
| `NhomStagingTests` — phân quyền staging, MySQL thật | 4 | 0 | 0 |
| `NhomHttpEndToEndTests` — HTTP thật qua Tomcat và MySQL | 1 | 0 | 0 |
| `R2LiveTests` — Cloudflare thật | 0 | 0 | **1** |
| Các test sẵn có của repository | 40 | 0 | 0 |
| **Tổng (Codex trước refactor)** | **99** | **0** | **1** |

Surefire: **Tests run: 100, Failures: 0, Errors: 0, Skipped: 1 — BUILD SUCCESS**.
`clean verify` cũng đã compile, đóng gói và repackage Spring Boot JAR thành công.

Kiểm tra schema riêng sau khi PR #31 merge:

```bash
.\backend\mvnw.cmd -f backend/pom.xml "-Dtest=QldtApplicationTests" "-Dspring.jpa.hibernate.ddl-auto=validate" clean test
```

**1 PASS, 0 FAIL/ERROR, 0 SKIP** trên cùng MySQL 8.4.7/Flyway V1–V5. Lượt validate không clean đầu tiên gặp class TV2 cũ từ build trước rebase (`missing column id`); đã đối chiếu source với develop, kiểm tra class bằng `javap`, clean và chạy lại thành công. Không sửa Entity TV2 để xử lý lỗi build cũ.

`git diff --check`: PASS.

Các lượt chạy ban đầu đã phát hiện và xử lý: API strict PDFBox (`parse(false)`), Mockito cần javaagent trên runtime Java 21, và key fixture seed phải duy nhất giữa nhiều lượt test. Không bỏ test lỗi hoặc thay MySQL bằng mock để có kết quả cuối.

NHOM-01…14 đều có test đặt tên tương ứng trong `NhomMySqlTests`. Bao phủ thêm rollback sau chèn nhóm, quyền DB thay cho role giả từ client, upload bù trừ, gửi lặp PDF, lịch sử nhóm kết thúc, key traversal, object sai hash và tất cả POST thiếu/sai CSRF.

## 9. Kết quả local qua HTTP thật

`NhomHttpEndToEndTests` khởi động application thật trên Tomcat cổng ngẫu nhiên với profile `staging`; không dùng MockMvc cho lượt này. HTTP client giữ cookie, đăng nhập BCrypt qua `/login`, lấy CSRF từ HTML và gửi form/multipart.

Đã xác minh:

1. Trưởng tạo nhóm, SQL đọc lại group và membership trưởng đã commit.
2. Hai tài khoản SV khác gửi yêu cầu; trưởng duyệt; SQL xác nhận ba membership chính thức.
3. Chốt nhóm; SQL xác nhận ba cờ snapshot.
4. Thành viên thường bị 403 khi đăng ký; trưởng đăng ký dù gửi kèm group giả; SQL xác nhận actor/nhóm đúng.
5. Trưởng khoa đăng nhập duyệt; SQL đọc lại `DA_DUYET`.
6. Gửi PDF thật bằng multipart có hidden CSRF, SQL metadata và bytes ở adapter khớp SHA-256.
7. Thành viên snapshot tải đúng PDF; người ngoài bị 403; member không được nộp thay trưởng.
8. TXT giả PDF bị 400, thiếu CSRF bị 403, file 20 MiB + 1 bị 413 với lỗi tiếng Việt; không phát sinh metadata mới.
9. Mô phỏng thiếu object: HTTP 404, metadata vẫn giữ.

Test không dùng session hoặc dữ liệu Railway. Adapter ở lượt HTTP là fake lưu bytes trong bộ nhớ của tiến trình thử, không phải xác nhận durable storage trên Render/R2.

## 10. Biến Render và checklist staging sau review/merge

| Biến | Yêu cầu |
|---|---|
| `R2_ENDPOINT` | Endpoint HTTPS S3 API của account R2 hiện có |
| `R2_BUCKET` | Bucket private hiện có, không bật public access |
| `R2_ACCESS_KEY_ID` | Secret phía server, chỉ quyền cần thiết trên bucket |
| `R2_SECRET_ACCESS_KEY` | Secret phía server, không log/commit |
| `R2_REGION` | Không bắt buộc, mặc định `auto` |

Giữ các biến database/profile Render hiện có; nhánh này không thay secret hoặc cấu hình triển khai.
Chủ dự án chỉ deploy sau review và merge được phê duyệt. Dùng tài khoản QA riêng có vai trò DB đúng và nhóm thử mới, không reset DEMO.

| Route | Thao tác và điều kiện đạt |
|---|---|
| `/nhom` | GET 200, header/breadcrumb thật; lọc từ khóa/đợt/trạng thái, chuyển trang không mất bộ lọc; outsider vẫn phải đăng nhập |
| `/nhom/tao` | Tạo nhóm bằng SV còn trống; SQL có đúng group + trưởng; gửi lặp không có group mồ côi; đổi actor ẩn không tác dụng |
| `/nhom/cua-toi` | SV khác gửi yêu cầu; trưởng duyệt/từ chối; SV thường/nhóm khác bị cấm; thử suất cuối đồng thời; nhóm 2 người không chốt, 3–5 chốt được; sau chốt không sửa |
| `/dang-ky-de-tai` | Chỉ đề tài công bố trong đợt/cửa sổ hợp lệ; HTML chỉ gửi `maDeTai`; member không đăng ký; trưởng gửi một lần; thấy trạng thái/lý do từ chối; retry không sinh bản thứ hai |
| `/dang-ky-de-tai/duyet` | Chỉ trưởng khoa duyệt/từ chối có lý do; SQL actor/timestamp chính xác; dùng SV ở `qa-open` vẫn không được mutation; kiểm tra thêm profile `staging` thông thường |
| `/bao-cao` | Hiển thị các phiên bản cũ và phiên bản mới; member đúng snapshot tải được; SV khác đoán URL bị cấm; GV có/không có phân công được phân biệt; seed thiếu blob báo 404 và không mất metadata |
| `/bao-cao/nop` | Trưởng có đăng ký duyệt và còn hạn; PDF thật, 0 byte/TXT giả/20 MiB + 1; SHA-256 tải lại giống bytes gốc; hai upload khác nhau có version khác nhau; storage lỗi không tạo metadata giả |

Trước khi xác nhận PDF hoàn thành: chạy `R2LiveTests` với bốn biến thật trên bucket hiện có, xác minh PUT/GET/hash/DELETE của object test; xác nhận bucket private. Sau đó kiểm tra PDF qua ứng dụng, restart/redeploy Render và tải lại cùng version để chứng minh độ bền. Kiểm tra CSRF thiếu/sai trên cả hai profile, đối chiếu rows MySQL và giám sát lỗi dọn object. Ghi lại kết quả thật; checklist này hiện **chưa PASS staging**.

## 11. PR và rủi ro chưa đóng

**PR URL: chưa có.** Git CLI báo không có thông tin đăng nhập; GitHub API `create_tree` trả **403 Resource not accessible by integration**, kể cả khi loại workflow khỏi yêu cầu. Kết nối chỉ có installation cho `y0unglewis`, chưa có quyền ghi repo đích `monmon89hg/ql-detai-sinhvien`. Không có branch/PR mới được xuất bản lên GitHub.

Bản bàn giao gồm Git patch, Git bundle của riêng commit Nhóm/đăng ký/báo cáo (base prerequisite `d83b4c6`), hướng dẫn áp dụng và `PR_BODY.md` có `Refs #9`. Người có quyền ghi repo cần push nhánh hoặc cấp kết nối quyền ghi repo đích. Sau khi push, tạo **draft PR vào develop**, không merge. Mô tả và checklist review đã chuẩn bị đầy đủ.

- **R2 thật:** chưa có credential trong môi trường làm việc; test cloud bị skip. Không xác nhận upload/download bền vững đã hoàn tất.
- **Render:** chưa deploy/test; phải chờ owner review/merge, không tự merge hoặc đóng #9.
- **PR #31:** owner đã merge; nhánh đã cập nhật và regression đầy đủ PASS. Không còn blocker tích hợp TV2 tại base bàn giao.
- **Chính sách lifecycle:** reviewer cần xác nhận hành vi `DA_DUYET`/bắt đầu thực hiện và giữ bản đăng ký bị từ chối như phần 5.
- **Hai hệ lưu trữ:** bù trừ có kiểm thử, nhưng crash cứng/DELETE liên tục thất bại cần đối soát; không có cam kết atomic commit phân tán.
- **GitHub/CI:** quyền ghi repo bị từ chối; chưa push/tạo PR, workflow Nhóm/đăng ký/báo cáo chưa chạy trên GitHub. Kết quả local không được gọi là CI PASS.


## Ghi chú tái cấu trúc sau bàn giao

Tên package, class, workflow, template lỗi và nhánh đã được đổi từ nhãn phân công thành tên nghiệp vụ `nhom`, khớp cách tổ chức theo tầng và domain `detai`/`hoidong` trong repository. URL nghiệp vụ và schema MySQL không đổi. Các bài test cần chạy lại sau khi áp dụng refactor; không sử dụng kết quả Codex cũ để xác nhận bản refactor PASS.
