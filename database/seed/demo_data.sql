-- DEMO DATA v2.0 - Nhóm 12 / CSDL 2.0 / MySQL 8.4
-- Mục tiêu: dữ liệu mẫu có FK/CHECK hợp lệ, ưu tiên UC11–UC17.
-- An toàn: KHÔNG có thông tin cá nhân thật, KHÔNG có mật khẩu mặc định.
-- Mọi tài khoản demo ở trạng thái KHOA; BCrypt ngẫu nhiên không có mật khẩu cấp cho ai.
-- Khi TV1 hoàn thiện login, tạo/khôi phục mật khẩu và mở khóa qua quy trình admin an toàn.
-- KHÔNG sửa V1–V5 đã áp dụng bởi Flyway. File này chạy riêng, chủ động, trên DB demo.
-- Tự kiểm tra SELECT DATABASE() trước khi chạy; script không chứa USE để tái sử dụng local/cloud.
-- Các mã dữ liệu mẫu mang tiền tố DEMO_ để tránh đụng dữ liệu thực.
-- Rerunnable: chỉ INSERT nếu PK chưa tồn tại; KHÔNG UPDATE/DELETE bản ghi cũ.
-- Lưu ý: nếu đã có dữ liệu dùng trùng email/mã nhưng khác PK, SQL sẽ báo lỗi (đúng).
-- Full local QA snapshot: 2026-10-20 12:00:00 Asia/Ho_Chi_Minh.
-- PDF keys use an explicit PRIVATE LOCAL QA store, NOT an implemented app storage path.
-- Use demo_seed.py: verifies real files before setting the preflight variables.
-- Railway/application import is BLOCKED until private application storage is integrated.

SET NAMES utf8mb4;
SELECT DATABASE() AS target_schema_before_seed;

-- Session-local CHECK gates; no persistent schema objects and no target schema hardcoded.
CREATE TEMPORARY TABLE IF NOT EXISTS demo_seed_preflight (
    gate VARCHAR(80) NOT NULL, ok TINYINT NOT NULL,
    CONSTRAINT ck_demo_seed_preflight CHECK (ok = 1)
);
INSERT INTO demo_seed_preflight SELECT 'SELECTED_SCHEMA_MUST_MATCH_EXPLICIT_EXPECTATION',
    COALESCE(BINARY DATABASE() = BINARY @demo_expected_schema AND CHAR_LENGTH(@demo_expected_schema) > 0, 0);
INSERT INTO demo_seed_preflight SELECT 'MYSQL_8_4_REQUIRED', VERSION() LIKE '8.4.%';
INSERT INTO demo_seed_preflight SELECT 'PRIVATE_LOCAL_QA_FILES_MUST_BE_VERIFIED_BY_HELPER',
    COALESCE(@demo_local_fixture_only = 1 AND BINARY @demo_fixture_manifest_sha = BINARY 'bcb71c3167edc6dd7195244d7f2e877f25dd8353353f26a9db2c331fc8ff8dcb', 0);
INSERT INTO demo_seed_preflight SELECT 'FIVE_V5_ROLES_REQUIRED', COUNT(*) = 5 FROM vai_tro
WHERE ma_vai_tro IN ('ADMIN','TRUONG_KHOA','TRUONG_BO_MON','GIANG_VIEN','SINH_VIEN');

START TRANSACTION;

