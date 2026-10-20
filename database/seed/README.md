# Bộ dữ liệu demo CSDL 2.0 — Nhóm 12

**Database-ready trong môi trường QA local; chưa website-feature-ready.**

Snapshot cố định `2026-10-20 12:00:00`, nghiệp vụ theo `Asia/Ho_Chi_Minh`.
Bộ mẫu có **257 dòng demo + 5 vai trò V5 = 262 dòng, đủ 17 bảng**.
Xem [bàn giao và ma trận kịch bản](HANDOFF_VI.md), [minh chứng kiểm thử](test_results.json).

## Phạm vi và căn cứ

Đã đọc toàn bộ V1–V5 và seed cũ ở commit `4897835a58e8b48fa656f4c8bcca14cf8b68c124`;
đối chiếu `CSDL 2.0.pdf` (21 trang) và `DAC_TA_YEU_CAU_FR_NFR_NHOM12.md`
(bản ghi phiên bản 11/10/2026). Hai tài liệu thiết kế được cung cấp trong tài liệu dự án,
không nằm trong repository. Không sử dụng thiết kế cũ với khóa `id AUTO_INCREMENT`.

Chỉ sửa `database/seed/`. Không sửa migration, Entity/Service/Controller, bảo mật hay cấu hình ứng dụng.
Seed không được Flyway hoặc Spring Boot tự chạy. Các mã cũ được giữ nguyên.

Mã hiện tại chỉ có `QldtApplication.java`, chưa có nghiệp vụ đăng nhập, nhóm,
báo cáo, hội đồng, chấm điểm, kết quả hay storage. **Có dữ liệu không chứng minh website chạy được.**

## Chuẩn bị local

Cần MySQL **8.4.x**, Python 3.10+, và tài khoản MySQL local có quyền phù hợp.
`seed` cần SELECT/INSERT/CREATE TEMPORARY TABLES; `test` còn cần CREATE DATABASE
và UPDATE trong các schema QA mới. Giữ FK/CHECK và strict SQL mode hoạt động.

Từ thư mục gốc repo:

```bash
python3 -m venv database/seed/.venv
```

Kích hoạt venv theo hệ điều hành, sau đó:

```bash
python -m pip install -r database/seed/requirements.txt
python database/seed/fixture_tools.py check
```

Helper hỏi mật khẩu MySQL bằng `getpass`; không truyền mật khẩu trong đối số, không commit vào file.
Chỉ khi cần chạy không tương tác mới cấp `DEMO_DB_PASSWORD` qua cơ chế bí mật của môi trường,
rồi xóa khỏi phiên. Không dùng thông tin Railway. Helper từ chối mọi host ngoài loopback.

## PDF và kho fixture local

Repo **chưa có kiến trúc upload/storage hoặc download** để ánh xạ `object_key`.
Prefix dưới đây là hợp đồng **QA local riêng**, không phải đường dẫn ứng dụng đã triển khai:

```text
<private-fixture-root>/demo-fixtures/v1/<ma_nhom>/v<phien_ban>.pdf
```

Sáu PDF trong `fixtures/` có nội dung hư cấu, ghi rõ DEMONSTRATION DATA ONLY, nhúng font để đọc nhất quán;
`manifest.json` lưu tên file, người nộp, phiên bản, mốc nộp, MIME, số byte và SHA-256 thật.
File mẫu trong repo public không chứa tài liệu học vụ thật. Không dùng URL GitHub làm URL tải báo cáo.

Chọn một **đường dẫn tuyệt đối bên ngoài repo và webroot** rồi provision:

```bash
python database/seed/fixture_tools.py provision --fixture-root /absolute/private/qldt-fixtures
python database/seed/fixture_tools.py check --fixture-root /absolute/private/qldt-fixtures
```

