SET NAMES utf8mb4;
SET time_zone = '+00:00';

CREATE TABLE bo_mon (
    id                  BIGINT AUTO_INCREMENT,
    ma_bo_mon           VARCHAR(20) COLLATE utf8mb4_bin NOT NULL,
    ten_bo_mon          VARCHAR(150)    NOT NULL,
    mo_ta               VARCHAR(500)    NULL,
    hoat_dong           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_bo_mon PRIMARY KEY (id),
    CONSTRAINT uq_bo_mon_ma UNIQUE (ma_bo_mon),
    CONSTRAINT ck_bo_mon_ma CHECK (CHAR_LENGTH(TRIM(ma_bo_mon)) > 0),
    CONSTRAINT ck_bo_mon_ten CHECK (CHAR_LENGTH(TRIM(ten_bo_mon)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE nguoi_dung (
    id                  BIGINT AUTO_INCREMENT,
    ma_nguoi_dung       VARCHAR(30) COLLATE utf8mb4_bin NOT NULL,
    ho_ten              VARCHAR(100)    NOT NULL,
    email               VARCHAR(120)    NOT NULL,
    mat_khau_hash       VARCHAR(255)    NOT NULL,
    loai_nguoi_dung     VARCHAR(10) COLLATE utf8mb4_bin NOT NULL,
    bo_mon_id           BIGINT          NULL,
    so_dien_thoai       VARCHAR(20)     NULL,
    lop_hanh_chinh      VARCHAR(50)     NULL,
    trang_thai          VARCHAR(15) COLLATE utf8mb4_bin NOT NULL DEFAULT 'HOAT_DONG',
    buoc_doi_mat_khau   BOOLEAN         NOT NULL DEFAULT TRUE,
    lan_dang_nhap_cuoi  DATETIME(6)     NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_nguoi_dung PRIMARY KEY (id),
    CONSTRAINT uq_nguoi_dung_ma UNIQUE (ma_nguoi_dung),
    CONSTRAINT uq_nguoi_dung_email UNIQUE (email),
    CONSTRAINT fk_nguoi_dung_bo_mon
        FOREIGN KEY (bo_mon_id) REFERENCES bo_mon(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_nguoi_dung_loai
        CHECK (loai_nguoi_dung IN ('SV', 'GV', 'CAN_BO')),
    CONSTRAINT ck_nguoi_dung_trang_thai
        CHECK (trang_thai IN ('HOAT_DONG', 'KHOA')),
    CONSTRAINT ck_nguoi_dung_ho_ten
        CHECK (CHAR_LENGTH(TRIM(ho_ten)) > 0),
    CONSTRAINT ck_nguoi_dung_ma
        CHECK (CHAR_LENGTH(TRIM(ma_nguoi_dung)) > 0),
    INDEX idx_nguoi_dung_bo_mon_loai (bo_mon_id, loai_nguoi_dung)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE vai_tro (
    id                  BIGINT AUTO_INCREMENT,
    ma_vai_tro          VARCHAR(30) COLLATE utf8mb4_bin NOT NULL,
    ten_vai_tro         VARCHAR(100)    NOT NULL,
    mo_ta               VARCHAR(255)    NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_vai_tro PRIMARY KEY (id),
    CONSTRAINT uq_vai_tro_ma UNIQUE (ma_vai_tro),
    CONSTRAINT ck_vai_tro_ma
        CHECK (ma_vai_tro IN ('ADMIN', 'TRUONG_KHOA', 'GIANG_VIEN', 'SINH_VIEN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE nguoi_dung_vai_tro (
    id                  BIGINT AUTO_INCREMENT,
    nguoi_dung_id       BIGINT          NOT NULL,
    vai_tro_id          BIGINT          NOT NULL,
    nguoi_cap_id        BIGINT          NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_nguoi_dung_vai_tro PRIMARY KEY (id),
    CONSTRAINT uq_ndvt_nguoi_dung_vai_tro UNIQUE (nguoi_dung_id, vai_tro_id),
    CONSTRAINT fk_ndvt_nguoi_dung
        FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ndvt_vai_tro
        FOREIGN KEY (vai_tro_id) REFERENCES vai_tro(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_ndvt_nguoi_cap
        FOREIGN KEY (nguoi_cap_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_ndvt_vai_tro_nguoi_dung (vai_tro_id, nguoi_dung_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE thong_bao (
    id                  BIGINT AUTO_INCREMENT,
    tieu_de             VARCHAR(200)    NOT NULL,
    noi_dung            TEXT            NOT NULL,
    nguoi_dang_id       BIGINT          NOT NULL,
    dot_dang_ky_id      BIGINT          NULL,
    pham_vi             VARCHAR(15) COLLATE utf8mb4_bin NOT NULL DEFAULT 'TAT_CA',
    trang_thai          VARCHAR(15) COLLATE utf8mb4_bin NOT NULL DEFAULT 'NHAP',
    cong_bo_luc         DATETIME(6)     NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_thong_bao PRIMARY KEY (id),
    CONSTRAINT fk_thong_bao_nguoi_dang
        FOREIGN KEY (nguoi_dang_id) REFERENCES nguoi_dung(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_thong_bao_pham_vi
        CHECK (pham_vi IN ('TAT_CA', 'SINH_VIEN', 'GIANG_VIEN')),
    CONSTRAINT ck_thong_bao_trang_thai
        CHECK (trang_thai IN ('NHAP', 'CONG_BO', 'AN')),
    CONSTRAINT ck_thong_bao_cong_bo
        CHECK (trang_thai <> 'CONG_BO' OR cong_bo_luc IS NOT NULL),
    INDEX idx_thong_bao_trang_thai_cong_bo (trang_thai, cong_bo_luc),
    INDEX idx_thong_bao_dot_dang_ky (dot_dang_ky_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


/*
ALTER TABLE thong_bao
    ADD CONSTRAINT fk_thong_bao_dot_dang_ky
        FOREIGN KEY (dot_dang_ky_id) REFERENCES dot_dang_ky(id)
        ON DELETE RESTRICT ON UPDATE RESTRICT;
*/
