package vn.edu.group12.qldt.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BoMonCapNhatForm {

    @NotBlank(message = "Tên bộ môn không được để trống")
    @Size(max = 150, message = "Tên bộ môn tối đa 150 ký tự")
    private String tenBoMon;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String moTa;

    public String getTenBoMon() { return tenBoMon; }
    public void setTenBoMon(String tenBoMon) { this.tenBoMon = tenBoMon; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}