-- 3 bộ môn giả lập
-- bo_mon: 3 dòng mẫu.
INSERT INTO bo_mon (ma_bo_mon, ten_bo_mon, mo_ta, created_at, updated_at)
SELECT s.ma_bo_mon, s.ten_bo_mon, s.mo_ta, s.created_at, '2026-10-20 12:00:00'
FROM (
    SELECT 'DEMO_CNPM' AS ma_bo_mon, 'Công nghệ phần mềm (demo)' AS ten_bo_mon, 'Dữ liệu mẫu phục vụ kiểm thử; không phải bộ môn thật' AS mo_ta, '2026-07-01 08:00:00' AS created_at
    UNION ALL SELECT 'DEMO_HTTT', 'Hệ thống thông tin (demo)', 'Dữ liệu mẫu phục vụ kiểm thử', '2026-07-01 08:00:00'
    UNION ALL SELECT 'DEMO_KHMT', 'Khoa học máy tính (demo)', 'Dữ liệu mẫu phục vụ kiểm thử', '2026-07-01 08:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM bo_mon AS e WHERE e.ma_bo_mon = s.ma_bo_mon);

-- 25 tài khoản giả lập; tất cả KHOA; mật khẩu BCrypt ngẫu nhiên KHÔNG công bố (không dùng để đăng nhập)
-- nguoi_dung: 25 dòng mẫu.
INSERT INTO nguoi_dung (ma_nguoi_dung, ho_ten, email, mat_khau_hash, loai_nguoi_dung, ma_bo_mon, trang_thai, lop_hanh_chinh, created_at, updated_at)
SELECT s.ma_nguoi_dung, s.ho_ten, s.email, s.mat_khau_hash, s.loai_nguoi_dung, s.ma_bo_mon, s.trang_thai, s.lop_hanh_chinh, s.created_at, '2026-10-20 12:00:00'
FROM (
    SELECT 'DEMO_CB01' AS ma_nguoi_dung, 'Cán bộ demo 01' AS ho_ten, 'demo.cb01@example.test' AS email, '$2y$10$Z5FfbJMNNLB09QZAtqqQJu0I3C73oxixyjayYGP0AxHUkXGTHLhmy' AS mat_khau_hash, 'CAN_BO' AS loai_nguoi_dung, NULL AS ma_bo_mon, 'KHOA' AS trang_thai, NULL AS lop_hanh_chinh, '2026-07-05 08:00:00' AS created_at
    UNION ALL SELECT 'DEMO_CB02', 'Cán bộ demo 02', 'demo.cb02@example.test', '$2y$10$/X/jfDQxNkkjmu5Er3NcIOm09.zT0TErVdvoLgj1EVLM5Q1Lu1tqW', 'CAN_BO', NULL, 'KHOA', NULL, '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_GV01', 'Giảng viên demo 01', 'demo.gv01@example.test', '$2y$10$JXklodncAJWCZrLninqvm.9bh3wWZmsWkx/fbOVSIL//29AvHQcp.', 'GV', 'DEMO_CNPM', 'KHOA', NULL, '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_GV02', 'Giảng viên demo 02', 'demo.gv02@example.test', '$2y$10$w9ebX8JStXt8Lw3z1SLXoORSmjSUvH.WHGI8ORBGPkoTO7d1aMVxS', 'GV', 'DEMO_HTTT', 'KHOA', NULL, '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_GV03', 'Giảng viên demo 03', 'demo.gv03@example.test', '$2y$10$T9aym1teDo0ScQ/pnp0G.uHq84I/RPDzmfxYsVpIK2SqcDvtkj2pi', 'GV', 'DEMO_KHMT', 'KHOA', NULL, '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_GV04', 'Giảng viên demo 04', 'demo.gv04@example.test', '$2y$10$WhwtPPUa09QJPdcdPxUzNudPCJCDPczhk0mZzez4WKKTQMLv1inSW', 'GV', 'DEMO_CNPM', 'KHOA', NULL, '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_GV05', 'Giảng viên demo 05', 'demo.gv05@example.test', '$2y$10$t1JflfcrrWpC4GNZdzPhrOxvo1om1Fd/Q2W5fGCvX1uWIGJ6NBvmq', 'GV', 'DEMO_HTTT', 'KHOA', NULL, '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_GV06', 'Giảng viên demo 06', 'demo.gv06@example.test', '$2y$10$0nVAh3jcplSVsF6BnBdo8O9AXvyhPSr3gLLKH0kevbcNKw0QfJIUO', 'GV', 'DEMO_KHMT', 'KHOA', NULL, '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV01', 'Sinh viên demo 01', 'demo.sv01@example.test', '$2y$10$aBcIv/gzFPiBo1d5h/22OuPEpc9lR9guKnrk1g0scrLOYFVAwWPFu', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV02', 'Sinh viên demo 02', 'demo.sv02@example.test', '$2y$10$feT2S3zdop.mA3Ebnf1Ite/hyAfLsm.38Kp7MDDN/aDK3vYIEO7Nm', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV03', 'Sinh viên demo 03', 'demo.sv03@example.test', '$2y$10$nFNtkV1DvNRdz1/uq48bUOj9bajQXz7r0PlxzkQCR0uGgHX5RflU2', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV04', 'Sinh viên demo 04', 'demo.sv04@example.test', '$2y$10$TItW/F5wtoM3AvoRgzgPduLD5cfn7qkQeiIchOB9.rlUC4VDHGua2', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV05', 'Sinh viên demo 05', 'demo.sv05@example.test', '$2y$10$tOQ3ttTS5L6CCWrZZtHOmOx0mZ74nedwLmy29N5gRQnWNj3Dl7Qxi', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV06', 'Sinh viên demo 06', 'demo.sv06@example.test', '$2y$10$m5DpCbaFp7zm7.9chTu9K.qG3f8FyTMs.RPuqHBzEyJhLQmYUUyh.', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV07', 'Sinh viên demo 07', 'demo.sv07@example.test', '$2y$10$Y0yfJJxGyO7ZE1.mLhdXa.mWBX4QLV4HCKGlsiSYWa6huzuq3jKBO', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV08', 'Sinh viên demo 08', 'demo.sv08@example.test', '$2y$10$DavIHsCP0Ek.14tQiCG6m.okwBX4mPuJvir0/IHMV5LoyjQzPu1pS', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV09', 'Sinh viên demo 09', 'demo.sv09@example.test', '$2y$10$MFxRE4nsCCdYzOdQRppAWevWsugxPemaxk9yBA44/WcGYyAJHlt2K', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV10', 'Sinh viên demo 10', 'demo.sv10@example.test', '$2y$10$LpgJUBGXI8JcHyvnZCUOhOzW/oTwfcZKgwaSaj7ZXVfqUhiCiv59O', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV11', 'Sinh viên demo 11', 'demo.sv11@example.test', '$2y$10$dtltjtVhfMPuSIaGHqV10OEdWg8UGP0G4xk0SL5zZ5SSda7Ol8Gt2', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV12', 'Sinh viên demo 12', 'demo.sv12@example.test', '$2y$10$ta6vgwtt8oe816akgcVJ3.1lzygQ1gSKYx96znzTgpDS7oi1kRg6e', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV13', 'Sinh viên demo 13', 'demo.sv13@example.test', '$2y$10$qwxnlz0Kz/ACuqXx5qZihuug2ryCkUn.gMtrtu3Ed3YSD3CNQVGkO', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV14', 'Sinh viên demo 14', 'demo.sv14@example.test', '$2y$10$NGvQrLYd9rJwPXPstHW.vOWy5uTtmzzV4NBxZWgvcafvMmVRQpvpS', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV15', 'Sinh viên demo 15', 'demo.sv15@example.test', '$2y$10$Osib3PQHcisB12vUSAbM4ublrWQYxuqUEto8Kdq9RWhDf2HLtjLem', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV16', 'Sinh viên demo 16', 'demo.sv16@example.test', '$2y$10$Lm9JZWRI8vRnwt9PEq/q/eAa//3Y.XoUeD0CNrm8mEuCDnO9U7PNi', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
    UNION ALL SELECT 'DEMO_SV17', 'Sinh viên demo 17', 'demo.sv17@example.test', '$2y$10$JYg3p/BXu46e0GIS4h4OH.IQAp0Y9pPzEf14ELC5Wd/QCRSrcYF7G', 'SV', NULL, 'KHOA', 'DEMO-K26', '2026-07-05 08:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM nguoi_dung AS e WHERE e.ma_nguoi_dung = s.ma_nguoi_dung);

-- Gán vai trò (vai_tro đã được V5 tạo); không cấp quyền từ tài khoản thật
-- nguoi_dung_vai_tro: 26 dòng mẫu.
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ma_nguoi_cap, created_at, updated_at)
SELECT s.ma_nguoi_dung, s.ma_vai_tro, s.ma_nguoi_cap, s.created_at, '2026-10-20 12:00:00'
FROM (
    SELECT 'DEMO_CB01' AS ma_nguoi_dung, 'ADMIN' AS ma_vai_tro, NULL AS ma_nguoi_cap, '2026-07-06 08:00:00' AS created_at
    UNION ALL SELECT 'DEMO_CB02', 'TRUONG_KHOA', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_GV01', 'GIANG_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_GV02', 'GIANG_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_GV03', 'GIANG_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_GV04', 'GIANG_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_GV05', 'GIANG_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_GV06', 'GIANG_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_GV01', 'TRUONG_BO_MON', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV01', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV02', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV03', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV04', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV05', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV06', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV07', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV08', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV09', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV10', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV11', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV12', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV13', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV14', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV15', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV16', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
    UNION ALL SELECT 'DEMO_SV17', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM nguoi_dung_vai_tro AS e WHERE e.ma_nguoi_dung = s.ma_nguoi_dung AND e.ma_vai_tro = s.ma_vai_tro);

-- 3 đợt theo loại đề tài; mốc 2026 cố định, về sau có thể đã hết hạn
-- dot_dang_ky: 3 dòng mẫu.
INSERT INTO dot_dang_ky (ma_dot, ten_dot, loai_de_tai, bat_dau_gv, ket_thuc_gv, bat_dau_sv, ket_thuc_sv, han_nop_bao_cao, han_nop_diem_pb, ngay_bao_cao_hd, trang_thai, ma_nguoi_tao, created_at, updated_at)
SELECT s.ma_dot, s.ten_dot, s.loai_de_tai, s.bat_dau_gv, s.ket_thuc_gv, s.bat_dau_sv, s.ket_thuc_sv, s.han_nop_bao_cao, s.han_nop_diem_pb, s.ngay_bao_cao_hd, s.trang_thai, s.ma_nguoi_tao, s.created_at, '2026-10-20 12:00:00'
FROM (
    SELECT 'DEMO_DOT_MH26' AS ma_dot, 'Đợt môn học 2026 (demo)' AS ten_dot, 'MON_HOC' AS loai_de_tai, '2026-10-01 00:00:00' AS bat_dau_gv, '2026-10-10 23:59:59' AS ket_thuc_gv, '2026-10-11 00:00:00' AS bat_dau_sv, '2026-10-28 23:59:59' AS ket_thuc_sv, '2026-11-15 23:59:59' AS han_nop_bao_cao, NULL AS han_nop_diem_pb, NULL AS ngay_bao_cao_hd, 'CONG_BO' AS trang_thai, 'DEMO_CB02' AS ma_nguoi_tao, '2026-07-10 08:00:00' AS created_at
    UNION ALL SELECT 'DEMO_DOT_NC26', 'Đợt nghiên cứu khoa học 2026 (demo)', 'NCKH', '2026-09-01 00:00:00', '2026-09-10 23:59:59', '2026-09-11 00:00:00', '2026-10-29 23:59:59', '2026-11-30 23:59:59', NULL, NULL, 'CONG_BO', 'DEMO_CB02', '2026-07-10 08:00:00'
    UNION ALL SELECT 'DEMO_DOT_KL26', 'Đợt khóa luận 2026 (demo)', 'KLTN', '2026-08-01 00:00:00', '2026-08-20 23:59:59', '2026-08-21 00:00:00', '2026-09-20 23:59:59', '2026-12-01 23:59:59', '2026-12-10 23:59:59', '2026-12-20', 'CONG_BO', 'DEMO_CB02', '2026-07-10 08:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM dot_dang_ky AS e WHERE e.ma_dot = s.ma_dot);

-- 9 đề tài công bố / nháp / chờ duyệt
-- de_tai: 9 dòng mẫu.
INSERT INTO de_tai (ma_de_tai, ma_dot, ma_bo_mon, ten_de_tai, noi_dung, yeu_cau, ma_nguoi_de_xuat, trang_thai, ma_nguoi_duyet, duyet_luc, cong_bo_luc, created_at, updated_at)
SELECT s.ma_de_tai, s.ma_dot, s.ma_bo_mon, s.ten_de_tai, s.noi_dung, s.yeu_cau, s.ma_nguoi_de_xuat, s.trang_thai, s.ma_nguoi_duyet, s.duyet_luc, s.cong_bo_luc, s.created_at, '2026-10-20 12:00:00'
FROM (
    SELECT 'DEMO_DT01' AS ma_de_tai, 'DEMO_DOT_MH26' AS ma_dot, 'DEMO_CNPM' AS ma_bo_mon, 'Quản lý thư viện số (demo)' AS ten_de_tai, 'Mô tả mẫu dành cho kiểm thử nghiệp vụ, không sử dụng trong học vụ thật.' AS noi_dung, 'Chạy demo; yêu cầu và dữ liệu chỉ phục vụ kiểm thử' AS yeu_cau, 'DEMO_GV01' AS ma_nguoi_de_xuat, 'CONG_BO' AS trang_thai, 'DEMO_CB02' AS ma_nguoi_duyet, '2026-10-09 10:00:00' AS duyet_luc, '2026-10-11 07:00:00' AS cong_bo_luc, '2026-10-03 08:00:00' AS created_at
    UNION ALL SELECT 'DEMO_DT02', 'DEMO_DOT_MH26', 'DEMO_HTTT', 'Hệ thống điểm danh lớp (demo)', 'Mô tả mẫu dành cho kiểm thử nghiệp vụ, không sử dụng trong học vụ thật.', 'Chạy demo; yêu cầu và dữ liệu chỉ phục vụ kiểm thử', 'DEMO_GV02', 'CONG_BO', 'DEMO_CB02', '2026-10-09 10:00:00', '2026-10-11 07:00:00', '2026-10-03 08:00:00'
    UNION ALL SELECT 'DEMO_DT03', 'DEMO_DOT_MH26', 'DEMO_KHMT', 'Gợi ý tài liệu học tập (demo)', 'Mô tả mẫu dành cho kiểm thử nghiệp vụ, không sử dụng trong học vụ thật.', 'Chạy demo; yêu cầu và dữ liệu chỉ phục vụ kiểm thử', 'DEMO_GV03', 'NHAP', NULL, NULL, NULL, '2026-10-03 08:00:00'
    UNION ALL SELECT 'DEMO_DT04', 'DEMO_DOT_MH26', 'DEMO_CNPM', 'Quản lý nhà thuốc (demo)', 'Mô tả mẫu dành cho kiểm thử nghiệp vụ, không sử dụng trong học vụ thật.', 'Chạy demo; yêu cầu và dữ liệu chỉ phục vụ kiểm thử', 'DEMO_GV04', 'CONG_BO', 'DEMO_CB02', '2026-10-09 10:00:00', '2026-10-11 07:00:00', '2026-10-03 08:00:00'
    UNION ALL SELECT 'DEMO_DT05', 'DEMO_DOT_NC26', 'DEMO_HTTT', 'Phân tích dữ liệu học tập (demo)', 'Mô tả mẫu dành cho kiểm thử nghiệp vụ, không sử dụng trong học vụ thật.', 'Chạy demo; yêu cầu và dữ liệu chỉ phục vụ kiểm thử', 'DEMO_GV03', 'CONG_BO', 'DEMO_CB02', '2026-09-09 10:00:00', '2026-09-11 07:00:00', '2026-09-03 08:00:00'
    UNION ALL SELECT 'DEMO_DT06', 'DEMO_DOT_NC26', 'DEMO_KHMT', 'Trực quan hóa tri thức (demo)', 'Mô tả mẫu dành cho kiểm thử nghiệp vụ, không sử dụng trong học vụ thật.', 'Chạy demo; yêu cầu và dữ liệu chỉ phục vụ kiểm thử', 'DEMO_GV06', 'CHO_DUYET', NULL, NULL, NULL, '2026-09-03 08:00:00'
    UNION ALL SELECT 'DEMO_DT07', 'DEMO_DOT_KL26', 'DEMO_CNPM', 'Nền tảng quản lý khóa luận (demo)', 'Mô tả mẫu dành cho kiểm thử nghiệp vụ, không sử dụng trong học vụ thật.', 'Chạy demo; yêu cầu và dữ liệu chỉ phục vụ kiểm thử', 'DEMO_GV05', 'CONG_BO', 'DEMO_CB02', '2026-08-19 10:00:00', '2026-08-21 07:00:00', '2026-08-03 08:00:00'
    UNION ALL SELECT 'DEMO_DT08', 'DEMO_DOT_KL26', 'DEMO_KHMT', 'Phân loại tài liệu bằng NLP (demo)', 'Mô tả mẫu dành cho kiểm thử nghiệp vụ, không sử dụng trong học vụ thật.', 'Chạy demo; yêu cầu và dữ liệu chỉ phục vụ kiểm thử', 'DEMO_GV06', 'CONG_BO', 'DEMO_CB02', '2026-08-19 10:00:00', '2026-08-21 07:00:00', '2026-08-03 08:00:00'
    UNION ALL SELECT 'DEMO_DT09', 'DEMO_DOT_KL26', 'DEMO_HTTT', 'Hệ thống tư vấn học phần (demo)', 'Mô tả mẫu dành cho kiểm thử nghiệp vụ, không sử dụng trong học vụ thật.', 'Chạy demo; yêu cầu và dữ liệu chỉ phục vụ kiểm thử', 'DEMO_GV02', 'CONG_BO', 'DEMO_CB02', '2026-08-19 10:00:00', '2026-08-21 07:00:00', '2026-08-03 08:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM de_tai AS e WHERE e.ma_de_tai = s.ma_de_tai);

-- Phân công GVHD + PB hoặc chấm độc lập, không kiêm nhiệm trong một đề tài
-- de_tai_giang_vien: 14 dòng mẫu.
INSERT INTO de_tai_giang_vien (ma_de_tai, ma_giang_vien, loai_phan_cong, ma_nguoi_phan_cong, phan_cong_luc, created_at, updated_at)
SELECT s.ma_de_tai, s.ma_giang_vien, s.loai_phan_cong, s.ma_nguoi_phan_cong, s.phan_cong_luc, s.created_at, '2026-10-20 12:00:00'
FROM (
    SELECT 'DEMO_DT01' AS ma_de_tai, 'DEMO_GV01' AS ma_giang_vien, 'HUONG_DAN' AS loai_phan_cong, 'DEMO_CB02' AS ma_nguoi_phan_cong, '2026-10-09 09:00:00' AS phan_cong_luc, '2026-10-09 09:00:00' AS created_at
    UNION ALL SELECT 'DEMO_DT01', 'DEMO_GV02', 'CHAM_DOC_LAP', 'DEMO_CB02', '2026-10-09 09:00:00', '2026-10-09 09:00:00'
    UNION ALL SELECT 'DEMO_DT02', 'DEMO_GV02', 'HUONG_DAN', 'DEMO_CB02', '2026-10-09 09:00:00', '2026-10-09 09:00:00'
    UNION ALL SELECT 'DEMO_DT02', 'DEMO_GV03', 'CHAM_DOC_LAP', 'DEMO_CB02', '2026-10-09 09:00:00', '2026-10-09 09:00:00'
    UNION ALL SELECT 'DEMO_DT04', 'DEMO_GV04', 'HUONG_DAN', 'DEMO_CB02', '2026-10-09 09:00:00', '2026-10-09 09:00:00'
    UNION ALL SELECT 'DEMO_DT04', 'DEMO_GV06', 'CHAM_DOC_LAP', 'DEMO_CB02', '2026-10-09 09:00:00', '2026-10-09 09:00:00'
    UNION ALL SELECT 'DEMO_DT05', 'DEMO_GV03', 'HUONG_DAN', 'DEMO_CB02', '2026-09-09 09:00:00', '2026-09-09 09:00:00'
    UNION ALL SELECT 'DEMO_DT05', 'DEMO_GV05', 'CHAM_DOC_LAP', 'DEMO_CB02', '2026-09-09 09:00:00', '2026-09-09 09:00:00'
    UNION ALL SELECT 'DEMO_DT07', 'DEMO_GV05', 'HUONG_DAN', 'DEMO_CB02', '2026-08-19 09:00:00', '2026-08-19 09:00:00'
    UNION ALL SELECT 'DEMO_DT07', 'DEMO_GV01', 'PHAN_BIEN', 'DEMO_CB02', '2026-08-19 09:00:00', '2026-08-19 09:00:00'
    UNION ALL SELECT 'DEMO_DT08', 'DEMO_GV06', 'HUONG_DAN', 'DEMO_CB02', '2026-08-19 09:00:00', '2026-08-19 09:00:00'
    UNION ALL SELECT 'DEMO_DT08', 'DEMO_GV02', 'PHAN_BIEN', 'DEMO_CB02', '2026-08-19 09:00:00', '2026-08-19 09:00:00'
    UNION ALL SELECT 'DEMO_DT09', 'DEMO_GV02', 'HUONG_DAN', 'DEMO_CB02', '2026-08-19 09:00:00', '2026-08-19 09:00:00'
    UNION ALL SELECT 'DEMO_DT09', 'DEMO_GV03', 'PHAN_BIEN', 'DEMO_CB02', '2026-08-19 09:00:00', '2026-08-19 09:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM de_tai_giang_vien AS e WHERE e.ma_de_tai = s.ma_de_tai AND e.ma_giang_vien = s.ma_giang_vien);

-- 5 nhóm ở nhiều trạng thái; nhóm DA_DUYET đã chốt danh sách
-- nhom_sinh_vien: 5 dòng mẫu.
INSERT INTO nhom_sinh_vien (ma_nhom, ten_nhom, ma_dot, ma_nguoi_tao, trang_thai, chot_luc, created_at, updated_at)
SELECT s.ma_nhom, s.ten_nhom, s.ma_dot, s.ma_nguoi_tao, s.trang_thai, s.chot_luc, s.created_at, '2026-10-20 12:00:00'
FROM (
    SELECT 'DEMO_N01' AS ma_nhom, 'Nhóm đang lập thiếu thành viên' AS ten_nhom, 'DEMO_DOT_MH26' AS ma_dot, 'DEMO_SV01' AS ma_nguoi_tao, 'DANG_LAP' AS trang_thai, NULL AS chot_luc, '2026-10-12 07:00:00' AS created_at
    UNION ALL SELECT 'DEMO_N02', 'Nhóm thư viện đã duyệt', 'DEMO_DOT_MH26', 'DEMO_SV04', 'DANG_THUC_HIEN', '2026-10-18 10:00:00', '2026-10-16 07:00:00'
    UNION ALL SELECT 'DEMO_N03', 'Nhóm NCKH chờ duyệt đăng ký', 'DEMO_DOT_NC26', 'DEMO_SV07', 'DANG_LAP', NULL, '2026-10-16 09:00:00'
    UNION ALL SELECT 'DEMO_N04', 'Nhóm khóa luận đã duyệt', 'DEMO_DOT_KL26', 'DEMO_SV10', 'DANG_THUC_HIEN', '2026-09-18 10:00:00', '2026-09-13 07:00:00'
    UNION ALL SELECT 'DEMO_N05', 'Nhóm đăng ký bị từ chối', 'DEMO_DOT_MH26', 'DEMO_SV14', 'DANG_LAP', NULL, '2026-10-16 07:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM nhom_sinh_vien AS e WHERE e.ma_nhom = s.ma_nhom);

-- 18 quan hệ thành viên gồm tham gia/chờ duyệt/từ chối; snapshot cho nhóm đã duyệt
-- thanh_vien_nhom: 18 dòng mẫu.
INSERT INTO thanh_vien_nhom (ma_nhom, ma_sinh_vien, trang_thai, la_nhom_truong, la_thanh_vien_chot, yeu_cau_luc, tham_gia_luc, ket_thuc_luc, created_at, updated_at)
SELECT s.ma_nhom, s.ma_sinh_vien, s.trang_thai, s.la_nhom_truong, s.la_thanh_vien_chot, s.yeu_cau_luc, s.tham_gia_luc, s.ket_thuc_luc, s.created_at, '2026-10-20 12:00:00'
FROM (
    SELECT 'DEMO_N01' AS ma_nhom, 'DEMO_SV01' AS ma_sinh_vien, 'THAM_GIA' AS trang_thai, 1 AS la_nhom_truong, 0 AS la_thanh_vien_chot, '2026-10-12 07:00:00' AS yeu_cau_luc, '2026-10-12 07:00:00' AS tham_gia_luc, NULL AS ket_thuc_luc, '2026-10-12 07:00:00' AS created_at
    UNION ALL SELECT 'DEMO_N01', 'DEMO_SV02', 'THAM_GIA', 0, 0, '2026-10-12 09:00:00', '2026-10-12 11:00:00', NULL, '2026-10-12 09:00:00'
    UNION ALL SELECT 'DEMO_N01', 'DEMO_SV03', 'CHO_DUYET', 0, 0, '2026-10-19 11:00:00', NULL, NULL, '2026-10-19 11:00:00'
    UNION ALL SELECT 'DEMO_N01', 'DEMO_SV16', 'TU_CHOI', 0, 0, '2026-10-16 06:00:00', NULL, NULL, '2026-10-16 06:00:00'
    UNION ALL SELECT 'DEMO_N02', 'DEMO_SV04', 'THAM_GIA', 1, 1, '2026-10-16 07:00:00', '2026-10-16 07:00:00', NULL, '2026-10-16 07:00:00'
    UNION ALL SELECT 'DEMO_N02', 'DEMO_SV05', 'THAM_GIA', 0, 1, '2026-10-16 08:00:00', '2026-10-17 09:00:00', NULL, '2026-10-16 08:00:00'
    UNION ALL SELECT 'DEMO_N02', 'DEMO_SV06', 'THAM_GIA', 0, 1, '2026-10-16 08:00:00', '2026-10-17 09:00:00', NULL, '2026-10-16 08:00:00'
    UNION ALL SELECT 'DEMO_N03', 'DEMO_SV07', 'THAM_GIA', 1, 0, '2026-10-16 09:00:00', '2026-10-16 09:00:00', NULL, '2026-10-16 09:00:00'
    UNION ALL SELECT 'DEMO_N03', 'DEMO_SV08', 'THAM_GIA', 0, 0, '2026-10-16 10:00:00', '2026-10-17 10:00:00', NULL, '2026-10-16 10:00:00'
    UNION ALL SELECT 'DEMO_N03', 'DEMO_SV09', 'THAM_GIA', 0, 0, '2026-10-16 10:00:00', '2026-10-17 10:00:00', NULL, '2026-10-16 10:00:00'
    UNION ALL SELECT 'DEMO_N04', 'DEMO_SV10', 'THAM_GIA', 1, 1, '2026-09-13 07:00:00', '2026-09-13 07:00:00', NULL, '2026-09-13 07:00:00'
    UNION ALL SELECT 'DEMO_N04', 'DEMO_SV11', 'THAM_GIA', 0, 1, '2026-09-13 08:00:00', '2026-09-14 09:00:00', NULL, '2026-09-13 08:00:00'
    UNION ALL SELECT 'DEMO_N04', 'DEMO_SV12', 'THAM_GIA', 0, 1, '2026-09-13 08:00:00', '2026-09-14 09:00:00', NULL, '2026-09-13 08:00:00'
    UNION ALL SELECT 'DEMO_N04', 'DEMO_SV13', 'THAM_GIA', 0, 1, '2026-09-13 08:00:00', '2026-09-14 09:00:00', NULL, '2026-09-13 08:00:00'
    UNION ALL SELECT 'DEMO_N05', 'DEMO_SV14', 'THAM_GIA', 1, 0, '2026-10-16 07:00:00', '2026-10-16 07:00:00', NULL, '2026-10-16 07:00:00'
    UNION ALL SELECT 'DEMO_N05', 'DEMO_SV15', 'THAM_GIA', 0, 0, '2026-10-16 08:00:00', '2026-10-17 09:00:00', NULL, '2026-10-16 08:00:00'
    UNION ALL SELECT 'DEMO_N05', 'DEMO_SV16', 'THAM_GIA', 0, 0, '2026-10-16 08:00:00', '2026-10-17 09:00:00', NULL, '2026-10-16 08:00:00'
    UNION ALL SELECT 'DEMO_N05', 'DEMO_SV17', 'THAM_GIA', 0, 0, '2026-10-16 08:00:00', '2026-10-17 09:00:00', NULL, '2026-10-16 08:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM thanh_vien_nhom AS e WHERE e.ma_nhom = s.ma_nhom AND e.ma_sinh_vien = s.ma_sinh_vien);

-- 4 đăng ký: đang thực hiện, chờ duyệt, từ chối; nhóm N01 thiếu người nên không có đăng ký
-- dang_ky_de_tai: 4 dòng mẫu.
INSERT INTO dang_ky_de_tai (ma_nhom, ma_de_tai, ma_nguoi_dang_ky, trang_thai, dang_ky_luc, ma_nguoi_duyet, duyet_luc, ly_do_tu_choi, created_at, updated_at)
SELECT s.ma_nhom, s.ma_de_tai, s.ma_nguoi_dang_ky, s.trang_thai, s.dang_ky_luc, s.ma_nguoi_duyet, s.duyet_luc, s.ly_do_tu_choi, s.created_at, '2026-10-20 12:00:00'
FROM (
    SELECT 'DEMO_N02' AS ma_nhom, 'DEMO_DT01' AS ma_de_tai, 'DEMO_SV04' AS ma_nguoi_dang_ky, 'DANG_THUC_HIEN' AS trang_thai, '2026-10-17 12:00:00' AS dang_ky_luc, 'DEMO_CB02' AS ma_nguoi_duyet, '2026-10-18 10:00:00' AS duyet_luc, NULL AS ly_do_tu_choi, '2026-10-17 12:00:00' AS created_at
    UNION ALL SELECT 'DEMO_N03', 'DEMO_DT05', 'DEMO_SV07', 'CHO_DUYET', '2026-10-19 12:00:00', NULL, NULL, NULL, '2026-10-19 12:00:00'
    UNION ALL SELECT 'DEMO_N04', 'DEMO_DT07', 'DEMO_SV10', 'DANG_THUC_HIEN', '2026-09-17 12:00:00', 'DEMO_CB02', '2026-09-18 10:00:00', NULL, '2026-09-17 12:00:00'
    UNION ALL SELECT 'DEMO_N05', 'DEMO_DT02', 'DEMO_SV14', 'TU_CHOI', '2026-10-18 12:00:00', 'DEMO_CB02', '2026-10-19 16:00:00', 'Thiếu phần giải trình yêu cầu (dữ liệu demo)', '2026-10-18 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM dang_ky_de_tai AS e WHERE e.ma_nhom = s.ma_nhom);

-- 2 thông báo demo theo phạm vi, đều được công bố
-- thong_bao: 2 dòng mẫu.
INSERT INTO thong_bao (ma_thong_bao, tieu_de, noi_dung, ma_nguoi_dang, ma_dot, pham_vi, trang_thai, cong_bo_luc, created_at, updated_at)
SELECT s.ma_thong_bao, s.tieu_de, s.noi_dung, s.ma_nguoi_dang, s.ma_dot, s.pham_vi, s.trang_thai, s.cong_bo_luc, s.created_at, '2026-10-20 12:00:00'
FROM (
    SELECT 'DEMO_TB01' AS ma_thong_bao, 'Thông báo mở đợt môn học (demo)' AS tieu_de, 'Dữ liệu thử nghiệm; thông báo không có hiệu lực học vụ.' AS noi_dung, 'DEMO_CB02' AS ma_nguoi_dang, 'DEMO_DOT_MH26' AS ma_dot, 'SINH_VIEN' AS pham_vi, 'CONG_BO' AS trang_thai, '2026-10-11 08:00:00' AS cong_bo_luc, '2026-10-10 08:00:00' AS created_at
    UNION ALL SELECT 'DEMO_TB02', 'Thông báo dành cho giảng viên (demo)', 'Nội dung kiểm thử theo phân quyền.', 'DEMO_CB01', NULL, 'GIANG_VIEN', 'CONG_BO', '2026-10-15 08:00:00', '2026-10-14 08:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM thong_bao AS e WHERE e.ma_thong_bao = s.ma_thong_bao);

-- Inactive reference department; additional 1 rows in bo_mon.
INSERT INTO bo_mon (ma_bo_mon, ten_bo_mon, mo_ta, hoat_dong, created_at, updated_at)
SELECT s.ma_bo_mon, s.ten_bo_mon, s.mo_ta, s.hoat_dong, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_OLD' AS ma_bo_mon, 'Bộ môn lưu trữ (demo)' AS ten_bo_mon, 'Danh mục đã ngừng hoạt động; không chọn cho dữ liệu mới.' AS mo_ta, 0 AS hoat_dong, '2026-07-01 08:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
) AS s
WHERE NOT EXISTS (SELECT 1 FROM bo_mon AS e WHERE e.ma_bo_mon = s.ma_bo_mon);


-- Fictional locked students; random discarded passwords, no published login secret; additional 22 rows in nguoi_dung.
INSERT INTO nguoi_dung (ma_nguoi_dung, ho_ten, email, mat_khau_hash, loai_nguoi_dung, trang_thai, buoc_doi_mat_khau, lop_hanh_chinh, created_at, updated_at)
SELECT s.ma_nguoi_dung, s.ho_ten, s.email, s.mat_khau_hash, s.loai_nguoi_dung, s.trang_thai, s.buoc_doi_mat_khau, s.lop_hanh_chinh, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_SV18' AS ma_nguoi_dung, 'Sinh viên demo 18' AS ho_ten, 'demo.sv18@example.test' AS email, '$2b$12$/kT4YHN83ciip7tuZD/XT.XeBpAqhuSPk38FzNp9c3.WIHeuH609C' AS mat_khau_hash, 'SV' AS loai_nguoi_dung, 'KHOA' AS trang_thai, 1 AS buoc_doi_mat_khau, 'DEMO-K26' AS lop_hanh_chinh, '2026-07-05 08:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_SV19', 'Sinh viên demo 19', 'demo.sv19@example.test', '$2b$12$5zs7tyWteNbSWssTc0Zjge8P4ZmbpTDwBIB8ujliSyH.ACsGewUwG', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV20', 'Sinh viên demo 20', 'demo.sv20@example.test', '$2b$12$2h3Ft.eEqc8JGg7AXyHLue65cGPdOAKNhRn6vT8W5E2/UyK4xh.DC', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV21', 'Sinh viên demo 21', 'demo.sv21@example.test', '$2b$12$2BVtv.i8OZUMOXAjN2zZkOrbNcgzv8PvxOyG1nLf8ULgfzVzL/nq.', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV22', 'Sinh viên demo 22', 'demo.sv22@example.test', '$2b$12$MWY70LhDvvMzvP6QfDLcfuGTrqSC1ippcBuM1JElprAGN.uCGYmEa', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV23', 'Sinh viên demo 23', 'demo.sv23@example.test', '$2b$12$VskMF4pOl3c/U2JJjEj.GevZM3ILxrb1tCqTHiWCj4j84HSajXBhu', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV24', 'Sinh viên demo 24', 'demo.sv24@example.test', '$2b$12$jjrbCremWOsJLjVSDPpIcOR5SnnHZsA0Yd.0xmIW69Nx3AfERuvhW', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV25', 'Sinh viên demo 25', 'demo.sv25@example.test', '$2b$12$J7d4RXVSzmBfcJ9vQYpheuJ/GuU0lLGlKfBiBsVBfMCAh0OXGBFeW', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV26', 'Sinh viên demo 26', 'demo.sv26@example.test', '$2b$12$goW7b/r8y0FRzeXOYzuB3OrRzW3/z2dn7nbeD8A/B5fdKsdf3tk7O', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV27', 'Sinh viên demo 27', 'demo.sv27@example.test', '$2b$12$GdYGBTafz9ddP4FW9BIZReCNIqOhYguWFRHh51JStNzPGC6LHKpWO', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV28', 'Sinh viên demo 28', 'demo.sv28@example.test', '$2b$12$qsT8zFRzCxXZeNu6YxInouO4NrW8zVAVaGwD2AyWGpU5KP9OJoNou', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV29', 'Sinh viên demo 29', 'demo.sv29@example.test', '$2b$12$NloiiaxhdzB8bQd/O6qc1u2ChINvJSCF1dBDvf6QFHV7Um5ZBlYYy', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV30', 'Sinh viên demo 30', 'demo.sv30@example.test', '$2b$12$dVakGuL3QQ5yFhNN25Vl7.lHoKis0YS1gS7Jb2xY3LG99UHtuByYy', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV31', 'Sinh viên demo 31', 'demo.sv31@example.test', '$2b$12$kHz0.wjJWLBr.NHyYwzsUe4M9FZXMFBi2/y.PzoTLGir6NbhiWdJK', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV32', 'Sinh viên demo 32', 'demo.sv32@example.test', '$2b$12$3/wsenOHbquMWEBhiA4bxeE50hWNfGs3yMHpZxcS76J7DhoQoavmG', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV33', 'Sinh viên demo 33', 'demo.sv33@example.test', '$2b$12$4wTpo3T9tuoNik5DoOoDGOY5K0DgR0fQbc3q7ucU.gMvg.dqRJEj2', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV34', 'Sinh viên demo 34', 'demo.sv34@example.test', '$2b$12$eVHIHIPrQDnuI0RlRMB5wek5gWASDI3aPPSJeN0KLKDTYw5LFENra', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV35', 'Sinh viên demo 35', 'demo.sv35@example.test', '$2b$12$7vF3O1mi3V1UjKjPDP7eMO.3CgFK66yxhVXsDVQt/A2KMWavQh47y', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV36', 'Sinh viên demo 36', 'demo.sv36@example.test', '$2b$12$DRmRtGRuP.lpSVmEb69D0OkUjYGLU8bf9TeRn8Sf91qWra8/HLIay', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV37', 'Sinh viên demo 37', 'demo.sv37@example.test', '$2b$12$8um3WMoBNJDqtGo..OqbyOUI6MuMrvlYGeellRz2s4bHPeVtPmjZa', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV38', 'Sinh viên demo 38', 'demo.sv38@example.test', '$2b$12$Akoe2zP406LDk0ARh5ExC.gYsRB4cyUNixqZtlaYjSr27iMWFWqWq', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV39', 'Sinh viên demo 39', 'demo.sv39@example.test', '$2b$12$PvgdqIBTXmHG/s7Tuq2sR.X4T2KAZT/eEtbkE2oa60nJm1hsXNMmi', 'SV', 'KHOA', 1, 'DEMO-K26', '2026-07-05 08:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM nguoi_dung AS e WHERE e.ma_nguoi_dung = s.ma_nguoi_dung);


-- Preserve V5 roles; append student role links; additional 22 rows in nguoi_dung_vai_tro.
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ma_nguoi_cap, created_at, updated_at)
SELECT s.ma_nguoi_dung, s.ma_vai_tro, s.ma_nguoi_cap, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_SV18' AS ma_nguoi_dung, 'SINH_VIEN' AS ma_vai_tro, 'DEMO_CB01' AS ma_nguoi_cap, '2026-07-06 08:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_SV19', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV20', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV21', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV22', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV23', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV24', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV25', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV26', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV27', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV28', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV29', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV30', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV31', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV32', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV33', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV34', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV35', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV36', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV37', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV38', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_SV39', 'SINH_VIEN', 'DEMO_CB01', '2026-07-06 08:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM nguoi_dung_vai_tro AS e WHERE e.ma_nguoi_dung = s.ma_nguoi_dung AND e.ma_vai_tro = s.ma_vai_tro);


-- Published live round through October 31, closed historical rounds, draft TLCN; additional 4 rows in dot_dang_ky.
INSERT INTO dot_dang_ky (ma_dot, ten_dot, loai_de_tai, bat_dau_gv, ket_thuc_gv, bat_dau_sv, ket_thuc_sv, han_nop_bao_cao, han_nop_diem_pb, ngay_bao_cao_hd, trang_thai, ma_nguoi_tao, created_at, updated_at)
SELECT s.ma_dot, s.ten_dot, s.loai_de_tai, s.bat_dau_gv, s.ket_thuc_gv, s.bat_dau_sv, s.ket_thuc_sv, s.han_nop_bao_cao, s.han_nop_diem_pb, s.ngay_bao_cao_hd, s.trang_thai, s.ma_nguoi_tao, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_DOT_LATE26' AS ma_dot, 'Đợt trình diễn cuối tháng 10 (demo)' AS ten_dot, 'MON_HOC' AS loai_de_tai, '2026-10-01 00:00:00' AS bat_dau_gv, '2026-10-10 23:59:59' AS ket_thuc_gv, '2026-10-11 00:00:00' AS bat_dau_sv, '2026-10-31 23:59:59' AS ket_thuc_sv, '2026-11-15 23:59:59' AS han_nop_bao_cao, NULL AS han_nop_diem_pb, NULL AS ngay_bao_cao_hd, 'CONG_BO' AS trang_thai, 'DEMO_CB02' AS ma_nguoi_tao, '2026-07-10 08:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_DOT_KLH26', 'Khóa luận lịch sử tháng 9 (demo)', 'KLTN', '2026-07-11 00:00:00', '2026-07-20 23:59:59', '2026-07-21 00:00:00', '2026-08-10 23:59:59', '2026-09-10 23:59:59', '2026-09-15 23:59:59', '2026-09-20', 'DONG', 'DEMO_CB02', '2026-07-10 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DOT_MHH26', 'Môn học lịch sử tháng 9 (demo)', 'MON_HOC', '2026-07-11 00:00:00', '2026-07-20 23:59:59', '2026-07-21 00:00:00', '2026-08-10 23:59:59', '2026-09-10 23:59:59', NULL, NULL, 'DONG', 'DEMO_CB02', '2026-07-10 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DOT_NEXT27', 'Đợt tiểu luận chuyên ngành chưa mở (demo)', 'TLCN', '2027-01-01 00:00:00', '2027-01-10 23:59:59', '2027-01-11 00:00:00', '2027-01-20 23:59:59', '2027-02-10 23:59:59', '2027-02-12 23:59:59', NULL, 'NHAP', 'DEMO_CB02', '2026-10-20 08:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM dot_dang_ky AS e WHERE e.ma_dot = s.ma_dot);


-- Published, approved unpublished and rejected topics with review evidence; additional 8 rows in de_tai.
INSERT INTO de_tai (ma_de_tai, ma_dot, ma_bo_mon, ten_de_tai, noi_dung, yeu_cau, ma_nguoi_de_xuat, trang_thai, ma_nguoi_duyet, duyet_luc, ly_do_tu_choi, cong_bo_luc, created_at, updated_at)
SELECT s.ma_de_tai, s.ma_dot, s.ma_bo_mon, s.ten_de_tai, s.noi_dung, s.yeu_cau, s.ma_nguoi_de_xuat, s.trang_thai, s.ma_nguoi_duyet, s.duyet_luc, s.ly_do_tu_choi, s.cong_bo_luc, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_DT10' AS ma_de_tai, 'DEMO_DOT_LATE26' AS ma_dot, 'DEMO_CNPM' AS ma_bo_mon, 'Theo dõi thiết bị thực hành (demo)' AS ten_de_tai, 'Dữ liệu hư cấu; mô hình dữ liệu, phân quyền, báo cáo và minh chứng kiểm thử.' AS noi_dung, 'Báo cáo PDF và kiểm thử nghiệp vụ; không chứa dữ liệu thật.' AS yeu_cau, 'DEMO_GV01' AS ma_nguoi_de_xuat, 'CONG_BO' AS trang_thai, 'DEMO_CB02' AS ma_nguoi_duyet, '2026-10-09 10:00:00' AS duyet_luc, NULL AS ly_do_tu_choi, '2026-10-11 07:00:00' AS cong_bo_luc, '2026-10-03 08:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_DT11', 'DEMO_DOT_LATE26', 'DEMO_CNPM', 'Quản lý lịch tư vấn học tập (demo)', 'Dữ liệu hư cấu; mô hình dữ liệu, phân quyền, báo cáo và minh chứng kiểm thử.', 'Báo cáo PDF và kiểm thử nghiệp vụ; không chứa dữ liệu thật.', 'DEMO_GV04', 'CONG_BO', 'DEMO_CB02', '2026-10-09 10:00:00', NULL, '2026-10-11 07:00:00', '2026-10-03 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT12', 'DEMO_DOT_MH26', 'DEMO_CNPM', 'Tra cứu tài nguyên bộ môn (demo)', 'Dữ liệu hư cấu; mô hình dữ liệu, phân quyền, báo cáo và minh chứng kiểm thử.', 'Báo cáo PDF và kiểm thử nghiệp vụ; không chứa dữ liệu thật.', 'DEMO_GV04', 'DA_DUYET', 'DEMO_CB02', '2026-10-09 10:00:00', NULL, NULL, '2026-10-03 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT13', 'DEMO_DOT_MH26', 'DEMO_CNPM', 'Nền tảng học tập chưa đủ phạm vi (demo)', 'Dữ liệu hư cấu; mô hình dữ liệu, phân quyền, báo cáo và minh chứng kiểm thử.', 'Báo cáo PDF và kiểm thử nghiệp vụ; không chứa dữ liệu thật.', 'DEMO_GV04', 'TU_CHOI', 'DEMO_CB02', '2026-10-09 10:00:00', 'Phạm vi quá rộng, chưa xác định dữ liệu và tiêu chí nghiệm thu (demo).', NULL, '2026-10-03 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT14', 'DEMO_DOT_KLH26', 'DEMO_CNPM', 'Kho lưu trữ khóa luận (demo)', 'Dữ liệu hư cấu; mô hình dữ liệu, phân quyền, báo cáo và minh chứng kiểm thử.', 'Báo cáo PDF và kiểm thử nghiệp vụ; không chứa dữ liệu thật.', 'DEMO_GV01', 'CONG_BO', 'DEMO_CB02', '2026-07-19 10:00:00', NULL, '2026-07-21 07:00:00', '2026-07-12 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT15', 'DEMO_DOT_KLH26', 'DEMO_CNPM', 'Tìm kiếm tri thức học đường (demo)', 'Dữ liệu hư cấu; mô hình dữ liệu, phân quyền, báo cáo và minh chứng kiểm thử.', 'Báo cáo PDF và kiểm thử nghiệp vụ; không chứa dữ liệu thật.', 'DEMO_GV01', 'CONG_BO', 'DEMO_CB02', '2026-07-19 10:00:00', NULL, '2026-07-21 07:00:00', '2026-07-12 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT16', 'DEMO_DOT_KLH26', 'DEMO_CNPM', 'Chỉ mục tài nguyên học tập (demo)', 'Dữ liệu hư cấu; mô hình dữ liệu, phân quyền, báo cáo và minh chứng kiểm thử.', 'Báo cáo PDF và kiểm thử nghiệp vụ; không chứa dữ liệu thật.', 'DEMO_GV01', 'CONG_BO', 'DEMO_CB02', '2026-07-19 10:00:00', NULL, '2026-07-21 07:00:00', '2026-07-12 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT17', 'DEMO_DOT_MHH26', 'DEMO_CNPM', 'Đặt lịch phòng thí nghiệm (demo)', 'Dữ liệu hư cấu; mô hình dữ liệu, phân quyền, báo cáo và minh chứng kiểm thử.', 'Báo cáo PDF và kiểm thử nghiệp vụ; không chứa dữ liệu thật.', 'DEMO_GV01', 'CONG_BO', 'DEMO_CB02', '2026-07-19 10:00:00', NULL, '2026-07-21 07:00:00', '2026-07-12 08:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM de_tai AS e WHERE e.ma_de_tai = s.ma_de_tai);


-- Supervisor exists before approval; reviewers are distinct from supervisors; additional 13 rows in de_tai_giang_vien.
INSERT INTO de_tai_giang_vien (ma_de_tai, ma_giang_vien, loai_phan_cong, ma_nguoi_phan_cong, phan_cong_luc, created_at, updated_at)
SELECT s.ma_de_tai, s.ma_giang_vien, s.loai_phan_cong, s.ma_nguoi_phan_cong, s.phan_cong_luc, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_DT10' AS ma_de_tai, 'DEMO_GV01' AS ma_giang_vien, 'HUONG_DAN' AS loai_phan_cong, 'DEMO_CB02' AS ma_nguoi_phan_cong, '2026-10-09 09:00:00' AS phan_cong_luc, '2026-10-09 09:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_DT10', 'DEMO_GV02', 'CHAM_DOC_LAP', 'DEMO_CB02', '2026-10-09 09:00:00', '2026-10-09 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT11', 'DEMO_GV04', 'HUONG_DAN', 'DEMO_CB02', '2026-10-09 09:00:00', '2026-10-09 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT11', 'DEMO_GV06', 'CHAM_DOC_LAP', 'DEMO_CB02', '2026-10-09 09:00:00', '2026-10-09 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT12', 'DEMO_GV04', 'HUONG_DAN', 'DEMO_CB02', '2026-10-09 09:00:00', '2026-10-09 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT14', 'DEMO_GV01', 'HUONG_DAN', 'DEMO_CB02', '2026-07-19 09:00:00', '2026-07-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT14', 'DEMO_GV02', 'PHAN_BIEN', 'DEMO_CB02', '2026-07-19 09:00:00', '2026-07-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT15', 'DEMO_GV01', 'HUONG_DAN', 'DEMO_CB02', '2026-07-19 09:00:00', '2026-07-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT15', 'DEMO_GV02', 'PHAN_BIEN', 'DEMO_CB02', '2026-07-19 09:00:00', '2026-07-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT16', 'DEMO_GV01', 'HUONG_DAN', 'DEMO_CB02', '2026-07-19 09:00:00', '2026-07-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT16', 'DEMO_GV02', 'PHAN_BIEN', 'DEMO_CB02', '2026-07-19 09:00:00', '2026-07-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT17', 'DEMO_GV01', 'HUONG_DAN', 'DEMO_CB02', '2026-07-19 09:00:00', '2026-07-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_DT17', 'DEMO_GV02', 'CHAM_DOC_LAP', 'DEMO_CB02', '2026-07-19 09:00:00', '2026-07-19 09:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM de_tai_giang_vien AS e WHERE e.ma_de_tai = s.ma_de_tai AND e.ma_giang_vien = s.ma_giang_vien);


-- Ready, finalized, assessing and completed groups; preserve old N01-N05; additional 6 rows in nhom_sinh_vien.
INSERT INTO nhom_sinh_vien (ma_nhom, ten_nhom, ma_dot, ma_nguoi_tao, trang_thai, chot_luc, ket_thuc_luc, created_at, updated_at)
SELECT s.ma_nhom, s.ten_nhom, s.ma_dot, s.ma_nguoi_tao, s.trang_thai, s.chot_luc, s.ket_thuc_luc, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_N06' AS ma_nhom, 'Nhóm sẵn sàng đăng ký (demo)' AS ten_nhom, 'DEMO_DOT_LATE26' AS ma_dot, 'DEMO_SV18' AS ma_nguoi_tao, 'DANG_LAP' AS trang_thai, NULL AS chot_luc, NULL AS ket_thuc_luc, '2026-10-18 07:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_N07', 'Nhóm vừa được duyệt năm thành viên (demo)', 'DEMO_DOT_LATE26', 'DEMO_SV21', 'DA_CHOT', '2026-10-20 09:00:00', NULL, '2026-10-18 07:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N08', 'Nhóm đã công bố kết quả (demo)', 'DEMO_DOT_KLH26', 'DEMO_SV26', 'HOAN_THANH', '2026-08-04 10:00:00', '2026-09-21 08:00:00', '2026-08-01 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N09', 'Nhóm đã tổng hợp chưa công bố (demo)', 'DEMO_DOT_KLH26', 'DEMO_SV29', 'DANG_THUC_HIEN', '2026-08-04 10:00:00', NULL, '2026-08-01 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'Nhóm còn phiếu nháp (demo)', 'DEMO_DOT_KLH26', 'DEMO_SV32', 'DANG_THUC_HIEN', '2026-08-04 10:00:00', NULL, '2026-08-01 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N11', 'Nhóm chấm độc lập (demo)', 'DEMO_DOT_MHH26', 'DEMO_SV35', 'DANG_THUC_HIEN', '2026-08-04 10:00:00', NULL, '2026-08-01 08:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM nhom_sinh_vien AS e WHERE e.ma_nhom = s.ma_nhom);


-- Exactly one current leader, or one historical snapshot leader for completed groups; additional 20 rows in thanh_vien_nhom.
INSERT INTO thanh_vien_nhom (ma_nhom, ma_sinh_vien, trang_thai, la_nhom_truong, la_thanh_vien_chot, yeu_cau_luc, tham_gia_luc, ket_thuc_luc, created_at, updated_at)
SELECT s.ma_nhom, s.ma_sinh_vien, s.trang_thai, s.la_nhom_truong, s.la_thanh_vien_chot, s.yeu_cau_luc, s.tham_gia_luc, s.ket_thuc_luc, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_N06' AS ma_nhom, 'DEMO_SV18' AS ma_sinh_vien, 'THAM_GIA' AS trang_thai, 1 AS la_nhom_truong, 0 AS la_thanh_vien_chot, '2026-10-18 07:00:00' AS yeu_cau_luc, '2026-10-18 07:00:00' AS tham_gia_luc, NULL AS ket_thuc_luc, '2026-10-18 07:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_N06', 'DEMO_SV19', 'THAM_GIA', 0, 0, '2026-10-19 09:00:00', '2026-10-19 09:00:00', NULL, '2026-10-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N06', 'DEMO_SV20', 'THAM_GIA', 0, 0, '2026-10-19 09:00:00', '2026-10-19 09:00:00', NULL, '2026-10-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N07', 'DEMO_SV21', 'THAM_GIA', 1, 1, '2026-10-18 07:00:00', '2026-10-18 07:00:00', NULL, '2026-10-18 07:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N07', 'DEMO_SV22', 'THAM_GIA', 0, 1, '2026-10-19 09:00:00', '2026-10-19 09:00:00', NULL, '2026-10-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N07', 'DEMO_SV23', 'THAM_GIA', 0, 1, '2026-10-19 09:00:00', '2026-10-19 09:00:00', NULL, '2026-10-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N07', 'DEMO_SV24', 'THAM_GIA', 0, 1, '2026-10-19 09:00:00', '2026-10-19 09:00:00', NULL, '2026-10-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N07', 'DEMO_SV25', 'THAM_GIA', 0, 1, '2026-10-19 09:00:00', '2026-10-19 09:00:00', NULL, '2026-10-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N08', 'DEMO_SV26', 'KET_THUC', 1, 1, '2026-08-01 08:00:00', '2026-08-01 08:00:00', '2026-09-21 08:00:00', '2026-08-01 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N08', 'DEMO_SV27', 'KET_THUC', 0, 1, '2026-08-02 09:00:00', '2026-08-02 09:00:00', '2026-09-21 08:00:00', '2026-08-02 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N08', 'DEMO_SV28', 'KET_THUC', 0, 1, '2026-08-02 09:00:00', '2026-08-02 09:00:00', '2026-09-21 08:00:00', '2026-08-02 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N09', 'DEMO_SV29', 'THAM_GIA', 1, 1, '2026-08-01 08:00:00', '2026-08-01 08:00:00', NULL, '2026-08-01 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N09', 'DEMO_SV30', 'THAM_GIA', 0, 1, '2026-08-02 09:00:00', '2026-08-02 09:00:00', NULL, '2026-08-02 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N09', 'DEMO_SV31', 'THAM_GIA', 0, 1, '2026-08-02 09:00:00', '2026-08-02 09:00:00', NULL, '2026-08-02 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'DEMO_SV32', 'THAM_GIA', 1, 1, '2026-08-01 08:00:00', '2026-08-01 08:00:00', NULL, '2026-08-01 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'DEMO_SV33', 'THAM_GIA', 0, 1, '2026-08-02 09:00:00', '2026-08-02 09:00:00', NULL, '2026-08-02 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'DEMO_SV34', 'THAM_GIA', 0, 1, '2026-08-02 09:00:00', '2026-08-02 09:00:00', NULL, '2026-08-02 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N11', 'DEMO_SV35', 'THAM_GIA', 1, 1, '2026-08-01 08:00:00', '2026-08-01 08:00:00', NULL, '2026-08-01 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N11', 'DEMO_SV36', 'THAM_GIA', 0, 1, '2026-08-02 09:00:00', '2026-08-02 09:00:00', NULL, '2026-08-02 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N11', 'DEMO_SV37', 'THAM_GIA', 0, 1, '2026-08-02 09:00:00', '2026-08-02 09:00:00', NULL, '2026-08-02 09:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM thanh_vien_nhom AS e WHERE e.ma_nhom = s.ma_nhom AND e.ma_sinh_vien = s.ma_sinh_vien);


-- Approved, assessing and completed registrations; no invented rejection retry policy; additional 5 rows in dang_ky_de_tai.
INSERT INTO dang_ky_de_tai (ma_nhom, ma_de_tai, ma_nguoi_dang_ky, trang_thai, dang_ky_luc, ma_nguoi_duyet, duyet_luc, bat_dau_cham_luc, ket_thuc_luc, created_at, updated_at)
SELECT s.ma_nhom, s.ma_de_tai, s.ma_nguoi_dang_ky, s.trang_thai, s.dang_ky_luc, s.ma_nguoi_duyet, s.duyet_luc, s.bat_dau_cham_luc, s.ket_thuc_luc, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_N07' AS ma_nhom, 'DEMO_DT11' AS ma_de_tai, 'DEMO_SV21' AS ma_nguoi_dang_ky, 'DA_DUYET' AS trang_thai, '2026-10-19 12:00:00' AS dang_ky_luc, 'DEMO_CB02' AS ma_nguoi_duyet, '2026-10-20 09:00:00' AS duyet_luc, NULL AS bat_dau_cham_luc, NULL AS ket_thuc_luc, '2026-10-19 12:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_N08', 'DEMO_DT14', 'DEMO_SV26', 'HOAN_THANH', '2026-08-03 12:00:00', 'DEMO_CB02', '2026-08-04 10:00:00', '2026-09-11 08:00:00', '2026-09-21 08:00:00', '2026-08-03 12:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N09', 'DEMO_DT15', 'DEMO_SV29', 'DANG_CHAM', '2026-08-03 12:00:00', 'DEMO_CB02', '2026-08-04 10:00:00', '2026-09-11 08:00:00', NULL, '2026-08-03 12:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'DEMO_DT16', 'DEMO_SV32', 'DANG_CHAM', '2026-08-03 12:00:00', 'DEMO_CB02', '2026-08-04 10:00:00', '2026-09-11 08:00:00', NULL, '2026-08-03 12:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N11', 'DEMO_DT17', 'DEMO_SV35', 'DANG_CHAM', '2026-08-03 12:00:00', 'DEMO_CB02', '2026-08-04 10:00:00', '2026-09-11 08:00:00', NULL, '2026-08-03 12:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM dang_ky_de_tai AS e WHERE e.ma_nhom = s.ma_nhom);


-- Announcement visibility fixtures: draft and hidden; additional 2 rows in thong_bao.
INSERT INTO thong_bao (ma_thong_bao, tieu_de, noi_dung, ma_nguoi_dang, ma_dot, pham_vi, trang_thai, cong_bo_luc, created_at, updated_at)
SELECT s.ma_thong_bao, s.tieu_de, s.noi_dung, s.ma_nguoi_dang, s.ma_dot, s.pham_vi, s.trang_thai, s.cong_bo_luc, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_TB03' AS ma_thong_bao, 'Thông báo còn nháp (demo)' AS tieu_de, 'Chưa hiển thị cho người đọc thông thường.' AS noi_dung, 'DEMO_CB01' AS ma_nguoi_dang, 'DEMO_DOT_NEXT27' AS ma_dot, 'TAT_CA' AS pham_vi, 'NHAP' AS trang_thai, NULL AS cong_bo_luc, '2026-10-20 08:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_TB04', 'Thông báo đã ẩn (demo)', 'Đã công bố rồi ẩn; giữ mốc công bố để đối chiếu lịch sử.', 'DEMO_CB02', 'DEMO_DOT_MH26', 'SINH_VIEN', 'AN', '2026-10-11 08:00:00', '2026-10-10 08:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM thong_bao AS e WHERE e.ma_thong_bao = s.ma_thong_bao);


