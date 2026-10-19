
CREATE TABLE bo_mon (
    ma_bo_mon VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ten_bo_mon VARCHAR(150) NOT NULL,
    mo_ta VARCHAR(500) NULL,
    hoat_dong BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_bo_mon PRIMARY KEY (ma_bo_mon),
    CONSTRAINT ck_bo_mon_ma CHECK (CHAR_LENGTH(TRIM(ma_bo_mon)) > 0),
    CONSTRAINT ck_bo_mon_ten CHECK (CHAR_LENGTH(TRIM(ten_bo_mon)) > 0),
    CONSTRAINT ck_bo_mon_hoat_dong CHECK (hoat_dong IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE nguoi_dung (
    ma_nguoi_dung VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ho_ten VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL,
    mat_khau_hash VARCHAR(255) NOT NULL,
    loai_nguoi_dung VARCHAR(10) NOT NULL,
    ma_bo_mon VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    so_dien_thoai VARCHAR(20) NULL,
    lop_hanh_chinh VARCHAR(50) NULL,
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'HOAT_DONG',
    buoc_doi_mat_khau BOOLEAN NOT NULL DEFAULT TRUE,
    lan_dang_nhap_cuoi DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_nguoi_dung PRIMARY KEY (ma_nguoi_dung),
    CONSTRAINT uq_nguoi_dung_email UNIQUE (email),
    CONSTRAINT fk_nguoi_dung_bo_mon FOREIGN KEY (ma_bo_mon) REFERENCES bo_mon(ma_bo_mon)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_nguoi_dung_loai CHECK (loai_nguoi_dung IN ('SV','GV','CAN_BO')),
    CONSTRAINT ck_nguoi_dung_trang_thai CHECK (trang_thai IN ('HOAT_DONG','KHOA')),
    CONSTRAINT ck_nguoi_dung_buoc_doi_mk CHECK (buoc_doi_mat_khau IN (0,1)),
    CONSTRAINT ck_nguoi_dung_ma CHECK (CHAR_LENGTH(TRIM(ma_nguoi_dung)) > 0),
    CONSTRAINT ck_nguoi_dung_ho_ten CHECK (CHAR_LENGTH(TRIM(ho_ten)) > 0),
    INDEX idx_nguoi_dung_bo_mon_loai (ma_bo_mon, loai_nguoi_dung)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE vai_tro (
    ma_vai_tro VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ten_vai_tro VARCHAR(100) NOT NULL,
    mo_ta VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_vai_tro PRIMARY KEY (ma_vai_tro),
    CONSTRAINT ck_vai_tro_ma CHECK (
        ma_vai_tro IN ('ADMIN','TRUONG_KHOA','TRUONG_BO_MON','GIANG_VIEN','SINH_VIEN')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE nguoi_dung_vai_tro (
    ma_nguoi_dung VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_vai_tro VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_nguoi_cap VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_nguoi_dung_vai_tro PRIMARY KEY (ma_nguoi_dung, ma_vai_tro),
    CONSTRAINT fk_ndvt_nguoi_dung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ndvt_vai_tro FOREIGN KEY (ma_vai_tro) REFERENCES vai_tro(ma_vai_tro)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ndvt_nguoi_cap FOREIGN KEY (ma_nguoi_cap) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_ndvt_vai_tro_nguoi_dung (ma_vai_tro, ma_nguoi_dung)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE thong_bao (
    ma_thong_bao VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    tieu_de VARCHAR(200) NOT NULL,
    noi_dung TEXT NOT NULL,
    ma_nguoi_dang VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_dot VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    pham_vi VARCHAR(15) NOT NULL DEFAULT 'TAT_CA',
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'NHAP',
    cong_bo_luc DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_thong_bao PRIMARY KEY (ma_thong_bao),
    CONSTRAINT fk_thong_bao_nguoi_dang FOREIGN KEY (ma_nguoi_dang) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_thong_bao_pham_vi CHECK (pham_vi IN ('TAT_CA','SINH_VIEN','GIANG_VIEN')),
    CONSTRAINT ck_thong_bao_trang_thai CHECK (trang_thai IN ('NHAP','CONG_BO','AN')),
    CONSTRAINT ck_thong_bao_cong_bo CHECK (
        (trang_thai='CONG_BO' AND cong_bo_luc IS NOT NULL) OR trang_thai<>'CONG_BO'
    ),
    CONSTRAINT ck_thong_bao_tieu_de CHECK (CHAR_LENGTH(TRIM(tieu_de)) > 0),
    INDEX idx_thong_bao_trang_thai_cong_bo (trang_thai, cong_bo_luc),
    INDEX idx_thong_bao_dot (ma_dot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