Windows có thể dùng đường dẫn như `D:/qldt-private-fixtures`.
Provision chỉ tạo file chưa tồn tại, chấp nhận file trùng checksum; từ chối ghi đè file khác nội dung.
File/đường dẫn symlink bên trong cây object bị từ chối. Trên POSIX, thư mục mới dùng mode 0700,
file mới 0600. Giữ thư mục này ngoài nơi web server phục vụ file tĩnh.

Có thể chèn `nop_bao_cao` **ngay trong DB QA local** sau khi helper xác minh đủ file trong kho này.
Đây là liên kết DB–fixture có kiểm tra byte thực, **chưa phải báo cáo tải được từ website**.
**Chưa chèn vào DB ứng dụng/Railway** cho tới khi có adapter private storage và tải có phân quyền.
Không dùng cờ giả để vượt gate file.

Tái sinh byte xác định với đúng phiên bản dependency đã pin:

```bash
python database/seed/fixture_tools.py generate
git diff --exit-code -- database/seed/fixtures
```

Nếu đổi nội dung/generator có chủ đích, phải cập nhật cùng lúc manifest, metadata trong SQL,
digest ở gate SQL và kiểm thử. Nếu kho fixture cũ có byte khác, dùng một fixture root mới;
provision không ghi đè. Không tái sinh hash tài khoản khi chạy seed.

## Kiểm thử từ schema mới — cách khuyến nghị

Có MySQL 8.4 local đang chạy, thay port/user và fixture root cho đúng máy:

```bash
python database/seed/demo_seed.py test --host 127.0.0.1 --port 3306 --user root --fixture-root /absolute/private/qldt-fixtures --report /absolute/path/test-results.json
```

Lệnh **không nhận schema hiện có**. Nó tạo các schema `qldt_demo_qa_*_<random>` chưa tồn tại,
áp dụng nguyên văn năm SQL V1–V5 theo thứ tự, nạp seed, kiểm tra, chạy lại và chạy negative tests.
Các schema được giữ lại để kiểm tra; không DROP, TRUNCATE, DELETE hoặc tắt FK.
Đây là kiểm thử SQL migration, **không giả mạo bảng lịch sử Flyway** và không phải test Flyway engine.

Đầu ra phân biệt PASS/FAIL/SKIP; exit code khác 0 khi có lỗi thực.
Test dữ liệu ngoài demo dùng các hàng hư cấu `KEEP_*` và so fingerprint trước/sau trên cả 17 bảng.
Count report chỉ tính dữ liệu `DEMO_` và 5 vai trò V5, không tính các hàng đối chứng `KEEP_*`.
Negative tests dùng transaction rồi rollback, kể cả khi DB chấp nhận ràng buộc chéo còn thiếu ở Service.
Không có hàng cố ý sai nào được commit.

## Seed một schema demo local đã được khởi tạo hợp lệ

Chỉ dùng schema demo đã có V1–V5. Với DB ứng dụng do Flyway quản lý, kiểm tra lịch sử
Flyway thực trước; không SOURCE lại V1–V5 lên schema có sẵn. Không trộn khởi tạo thủ công
với lịch sử Flyway của ứng dụng.

Trước hết, xác nhận instance/schema bằng MySQL client (thay các placeholder):

```bash
mysql --host=127.0.0.1 --port=3306 --user=YOUR_LOCAL_USER --password --database=YOUR_DEMO_SCHEMA -e "SELECT DATABASE(), VERSION(), @@foreign_key_checks;"
```

Sao lưu trước nếu schema đã có dữ liệu (các biến dưới đây chỉ là placeholder của máy local):

```bash
mysqldump --host=127.0.0.1 --port=3306 --user=YOUR_LOCAL_USER --password --single-transaction --no-tablespaces --set-gtid-purged=OFF YOUR_DEMO_SCHEMA > before-demo-seed.sql
```

Đặt bản backup ngoài repo, bảo vệ quyền đọc; thử phục hồi vào schema riêng khi cần.
Sau đó:

```bash
python database/seed/demo_seed.py seed --host 127.0.0.1 --port 3306 --user YOUR_LOCAL_USER --database YOUR_DEMO_SCHEMA --fixture-root /absolute/private/qldt-fixtures
python database/seed/demo_seed.py validate --host 127.0.0.1 --port 3306 --user YOUR_LOCAL_USER --database YOUR_DEMO_SCHEMA --fixture-root /absolute/private/qldt-fixtures
```

Helper kiểm tra schema được chọn trùng tên đã chỉ định, MySQL 8.4, strict mode, FK,
PDF thật và SHA-256; lấy advisory lock theo schema. Mọi dữ liệu nghiệp vụ được kiểm tra
**trước COMMIT**, lỗi bất kỳ rollback toàn bộ phần chèn mới.

`validate_demo_data.sql` chỉ đọc, có thể chạy riêng qua client để xem 17 count và các invariant.
Helper còn bổ sung kiểm tra mọi FK lấy từ `information_schema`, đối chiếu PDF với byte thật.
Kết quả hiện tại: **89 checks, 0 FAIL** trên snapshot chuẩn.

## Chạy lại và dữ liệu seed v1

Mỗi INSERT dùng `WHERE NOT EXISTS` theo PK thật, không dùng INSERT IGNORE hoặc UPSERT.
Không cập nhật/xóa hàng đã tồn tại. Tên, trạng thái, version và timestamp người dùng đã sửa được giữ nguyên.
Trùng email/object key với PK khác phải báo lỗi; không tự gộp hoặc gán nhầm chủ sở hữu.

Một số timestamp nguồn v1 có sai thứ tự: GVHD được phân công sau lúc duyệt đề tài;
membership trưởng nhóm được tạo sau khi nhóm ra đời. Bản v2 sửa giá trị cho **lần nạp mới**,
giữ nguyên mã và hash cũ. Không tự UPDATE các hàng v1 đã có trên máy người dùng.
Nếu schema còn hàng v1 sai mốc, gate kiểm tra trước COMMIT sẽ báo lỗi và rollback các hàng mới.
Chọn schema demo mới là cách an toàn; nếu muốn giữ schema v1 thì cần chủ dữ liệu phê duyệt
một bản sửa cụ thể sau backup. Không chạy SQL sửa hàng loạt tự động.

Count kiểm tra là baseline: sau khi demo nghiệp vụ làm thay đổi số hàng/trạng thái, baseline có thể FAIL hợp lý.
Đọc từng check; không ghi đè dữ liệu để ép về PASS. Tạo schema QA mới để tái lập snapshot chuẩn.

## Lỗi và transaction

- Dùng helper là đường chạy được kiểm thử. Không chạy đồng thời cùng các thao tác demo đang ghi dữ liệu;
  advisory lock chỉ tuần tự hóa các helper seed, không khóa toàn bộ ứng dụng.
- File SQL có các CHECK gate session và một transaction. Chạy trực tiếp sẽ thiếu biến opt-in và bị chặn.
  Gate không thể tự đọc storage; chỉ helper xác minh file mới được thiết lập các biến đó.
- MySQL batch không dùng `--force` sẽ dừng khi lỗi; ngắt kết nối rollback transaction chưa commit.
  Trong GUI hoặc client có tiếp tục sau lỗi, phải **dừng và ROLLBACK ngay**; không chạy tiếp tới COMMIT.
  Không dùng `--force`, không tắt CHECK/FK, không bỏ qua lỗi duplicate.
- Nếu helper thất bại giữa chừng, xem lỗi/count, xác nhận rollback và dữ liệu trước đó, giải quyết nguyên nhân rồi chạy lại.
  Không tự xóa hàng demo cũ. Nếu mất kết nối đúng lúc COMMIT, kiểm tra dữ liệu trước khi kết luận kết quả;
  seed idempotent cho phép chạy lại an toàn sau khi xác minh.