-- Real verified PDFs; paths are private-local QA object keys, not website URLs; additional 6 rows in nop_bao_cao.
INSERT INTO nop_bao_cao (ma_nhom, phien_ban, ma_nguoi_nop, ten_file_goc, object_key, mime_type, kich_thuoc_byte, sha256, nop_luc, trang_thai, created_at, updated_at)
SELECT s.ma_nhom, s.phien_ban, s.ma_nguoi_nop, s.ten_file_goc, s.object_key, s.mime_type, s.kich_thuoc_byte, s.sha256, s.nop_luc, s.trang_thai, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_N02' AS ma_nhom, 1 AS phien_ban, 'DEMO_SV04' AS ma_nguoi_nop, 'DEMO_N02-v1.pdf' AS ten_file_goc, 'demo-fixtures/v1/DEMO_N02/v1.pdf' AS object_key, 'application/pdf' AS mime_type, 66001 AS kich_thuoc_byte, 'e6be786edab36e3a11d9e75d364d068791fd3f382e4f075f6fd76ec77290c7e0' AS sha256, '2026-10-19 14:00:00' AS nop_luc, 'HOP_LE' AS trang_thai, '2026-10-19 14:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_N08', 1, 'DEMO_SV26', 'DEMO_N08-v1.pdf', 'demo-fixtures/v1/DEMO_N08/v1.pdf', 'application/pdf', 66014, 'a58ecba2da7469735b77f549da4cb8d32779d6bdd5ecd48e33c16566637457ba', '2026-09-05 09:00:00', 'HOP_LE', '2026-09-05 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N08', 2, 'DEMO_SV26', 'DEMO_N08-v2.pdf', 'demo-fixtures/v1/DEMO_N08/v2.pdf', 'application/pdf', 66027, 'aa261d7e7d6a511685572a1e02c0e908b6fb4e61e71c260df7791f58babcbeb5', '2026-09-09 09:00:00', 'HOP_LE', '2026-09-09 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N09', 1, 'DEMO_SV29', 'DEMO_N09-v1.pdf', 'demo-fixtures/v1/DEMO_N09/v1.pdf', 'application/pdf', 66016, 'f45213ce4d45bde8b2491cfa6391ca2671cf337337af1afa714b438541c8742b', '2026-09-08 09:00:00', 'HOP_LE', '2026-09-08 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 1, 'DEMO_SV32', 'DEMO_N10-v1.pdf', 'demo-fixtures/v1/DEMO_N10/v1.pdf', 'application/pdf', 66029, 'bd9c51d6841606a42e0bb5cbcc021c9048b0455f784b93521ea0363cd8876771', '2026-09-09 10:00:00', 'HOP_LE', '2026-09-09 10:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N11', 1, 'DEMO_SV35', 'DEMO_N11-v1.pdf', 'demo-fixtures/v1/DEMO_N11/v1.pdf', 'application/pdf', 66017, '82fb1c02635e3bfe092d49a5dc7e2bdc4189e6ce4cee474f5c5290811a205e99', '2026-09-09 11:00:00', 'HOP_LE', '2026-09-09 11:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM nop_bao_cao AS e WHERE e.ma_nhom = s.ma_nhom AND e.phien_ban = s.phien_ban);


