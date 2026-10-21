## Thay đổi

Chuẩn bị bộ dữ liệu CSDL 2.0 cho các kịch bản tạo nhóm, đăng ký, PDF, hội đồng, chấm điểm và công bố kết quả.
Đủ **17 bảng, 257 dòng demo + 5 role V5 = 262 dòng**, giữ mã và hash demo cũ, mọi account demo vẫn khóa.

- Bổ sung sáu PDF thật, phiên bản, size và SHA-256; provision vào kho fixture local riêng trước khi ghi metadata.
- Thêm kiểm tra schema/PDF, helper local-only, validation trước COMMIT và rollback khi lỗi.
- Có SQL validation chỉ đọc, ma trận A–I, README và HANDOFF_VI.md.
- Có đợt đăng ký hợp lệ đến 31/10/2026 và các hồ sơ lịch sử chấm/công bố kết quả.

## Kiểm thử thực tế

MySQL Community **8.4.13**: **30 PASS, 0 FAIL, 6 SKIP**; **89 checks dữ liệu/FK/PDF, 0 FAIL**.
Minh chứng tại `database/seed/test_results.json`.

Đã thử seed mới/chạy lại, giữ dữ liệu ngoài demo và chỉnh sửa của chủ dữ liệu, negative FK/UNIQUE/CHECK,
đủ số phiếu nhưng sai danh tính giảng viên, checksum PDF, rollback khi trùng email giữa seed,
và từ chối nâng cấp seed v1 có timestamp sai mà không ghi đè hàng cũ.

Các SKIP: Service chặn người thứ sáu, HTTP report authorization, luồng website,
concurrency, Flyway engine/Spring startup và SMTP vì module chưa có hoặc môi trường chưa đủ.

## Giới hạn

**Database-ready chỉ trong QA local.** Chưa xác nhận website hoạt động hoặc PDF tải được qua ứng dụng.
Chưa có application storage; phải tích hợp private persistent storage và phân quyền trước import cloud.

Không sửa V1–V5 hoặc application feature, không kích hoạt account, không deploy/seed Railway.
Giữ PR **Draft**, chưa merge.
