<p align="center">
  <img src="frontend/static/images/ute_logo.png" alt="Logo HCMUTE" width="78">
</p>

<h1 align="center">Hệ thống quản lý đề tài sinh viên Khoa CNTT</h1>

<p align="center">
  Đồ án cuối kỳ môn <strong>Lập trình Web (WEPR330479)</strong><br>
  <strong>Nhóm 12</strong> · Học kỳ 1, năm học 2026–2027
</p>

Ứng dụng web hỗ trợ Khoa CNTT quản lý các đợt đăng ký, đề tài, nhóm sinh viên, việc nộp báo cáo, hội đồng chấm điểm và kết quả. Hệ thống được xây dựng theo kiến trúc **Spring Boot MVC + Thymeleaf**, sử dụng **MySQL** để lưu trữ dữ liệu.

Ứng dụng được đóng gói bằng **Docker**, triển khai chính trên **Railway**, với **Render** là môi trường phụ; **Railway MySQL 8.4** lưu dữ liệu nghiệp vụ và **Cloudflare R2** lưu tệp báo cáo PDF. Mọi phân hệ dùng chung cơ chế xác thực, phân quyền và giao diện Thymeleaf.

> **Trạng thái (25/10/2026):** Mã nguồn đã tích hợp từ `develop` vào `main` để nộp bản chính thức; CI gồm kiểm thử build/MVC/Security và MySQL 8.4. Kết quả CI của một commit không thay thế kiểm thử thủ công trên môi trường triển khai.

**Website chính (Railway):** https://ql-detai-sinhvien-production.up.railway.app  
**Website phụ (Render):** https://ql-detai-sinhvien.onrender.com  
**Repository:** https://github.com/monmon89hg/ql-detai-sinhvien

## 1. Thành viên và phân công