-- KLTN committees only; draft may be incomplete; additional 5 rows in hoi_dong.
INSERT INTO hoi_dong (ma_hoi_dong, ten_hoi_dong, ma_dot, dia_diem, trang_thai, ma_nguoi_tao, created_at, updated_at)
SELECT s.ma_hoi_dong, s.ten_hoi_dong, s.ma_dot, s.dia_diem, s.trang_thai, s.ma_nguoi_tao, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_HD01' AS ma_hoi_dong, 'Hội đồng demo 01' AS ten_hoi_dong, 'DEMO_DOT_KLH26' AS ma_dot, 'Phòng DEMO-01' AS dia_diem, 'DA_KHOA' AS trang_thai, 'DEMO_CB02' AS ma_nguoi_tao, '2026-08-05 08:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_HD02', 'Hội đồng demo 02', 'DEMO_DOT_KLH26', 'Phòng DEMO-02', 'DANG_CHAM', 'DEMO_CB02', '2026-08-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD03', 'Hội đồng demo 03', 'DEMO_DOT_KLH26', 'Phòng DEMO-03', 'DANG_CHAM', 'DEMO_CB02', '2026-08-05 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD04', 'Hội đồng demo 04', 'DEMO_DOT_KL26', 'Phòng DEMO-04', 'SAN_SANG', 'DEMO_CB02', '2026-10-19 08:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD05', 'Hội đồng demo 05', 'DEMO_DOT_KL26', 'Phòng DEMO-05', 'NHAP', 'DEMO_CB02', '2026-10-19 08:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM hoi_dong AS e WHERE e.ma_hoi_dong = s.ma_hoi_dong);


