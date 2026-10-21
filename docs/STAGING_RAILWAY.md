# Staging tích hợp trên Railway — nhóm WEPR

## Mục tiêu và phạm vi

- Cho thành viên kiểm thử **dữ liệu thật trên MySQL dành cho QA**, không phải database học vụ chính thức.
- Mỗi thành viên đăng nhập bằng tài khoản `nguoi_dung` và vai trò trong `nguoi_dung_vai_tro`.
- Không sử dụng `PREVIEW_USERNAME`/`PREVIEW_PASSWORD` với profile `staging`. Profile `preview` hiện tại **chỉ đọc** và vẫn được giữ nguyên.
- `staging` chỉ được kích hoạt độc lập: không đồng thời kích hoạt `preview` hoặc `hoi-dong-local`.
- Các thao tác ghi chỉ được cho phép trên những route đã đăng ký quyền; route ghi của module mới mặc định 403 cho đến khi thêm rule và test.

## Bước 1 — Preflight, tuyệt đối không thay đổi dữ liệu vội

1. Xác nhận GitHub CI của PR staging xanh, đã merge vào `develop`.
2. Ở Railway project **charming-growth**, service hiện có là `ql-detai-sinhvien` và `MySQL`; không tạo project khác.
3. **Sao lưu MySQL và xác minh có thể phục hồi** trước khi thay đổi schema hoặc mở ghi.
4. Dùng công cụ SQL của quản trị viên để **chỉ đọc** (không chạy trên database khác):

```sql
SELECT DATABASE() AS current_schema, VERSION() AS mysql_version;
SELECT COUNT(*) AS migration_count FROM flyway_schema_history;
SELECT installed_rank, version, success FROM flyway_schema_history ORDER BY installed_rank;
SELECT COUNT(*) AS active_test_accounts FROM nguoi_dung WHERE trang_thai = 'HOAT_DONG';
SELECT ma_vai_tro, COUNT(*) AS members FROM nguoi_dung_vai_tro GROUP BY ma_vai_tro;
```

Nếu bảng `flyway_schema_history` hoặc `nguoi_dung` không tồn tại, **dừng tại đây**: cần xác minh liệu schema đã được Flyway quản lý hay chưa. Không bật `SPRING_FLYWAY_ENABLED=true` hoặc tự nạp seed lên database đang có dữ liệu khi chưa đối chiếu tình trạng schema.

## Bước 2 — Cấp tài khoản thử nghiệm (chủ dự án/TV1)

Bộ seed `database/seed/` chứa 47 người dùng demo có `trang_thai='KHOA'`, hash từ mật khẩu đã bỏ. Không thể dùng chúng đăng nhập ngay.

- Chỉ chọn một số người dùng kiểm thử đã duyệt cho vai trò `TRUONG_KHOA`, `GIANG_VIEN`, `SINH_VIEN` (và `ADMIN` khi module có hỗ trợ).
- Dùng BCrypt tạo **mật khẩu riêng, ngẫu nhiên**, mở khóa đúng các tài khoản cần thiết, cấp đúng vai trò từ `nguoi_dung_vai_tro`. Kiểm tra `enabled` thông qua `trang_thai = 'HOAT_DONG'`.
- Không chia sẻ chung một mật khẩu, không ghi hash/mật khẩu thật vào repo, PR, screenshot hoặc log.
- Sau đợt kiểm thử thu hồi hoặc khóa lại các tài khoản thử nghiệm. Quy trình đổi mật khẩu lần đầu và vô hiệu phiên khi đổi quyền **chưa được chứng minh đầy đủ** bởi PR này.

## Bước 3 — Variables và build

Service Spring Boot hiện có các biến `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_FLYWAY_ENABLED`, `SPRING_JPA_HIBERNATE_DDL_AUTO`, `PORT` và `SERVER_PORT`. Giá trị thực tế phải được chủ dự án kiểm tra trong Railway; không ghi lại mật khẩu.

Thiết lập bổ sung trong Railway service **sau khi code staging đã merge và database/account được xác minh**:

```dotenv
SPRING_PROFILES_ACTIVE=staging
SERVER_FORWARD_HEADERS_STRATEGY=framework
```

- Builder: `Dockerfile`, path: `Dockerfile`, Root Directory: thư mục gốc (để trống).
- Source branch: `develop`.
- Không cần `PREVIEW_USERNAME` hoặc `PREVIEW_PASSWORD` trong profile staging.
- Kiểm tra URL JDBC trỏ tới database MySQL dành cho QA trong cùng project; **không dùng localhost trong Railway**.
- Tuyệt đối không đổi các biến `SPRING_FLYWAY_ENABLED` và DDL mode theo phỏng đoán.
- Nếu đây vẫn là environment tên `production`, hãy coi nó là **website QA công khai có đăng nhập** và dùng dữ liệu giả lập, không dữ liệu thật của sinh viên.

## Bước 4 — Smoke test sau khi deploy

1. Đăng nhập sai/ẩn danh: bị yêu cầu đăng nhập, không xem dữ liệu.
2. Tài khoản sinh viên xem Dashboard, không vào `/hoi-dong`, không tạo đợt đăng ký.
3. Tài khoản giảng viên đề xuất đề tài và truy cập `/cham-diem` theo phân quyền; không duyệt đề tài.
4. Tài khoản trưởng khoa dùng trang `/hoi-dong` và thao tác hợp lệ kèm CSRF.
5. POST thiếu CSRF phải bị từ chối; POST route mới chưa duyệt quyền phải 403.
6. Thao tác nghiệp vụ hợp lệ phải thay đổi MySQL, đọc lại từ MySQL xác nhận và thử rollback/khôi phục **trong schema QA**.
7. Kiểm tra dữ liệu seed, ảnh, CSS, JS, PDF upload và các tính năng chưa merge riêng; không đánh dấu PASS nếu chưa có module.

Bài `StagingSecurityTests` chỉ kiểm tra filter với MockMvc, không xác nhận đăng nhập database thực hoặc luồng tạo/sửa/xóa đã hoạt động end-to-end.

## Quy tắc review cho module mới

Mỗi module thêm POST/PUT/PATCH/DELETE phải cập nhật rule trong `StagingSecurityConfig`, kiểm tra role, CSRF, validation và nghiệp vụ; không mở `anyRequest().authenticated()` cho mọi thao tác ghi. Bản triển khai đầu tiên chỉ bao phủ các route của đề tài, đợt đăng ký, hội đồng, chấm điểm hiện có.
