-- V3__create_registration_tables.sql
-- Nguyen Thi Nhu Y - Cum bang nhom sinh vien, thanh vien nhom, dang ky de tai, nop bao cao
-- MySQL 8.4 / Flyway
--
-- Phu thuoc:
--   V1: nguoi_dung
--   V2: dot_dang_ky, de_tai
--

CREATE TABLE nhom_sinh_vien (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ma_nhom VARCHAR(30) NOT NULL,
    ten_nhom VARCHAR(100) NOT NULL,
    dot_dang_ky_id BIGINT NOT NULL,
    nguoi_tao_id BIGINT NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'DANG_LAP',
    chot_luc DATETIME(6) NULL,
    ket_thuc_luc DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_nhom_sinh_vien
        PRIMARY KEY (id),

    CONSTRAINT uq_nhom_sinh_vien_ma_nhom
        UNIQUE (ma_nhom),

    CONSTRAINT fk_nhom_sinh_vien_dot_dang_ky
        FOREIGN KEY (dot_dang_ky_id)
        REFERENCES dot_dang_ky (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_nhom_sinh_vien_nguoi_tao
        FOREIGN KEY (nguoi_tao_id)
        REFERENCES nguoi_dung (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT ck_nhom_sinh_vien_trang_thai
        CHECK (
            trang_thai IN (
                'DANG_LAP',
                'DA_CHOT',
                'DANG_THUC_HIEN',
                'HOAN_THANH',
                'GIAI_TAN'
            )
        ),

    INDEX idx_nhom_sinh_vien_dot_trang_thai
        (dot_dang_ky_id, trang_thai)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE thanh_vien_nhom (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nhom_id BIGINT NOT NULL,
    sinh_vien_id BIGINT NOT NULL,
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'CHO_DUYET',
    la_nhom_truong BOOLEAN NOT NULL DEFAULT FALSE,
    la_thanh_vien_chot BOOLEAN NOT NULL DEFAULT FALSE,
    yeu_cau_luc DATETIME(6) NOT NULL,
    tham_gia_luc DATETIME(6) NULL,
    ket_thuc_luc DATETIME(6) NULL,

    -- Khi THAM_GIA, cot nay mang sinh_vien_id.
    -- Khi khong THAM_GIA, cot la NULL.
    -- UNIQUE cho phep nhieu NULL, nen mot SV chi co mot nhom dang hoat dong.
    sv_dang_tham_gia BIGINT
        GENERATED ALWAYS AS (
            CASE
                WHEN trang_thai = 'THAM_GIA' THEN sinh_vien_id
                ELSE NULL
            END
        ) STORED,

    -- Khi dang THAM_GIA va la nhom truong, cot nay mang nhom_id.
    -- UNIQUE dam bao toi da mot truong dang hoat dong moi nhom.
    nhom_co_truong BIGINT
        GENERATED ALWAYS AS (
            CASE
                WHEN trang_thai = 'THAM_GIA'
                     AND la_nhom_truong = TRUE
                THEN nhom_id
                ELSE NULL
            END
        ) STORED,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_thanh_vien_nhom
        PRIMARY KEY (id),

    CONSTRAINT uq_thanh_vien_nhom_nhom_sinh_vien
        UNIQUE (nhom_id, sinh_vien_id),

    CONSTRAINT uq_thanh_vien_nhom_sv_dang_tham_gia
        UNIQUE (sv_dang_tham_gia),

    CONSTRAINT uq_thanh_vien_nhom_nhom_co_truong
        UNIQUE (nhom_co_truong),

    CONSTRAINT fk_thanh_vien_nhom_nhom
        FOREIGN KEY (nhom_id)
        REFERENCES nhom_sinh_vien (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_thanh_vien_nhom_sinh_vien
        FOREIGN KEY (sinh_vien_id)
        REFERENCES nguoi_dung (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT ck_thanh_vien_nhom_trang_thai
        CHECK (
            trang_thai IN (
                'CHO_DUYET',
                'THAM_GIA',
                'TU_CHOI',
                'KET_THUC'
            )
        ),

    CONSTRAINT ck_thanh_vien_nhom_moc_thoi_gian
        CHECK (
            (
                trang_thai = 'THAM_GIA'
                AND tham_gia_luc IS NOT NULL
                AND ket_thuc_luc IS NULL
            )
            OR
            (
                trang_thai = 'KET_THUC'
                AND tham_gia_luc IS NOT NULL
                AND ket_thuc_luc IS NOT NULL
                AND ket_thuc_luc >= tham_gia_luc
            )
            OR
            trang_thai IN ('CHO_DUYET', 'TU_CHOI')
        ),

    INDEX idx_thanh_vien_nhom_sv_thanh_vien_chot
        (sinh_vien_id, la_thanh_vien_chot)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE dang_ky_de_tai (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nhom_id BIGINT NOT NULL,
    de_tai_id BIGINT NOT NULL,
    nguoi_dang_ky_id BIGINT NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'CHO_DUYET',
    dang_ky_luc DATETIME(6) NOT NULL,
    nguoi_duyet_id BIGINT NULL,
    duyet_luc DATETIME(6) NULL,
    ly_do_tu_choi VARCHAR(1000) NULL,
    bat_dau_cham_luc DATETIME(6) NULL,
    ket_thuc_luc DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_dang_ky_de_tai
        PRIMARY KEY (id),

    CONSTRAINT uq_dang_ky_de_tai_nhom
        UNIQUE (nhom_id),

    CONSTRAINT fk_dang_ky_de_tai_nhom
        FOREIGN KEY (nhom_id)
        REFERENCES nhom_sinh_vien (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_dang_ky_de_tai_de_tai
        FOREIGN KEY (de_tai_id)
        REFERENCES de_tai (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_dang_ky_de_tai_nguoi_dang_ky
        FOREIGN KEY (nguoi_dang_ky_id)
        REFERENCES nguoi_dung (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_dang_ky_de_tai_nguoi_duyet
        FOREIGN KEY (nguoi_duyet_id)
        REFERENCES nguoi_dung (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT ck_dang_ky_de_tai_trang_thai
        CHECK (
            trang_thai IN (
                'CHO_DUYET',
                'TU_CHOI',
                'DA_DUYET',
                'DANG_THUC_HIEN',
                'DANG_CHAM',
                'HOAN_THANH'
            )
        ),

    -- Neu TU_CHOI thi phai co nguoi duyet, thoi diem duyet va ly do.
    -- Neu khong TU_CHOI thi khong giu ly do tu choi.
    CONSTRAINT ck_dang_ky_de_tai_tu_choi
        CHECK (
            (
                trang_thai = 'TU_CHOI'
                AND nguoi_duyet_id IS NOT NULL
                AND duyet_luc IS NOT NULL
                AND ly_do_tu_choi IS NOT NULL
                AND CHAR_LENGTH(TRIM(ly_do_tu_choi)) > 0
            )
            OR
            (
                trang_thai <> 'TU_CHOI'
                AND ly_do_tu_choi IS NULL
            )
        ),

    INDEX idx_dang_ky_de_tai_de_tai_trang_thai
        (de_tai_id, trang_thai),

    INDEX idx_dang_ky_de_tai_trang_thai_dang_ky_luc
        (trang_thai, dang_ky_luc)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE nop_bao_cao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dang_ky_id BIGINT NOT NULL,
    nguoi_nop_id BIGINT NOT NULL,
    phien_ban INT NOT NULL,
    ten_file_goc VARCHAR(255) NOT NULL,
    object_key VARCHAR(512)
        CHARACTER SET utf8mb4
        COLLATE utf8mb4_bin
        NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    kich_thuoc_byte BIGINT NOT NULL,
    sha256 CHAR(64)
        CHARACTER SET ascii
        COLLATE ascii_bin
        NOT NULL,
    nop_luc DATETIME(6) NOT NULL,
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'HOP_LE',

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_nop_bao_cao
        PRIMARY KEY (id),

    CONSTRAINT uq_nop_bao_cao_dang_ky_phien_ban
        UNIQUE (dang_ky_id, phien_ban),

    CONSTRAINT uq_nop_bao_cao_object_key
        UNIQUE (object_key),

    CONSTRAINT fk_nop_bao_cao_dang_ky
        FOREIGN KEY (dang_ky_id)
        REFERENCES dang_ky_de_tai (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_nop_bao_cao_nguoi_nop
        FOREIGN KEY (nguoi_nop_id)
        REFERENCES nguoi_dung (id)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT ck_nop_bao_cao_phien_ban
        CHECK (phien_ban > 0),

    CONSTRAINT ck_nop_bao_cao_kich_thuoc
        CHECK (
            kich_thuoc_byte > 0
            AND kich_thuoc_byte <= 20971520
        ),

    CONSTRAINT ck_nop_bao_cao_trang_thai
        CHECK (
            trang_thai IN ('HOP_LE', 'THU_HOI')
        ),

    CONSTRAINT ck_nop_bao_cao_mime_type
        CHECK (mime_type = 'application/pdf'),

    INDEX idx_nop_bao_cao_dang_ky_trang_thai_phien_ban
        (dang_ky_id, trang_thai, phien_ban)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;