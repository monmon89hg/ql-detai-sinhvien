-- Giữ nguyên dữ liệu và chặn xóa hội đồng đang được tham chiếu.
-- Khi sửa mã hội đồng, các liên kết tự đổi mã trong cùng giao dịch.
ALTER TABLE thanh_vien_hoi_dong
    DROP FOREIGN KEY fk_tvhd_hoi_dong,
    ADD CONSTRAINT fk_tvhd_hoi_dong_cascade FOREIGN KEY (ma_hoi_dong)
        REFERENCES hoi_dong(ma_hoi_dong)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE hoi_dong_de_tai
    DROP FOREIGN KEY fk_hddt_hoi_dong,
    ADD CONSTRAINT fk_hddt_hoi_dong_cascade FOREIGN KEY (ma_hoi_dong)
        REFERENCES hoi_dong(ma_hoi_dong)
        ON DELETE RESTRICT ON UPDATE CASCADE;
