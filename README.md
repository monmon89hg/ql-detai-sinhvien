<p align="center">
  <img src="frontend/static/images/ute_logo.png" alt="Logo HCMUTE" width="78">
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
├── frontend/
│   ├── templates/                         # Thymeleaf, fragments và các trang nghiệp vụ
│   └── static/                            # CSS, JavaScript, ảnh dùng chung
├── backend/
│   ├── .mvn/                              # Maven Wrapper
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/                      # Controller, Service, Repository, Entity...
│       │   └── resources/
│       │       ├── db/migration/          # SQL migration Flyway
│       │       ├── application.properties
│       │       └── application-local.properties # Cấu hình riêng, không đưa lên Git
│       └── test/                          # Mã kiểm thử
├── database/seed/                        # Demo seed, PDF fixtures, QA scripts
├── docs/
│   └── QUY_UOC_TICH_HOP_UI.md             # Hướng dẫn dùng Base UI
└── README.md
```

Spring Boot tiếp tục render Thymeleaf và phục vụ CSS/JS trên cùng địa chỉ web.
`backend/pom.xml` chép `frontend/templates` và `frontend/static` vào classpath khi build;
đường dẫn view, fragments và URL `/css`, `/js`, `/images` được giữ nguyên.
Frontend không cần server hay lệnh chạy riêng. Khi chỉnh HTML/CSS/JS, khởi động lại
backend bằng Maven để chép phiên bản mới vào classpath; `spring.thymeleaf.cache=false`
không tự đồng bộ file nguồn ngoài classpath. Khi đóng gói, giao diện nằm sẵn trong JAR.

Kiểm tra tích hợp giao diện từ thư mục gốc repo:

```powershell
.\backend\mvnw.cmd -f backend/pom.xml "-Dtest=FrontendIntegrationTests" test
```

Bộ test này render các trang hội đồng thật, kiểm tra tài nguyên tĩnh, route phân công,
CSRF và quyền truy cập; Service và DataSource được giả lập nên không kết nối hoặc sửa MySQL.
Kiểm thử nghiệp vụ trên database thật vẫn cần chạy riêng với cấu hình local phù hợp.

**Quy ước giao diện:** các trang nghiệp vụ sử dụng lại các Thymeleaf fragments và CSS chung; không tạo Header/Sidebar riêng cho từng phân hệ. Xem chi tiết tại [`docs/QUY_UOC_TICH_HOP_UI.md`](docs/QUY_UOC_TICH_HOP_UI.md) sau khi PR Base UI được merge.

## 5. Hướng dẫn cài đặt và chạy local

Hướng dẫn dưới đây dùng **Windows PowerShell**, **MySQL 8.4** và nhánh `develop`. Các bước thực hiện trên **máy cá nhân**, không sử dụng database Railway.

### Bước 1 — Chuẩn bị

Cài đặt **JDK 21**, **Git**, **MySQL Server 8.4**, **Python 3.10+** và **MySQL Workbench** (để xem dữ liệu). Dự án dùng Maven Wrapper nên không cần cài Maven riêng.

```powershell
java -version
javac -version
git --version
py --version
```

### Bước 2 — Lấy source

```powershell
git clone https://github.com/monmon89hg/ql-detai-sinhvien.git
cd ql-detai-sinhvien
git switch develop
```

Nếu đã clone repo từ trước, hãy lưu an toàn các thay đổi chưa commit, rồi chạy trong thư mục repo:

```powershell
git fetch origin
git switch develop
git pull --ff-only origin develop
```

Kiểm tra Maven Wrapper sau khi đã ở thư mục repository:

```powershell
.\backend\mvnw.cmd -f backend/pom.xml -v
```

### Bước 3 — Tạo database local

Trong **MySQL Workbench**, kết nối MySQL trên máy và chạy:

```sql
CREATE DATABASE qldt_local_demo
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

Nếu tên database đã tồn tại, dùng một tên **mới, chưa có bảng**. Không chạy lại các file V1–V5 thủ công trên schema đã do Flyway quản lý.

### Bước 4 — Chạy Spring Boot để Flyway tạo bảng

Tạo file **`backend/src/main/resources/application-local.properties`** (file này đã được `.gitignore` loại khỏi Git), với nội dung:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/qldt_local_demo
spring.datasource.username=root
spring.datasource.password=MAT_KHAU_MYSQL_LOCAL
spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate
```

Thay `MAT_KHAU_MYSQL_LOCAL` bằng mật khẩu MySQL trên **máy của bạn**; nếu dùng tên schema/user/port khác, sửa cho khớp. Không commit hoặc chia sẻ file này.

Nếu đã có file cấu hình local trước khi tách thư mục, chuyển nguyên file vào vị trí mới;
giữ nguyên URL, user, mật khẩu hoặc tham chiếu biến môi trường. Ví dụ MySQL 8.4 cài
song song trên cổng `3307` thì URL vẫn dùng `127.0.0.1:3307/qldt_local_demo`.
Nếu file dùng `${DB_USERNAME}` và `${DB_PASSWORD}`, đặt các biến trong chính phiên PowerShell chạy backend:

```powershell
$env:DB_USERNAME = "root"
$taskDbPassword = Read-Host "Mật khẩu MySQL local" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $taskDbPassword).Password
```

Chạy trong PowerShell, từ thư mục gốc repo:

```powershell
.\backend\mvnw.cmd -f backend/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```

Để dùng đăng nhập và phân quyền hội đồng local hiện tại, chạy với cả hai profile:

```powershell
.\backend\mvnw.cmd -f backend/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local,hoi-dong-local"
```

Cũng có thể `cd backend` rồi chạy `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local,hoi-dong-local"`.
Trở về thư mục gốc repo trước khi chạy các lệnh Python ở bước 5.
Việc tách thư mục không tự kích hoạt tài khoản demo và không thay đổi dữ liệu MySQL.