-- 3-4 lecturers for ready/active committees, distinct chair and secretary; additional 14 rows in thanh_vien_hoi_dong.
INSERT INTO thanh_vien_hoi_dong (ma_hoi_dong, ma_giang_vien, chuc_danh, created_at, updated_at)
SELECT s.ma_hoi_dong, s.ma_giang_vien, s.chuc_danh, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_HD01' AS ma_hoi_dong, 'DEMO_GV03' AS ma_giang_vien, 'CHU_TICH' AS chuc_danh, '2026-08-05 09:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_HD01', 'DEMO_GV04', 'THU_KY', '2026-08-05 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD01', 'DEMO_GV05', 'UY_VIEN', '2026-08-05 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD02', 'DEMO_GV03', 'CHU_TICH', '2026-08-05 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD02', 'DEMO_GV04', 'THU_KY', '2026-08-05 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD02', 'DEMO_GV06', 'UY_VIEN', '2026-08-05 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD03', 'DEMO_GV03', 'CHU_TICH', '2026-08-05 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD03', 'DEMO_GV04', 'THU_KY', '2026-08-05 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD03', 'DEMO_GV05', 'UY_VIEN', '2026-08-05 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD03', 'DEMO_GV06', 'UY_VIEN', '2026-08-05 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD04', 'DEMO_GV02', 'CHU_TICH', '2026-10-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD04', 'DEMO_GV03', 'THU_KY', '2026-10-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD04', 'DEMO_GV04', 'UY_VIEN', '2026-10-19 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_HD05', 'DEMO_GV02', 'CHU_TICH', '2026-10-19 09:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM thanh_vien_hoi_dong AS e WHERE e.ma_hoi_dong = s.ma_hoi_dong AND e.ma_giang_vien = s.ma_giang_vien);


