package vn.edu.group12.qldt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HoiDongForm {

    @NotBlank(message = "Vui lòng nhập mã hội đồng.")
    @Size(max = 30, message = "Mã hội đồng tối đa 30 ký tự.")
    private String maHoiDong;

    @NotBlank(message = "Vui lòng nhập tên hội đồng.")
    @Size(max = 150, message = "Tên hội đồng tối đa 150 ký tự.")
    private String tenHoiDong;

    @NotBlank(message = "Vui lòng chọn đợt đăng ký.")
    @Size(max = 30, message = "Mã đợt tối đa 30 ký tự.")
    private String maDot;

    @Size(max = 200, message = "Địa điểm tối đa 200 ký tự.")
    private String diaDiem;

    public HoiDongForm() {
    }

    public String getMaHoiDong() {
        return maHoiDong;
    }

    public void setMaHoiDong(String maHoiDong) {
        this.maHoiDong = maHoiDong;
    }

    public String getTenHoiDong() {
        return tenHoiDong;
    }

    public void setTenHoiDong(String tenHoiDong) {
        this.tenHoiDong = tenHoiDong;
    }

    public String getMaDot() {
        return maDot;
    }

    public void setMaDot(String maDot) {
        this.maDot = maDot;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }
}