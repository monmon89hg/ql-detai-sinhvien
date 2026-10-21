
CREATE TABLE hoi_dong (
    ma_hoi_dong VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ten_hoi_dong VARCHAR(150) NOT NULL,
    ma_dot VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    dia_diem VARCHAR(200) NULL,
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'NHAP',
    ma_nguoi_tao VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_hoi_dong PRIMARY KEY (ma_hoi_dong),
    CONSTRAINT fk_hoi_dong_dot FOREIGN KEY (ma_dot) REFERENCES dot_dang_ky(ma_dot)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_hoi_dong_nguoi_tao FOREIGN KEY (ma_nguoi_tao) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_hoi_dong_trang_thai CHECK (trang_thai IN ('NHAP','SAN_SANG','DANG_CHAM','DA_KHOA')),
    INDEX idx_hoi_dong_dot_trang_thai (ma_dot, trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE hoi_dong_de_tai (
    ma_nhom VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_hoi_dong VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_nguoi_phan_cong VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    phan_cong_luc DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_hoi_dong_de_tai PRIMARY KEY (ma_nhom),
    CONSTRAINT fk_hddt_nhom FOREIGN KEY (ma_nhom) REFERENCES dang_ky_de_tai(ma_nhom)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_hddt_hoi_dong FOREIGN KEY (ma_hoi_dong) REFERENCES hoi_dong(ma_hoi_dong)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_hddt_nguoi_phan_cong FOREIGN KEY (ma_nguoi_phan_cong) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_hddt_hoi_dong_nhom (ma_hoi_dong, ma_nhom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE thanh_vien_hoi_dong (
    ma_hoi_dong VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_giang_vien VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    chuc_danh VARCHAR(15) NOT NULL,
    chuc_danh_doc_nhat VARCHAR(15)
        GENERATED ALWAYS AS (CASE WHEN chuc_danh IN ('CHU_TICH','THU_KY') THEN chuc_danh ELSE NULL END) STORED,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_thanh_vien_hoi_dong PRIMARY KEY (ma_hoi_dong, ma_giang_vien),
    CONSTRAINT uq_tvhd_chuc_danh UNIQUE (ma_hoi_dong, chuc_danh_doc_nhat),
    CONSTRAINT fk_tvhd_hoi_dong FOREIGN KEY (ma_hoi_dong) REFERENCES hoi_dong(ma_hoi_dong)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_tvhd_giang_vien FOREIGN KEY (ma_giang_vien) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_tvhd_chuc_danh CHECK (chuc_danh IN ('CHU_TICH','THU_KY','UY_VIEN')),
    INDEX idx_tvhd_giang_vien_hoi_dong (ma_giang_vien, ma_hoi_dong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE phieu_cham_diem (
    ma_nhom VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_giang_vien VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    loai_phieu VARCHAR(20) NOT NULL,
    ma_de_tai_phan_cong VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    ma_nhom_hoi_dong VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    diem DECIMAL(4,2) NULL,
    nhan_xet TEXT NULL,
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'NHAP',
    nop_luc DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_phieu_cham_diem PRIMARY KEY (ma_nhom, ma_giang_vien),
    CONSTRAINT fk_pcd_dang_ky FOREIGN KEY (ma_nhom) REFERENCES dang_ky_de_tai(ma_nhom)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_pcd_giang_vien FOREIGN KEY (ma_giang_vien) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_pcd_hoi_dong_de_tai FOREIGN KEY (ma_nhom_hoi_dong) REFERENCES hoi_dong_de_tai(ma_nhom)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_pcd_phan_cong FOREIGN KEY (ma_de_tai_phan_cong, ma_giang_vien)
        REFERENCES de_tai_giang_vien(ma_de_tai, ma_giang_vien)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_pcd_loai CHECK (loai_phieu IN ('PHAN_BIEN','HOI_DONG','CHAM_DOC_LAP')),
    CONSTRAINT ck_pcd_diem CHECK (diem IS NULL OR (diem >= 0 AND diem <= 10)),
    CONSTRAINT ck_pcd_trang_thai CHECK (trang_thai IN ('NHAP','DA_NOP')),
    CONSTRAINT ck_pcd_nguon CHECK (
        (loai_phieu='HOI_DONG' AND ma_nhom_hoi_dong IS NOT NULL AND ma_nhom_hoi_dong=ma_nhom
            AND ma_de_tai_phan_cong IS NULL)
        OR (loai_phieu IN ('PHAN_BIEN','CHAM_DOC_LAP') AND ma_de_tai_phan_cong IS NOT NULL
            AND ma_nhom_hoi_dong IS NULL)
    ),
    CONSTRAINT ck_pcd_nop CHECK (
        (trang_thai='NHAP' AND nop_luc IS NULL)
        OR (trang_thai='DA_NOP' AND diem IS NOT NULL AND nhan_xet IS NOT NULL
            AND CHAR_LENGTH(TRIM(nhan_xet)) > 0 AND nop_luc IS NOT NULL)
    ),
    INDEX idx_pcd_giang_vien_trang_thai (ma_giang_vien, trang_thai),
    INDEX idx_pcd_nhom_trang_thai (ma_nhom, trang_thai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ket_qua (
    ma_nhom VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    so_phieu INT NOT NULL,
    tong_diem DECIMAL(8,2) NOT NULL,
    diem_cuoi DECIMAL(4,2) NOT NULL,
    nhan_xet_tong_hop TEXT NULL,
    ma_nguoi_tong_hop VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    tong_hop_luc DATETIME(6) NOT NULL,
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'DA_TONG_HOP',
    ma_nguoi_cong_bo VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    cong_bo_luc DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_ket_qua PRIMARY KEY (ma_nhom),
    CONSTRAINT fk_ket_qua_dang_ky FOREIGN KEY (ma_nhom) REFERENCES dang_ky_de_tai(ma_nhom)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ket_qua_nguoi_tong_hop FOREIGN KEY (ma_nguoi_tong_hop) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ket_qua_nguoi_cong_bo FOREIGN KEY (ma_nguoi_cong_bo) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_ket_qua_so_phieu CHECK (so_phieu > 0),
    CONSTRAINT ck_ket_qua_tong_diem CHECK (tong_diem >= 0 AND tong_diem <= so_phieu * 10),
    CONSTRAINT ck_ket_qua_diem_cuoi CHECK (diem_cuoi BETWEEN 0 AND 10),
    CONSTRAINT ck_ket_qua_cong_thuc CHECK (diem_cuoi = ROUND(tong_diem / so_phieu, 2)),
    CONSTRAINT ck_ket_qua_trang_thai CHECK (trang_thai IN ('DA_TONG_HOP','CONG_BO')),
    CONSTRAINT ck_ket_qua_cong_bo CHECK (
        (trang_thai='DA_TONG_HOP' AND ma_nguoi_cong_bo IS NULL AND cong_bo_luc IS NULL)
        OR (trang_thai='CONG_BO' AND ma_nguoi_cong_bo IS NOT NULL AND cong_bo_luc IS NOT NULL)
    ),
    INDEX idx_ket_qua_trang_thai_cong_bo (trang_thai, cong_bo_luc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