-- Assignments match registration rounds and exclude all supervisors/reviewers; additional 4 rows in hoi_dong_de_tai.
INSERT INTO hoi_dong_de_tai (ma_nhom, ma_hoi_dong, ma_nguoi_phan_cong, phan_cong_luc, created_at, updated_at)
SELECT s.ma_nhom, s.ma_hoi_dong, s.ma_nguoi_phan_cong, s.phan_cong_luc, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_N08' AS ma_nhom, 'DEMO_HD01' AS ma_hoi_dong, 'DEMO_CB02' AS ma_nguoi_phan_cong, '2026-08-06 10:00:00' AS phan_cong_luc, '2026-08-06 10:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_N09', 'DEMO_HD02', 'DEMO_CB02', '2026-08-06 10:00:00', '2026-08-06 10:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'DEMO_HD03', 'DEMO_CB02', '2026-08-06 10:00:00', '2026-08-06 10:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N04', 'DEMO_HD04', 'DEMO_CB02', '2026-10-19 10:00:00', '2026-10-19 10:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM hoi_dong_de_tai AS e WHERE e.ma_nhom = s.ma_nhom);


-- All three grading types; zero is distinct from NULL; drafts never counted as submitted; additional 14 rows in phieu_cham_diem.
INSERT INTO phieu_cham_diem (ma_nhom, ma_giang_vien, loai_phieu, ma_de_tai_phan_cong, ma_nhom_hoi_dong, diem, nhan_xet, trang_thai, nop_luc, created_at, updated_at)
SELECT s.ma_nhom, s.ma_giang_vien, s.loai_phieu, s.ma_de_tai_phan_cong, s.ma_nhom_hoi_dong, s.diem, s.nhan_xet, s.trang_thai, s.nop_luc, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_N08' AS ma_nhom, 'DEMO_GV02' AS ma_giang_vien, 'PHAN_BIEN' AS loai_phieu, 'DEMO_DT14' AS ma_de_tai_phan_cong, NULL AS ma_nhom_hoi_dong, 8 AS diem, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.' AS nhan_xet, 'DA_NOP' AS trang_thai, '2026-09-12 09:00:00' AS nop_luc, '2026-09-12 09:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_N09', 'DEMO_GV02', 'PHAN_BIEN', 'DEMO_DT15', NULL, 8.33, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.', 'DA_NOP', '2026-09-12 09:00:00', '2026-09-12 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'DEMO_GV02', 'PHAN_BIEN', 'DEMO_DT16', NULL, 9, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.', 'DA_NOP', '2026-09-12 09:00:00', '2026-09-12 09:00:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N08', 'DEMO_GV03', 'HOI_DONG', NULL, 'DEMO_N08', 7.5, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.', 'DA_NOP', '2026-09-20 09:30:00', '2026-09-20 09:30:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N08', 'DEMO_GV04', 'HOI_DONG', NULL, 'DEMO_N08', 8.5, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.', 'DA_NOP', '2026-09-20 09:30:00', '2026-09-20 09:30:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N08', 'DEMO_GV05', 'HOI_DONG', NULL, 'DEMO_N08', 9, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.', 'DA_NOP', '2026-09-20 09:30:00', '2026-09-20 09:30:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N09', 'DEMO_GV03', 'HOI_DONG', NULL, 'DEMO_N09', 7.11, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.', 'DA_NOP', '2026-09-20 12:30:00', '2026-09-20 12:30:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N09', 'DEMO_GV04', 'HOI_DONG', NULL, 'DEMO_N09', 9.02, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.', 'DA_NOP', '2026-09-20 12:30:00', '2026-09-20 12:30:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N09', 'DEMO_GV06', 'HOI_DONG', NULL, 'DEMO_N09', 8.87, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.', 'DA_NOP', '2026-09-20 12:30:00', '2026-09-20 12:30:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'DEMO_GV03', 'HOI_DONG', NULL, 'DEMO_N10', 8, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.', 'DA_NOP', '2026-09-20 09:30:00', '2026-09-20 09:30:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'DEMO_GV04', 'HOI_DONG', NULL, 'DEMO_N10', 0, 'Nháp, chưa chốt điểm (demo).', 'NHAP', NULL, '2026-09-20 09:10:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'DEMO_GV05', 'HOI_DONG', NULL, 'DEMO_N10', NULL, 'Nháp, chưa chốt điểm (demo).', 'NHAP', NULL, '2026-09-20 09:10:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N10', 'DEMO_GV06', 'HOI_DONG', NULL, 'DEMO_N10', 10, 'Dữ liệu demo: đánh giá theo báo cáo và buổi bảo vệ.', 'DA_NOP', '2026-09-20 09:40:00', '2026-09-20 09:40:00', '2026-10-20 12:00:00'
    UNION ALL SELECT 'DEMO_N11', 'DEMO_GV02', 'CHAM_DOC_LAP', 'DEMO_DT17', NULL, 8.5, 'Nháp, chưa chốt điểm (demo).', 'NHAP', NULL, '2026-09-11 09:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM phieu_cham_diem AS e WHERE e.ma_nhom = s.ma_nhom AND e.ma_giang_vien = s.ma_giang_vien);


