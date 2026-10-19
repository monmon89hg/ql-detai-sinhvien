CREATE TABLE dot_dang_ky (
    id                  BIGINT AUTO_INCREMENT,
    ma_dot              VARCHAR(30)     NOT NULL,
    ten_dot             VARCHAR(150)    NOT NULL,
    loai_de_tai         VARCHAR(15)     NOT NULL,
    bat_dau_gv          DATETIME(6)     NOT NULL,
    ket_thuc_gv         DATETIME(6)     NOT NULL,
    bat_dau_sv          DATETIME(6)     NOT NULL,
    ket_thuc_sv         DATETIME(6)     NOT NULL,
    han_nop_bao_cao     DATETIME(6)     NULL,
    han_nop_diem_pb     DATETIME(6)     NULL,
    ngay_bao_cao_hd     DATE            NULL,
    trang_thai          VARCHAR(15)     NOT NULL DEFAULT 'NHAP',
    nguoi_tao_id        BIGINT          NOT NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_dot_dang_ky PRIMARY KEY (id),
    CONSTRAINT uq_dot_dang_ky_ma UNIQUE (ma_dot),
    CONSTRAINT fk_dot_dang_ky_nguoi_tao 
        FOREIGN KEY (nguoi_tao_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_dot_loai_de_tai 
        CHECK (loai_de_tai IN ('MON_HOC', 'NCKH', 'TLCN', 'KLTN')),
    CONSTRAINT ck_dot_trang_thai 
        CHECK (trang_thai IN ('NHAP', 'CONG_BO', 'DONG')),
    CONSTRAINT ck_dot_thoi_gian 
        CHECK (bat_dau_gv < ket_thuc_gv AND ket_thuc_gv <= bat_dau_sv AND bat_dau_sv < ket_thuc_sv),
    CONSTRAINT ck_dot_han_pb 
        CHECK ((loai_de_tai IN ('TLCN','KLTN') AND han_nop_diem_pb IS NOT NULL) 
            OR (loai_de_tai IN ('MON_HOC','NCKH') AND han_nop_diem_pb IS NULL)),
    CONSTRAINT ck_dot_ngay_hd 
        CHECK ((loai_de_tai = 'KLTN' AND ngay_bao_cao_hd IS NOT NULL) 
            OR (loai_de_tai <> 'KLTN' AND ngay_bao_cao_hd IS NULL)),
    CONSTRAINT ck_dot_han_bao_cao 
        CHECK (han_nop_bao_cao IS NULL OR han_nop_bao_cao > ket_thuc_sv),
    CONSTRAINT ck_dot_han_diem_pb 
        CHECK (han_nop_diem_pb IS NULL OR han_nop_diem_pb > ket_thuc_sv)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_dot_dang_ky_trang_thai_sv 
    ON dot_dang_ky (trang_thai, bat_dau_sv, ket_thuc_sv);

CREATE TABLE de_tai (
    id                  BIGINT AUTO_INCREMENT,
    ma_de_tai           VARCHAR(30)     NOT NULL,
    dot_dang_ky_id      BIGINT          NOT NULL,
    bo_mon_id           BIGINT          NOT NULL,
    ten_de_tai          VARCHAR(255)    NOT NULL,
    noi_dung            TEXT            NOT NULL,
    yeu_cau             TEXT            NULL,
    nguoi_de_xuat_id    BIGINT          NOT NULL,
    trang_thai          VARCHAR(20)     NOT NULL DEFAULT 'NHAP',
    nguoi_duyet_id      BIGINT          NULL,
    duyet_luc           DATETIME(6)     NULL,
    ly_do_tu_choi       VARCHAR(1000)   NULL,
    cong_bo_luc         DATETIME(6)     NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_de_tai PRIMARY KEY (id),
    CONSTRAINT uq_de_tai_ma UNIQUE (ma_de_tai),
    CONSTRAINT fk_de_tai_dot_dang_ky 
        FOREIGN KEY (dot_dang_ky_id) REFERENCES dot_dang_ky(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_de_tai_bo_mon 
        FOREIGN KEY (bo_mon_id) REFERENCES bo_mon(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_de_tai_nguoi_de_xuat 
        FOREIGN KEY (nguoi_de_xuat_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_de_tai_nguoi_duyet 
        FOREIGN KEY (nguoi_duyet_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_de_tai_trang_thai 
        CHECK (trang_thai IN ('NHAP', 'CHO_DUYET', 'DA_DUYET', 'TU_CHOI', 'CONG_BO', 'NGUNG_NHAN')),
    CONSTRAINT ck_de_tai_tu_choi 
        CHECK ((trang_thai = 'TU_CHOI' AND ly_do_tu_choi IS NOT NULL) OR (trang_thai <> 'TU_CHOI')),
    CONSTRAINT ck_de_tai_cong_bo 
        CHECK ((trang_thai = 'CONG_BO' AND cong_bo_luc IS NOT NULL) OR (trang_thai <> 'CONG_BO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_de_tai_dot_trang_thai_bm 
    ON de_tai (dot_dang_ky_id, trang_thai, bo_mon_id);
CREATE INDEX idx_de_tai_nguoi_de_xuat 
    ON de_tai (nguoi_de_xuat_id);

CREATE TABLE de_tai_giang_vien (
    id                  BIGINT AUTO_INCREMENT,
    de_tai_id           BIGINT          NOT NULL,
    giang_vien_id       BIGINT          NOT NULL,
    loai_phan_cong      VARCHAR(20)     NOT NULL,
    nguoi_phan_cong_id  BIGINT          NOT NULL,
    phan_cong_luc       DATETIME(6)     NOT NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_de_tai_giang_vien PRIMARY KEY (id),
    CONSTRAINT uq_dtgv_de_tai_giang_vien UNIQUE (de_tai_id, giang_vien_id),
    CONSTRAINT fk_dtgv_de_tai 
        FOREIGN KEY (de_tai_id) REFERENCES de_tai(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_dtgv_giang_vien 
        FOREIGN KEY (giang_vien_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_dtgv_nguoi_phan_cong 
        FOREIGN KEY (nguoi_phan_cong_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_dtgv_loai_phan_cong 
        CHECK (loai_phan_cong IN ('HUONG_DAN', 'PHAN_BIEN', 'CHAM_DOC_LAP'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_dtgv_de_tai_loai 
    ON de_tai_giang_vien (de_tai_id, loai_phan_cong);
CREATE INDEX idx_dtgv_giang_vien_loai 
    ON de_tai_giang_vien (giang_vien_id, loai_phan_cong);
