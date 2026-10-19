<p align="center">
  <img src="src/main/resources/static/images/ute_logo.png" alt="Logo HCMUTE" width="78">
</p>

<h1 align="center">Hệ thống quản lý đề tài sinh viên Khoa CNTT</h1>

<p align="center">
  Đồ án cuối kỳ môn <strong>Lập trình Web (WEPR330479)</strong><br>
  <strong>Nhóm 12</strong> · Học kỳ 1, năm học 2026–2027
</p>

Ứng dụng web hỗ trợ Khoa CNTT quản lý các đợt đăng ký, đề tài, nhóm sinh viên, việc nộp báo cáo, hội đồng chấm điểm và kết quả. Hệ thống được xây dựng theo kiến trúc **Spring Boot MVC + Thymeleaf**, sử dụng **MySQL** để lưu trữ dữ liệu.

> **Lưu ý:** Các chức năng dưới đây là **phạm vi triển khai của đồ án**, không phải xác nhận rằng tất cả đã hoàn thành hoặc kiểm thử thành công. README sẽ được cập nhật theo mã nguồn thực tế sau khi tích hợp.

## 1. Thành viên và phân công

| Thành viên | MSSV | Họ và tên | GitHub | Phụ trách (Frontend + Backend) |
| --- | --- | --- | --- | --- |
| TV1 | 24162036 | Trần Phạm Đình Hoàng | [@Hoangzno1](https://github.com/Hoangzno1) | Tài khoản, phân quyền, bộ môn và thông báo |
| TV2 | 24162074 | Nguyễn Anh Minh | [@monmon89hg](https://github.com/monmon89hg) | Đợt đăng ký, đề tài và phân công giảng viên |
| TV3 | 24110070 | Nguyễn Thị Như Ý | [@y0unglewis](https://github.com/y0unglewis) | Nhóm sinh viên, đăng ký đề tài, nộp báo cáo; xây dựng Base UI chung |
| TV4 | 24110152 | Nguyễn Quốc An | [@NguyenAn220206](https://github.com/NguyenAn220206) | Hội đồng, chấm điểm, tổng hợp và công bố kết quả |

Mỗi thành viên triển khai **giao diện Thymeleaf, xử lý nghiệp vụ Spring Boot và tích hợp dữ liệu** cho phân hệ mình phụ trách. Các trang sử dụng chung Header, Sidebar, Footer và bộ CSS/JS của nhóm.

## 2. Chức năng dự kiến

- **Quản trị và phân quyền:** đăng nhập, đăng xuất, quản lý tài khoản sinh viên/giảng viên/cán bộ, bộ môn và thông báo.
- **Quản lý đề tài theo hai giai đoạn:** giai đoạn 1 tiếp nhận, xét duyệt và công bố đề tài; giai đoạn 2 cho phép nhóm sinh viên đăng ký các đề tài đã công bố.
- **Quản lý nhóm sinh viên:** thành lập nhóm, quản lý yêu cầu tham gia và nhóm trưởng. Khi đăng ký đề tài, nhóm phải có **3–5 thành viên chính thức** và đúng một nhóm trưởng; một sinh viên không đồng thời thuộc hai nhóm đang hoạt động.
- **Đăng ký đề tài:** nhóm trưởng gửi đăng ký và theo dõi trạng thái xét duyệt; mỗi nhóm có tối đa **một bản ghi đăng ký đề tài** theo thiết kế GT05.
- **Nộp báo cáo:** nhóm trưởng nộp tệp PDF (tối đa **20 MiB**), quản lý các phiên bản báo cáo.
- **Hội đồng và chấm điểm:** phân công hội đồng **3–5 giảng viên**, nhập phiếu chấm, tổng hợp điểm; giảng viên không chấm đề tài mình hướng dẫn.
- **Kết quả:** công bố, tra cứu kết quả trong phạm vi được phân quyền và thông báo đến sinh viên.

## 3. Công nghệ sử dụng

| Thành phần | Công nghệ |
| --- | --- |
| Ngôn ngữ | Java 21 |
| Backend | Spring Boot, Spring MVC, Spring Data JPA, Spring Security, Validation |
| Frontend | Thymeleaf, HTML, CSS, JavaScript, Be Vietnam Pro |
| Cơ sở dữ liệu | MySQL 8.4 |
| Quản lý phiên bản CSDL | Flyway |
| Build và quản lý dependencies | Maven Wrapper |
| Quản lý mã nguồn và công việc | Git, GitHub Issues, Pull Requests |

Thiết kế CSDL của nhóm gồm **17 bảng**, sử dụng mã nghiệp vụ hoặc khóa ghép làm khóa chính; không dùng `id AUTO_INCREMENT` làm khóa chính thay thế.

## 4. Cấu trúc thư mục

Các đường dẫn chính trong cấu trúc dự án (một số thư mục được bổ sung dần khi triển khai):

```text
ql-detai-sinhvien/
├── .mvn/                                  # Maven Wrapper
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/                          # Controller, Service, Repository, Entity...
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── fragments/             # Header, Sidebar, Footer... dùng chung
│   │       │   └── shared/dashboard.html  # Trang Dashboard chung
│   │       ├── static/
│   │       │   ├── css/                   # CSS dùng chung
│   │       │   ├── js/                    # JavaScript dùng chung
│   │       │   └── images/ute_logo.png    # Logo HCMUTE
│   │       ├── db/migration/              # SQL migration Flyway
│   │       └── application.properties     # Cấu hình Spring Boot
│   └── test/                              # Mã kiểm thử
├── docs/
│   └── QUY_UOC_TICH_HOP_UI.md             # Hướng dẫn dùng Base UI
└── README.md
```

**Quy ước giao diện:** các trang nghiệp vụ sử dụng lại các Thymeleaf fragments và CSS chung; không tạo Header/Sidebar riêng cho từng phân hệ. Xem chi tiết tại [`docs/QUY_UOC_TICH_HOP_UI.md`](docs/QUY_UOC_TICH_HOP_UI.md) sau khi PR Base UI được merge.

## 5. Hướng dẫn chuẩn bị môi trường và chạy local

### Bước 1 — Cài đặt công cụ

Cài các công cụ sau:

1. **JDK 21** (kiểm tra đúng JDK, không chỉ JRE).
2. **Git** và **VS Code** hoặc IntelliJ IDEA.
3. **MySQL Server 8.4** (MySQL Workbench là công cụ hỗ trợ, không bắt buộc).

Dự án sử dụng **Maven Wrapper** và Tomcat nhúng, nên thông thường không phải cài Maven hoặc Tomcat riêng.

Mở terminal trong VS Code và kiểm tra:

```powershell
java -version
javac -version
git --version
```

`java` và `javac` cần hiển thị phiên bản **21.x**. Nếu terminal không nhận lệnh sau khi cài đặt, hãy mở lại VS Code và kiểm tra biến môi trường `PATH`/`JAVA_HOME`.

### Bước 2 — Clone repository

```powershell
git clone https://github.com/monmon89hg/ql-detai-sinhvien.git
cd ql-detai-sinhvien
```

Kiểm tra Maven Wrapper sử dụng đúng Java:

```powershell
.\mvnw.cmd -v
```

Trên macOS/Linux, dùng `./mvnw` thay cho `.\mvnw.cmd`.

### Bước 3 — Chuẩn bị MySQL

Khởi động MySQL Server và tạo schema local nếu chưa có:

```sql
CREATE DATABASE IF NOT EXISTS qldt
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

**Chú ý:** SQL tạo bảng phải tuân theo migration Flyway đã thống nhất trong repo. Không chạy đồng thời script tạo bảng thủ công và migration Flyway trên cùng một schema chưa được quản lý phù hợp, để tránh lỗi bảng đã tồn tại hoặc sai lịch sử migration.

### Bước 4 — Cấu hình kết nối

Khi phần cấu hình backend được tích hợp, mỗi thành viên cần thiết lập kết nối MySQL cho máy của mình. Ví dụ các thuộc tính Spring Boot thường dùng:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/qldt
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Đây là **ví dụ cấu hình**, cần đối chiếu với `application.properties`/profile thực tế của dự án trước khi sử dụng. Không commit mật khẩu MySQL, thông tin SMTP hoặc các tệp cấu hình chứa bí mật lên GitHub.

Nếu project sử dụng các biến môi trường trên, trong PowerShell có thể thiết lập cho **phiên terminal hiện tại**:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="<mat-khau-mysql-cua-ban>"
```

### Bước 5 — Build, kiểm thử và chạy ứng dụng

Sau khi dependencies, cấu hình MySQL và migration đã hoàn thiện:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Nếu ứng dụng dùng cổng mặc định của Spring Boot, có thể mở **http://localhost:8080**. Địa chỉ truy cập và tài khoản thử nghiệm sẽ được nhóm cập nhật theo cấu hình và dữ liệu mẫu thực tế.

> **Quan trọng:** Việc có source code và Base UI không đồng nghĩa backend đã chạy thành công. Nếu lệnh build/chạy báo lỗi, cần kiểm tra log, JDK, dependency, kết nối MySQL, cấu hình Flyway và các migration trước khi kết luận ứng dụng hoạt động.

## 6. Base UI chung và bản demo

- Base UI của nhóm gồm **Header, Sidebar, Footer, các Thymeleaf fragments**, CSS/JS, logo HCMUTE và trang Dashboard chung.
- Mỗi thành viên tạo các trang Thymeleaf nghiệp vụ của mình dựa trên layout chung.
- Tài liệu hướng dẫn sử dụng UI: [`docs/QUY_UOC_TICH_HOP_UI.md`](docs/QUY_UOC_TICH_HOP_UI.md).
- **[Xem bản demo UI trên GitHub Pages](https://y0unglewis.github.io/ql-detai-ui-demo/index.html)**: chỉ là giao diện HTML/CSS/JS tĩnh, **không có backend**.

## 7. Quy trình làm việc nhóm với GitHub

Nhóm áp dụng quy trình Git Flow đơn giản với hai nhánh dài hạn:

- `main`: nhánh ổn định, dùng cho bản phát hành và nộp đồ án.
- `develop`: nhánh tích hợp các chức năng, kiểm thử trước khi đưa lên `main`.

**Không push trực tiếp vào `main` hoặc `develop`.** Mọi thay đổi phải được tích hợp qua Pull Request (PR).

### 7.1. Quy ước đặt tên nhánh

- `feat/*`: phát triển chức năng mới.
- `fix/*`: sửa lỗi.
- `docs/*`: cập nhật tài liệu.
- `chore/*`: cấu hình, bảo trì, database migration.

Mỗi nhánh làm việc được tạo từ `develop`.

### 7.2. Bắt đầu công việc

Đảm bảo đã lưu an toàn các thay đổi local trước khi chuyển nhánh.

```powershell
git fetch origin
git switch develop
git pull --ff-only origin develop
git switch -c feat/ten-chuc-nang
```

### 7.3. Commit và push

```powershell
git status
git add <duong-dan-file>
git diff --cached
git commit -m "feat: implement feature name"
git push -u origin feat/ten-chuc-nang
```

Chỉ commit các file liên quan đến công việc. Không commit mật khẩu, token hoặc thông tin kết nối nhạy cảm.

### 7.4. Tạo Pull Request

1. Tạo PR với **base: `develop`**, compare: nhánh đang làm.
2. Mô tả các thay đổi, kiểm thử đã thực hiện và vấn đề còn tồn tại.
3. Liên kết GitHub Issue liên quan.
4. Yêu cầu ít nhất một thành viên khác review.
5. Chỉ merge khi đã xử lý các nhận xét và kiểm thử phù hợp.
6. Sau khi merge, cập nhật `develop` local trước khi bắt đầu công việc tiếp theo.

### 7.5. Tích hợp bản cuối vào main

Khi các phân hệ đã hoàn thiện và kiểm thử tích hợp thành công:

1. Tạo PR với **base: `main`**, compare: `develop`.
2. Kiểm tra build, database migration và luồng nghiệp vụ.
3. Nhờ thành viên khác review.
4. Merge PR vào `main` sau khi nhóm thống nhất.
5. Sử dụng `main` làm nguồn cho bản phát hành/nộp đồ án.

Không phát triển chức năng trực tiếp trên `main`.

### 7.6. Quy tắc database migration

- Các migration Flyway được quản lý chung trong repository.
- Không sửa migration đã áp dụng trên môi trường dùng chung.
- Khi thay đổi schema, cần thống nhất với các thành viên có bảng liên quan.
- Migration mới phải được kiểm thử trước khi merge vào `develop`.

## 8. Tài liệu và lưu ý

- Thiết kế CSDL/ERD: lưu trong tài liệu dự án của nhóm; file `.mwb` có thể mở bằng MySQL Workbench, bản ảnh/PDF dùng để đưa vào báo cáo.
- Quy ước Base UI: `docs/QUY_UOC_TICH_HOP_UI.md`.
- Các tài liệu Use Case, rubric và minh chứng kiểm thử sẽ được quản lý và cập nhật theo sản phẩm thực tế.
- Không commit mật khẩu, dữ liệu thật của sinh viên, file upload nhạy cảm hay thông tin bí mật.

---

**Nhóm 12 — Hệ thống quản lý đề tài sinh viên Khoa CNTT · WEPR330479**