Khi Spring Boot khởi động thành công, Flyway sẽ áp dụng V1–V5. Nhấn **Ctrl+C** để dừng ứng dụng trước khi nạp dữ liệu mẫu.

Trong Workbench, schema do Flyway quản lý sẽ có **17 bảng nghiệp vụ + `flyway_schema_history`**. Có thể kiểm tra:

```sql
SELECT installed_rank, version, description, success
FROM qldt_local_demo.flyway_schema_history
ORDER BY installed_rank;
```

Kỳ vọng: **5 migration thành công**. Ở giai đoạn hiện tại, mở `http://localhost:8080/` có thể chưa có trang chủ; đây không phải dấu hiệu Flyway chạy lỗi.

### Bước 5 — Nạp dữ liệu demo

Bộ dữ liệu có **257 bản ghi demo + 5 vai trò V5 = 262 bản ghi**, gồm 6 PDF mẫu. Chỉ dùng trên **schema local mới** đã tạo ở bước 4.

```powershell
py -m venv database\seed\.venv
.\database\seed\.venv\Scripts\python.exe -m pip install -r database\seed\requirements.txt
$fixtureRoot = Join-Path $env:USERPROFILE "qldt-private-fixtures"
.\database\seed\.venv\Scripts\python.exe database\seed\fixture_tools.py provision --fixture-root "$fixtureRoot"
.\database\seed\.venv\Scripts\python.exe database\seed\demo_seed.py seed --host 127.0.0.1 --port 3306 --user root --database qldt_local_demo --fixture-root "$fixtureRoot"
.\database\seed\.venv\Scripts\python.exe database\seed\demo_seed.py validate --host 127.0.0.1 --port 3306 --user root --database qldt_local_demo --fixture-root "$fixtureRoot"
```

Thư mục PDF mẫu được tạo trong hồ sơ người dùng Windows, **bên ngoài repository**. Script sẽ hỏi mật khẩu MySQL local khi kết nối. Kết quả kiểm tra mẫu đạt **89 checks, 0 FAIL** trên dữ liệu chưa bị chỉnh sửa. **Không chạy trực tiếp `demo_data.sql` bằng Execute All trong Workbench**: script cần kiểm tra PDF và điều kiện an toàn trước khi commit.

### Bước 6 — Xem dữ liệu trong Workbench

Refresh **SCHEMAS**, mở `qldt_local_demo` → **Tables**, hoặc chạy:

```sql
SELECT COUNT(*) AS so_bang_nghiep_vu
FROM information_schema.tables
WHERE table_schema = 'qldt_local_demo'
  AND table_type = 'BASE TABLE'
  AND table_name <> 'flyway_schema_history';

SELECT COUNT(*) AS so_nguoi_dung_demo
FROM qldt_local_demo.nguoi_dung
WHERE ma_nguoi_dung LIKE 'DEMO_%';
```

Kỳ vọng: **17 bảng nghiệp vụ** và **47 người dùng demo**. Các tài khoản demo đang **khóa**, không dùng để đăng nhập website. Sáu PDF trong repo là **file kiểm thử local**, chưa phải kho lưu trữ báo cáo của ứng dụng.

### Lưu ý

- Chỉ nạp dữ liệu mẫu vào **MySQL local**, không nhập lên Railway hoặc database dùng chung.
- Không sửa các migration V1–V5 đã áp dụng; khi cần thay đổi cấu trúc, tạo migration mới.
- Nếu đã nạp seed phiên bản cũ hoặc database có dữ liệu quan trọng, **sao lưu và dùng schema QA mới**, không ghi đè tùy tiện.
- Các chức năng đăng nhập, nhóm, upload báo cáo và chấm điểm phải được kiểm thử riêng khi các Controller/Service hoàn thiện.
- Hướng dẫn chi tiết và báo cáo test: [`database/seed/README.md`](database/seed/README.md), [`database/seed/HANDOFF_VI.md`](database/seed/HANDOFF_VI.md).

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

## 9. Tài liệu phân tích và thiết kế

- [Đặc tả yêu cầu FR/NFR](docs/DAC_TA_YEU_CAU_FR_NFR_NHOM12_FINAL.md) — 30 FR, 19 NFR.
- [Đặc tả Use Case](docs/USE_CASE_NHOM12_FINAL.md).
- [Quy ước tích hợp UI](docs/QUY_UOC_TICH_HOP_UI.md).

---

**Nhóm 12 — Hệ thống quản lý đề tài sinh viên Khoa CNTT · WEPR330479**
