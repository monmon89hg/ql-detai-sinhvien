-- Run once against the project's MySQL database before deploying the Java changes.
-- Converts existing role-targeted notices into personal notices for current members
-- of the corresponding role, preserving who would have received them at migration time.

CREATE TABLE thong_bao_nguoi_nhan (
    ma_thong_bao VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_nguoi_dung VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (ma_thong_bao, ma_nguoi_dung),
    CONSTRAINT fk_tbn_thong_bao
        FOREIGN KEY (ma_thong_bao) REFERENCES thong_bao(ma_thong_bao)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_tbn_nguoi_dung
        FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX idx_tbn_nguoi_dung_thong_bao (ma_nguoi_dung, ma_thong_bao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO thong_bao_nguoi_nhan (ma_thong_bao, ma_nguoi_dung)
SELECT DISTINCT tb.ma_thong_bao, ndvt.ma_nguoi_dung
FROM thong_bao tb
JOIN nguoi_dung_vai_tro ndvt
  ON (tb.pham_vi = 'SINH_VIEN' AND ndvt.ma_vai_tro = 'SINH_VIEN')
  OR (tb.pham_vi = 'GIANG_VIEN' AND ndvt.ma_vai_tro = 'GIANG_VIEN')
WHERE tb.pham_vi IN ('SINH_VIEN', 'GIANG_VIEN');

ALTER TABLE thong_bao
    DROP CHECK ck_thong_bao_pham_vi;

UPDATE thong_bao
SET pham_vi = 'CA_NHAN'
WHERE pham_vi IN ('SINH_VIEN', 'GIANG_VIEN');

ALTER TABLE thong_bao
    ADD CONSTRAINT ck_thong_bao_pham_vi
        CHECK (pham_vi IN ('TAT_CA', 'CA_NHAN'));