| Thành viên | MSSV | Họ và tên | GitHub | Phụ trách (Frontend + Backend) |
| --- | --- | --- | --- | --- |
| TV1 | 24162036 | Trần Phạm Đình Hoàng | [@Hoangzno1](https://github.com/Hoangzno1) | Tài khoản, phân quyền, bộ môn và thông báo |
| TV2 | 24162074 | Nguyễn Anh Minh | [@monmon89hg](https://github.com/monmon89hg) | Đợt đăng ký, đề tài và phân công giảng viên |
| TV3 | 24110070 | Nguyễn Thị Như Ý | [@y0unglewis](https://github.com/y0unglewis) | Nhóm sinh viên, đăng ký đề tài, nộp báo cáo; xây dựng Base UI chung |
| TV4 | 24110152 | Nguyễn Quốc An | [@NguyenAn220206](https://github.com/NguyenAn220206) | Hội đồng, chấm điểm, tổng hợp và công bố kết quả |

Mỗi thành viên triển khai **giao diện Thymeleaf, xử lý nghiệp vụ Spring Boot và tích hợp dữ liệu** cho phân hệ mình phụ trách. Các trang sử dụng chung Header, Sidebar, Footer và bộ CSS/JS của nhóm.

## 2. Các phân hệ chính

- **Quản trị và phân quyền:** đăng nhập, đăng xuất, quản lý tài khoản sinh viên/giảng viên/cán bộ, bộ môn và thông báo.
- **Quản lý đề tài theo hai giai đoạn:** giai đoạn 1 tiếp nhận, xét duyệt và công bố đề tài; giai đoạn 2 cho phép nhóm sinh viên đăng ký các đề tài đã công bố.
- **Quản lý nhóm sinh viên:** thành lập nhóm, quản lý yêu cầu tham gia và nhóm trưởng. Khi đăng ký đề tài, nhóm phải có **3–5 thành viên chính thức** và đúng một nhóm trưởng; một sinh viên không đồng thời thuộc hai nhóm đang hoạt động.
- **Đăng ký đề tài:** nhóm trưởng gửi đăng ký và theo dõi trạng thái xét duyệt; mỗi nhóm có tối đa **một bản ghi đăng ký đề tài** theo thiết kế GT05.
- **Nộp báo cáo:** nhóm trưởng nộp tệp PDF (tối đa **20 MiB**), quản lý các phiên bản báo cáo.
- **Hội đồng và chấm điểm:** phân công hội đồng **3–5 giảng viên**, nhập phiếu chấm, tổng hợp điểm; giảng viên không chấm đề tài mình hướng dẫn.
- **Kết quả:** công bố, tra cứu kết quả trong phạm vi được phân quyền và thông báo đến sinh viên.

## 3. Công nghệ và kiến trúc triển khai

| Thành phần | Công nghệ / dịch vụ | Vai trò trong hệ thống |
| --- | --- | --- |
| Ngôn ngữ | Java 21, JavaScript | Xử lý nghiệp vụ phía máy chủ và tương tác giao diện |
| Backend | Spring Boot 4.1.1, Spring MVC, Spring Security, Spring Data JPA, JDBC, Validation | Controller, Service, Repository, xác thực, phân quyền và truy cập dữ liệu |
| Frontend | Thymeleaf, HTML5, CSS3, JavaScript, Be Vietnam Pro | Render giao diện trên server và xử lý tương tác phía trình duyệt |
| Cơ sở dữ liệu | MySQL 8.4 (Railway) | Lưu thông tin tài khoản, đợt đăng ký, đề tài, nhóm, hội đồng và kết quả |
| Migration CSDL | Flyway V1–V8 | Quản lý phiên bản schema; không tự ý chạy migration trên production |
| Lưu trữ báo cáo PDF | Cloudflare R2, AWS SDK for Java v2 (S3 API) | Lưu nội dung PDF; metadata, phiên bản và SHA-256 nằm trong MySQL |
| Xử lý tệp | Apache PDFBox, Apache POI | Kiểm tra PDF và xuất dữ liệu Excel |
| Email thông báo | Spring Mail, SMTP | Gửi thông báo kết quả khi chức năng và SMTP được cấu hình |
| Logging & giám sát | Spring AOP | Đo thời gian thực thi và ghi nhận lỗi ở tầng Service |
| Build & đóng gói | Maven Wrapper, Docker (multi-stage build) | Tạo JAR và image triển khai |
| CI | GitHub Actions | Build, unit/MVC/Security tests và kiểm thử MySQL 8.4 độc lập |
| Triển khai ứng dụng | Railway (chính), Render (phụ) | Chạy container Spring Boot MVC + Thymeleaf |
| Quản lý mã nguồn | Git, GitHub | Quản lý branch, review và Pull Request |

### 3.1. Kiến trúc triển khai

```text
Trình duyệt người dùng
         |
         v
Railway — Docker container (Spring Boot MVC + Thymeleaf)
         |                                |
         v                                v
Railway — MySQL 8.4               Cloudflare R2 — PDF

Render — môi trường triển khai phụ (nếu được bật)
```

Toàn bộ giao diện Thymeleaf và tài nguyên tĩnh được đóng gói trong cùng ứng dụng Spring Boot; **không có frontend server riêng**. Railway sử dụng cổng `PORT` do nền tảng cung cấp; cấu hình local dùng cổng `8081`.

**CI và CD:** GitHub Actions chạy các quality gates của repository; Railway là môi trường triển khai ứng dụng chính, Render là môi trường phụ. Việc tự động deploy khi có commit mới phụ thuộc cấu hình kết nối nhánh GitHub tại từng dịch vụ; workflow CI không đồng nghĩa tự thực hiện lệnh deploy.

**Tài nguyên bên ngoài:** cấu hình Railway, R2 và SMTP được cung cấp qua biến môi trường / secret trên dịch vụ triển khai. Không lưu mật khẩu hoặc khóa truy cập vào repository. Flyway trong `application.properties` đang tắt mặc định; chỉ bật có kiểm soát trên schema phù hợp sau khi kiểm tra lịch sử migration.

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

## 5. Chạy ứng dụng và kiểm thử local (Windows)

Hướng dẫn dành cho **môi trường MySQL 8.4 trên máy cá nhân**, không dùng dữ liệu Railway. Repo dùng **Java 21**, Maven Wrapper, Spring Boot MVC + Thymeleaf, Spring Security, JPA/JdbcTemplate và Cloudflare R2 cho PDF. Frontend được đóng gói chung trong JAR từ thư mục `frontend/`; không chạy server frontend riêng.

### 5.1. Lấy code và chuẩn bị schema mới

```powershell
# Chạy trong thư mục repository; nhớ lưu thay đổi local trước khi cập nhật.
git fetch origin
git switch develop
git pull --ff-only origin develop
java -version
.\backend\mvnw.cmd -f backend/pom.xml -v
```

Tạo schema **mới, chưa có bảng** trong MySQL Workbench (hoặc chọn tên khác nếu đã tồn tại):

```sql
CREATE DATABASE qldt_local_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

Tạo `backend/src/main/resources/application-local.properties` (được `.gitignore` loại trừ), ví dụ:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/qldt_local_demo?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate
server.port=8081
```

Lưu ý: `application.properties` mặc định **tắt Flyway**. Chỉ bật trong **local schema mới** khi khởi tạo; không tùy tiện bật trên Railway đang có dữ liệu. Tránh commit mật khẩu, token R2, thông tin SMTP.

```powershell
$pw = Read-Host "Mật khẩu MySQL local" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $pw).Password
.\backend\mvnw.cmd -f backend/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```

Địa chỉ mặc định local: **http://localhost:8081/login**. Lần đầu chạy trên schema trống, Flyway tạo cấu trúc **V1–V8**; không tự có tài khoản để đăng nhập. Muốn thử đăng nhập cần dữ liệu/tài khoản thử nghiệm được cấp trên **local** (không dùng lại bí mật production).

### 5.2. Đối chiếu Flyway và số bảng (chỉ đọc)

```sql
SELECT installed_rank, version, description, success
FROM qldt_local_demo.flyway_schema_history
ORDER BY installed_rank;

SELECT COUNT(*) AS so_bang_nghiep_vu
FROM information_schema.tables
WHERE table_schema = 'qldt_local_demo'
  AND table_type = 'BASE TABLE'
  AND table_name <> 'flyway_schema_history';
```

Với schema mới, kỳ vọng **8 migration thành công, 19 bảng nghiệp vụ**. Nếu đang dùng schema cũ/đã migrate một phần, kiểm tra trạng thái thực tế và backup trước khi thay đổi; **không chạy lại** V1–V8 bằng `SOURCE` hoặc import lại toàn database.

### 5.3. Dữ liệu demo và cảnh báo tương thích

`database/seed/` lưu **bộ seed legacy được thiết kế/kiểm thử trên V1–V5** (257 dòng demo + 5 vai trò tham chiếu, 6 PDF mẫu, số liệu theo snapshot cũ). Trong đó có thông báo `pham_vi='SINH_VIEN'/'GIANG_VIEN'`; **V8 chỉ cho `TAT_CA`/`CA_NHAN`**, nên không coi lệnh seed legacy là đã tương thích V8. **Không chạy `demo_seed.py seed` trực tiếp trên schema V8 hoặc Railway** khi chưa có bản nâng cấp và thử nghiệm trên bản sao local.

Dùng tài liệu [`database/seed/README.md`](database/seed/README.md) chỉ khi kiểm thử snapshot V1–V5 độc lập; sử dụng fixture MySQL trong CI cho mã nguồn hiện tại. Không xóa dữ liệu QA/phân trang, các tài khoản đã tạo, hay nhập lại dữ liệu production nhằm làm demo.

### 5.4. Chạy kiểm thử và nghiệm thu các phân hệ

```powershell
# Test mặc định: Unit, MVC, Security; không kết nối Railway hoặc R2 thật.
.\backend\mvnw.cmd -B -ntp -f backend/pom.xml clean verify
```

Workflow `.github/workflows/ci.yml` gồm hai nhóm kiểm thử chính: **Build, Unit, MVC and Security** và **Database & End-to-End Integration (MySQL 8.4)**, sau đó qua **CI Quality Gate**. Job MySQL chạy profile `-Pci-mysql` trên hai schema cô lập (`qldt_ci`, `nhom_test_ci`), kiểm tra Flyway, truy cập dữ liệu và luồng HTTP; **không kết nối hoặc ghi Railway production**. Không sử dụng profile test MySQL với URL của production.

Khi nghiệm thu trên môi trường triển khai, kiểm tra theo vai trò và theo luồng nghiệp vụ toàn hệ thống: đăng nhập/đăng xuất và phân quyền; quản lý người dùng, bộ môn và thông báo; tạo và công bố đợt đăng ký; đề xuất/duyệt/công bố đề tài; nhóm sinh viên, đăng ký đề tài và báo cáo PDF; phân công hội đồng, chấm điểm, tổng hợp/công bố và tra cứu kết quả; xuất dữ liệu Excel và gửi email khi đã cấu hình SMTP. Kiểm tra các tình huống không được cấp quyền, đầu vào không hợp lệ và CSRF trên những thao tác thay đổi dữ liệu.

PDF sử dụng Cloudflare R2: cần xác nhận tải lên, tải xuống và đối chiếu metadata/SHA-256; nếu R2 chưa cấu hình, nghiệp vụ lưu/tải PDF bị chặn có chủ đích. Kết quả **Maven BUILD SUCCESS** xác nhận test mặc định đã qua, không tự chứng minh mọi luồng production hoạt động đúng; đối chiếu thêm CI MySQL và smoke test trên Railway (môi trường chính).

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

- [Đặc tả yêu cầu FR/NFR](docs/Dac_Ta_FR_NFR_NHOM12.md) — 30 FR, 19 NFR.
- [Đặc tả Use Case](docs/USE_CASE_NHOM12_FINAL.md).
- [Quy ước tích hợp UI](docs/QUY_UOC_TICH_HOP_UI.md).

---

**Nhóm 12 — Hệ thống quản lý đề tài sinh viên Khoa CNTT · WEPR330479**
