package vn.edu.group12.qldt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import vn.edu.group12.qldt.enums.ChucDanhHoiDong;

public class ThanhVienHoiDongForm {

    @NotBlank(message = "Vui lòng chọn giảng viên.")
    @Size(max = 30, message = "Mã giảng viên tối đa 30 ký tự.")
    private String maGiangVien;

    @NotNull(message = "Vui lòng chọn chức danh.")
    private ChucDanhHoiDong chucDanh;

    public ThanhVienHoiDongForm() {
    }

    public String getMaGiangVien() {
        return maGiangVien;
    }

    public void setMaGiangVien(String maGiangVien) {
        this.maGiangVien = maGiangVien;
    }

    public ChucDanhHoiDong getChucDanh() {
        return chucDanh;
    }

    public void setChucDanh(ChucDanhHoiDong chucDanh) {
        this.chucDanh = chucDanh;
    }
}