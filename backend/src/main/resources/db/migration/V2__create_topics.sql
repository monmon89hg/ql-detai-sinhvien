
CREATE TABLE dot_dang_ky (
    ma_dot VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ten_dot VARCHAR(150) NOT NULL,
    loai_de_tai VARCHAR(15) NOT NULL,
    bat_dau_gv DATETIME(6) NOT NULL,
    ket_thuc_gv DATETIME(6) NOT NULL,
    bat_dau_sv DATETIME(6) NOT NULL,
    ket_thuc_sv DATETIME(6) NOT NULL,
    han_nop_bao_cao DATETIME(6) NULL,
    han_nop_diem_pb DATETIME(6) NULL,
    ngay_bao_cao_hd DATE NULL,
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'NHAP',
    ma_nguoi_tao VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_dot_dang_ky PRIMARY KEY (ma_dot),
    CONSTRAINT fk_dot_dang_ky_nguoi_tao FOREIGN KEY (ma_nguoi_tao) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_dot_dang_ky_loai CHECK (loai_de_tai IN ('MON_HOC','NCKH','TLCN','KLTN')),
    CONSTRAINT ck_dot_dang_ky_trang_thai CHECK (trang_thai IN ('NHAP','CONG_BO','DONG')),
    CONSTRAINT ck_dot_dang_ky_thoi_gian CHECK (
        bat_dau_gv < ket_thuc_gv AND ket_thuc_gv <= bat_dau_sv AND bat_dau_sv < ket_thuc_sv
    ),
    CONSTRAINT ck_dot_dang_ky_han_pb CHECK (
        (loai_de_tai IN ('TLCN','KLTN') AND han_nop_diem_pb IS NOT NULL)
        OR (loai_de_tai IN ('MON_HOC','NCKH') AND han_nop_diem_pb IS NULL)
    ),
    CONSTRAINT ck_dot_dang_ky_ngay_hd CHECK (
        (loai_de_tai='KLTN' AND ngay_bao_cao_hd IS NOT NULL)
        OR (loai_de_tai<>'KLTN' AND ngay_bao_cao_hd IS NULL)
    ),
    CONSTRAINT ck_dot_dang_ky_han_bao_cao CHECK (han_nop_bao_cao IS NULL OR han_nop_bao_cao > ket_thuc_sv),
    CONSTRAINT ck_dot_dang_ky_han_diem_pb CHECK (han_nop_diem_pb IS NULL OR han_nop_diem_pb > ket_thuc_sv),
    INDEX idx_dot_dang_ky_trang_thai_sv (trang_thai, bat_dau_sv, ket_thuc_sv)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE thong_bao
    ADD CONSTRAINT fk_thong_bao_dot FOREIGN KEY (ma_dot) REFERENCES dot_dang_ky(ma_dot)
        ON DELETE RESTRICT ON UPDATE RESTRICT;

CREATE TABLE de_tai (
    ma_de_tai VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_dot VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_bo_mon VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ten_de_tai VARCHAR(255) NOT NULL,
    noi_dung TEXT NOT NULL,
    yeu_cau TEXT NULL,
    ma_nguoi_de_xuat VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'NHAP',
    ma_nguoi_duyet VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    duyet_luc DATETIME(6) NULL,
    ly_do_tu_choi VARCHAR(1000) NULL,
    cong_bo_luc DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_de_tai PRIMARY KEY (ma_de_tai),
    CONSTRAINT fk_de_tai_dot FOREIGN KEY (ma_dot) REFERENCES dot_dang_ky(ma_dot)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_de_tai_bo_mon FOREIGN KEY (ma_bo_mon) REFERENCES bo_mon(ma_bo_mon)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_de_tai_nguoi_de_xuat FOREIGN KEY (ma_nguoi_de_xuat) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_de_tai_nguoi_duyet FOREIGN KEY (ma_nguoi_duyet) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_de_tai_trang_thai CHECK (
        trang_thai IN ('NHAP','CHO_DUYET','DA_DUYET','TU_CHOI','CONG_BO','NGUNG_NHAN')
    ),
    CONSTRAINT ck_de_tai_ten CHECK (CHAR_LENGTH(TRIM(ten_de_tai)) > 0),
    CONSTRAINT ck_de_tai_noi_dung CHECK (CHAR_LENGTH(TRIM(noi_dung)) > 0),
    CONSTRAINT ck_de_tai_tu_choi CHECK (
        (trang_thai='TU_CHOI' AND ma_nguoi_duyet IS NOT NULL AND duyet_luc IS NOT NULL
            AND ly_do_tu_choi IS NOT NULL AND CHAR_LENGTH(TRIM(ly_do_tu_choi)) > 0)
        OR trang_thai<>'TU_CHOI'
    ),
    CONSTRAINT ck_de_tai_cong_bo CHECK (
        (trang_thai='CONG_BO' AND cong_bo_luc IS NOT NULL) OR trang_thai<>'CONG_BO'
    ),
    INDEX idx_de_tai_dot_trang_thai_bo_mon (ma_dot, trang_thai, ma_bo_mon),
    INDEX idx_de_tai_nguoi_de_xuat (ma_nguoi_de_xuat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE de_tai_giang_vien (
    ma_de_tai VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_giang_vien VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    loai_phan_cong VARCHAR(20) NOT NULL,
    ma_nguoi_phan_cong VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    phan_cong_luc DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_de_tai_giang_vien PRIMARY KEY (ma_de_tai, ma_giang_vien),
    CONSTRAINT fk_dtgv_de_tai FOREIGN KEY (ma_de_tai) REFERENCES de_tai(ma_de_tai)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_dtgv_giang_vien FOREIGN KEY (ma_giang_vien) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_dtgv_nguoi_phan_cong FOREIGN KEY (ma_nguoi_phan_cong) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_dtgv_loai CHECK (loai_phan_cong IN ('HUONG_DAN','PHAN_BIEN','CHAM_DOC_LAP')),
    INDEX idx_dtgv_de_tai_loai (ma_de_tai, loai_phan_cong),
    INDEX idx_dtgv_giang_vien_loai (ma_giang_vien, loai_phan_cong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
