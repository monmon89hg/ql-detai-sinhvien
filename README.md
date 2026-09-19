# Hệ thống quản lý đề tài sinh viên

Đồ án cuối kỳ môn Lập trình Web — Nhóm 12.

Ứng dụng web hỗ trợ quản lý đợt đăng ký, đề tài, nhóm sinh viên,
báo cáo và quá trình đánh giá kết quả.

## 1. Thành viên và phân công

| Thành viên | MSSV | GitHub | Phần phụ trách |
| --- | --- | --- | --- |
| Thành viên 1 — Nhóm trưởng | Bổ sung | Bổ sung | Khởi tạo dự án, người dùng, phân quyền, tích hợp |
| Thành viên 2 | Bổ sung | Bổ sung | Đợt đăng ký, đề tài, phân công giảng viên |
| Thành viên 3 | Bổ sung | Bổ sung | Nhóm sinh viên, đăng ký đề tài, nộp báo cáo |
| Thành viên 4 | Bổ sung | Bổ sung | Hội đồng, chấm điểm, tổng hợp kết quả |

Chi tiết đóng góp được ghi tại [docs/contributions.md](docs/contributions.md)
và các pull request của từng thành viên.

## 2. Chức năng dự kiến

- Đăng nhập và phân quyền người dùng.
- Quản lý bộ môn, giảng viên và sinh viên.
- Quản lý đợt đăng ký và danh sách đề tài.
- Thành lập nhóm sinh viên và đăng ký đề tài.
- Duyệt đăng ký và lưu lịch sử các lần đăng ký.
- Nộp và quản lý báo cáo.
- Phân công hội đồng, nhập điểm và tổng hợp kết quả.
- Công bố kết quả và quản lý thông báo.

Đây là phạm vi dự kiến; tiến độ triển khai được ghi ở mục 8.

## 3. Công nghệ

| Thành phần | Công nghệ |
| --- | --- |
| Ngôn ngữ | Java 21 LTS |
| Framework | Spring Boot 4.1.1 |
| Kiến trúc web | Spring MVC, render HTML phía server |
| Giao diện | Thymeleaf, HTML, CSS, JavaScript, Bootstrap 5.3.8 |
| Bảo mật | Spring Security, session cookie, BCrypt |
| Truy cập dữ liệu | Spring Data JPA, Hibernate |
| Cơ sở dữ liệu | MySQL 8.4 LTS |
| Quản lý thay đổi CSDL | Flyway |
| Build | Maven 3.9.16 qua Maven Wrapper |
| Quản lý mã nguồn | Git và GitHub |

Các thư viện thuộc hệ sinh thái Spring được quản lý phiên bản
thông qua Spring Boot trong `pom.xml`.

## 4. Cấu trúc dự án

Các đường dẫn chính và quy ước tổ chức:

| Đường dẫn | Nội dung |
| --- | --- |
| `src/main/java/` | Mã nguồn Java |
| `src/main/resources/templates/` | Giao diện Thymeleaf |
| `src/main/resources/static/` | CSS, JavaScript và tài nguyên giao diện |
| `src/main/resources/db/migration/` | SQL migration của Flyway |
| `src/main/resources/application.properties` | Cấu hình ứng dụng |
| `src/test/` | Mã kiểm thử |
| `database/seed/` | Dữ liệu mẫu |
| `database/checks/` | SQL kiểm tra dữ liệu và ràng buộc |
| `docs/database/` | Tài liệu thiết kế CSDL, ERD và từ điển dữ liệu |
| `docs/use-cases/` | Đặc tả và sơ đồ use case |
| `docs/contributions.md` | Phân công và ghi nhận đóng góp |

Một số thư mục sẽ được bổ sung khi bắt đầu triển khai phần tương ứng.

## 5. Chuẩn bị môi trường

Cài đặt:

- JDK 21.
- Git.
- MySQL Community Server 8.4.
- VS Code hoặc IntelliJ IDEA.

MySQL Workbench là công cụ tùy chọn để thao tác với database.
Không cần cài Maven hoặc Tomcat riêng khi sử dụng Maven Wrapper
và Tomcat nhúng của dự án.

