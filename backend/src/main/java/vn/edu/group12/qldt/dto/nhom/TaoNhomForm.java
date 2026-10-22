package vn.edu.group12.qldt.dto.nhom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TaoNhomForm {
    @NotBlank(message = "Vui lòng nhập tên nhóm.")
    @Size(max = 100, message = "Tên nhóm tối đa 100 ký tự.")
    private String tenNhom;

    @NotBlank(message = "Vui lòng chọn đợt đăng ký.")
    @Size(max = 30, message = "Mã đợt tối đa 30 ký tự.")
    private String maDot;

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }

    public String getMaDot() {
        return maDot;
    }

    public void setMaDot(String maDot) {
        this.maDot = maDot;
    }
}
