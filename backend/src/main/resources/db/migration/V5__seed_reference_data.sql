
INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta)
VALUES
    ('ADMIN', 'Quản trị', 'Quản trị tài khoản, phân quyền, bộ môn và thông báo'),
    ('TRUONG_KHOA', 'Trưởng khoa', 'Quản lý đợt, duyệt, phân công, hội đồng và công bố kết quả'),
    ('TRUONG_BO_MON', 'Trưởng bộ môn', 'Vai trò giảng viên phụ trách bộ môn; quyền chi tiết do service/controller quy định'),
    ('GIANG_VIEN', 'Giảng viên', 'Đề xuất, hướng dẫn, phản biện hoặc chấm theo phân công'),
    ('SINH_VIEN', 'Sinh viên', 'Tham gia nhóm, đăng ký đề tài, nộp theo quyền nhóm và xem kết quả')
ON DUPLICATE KEY UPDATE
    ten_vai_tro = VALUES(ten_vai_tro),
    mo_ta = VALUES(mo_ta);