- Tạo bảng tạm không tạo đối tượng schema lâu dài. Không sửa bảng lịch sử Flyway.

## Tài khoản demo: giữ khóa và cấp quyền an toàn sau này

Tất cả **47 tài khoản demo KHOA**, `.test`, `buoc_doi_mat_khau=TRUE`, không có mật khẩu chung.
Hash được tạo từ mật khẩu ngẫu nhiên đã bỏ, không phải tài khoản đăng nhập được phát sẵn.
`DEMO_GV01` có GIANG_VIEN + TRUONG_BO_MON để thử đa vai trò.

Snapshot phân công sử dụng đúng loại hồ sơ và vai trò; khóa tài khoản hiện tại là yêu cầu bảo mật của fixture.
Service tương lai vẫn phải kiểm tra tài khoản hoạt động khi thao tác; seed không bỏ qua quy tắc này.

Sau khi TV1 có authentication và chủ dự án duyệt quy trình provisioning:

1. Dùng quản trị hợp lệ để chọn đúng một số ít mã cần cho kịch bản (trưởng/thành viên/GV/trưởng khoa).
2. Cấp reset token một lần hoặc mật khẩu ngẫu nhiên riêng từng tài khoản qua kênh riêng;
   dùng BCrypt tương thích PasswordEncoder đã chốt. `.test` không nhận email thật, không giả định SMTP reset hoạt động.
3. Bắt buộc đổi mật khẩu, rồi mở khóa đúng các mã đã duyệt; không phát hash/mật khẩu lên PR hoặc README.
4. Kiểm tra quyền server, CSRF, vô hiệu phiên khi khóa/đổi quyền; sau buổi demo khóa lại tài khoản đã cấp.
5. Ghi nhận đây là schema đã được provisioning; baseline "mọi account khóa" sẽ không còn PASS.

Task này không thực hiện bước kích hoạt nào và không tắt Spring Security.

## Các bước còn thiếu trước khi chủ dự án cân nhắc Railway

**Không có deployment hay thao tác seed Railway trong task này. Helper hiện tại cố ý chỉ hỗ trợ local.**

1. Hoàn thiện authentication, service/controller, transaction và kiểm thử quyền theo từng phân hệ.
2. Chốt private object storage hoặc volume persistent; không dùng filesystem tạm của container làm kho báo cáo bền vững.
3. TV3 tích hợp adapter lưu/đọc `object_key`. Thống nhất giữ prefix `demo-fixtures/v1/...` hay đổi hợp đồng;
   nếu đổi, cập nhật đồng bộ SQL/manifest/validator. Không suy đoán đường dẫn từ tên file gốc.
4. Chủ dự án provision sáu file ở đúng key trong kho private, kiểm tra byte/size/SHA-256 bằng đọc lại,
   kiểm thử download qua backend có phân quyền. Không có public URL cố định.
5. Dùng staging riêng, backup nhất quán và kiểm tra restore; xác nhận đúng instance/schema, V1–V5
   và dữ liệu hiện có. Kiểm tra xung đột PK/email/object key, nhất là các hàng v1 đã tồn tại.
6. Thử import/rollback trên bản sao staging, chạy toàn bộ check và kiểm thử website sau provisioning.
7. Chỉ sau khi hoàn tất các bước trên mới chuẩn bị **một task import riêng được chủ dự án cho phép**,
   với storage preflight dành cho kiến trúc thật. Không đổi host hoặc giả cờ "local fixture ready" để dùng helper này trên cloud.

Các điểm nghiệp vụ còn cần nhóm chốt: cách sửa đăng ký bị từ chối (GT05 vẫn một hàng/nhóm),
quyền tổng hợp cho loại không hội đồng, và chính sách mở rộng hội đồng ngoài KLTN.
Bộ mẫu hiện chỉ tổng hợp/công bố kết quả KLTN, dùng đúng chủ tịch và trưởng khoa theo tài liệu.
