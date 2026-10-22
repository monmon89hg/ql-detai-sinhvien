-- Mỗi sinh viên có tối đa một email thông báo cho một kết quả nhóm.
CREATE TABLE email_thong_bao_ket_qua (
    ma_nhom VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    ma_sinh_vien VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    dia_chi_email VARCHAR(120) NOT NULL,
    trang_thai VARCHAR(15) NOT NULL DEFAULT 'CHO_GUI',
    so_lan_thu INT NOT NULL DEFAULT 0,
    loi_cuoi VARCHAR(1000) NULL,
    lan_thu_cuoi_luc DATETIME(6) NULL,
    gui_luc DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_email_thong_bao_ket_qua
        PRIMARY KEY (ma_nhom, ma_sinh_vien),
    CONSTRAINT fk_email_ket_qua
        FOREIGN KEY (ma_nhom) REFERENCES ket_qua(ma_nhom),
    CONSTRAINT fk_email_sinh_vien
        FOREIGN KEY (ma_sinh_vien) REFERENCES nguoi_dung(ma_nguoi_dung),
    CONSTRAINT ck_email_trang_thai
        CHECK (trang_thai IN ('CHO_GUI', 'DA_GUI', 'LOI')),
    CONSTRAINT ck_email_so_lan_thu
        CHECK (so_lan_thu >= 0),

    INDEX idx_email_cho_gui (trang_thai, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;