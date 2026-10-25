-- Dữ liệu thử nghiệm dành cho database local.
USE qldt_local;

START TRANSACTION;

INSERT INTO nguoi_dung (
    ma_nguoi_dung,
    ho_ten,
    email,
    mat_khau_hash,
    loai_nguoi_dung,
    trang_thai,
    buoc_doi_mat_khau
)
VALUES (
    'TK_DEMO',
    'Trưởng khoa Demo',
    'truongkhoa.demo@example.test',
    '$2a$10$jO.u9lDD.s2Wr.hvFjtRCuv4wm8pTipgZo1y6AJRohszbe9fDVuuC',
    'CAN_BO',
    'HOAT_DONG',
    FALSE
);

INSERT INTO nguoi_dung_vai_tro (
    ma_nguoi_dung,
    ma_vai_tro
)
VALUES (
    'TK_DEMO',
    'TRUONG_KHOA'
);

COMMIT;

SELECT
    nd.ma_nguoi_dung,
    nd.ho_ten,
    nd.trang_thai,
    ndvt.ma_vai_tro
FROM nguoi_dung nd
JOIN nguoi_dung_vai_tro ndvt
    ON nd.ma_nguoi_dung = ndvt.ma_nguoi_dung
WHERE nd.ma_nguoi_dung = 'TK_DEMO';

-- Đợt KLTN phục vụ thử nghiệm hội đồng.
USE qldt_local;

INSERT INTO dot_dang_ky (
    ma_dot,
    ten_dot,
    loai_de_tai,
    bat_dau_gv,
    ket_thuc_gv,
    bat_dau_sv,
    ket_thuc_sv,
    han_nop_bao_cao,
    han_nop_diem_pb,
    ngay_bao_cao_hd,
    trang_thai,
    ma_nguoi_tao
)
VALUES (
    'DOT_KLTN_DEMO',
    'Đợt khóa luận tốt nghiệp thử nghiệm',
    'KLTN',
    '2026-09-01 00:00:00',
    '2026-09-10 00:00:00',
    '2026-09-11 00:00:00',
    '2026-09-20 00:00:00',
    '2026-11-01 00:00:00',
    '2026-11-10 00:00:00',
    '2026-11-15',
    'CONG_BO',
    'TK_DEMO'
);

SELECT
    ma_dot,
    ten_dot,
    loai_de_tai,
    trang_thai,
    ma_nguoi_tao
FROM dot_dang_ky
WHERE ma_dot = 'DOT_KLTN_DEMO';

-- Giảng viên phục vụ thử nghiệm hội đồng.
USE qldt_local;

SET @mat_khau_demo =
    '$2a$10$jO.u9lDD.s2Wr.hvFjtRCuv4wm8pTipgZo1y6AJRohszbe9fDVuuC';

START TRANSACTION;

INSERT INTO nguoi_dung (
    ma_nguoi_dung,
    ho_ten,
    email,
    mat_khau_hash,
    loai_nguoi_dung,
    trang_thai,
    buoc_doi_mat_khau
)
VALUES
    (
        'GV_DEMO_01', 'Giảng viên Demo 01',
        'gv01.demo@example.test', @mat_khau_demo,
        'GV', 'HOAT_DONG', FALSE
    ),
    (
        'GV_DEMO_02', 'Giảng viên Demo 02',
        'gv02.demo@example.test', @mat_khau_demo,
        'GV', 'HOAT_DONG', FALSE
    ),
    (
        'GV_DEMO_03', 'Giảng viên Demo 03',
        'gv03.demo@example.test', @mat_khau_demo,
        'GV', 'HOAT_DONG', FALSE
    ),
    (
        'GV_DEMO_04', 'Giảng viên Demo 04',
        'gv04.demo@example.test', @mat_khau_demo,
        'GV', 'HOAT_DONG', FALSE
    ),
    (
        'GV_DEMO_05', 'Giảng viên Demo 05',
        'gv05.demo@example.test', @mat_khau_demo,
        'GV', 'HOAT_DONG', FALSE
    ),
    (
        'GV_DEMO_06', 'Giảng viên Demo 06',
        'gv06.demo@example.test', @mat_khau_demo,
        'GV', 'HOAT_DONG', FALSE
    );

INSERT INTO nguoi_dung_vai_tro (
    ma_nguoi_dung,
    ma_vai_tro
)
VALUES
    ('GV_DEMO_01', 'GIANG_VIEN'),
    ('GV_DEMO_02', 'GIANG_VIEN'),
    ('GV_DEMO_03', 'GIANG_VIEN'),
    ('GV_DEMO_04', 'GIANG_VIEN'),
    ('GV_DEMO_05', 'GIANG_VIEN'),
    ('GV_DEMO_06', 'GIANG_VIEN');

COMMIT;