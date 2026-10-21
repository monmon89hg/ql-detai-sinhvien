
CREATE TABLE nhom_sinh_vien (
    ma_nhom VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ten_nhom VARCHAR(100) NOT NULL,
    ma_dot VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_nguoi_tao VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'DANG_LAP',
    chot_luc DATETIME(6) NULL,
    ket_thuc_luc DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_nhom_sinh_vien PRIMARY KEY (ma_nhom),
    CONSTRAINT fk_nhom_dot FOREIGN KEY (ma_dot) REFERENCES dot_dang_ky(ma_dot)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_nhom_nguoi_tao FOREIGN KEY (ma_nguoi_tao) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_nhom_trang_thai CHECK (
        trang_thai IN ('DANG_LAP','DA_CHOT','DANG_THUC_HIEN','HOAN_THANH','GIAI_TAN')
    ),
    INDEX idx_nhom_dot_trang_thai (ma_dot, trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE thanh_vien_nhom (
    ma_nhom VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_sinh_vien VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'CHO_DUYET',
    la_nhom_truong BOOLEAN NOT NULL DEFAULT FALSE,
    la_thanh_vien_chot BOOLEAN NOT NULL DEFAULT FALSE,
    yeu_cau_luc DATETIME(6) NOT NULL,
    tham_gia_luc DATETIME(6) NULL,
    ket_thuc_luc DATETIME(6) NULL,
    sv_dang_tham_gia VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin
        GENERATED ALWAYS AS (CASE WHEN trang_thai='THAM_GIA' THEN ma_sinh_vien ELSE NULL END) STORED,
    nhom_co_truong VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin
        GENERATED ALWAYS AS (
            CASE WHEN trang_thai='THAM_GIA' AND la_nhom_truong=TRUE THEN ma_nhom ELSE NULL END
        ) STORED,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_thanh_vien_nhom PRIMARY KEY (ma_nhom, ma_sinh_vien),
    CONSTRAINT uq_tvn_sv_dang_tham_gia UNIQUE (sv_dang_tham_gia),
    CONSTRAINT uq_tvn_nhom_co_truong UNIQUE (nhom_co_truong),
    CONSTRAINT fk_tvn_nhom FOREIGN KEY (ma_nhom) REFERENCES nhom_sinh_vien(ma_nhom)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_tvn_sinh_vien FOREIGN KEY (ma_sinh_vien) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_tvn_trang_thai CHECK (trang_thai IN ('CHO_DUYET','THAM_GIA','TU_CHOI','KET_THUC')),
    CONSTRAINT ck_tvn_nhom_truong CHECK (la_nhom_truong IN (0,1)),
    CONSTRAINT ck_tvn_thanh_vien_chot CHECK (la_thanh_vien_chot IN (0,1)),
    CONSTRAINT ck_tvn_moc_thoi_gian CHECK (
        (trang_thai IN ('CHO_DUYET','TU_CHOI') AND tham_gia_luc IS NULL AND ket_thuc_luc IS NULL)
        OR (trang_thai='THAM_GIA' AND tham_gia_luc IS NOT NULL AND ket_thuc_luc IS NULL)
        OR (trang_thai='KET_THUC' AND tham_gia_luc IS NOT NULL AND ket_thuc_luc IS NOT NULL
            AND ket_thuc_luc >= tham_gia_luc)
    ),
    INDEX idx_tvn_sv_chot (ma_sinh_vien, la_thanh_vien_chot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE dang_ky_de_tai (
    ma_nhom VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_de_tai VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_nguoi_dang_ky VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'CHO_DUYET',
    dang_ky_luc DATETIME(6) NOT NULL,
    ma_nguoi_duyet VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    duyet_luc DATETIME(6) NULL,
    ly_do_tu_choi VARCHAR(1000) NULL,
    bat_dau_cham_luc DATETIME(6) NULL,
    ket_thuc_luc DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_dang_ky_de_tai PRIMARY KEY (ma_nhom),
    CONSTRAINT fk_dkdt_nhom FOREIGN KEY (ma_nhom) REFERENCES nhom_sinh_vien(ma_nhom)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_dkdt_de_tai FOREIGN KEY (ma_de_tai) REFERENCES de_tai(ma_de_tai)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_dkdt_nguoi_dang_ky FOREIGN KEY (ma_nguoi_dang_ky) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_dkdt_nguoi_duyet FOREIGN KEY (ma_nguoi_duyet) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_dkdt_trang_thai CHECK (
        trang_thai IN ('CHO_DUYET','TU_CHOI','DA_DUYET','DANG_THUC_HIEN','DANG_CHAM','HOAN_THANH')
    ),
    CONSTRAINT ck_dkdt_tu_choi CHECK (
        (trang_thai='TU_CHOI' AND ma_nguoi_duyet IS NOT NULL AND duyet_luc IS NOT NULL
            AND ly_do_tu_choi IS NOT NULL AND CHAR_LENGTH(TRIM(ly_do_tu_choi)) > 0)
        OR (trang_thai<>'TU_CHOI' AND ly_do_tu_choi IS NULL)
    ),
    INDEX idx_dkdt_de_tai_trang_thai (ma_de_tai, trang_thai),
    INDEX idx_dkdt_trang_thai_luc (trang_thai, dang_ky_luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE nop_bao_cao (
    ma_nhom VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    phien_ban INT NOT NULL,
    ma_nguoi_nop VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ten_file_goc VARCHAR(255) NOT NULL,
    object_key VARCHAR(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    kich_thuoc_byte BIGINT NOT NULL,
    sha256 CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    nop_luc DATETIME(6) NOT NULL,
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'HOP_LE',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_nop_bao_cao PRIMARY KEY (ma_nhom, phien_ban),
    CONSTRAINT uq_nop_bao_cao_object_key UNIQUE (object_key),
    CONSTRAINT fk_nop_bao_cao_dang_ky FOREIGN KEY (ma_nhom) REFERENCES dang_ky_de_tai(ma_nhom)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_nop_bao_cao_nguoi_nop FOREIGN KEY (ma_nguoi_nop) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_nop_bao_cao_phien_ban CHECK (phien_ban > 0),
    CONSTRAINT ck_nop_bao_cao_kich_thuoc CHECK (kich_thuoc_byte > 0 AND kich_thuoc_byte <= 20971520),
    CONSTRAINT ck_nop_bao_cao_mime CHECK (mime_type='application/pdf'),
    CONSTRAINT ck_nop_bao_cao_trang_thai CHECK (trang_thai IN ('HOP_LE','THU_HOI')),
    INDEX idx_nop_bao_cao_dang_ky_trang_thai (ma_nhom, trang_thai, phien_ban)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