-- Exact required assessor identities/types and decimal averages; publication releases membership; additional 2 rows in ket_qua.
INSERT INTO ket_qua (ma_nhom, so_phieu, tong_diem, diem_cuoi, nhan_xet_tong_hop, ma_nguoi_tong_hop, tong_hop_luc, trang_thai, ma_nguoi_cong_bo, cong_bo_luc, created_at, updated_at)
SELECT s.ma_nhom, s.so_phieu, s.tong_diem, s.diem_cuoi, s.nhan_xet_tong_hop, s.ma_nguoi_tong_hop, s.tong_hop_luc, s.trang_thai, s.ma_nguoi_cong_bo, s.cong_bo_luc, s.created_at, s.updated_at
FROM (
    SELECT 'DEMO_N08' AS ma_nhom, 4 AS so_phieu, 33.00 AS tong_diem, 8.25 AS diem_cuoi, 'Demo: đủ một phiếu phản biện và ba phiếu hội đồng.' AS nhan_xet_tong_hop, 'DEMO_GV03' AS ma_nguoi_tong_hop, '2026-09-20 10:00:00' AS tong_hop_luc, 'CONG_BO' AS trang_thai, 'DEMO_CB02' AS ma_nguoi_cong_bo, '2026-09-21 08:00:00' AS cong_bo_luc, '2026-09-20 10:00:00' AS created_at, '2026-10-20 12:00:00' AS updated_at
    UNION ALL SELECT 'DEMO_N09', 4, 33.33, 8.33, 'Demo: đã tổng hợp, chưa công bố; sinh viên chưa được xem.', 'DEMO_GV03', '2026-09-20 13:00:00', 'DA_TONG_HOP', NULL, NULL, '2026-09-20 13:00:00', '2026-10-20 12:00:00'
) AS s
WHERE NOT EXISTS (SELECT 1 FROM ket_qua AS e WHERE e.ma_nhom = s.ma_nhom);

COMMIT;

SELECT DATABASE() AS target_schema_after_seed;
-- Run validate_demo_data.sql / demo_seed.py validate for complete read-only verification.
