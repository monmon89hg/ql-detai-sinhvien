-- TV4 - Các bảng hội đồng, chấm điểm và kết quả
-- MySQL 8.4 / InnoDB / utf8mb4
-- Phụ thuộc các bảng do thành viên khác tạo:
-- dot_dang_ky, nguoi_dung, dang_ky_de_tai, de_tai_giang_vien

CREATE TABLE IF NOT EXISTS hoi_dong (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    ma_hoi_dong         VARCHAR(30)  NOT NULL,
    ten_hoi_dong        VARCHAR(150) NOT NULL,
    dot_dang_ky_id      BIGINT NOT NULL,
    dia_diem            VARCHAR(200) NULL,
    trang_thai          VARCHAR(15)  NOT NULL DEFAULT 'NHAP',
    nguoi_tao_id        BIGINT NOT NULL,

    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                    ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_hoi_dong PRIMARY KEY (id),
    CONSTRAINT uq_hoi_dong_ma UNIQUE (ma_hoi_dong),
    CONSTRAINT fk_hoi_dong_dot
        FOREIGN KEY (dot_dang_ky_id) REFERENCES dot_dang_ky (id),
    CONSTRAINT fk_hoi_dong_nguoi_tao
        FOREIGN KEY (nguoi_tao_id) REFERENCES nguoi_dung (id),
    CONSTRAINT ck_hoi_dong_trang_thai
        CHECK (trang_thai IN ('NHAP', 'SAN_SANG', 'DANG_CHAM', 'DA_KHOA') )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_hoi_dong_dot_trang_thai
    ON hoi_dong (dot_dang_ky_id, trang_thai);


CREATE TABLE IF NOT EXISTS hoi_dong_de_tai (
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    hoi_dong_id         BIGINT NOT NULL,
    dang_ky_id          BIGINT NOT NULL,
    nguoi_phan_cong_id  BIGINT NOT NULL,
    phan_cong_luc       DATETIME(6) NOT NULL,

    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                    ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_hoi_dong_de_tai PRIMARY KEY (id),
    CONSTRAINT uq_hoi_dong_de_tai_dang_ky UNIQUE (dang_ky_id),
    CONSTRAINT fk_hddt_hoi_dong
        FOREIGN KEY (hoi_dong_id) REFERENCES hoi_dong (id),
    CONSTRAINT fk_hddt_dang_ky
        FOREIGN KEY (dang_ky_id) REFERENCES dang_ky_de_tai (id),
    CONSTRAINT fk_hddt_nguoi_phan_cong
        FOREIGN KEY (nguoi_phan_cong_id) REFERENCES nguoi_dung (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_hddt_hoi_dong_dang_ky
    ON hoi_dong_de_tai (hoi_dong_id, dang_ky_id);


CREATE TABLE IF NOT EXISTS thanh_vien_hoi_dong (
    id                      BIGINT NOT NULL AUTO_INCREMENT,
    hoi_dong_id             BIGINT NOT NULL,
    giang_vien_id           BIGINT NOT NULL,
    chuc_danh               VARCHAR(15) NOT NULL,
    chuc_danh_doc_nhat      VARCHAR(15)
        GENERATED ALWAYS AS (
            CASE
                WHEN chuc_danh IN ('CHU_TICH', 'THU_KY') THEN chuc_danh
                ELSE NULL
            END
        ) STORED,

    created_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                        ON UPDATE CURRENT_TIMESTAMP(6),
    version                 BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_thanh_vien_hoi_dong PRIMARY KEY (id),
    CONSTRAINT uq_tvhd_hoi_dong_giang_vien
        UNIQUE (hoi_dong_id, giang_vien_id),
    CONSTRAINT uq_tvhd_hoi_dong_chuc_danh
        UNIQUE (hoi_dong_id, chuc_danh_doc_nhat),
    CONSTRAINT fk_tvhd_hoi_dong
        FOREIGN KEY (hoi_dong_id) REFERENCES hoi_dong (id),
    CONSTRAINT fk_tvhd_giang_vien
        FOREIGN KEY (giang_vien_id) REFERENCES nguoi_dung (id),
    CONSTRAINT ck_tvhd_chuc_danh
        CHECK (chuc_danh IN ('CHU_TICH', 'THU_KY', 'UY_VIEN'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_tvhd_giang_vien_hoi_dong
    ON thanh_vien_hoi_dong (giang_vien_id, hoi_dong_id);


CREATE TABLE IF NOT EXISTS phieu_cham_diem (
    id                      BIGINT NOT NULL AUTO_INCREMENT,
    dang_ky_id              BIGINT NOT NULL,
    giang_vien_id           BIGINT NOT NULL,
    loai_phieu              VARCHAR(20) NOT NULL,
    phan_cong_gv_id         BIGINT NULL,
    hoi_dong_de_tai_id      BIGINT NULL,
    diem                    DECIMAL(4,2) NULL,
    nhan_xet                TEXT NULL,
    trang_thai              VARCHAR(15) NOT NULL DEFAULT 'NHAP',
    nop_luc                 DATETIME(6) NULL,

    created_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                        ON UPDATE CURRENT_TIMESTAMP(6),
    version                 BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_phieu_cham_diem PRIMARY KEY (id),
    CONSTRAINT uq_pcd_dang_ky_giang_vien
        UNIQUE (dang_ky_id, giang_vien_id),
    CONSTRAINT fk_pcd_dang_ky
        FOREIGN KEY (dang_ky_id) REFERENCES dang_ky_de_tai (id),
    CONSTRAINT fk_pcd_giang_vien
        FOREIGN KEY (giang_vien_id) REFERENCES nguoi_dung (id),
    CONSTRAINT fk_pcd_phan_cong_gv
        FOREIGN KEY (phan_cong_gv_id) REFERENCES de_tai_giang_vien (id),
    CONSTRAINT fk_pcd_hoi_dong_de_tai
        FOREIGN KEY (hoi_dong_de_tai_id) REFERENCES hoi_dong_de_tai (id),
    CONSTRAINT ck_pcd_loai_phieu
        CHECK (loai_phieu IN ('PHAN_BIEN', 'HOI_DONG', 'CHAM_DOC_LAP')),
    CONSTRAINT ck_pcd_trang_thai
        CHECK (trang_thai IN ('NHAP', 'DA_NOP')),
    CONSTRAINT ck_pcd_diem
        CHECK (diem IS NULL OR (diem >= 0 AND diem <= 10)),
    CONSTRAINT ck_pcd_nguon_phan_cong
        CHECK (
            (loai_phieu = 'HOI_DONG'
                AND hoi_dong_de_tai_id IS NOT NULL
                AND phan_cong_gv_id IS NULL)
            OR
            (loai_phieu IN ('PHAN_BIEN', 'CHAM_DOC_LAP')
                AND phan_cong_gv_id IS NOT NULL
                AND hoi_dong_de_tai_id IS NULL)
        ),
    CONSTRAINT ck_pcd_trang_thai_du_lieu
        CHECK (
            (trang_thai = 'NHAP'
                AND nop_luc IS NULL)
            OR
            (trang_thai = 'DA_NOP'
                AND diem IS NOT NULL
                AND nhan_xet IS NOT NULL
                AND CHAR_LENGTH(TRIM(nhan_xet)) > 0
                AND nop_luc IS NOT NULL)
        )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_pcd_giang_vien_trang_thai
    ON phieu_cham_diem (giang_vien_id, trang_thai);

CREATE INDEX idx_pcd_dang_ky_trang_thai
    ON phieu_cham_diem (dang_ky_id, trang_thai);


CREATE TABLE IF NOT EXISTS ket_qua (
    id                      BIGINT NOT NULL AUTO_INCREMENT,
    dang_ky_id              BIGINT NOT NULL,
    so_phieu                INT NOT NULL,
    tong_diem               DECIMAL(8,2) NOT NULL,
    diem_cuoi               DECIMAL(4,2) NOT NULL,
    nhan_xet_tong_hop       TEXT NULL,
    nguoi_tong_hop_id       BIGINT NOT NULL,
    tong_hop_luc            DATETIME(6) NOT NULL,
    trang_thai              VARCHAR(15) NOT NULL DEFAULT 'DA_TONG_HOP',
    nguoi_cong_bo_id        BIGINT NULL,
    cong_bo_luc             DATETIME(6) NULL,

    created_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                        ON UPDATE CURRENT_TIMESTAMP(6),
    version                 BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_ket_qua PRIMARY KEY (id),
    CONSTRAINT uq_ket_qua_dang_ky UNIQUE (dang_ky_id),
    CONSTRAINT fk_ket_qua_dang_ky
        FOREIGN KEY (dang_ky_id) REFERENCES dang_ky_de_tai (id),
    CONSTRAINT fk_ket_qua_nguoi_tong_hop
        FOREIGN KEY (nguoi_tong_hop_id) REFERENCES nguoi_dung (id),
    CONSTRAINT fk_ket_qua_nguoi_cong_bo
        FOREIGN KEY (nguoi_cong_bo_id) REFERENCES nguoi_dung (id),
    CONSTRAINT ck_ket_qua_so_phieu
        CHECK (so_phieu > 0),
    CONSTRAINT ck_ket_qua_tong_diem
        CHECK (tong_diem >= 0 AND tong_diem <= so_phieu * 10),
    CONSTRAINT ck_ket_qua_diem_cuoi
        CHECK (diem_cuoi BETWEEN 0 AND 10),
    CONSTRAINT ck_ket_qua_tinh_diem
        CHECK (diem_cuoi = ROUND(tong_diem / so_phieu, 2)),
    CONSTRAINT ck_ket_qua_trang_thai
        CHECK (trang_thai IN ('DA_TONG_HOP', 'CONG_BO')),
    CONSTRAINT ck_ket_qua_cong_bo
        CHECK (
            (trang_thai = 'DA_TONG_HOP'
                AND nguoi_cong_bo_id IS NULL
                AND cong_bo_luc IS NULL)
            OR
            (trang_thai = 'CONG_BO'
                AND nguoi_cong_bo_id IS NOT NULL
                AND cong_bo_luc IS NOT NULL)
        )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_ket_qua_trang_thai_cong_bo
    ON ket_qua (trang_thai, cong_bo_luc);