Kiểm tra Java:

```powershell
java -version
javac -version
```

Cả hai cần hiển thị phiên bản 21.x.

## 6. Lấy mã nguồn và kiểm tra cấu hình build

Clone repository:

```powershell
git clone https://github.com/monmon89hg/ql-detai-sinhvien.git
cd ql-detai-sinhvien
```

Kiểm tra Maven và Java mà dự án đang sử dụng:

```powershell
.\mvnw.cmd -v
```

Kết quả mong đợi:

- Apache Maven 3.9.16.
- Java version 21.x.

Trên macOS/Linux, sử dụng `./mvnw` thay cho `.\mvnw.cmd`.

### Trạng thái chạy ứng dụng

Dự án hiện mới có bộ khung. Cấu hình ứng dụng hiện tại:

```properties
spring.application.name=qldt
```

Chưa hoàn tất cấu hình kết nối MySQL, SQL tạo bảng và dữ liệu mẫu.
Vì vậy, chưa bảo đảm ứng dụng khởi động thành công.

Sau khi hoàn thành phần CSDL, nhóm sẽ bổ sung tại đây:

1. Cách tạo database và tài khoản local.
2. Các biến môi trường cần thiết.
3. Cách chạy migration và nạp dữ liệu mẫu.
4. Lệnh khởi động, địa chỉ truy cập và tài khoản demo.

Không đưa mật khẩu thật vào repository.

## 7. Quy trình làm việc nhóm

Mỗi nhiệm vụ được thực hiện trên một nhánh riêng.

Trước khi bắt đầu, bảo đảm các thay đổi đang làm đã được lưu
bằng commit hoặc stash, sau đó:

```powershell
git switch main
git pull --ff-only origin main
git switch -c feat/ten-chuc-nang
```

Sau khi hoàn thành một phần việc:

```powershell
git status
git diff
git add DUONG_DAN_FILE_DA_SUA
git diff --cached
git commit -m "feat: describe the implemented change"
git push -u origin feat/ten-chuc-nang
```

Thay tên nhánh, đường dẫn và nội dung commit bằng thông tin thực tế.

Mở pull request vào `main`, ghi rõ:

- Nội dung thay đổi.
- Task hoặc Issue liên quan.
- Cách kiểm tra và kết quả thực tế.
- Phần chưa hoàn thành, nếu có.

Một thành viên khác review trước khi merge.
Nhóm sử dụng Create a merge commit để giữ lịch sử commit.

### Quy tắc SQL

- SQL chính thức đặt trong `src/main/resources/db/migration/`.
- Đặt tên theo dạng `V1__create_core_tables.sql`.
- Thống nhất số migration trước khi merge.
- Không sửa migration đã được áp dụng trên môi trường chung
  hoặc máy của thành viên khác; tạo migration mới cho thay đổi tiếp theo.
- Dữ liệu demo phải là dữ liệu giả.
- Không commit mật khẩu, file báo cáo thật hoặc bản sao lưu riêng.

## 8. Tiến độ

- [x] Khởi tạo repository GitHub.
- [x] Tạo bộ khung Spring Boot và Maven Wrapper.
- [x] Thiết lập `.gitignore` và `.gitattributes`.
- [ ] Xác nhận build và chạy với JDK 21.
- [ ] Hoàn thiện đặc tả nghiệp vụ và thiết kế 17 bảng.
- [ ] Viết và kiểm tra SQL migration.
- [ ] Cấu hình kết nối MySQL.
- [ ] Chuẩn bị dữ liệu mẫu.
- [ ] Triển khai đăng nhập và phân quyền.
- [ ] Triển khai các chức năng nghiệp vụ.
- [ ] Kiểm thử tích hợp.
- [ ] Triển khai bản demo.
- [ ] Hoàn thiện báo cáo và hướng dẫn sử dụng.

Cập nhật checklist theo kết quả thực tế, không đánh dấu hoàn thành
chỉ vì đã tạo file hoặc thư mục.