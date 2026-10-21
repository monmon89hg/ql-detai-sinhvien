# Quy ước tích hợp Base UI 

## 1. Route điều hướng dự kiến

| Chức năng | Route | Nhóm nghiệp vụ |
|---|---|---|
| Tổng quan | `/dashboard` | Tổng quan |
| Quản lý tài khoản | `/admin/users` | Quản trị &amp; danh mục |
| Quản lý bộ môn | `/admin/bo-mon` | Quản trị &amp; danh mục |
| Thông báo | `/thong-bao` | Quản trị &amp; danh mục |
| Đợt đăng ký | `/dot-dang-ky` | Đợt đăng ký &amp; đề tài |
| Danh sách đề tài | `/de-tai` | Đợt đăng ký &amp; đề tài |
| Đề xuất đề tài | `/de-tai/de-xuat` | Đợt đăng ký &amp; đề tài |
| Duyệt / công bố đề tài | `/de-tai/duyet` | Đợt đăng ký &amp; đề tài |
| Tìm và tham gia nhóm | `/nhom` | Nhóm sinh viên &amp; báo cáo |
| Tạo nhóm sinh viên | `/nhom/tao` | Nhóm sinh viên &amp; báo cáo |
| Nhóm của tôi | `/nhom/cua-toi` | Nhóm sinh viên &amp; báo cáo |
| Đăng ký đề tài | `/dang-ky-de-tai` | Nhóm sinh viên &amp; báo cáo |
| Duyệt đăng ký nhóm | `/dang-ky-de-tai/duyet` | Nhóm sinh viên &amp; báo cáo |
| Nộp báo cáo | `/bao-cao` | Nhóm sinh viên &amp; báo cáo |
| Quản lý hội đồng | `/hoi-dong` | Hội đồng &amp; kết quả |
| Chấm điểm | `/cham-diem` | Hội đồng &amp; kết quả |
| Tổng hợp / công bố điểm | `/ket-qua/quan-ly` | Hội đồng &amp; kết quả |
| Tra cứu kết quả | `/ket-qua/cua-toi` | Hội đồng &amp; kết quả |

Các route ở đây chỉ là **đề xuất dùng chung**; chưa có Controller/Service tương ứng. Sau khi nhóm thống nhất thì hạn chế tự đổi tên route. Ví dụ mỗi người có thể thêm trang trong thư mục mình nhưng chỉ sử dụng header/sidebar CSS chung.

## 2. Tích hợp Thymeleaf

- Sửa HTML tại `frontend/templates/`, CSS/JS/ảnh tại `frontend/static/`; mã Java nằm trong `backend/src/main/java/`.
- Maven trong `backend/` đưa hai thư mục giao diện vào classpath và JAR. Không cần đổi tên view hoặc URL tài nguyên sau khi di chuyển; chạy lại backend để nhận HTML/CSS/JS mới.
- Trong trang thật, áp dụng `th:replace="~{fragments/head :: head('Tên trang')}"` cho `<head>`.
- Chèn header `th:replace="~{fragments/header :: header}"`, sidebar `th:replace="~{fragments/sidebar :: sidebar('menu-key')}"`.
- Sử dụng `fragments/breadcrumb.html`, `fragments/footer.html`, `fragments/messages.html`.
- CSS/JS lấy từ `/css/*.css` và `/js/common.js`.
- Đưa `currentUserName` từ Controller hoặc `@ControllerAdvice` cho header; không hardcode tên SV.
- **Bắt buộc bổ sung điều kiện hiển thị menu/nút theo vai trò ở server-side khi tích hợp Spring Security.** Base UI cố ý hiển thị toàn bộ chức năng để review mẫu giao diện, KHÔNG phải UI đã hoàn thiện phân quyền.
- Phân quyền thật thực hiện tại Controller/Service với `@PreAuthorize`, kiểm tra người dùng sở hữu nhóm/đề tài/kết quả.

## 3. Quy ước giao diện

- Xanh header `#214967`, sidebar `#f1f5f9`, active `#dbeaf6`, nền `#f7f9fc`, nội dung trắng.
- Font Be Vietnam Pro; có font hệ thống dự phòng; không gradient.
- Đừng viết CSS toàn cục từ trang con; thêm class riêng theo module nếu cần.
- Nút dashboard là **link chờ kết nối**, không thêm CRUD hay điều khiển ngoài rubric.

## 4. Thư mục theo mảng công việc

`auth`, `admin` | `topics`, `registration-rounds` | `groups`, `registrations`, `reports` | `assessment`, `results`. Họ tên/thành viên không hiển thị trên giao diện chung.
