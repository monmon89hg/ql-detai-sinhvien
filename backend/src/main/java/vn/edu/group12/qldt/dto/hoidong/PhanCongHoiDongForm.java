package vn.edu.group12.qldt.dto.hoidong;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PhanCongHoiDongForm {

    @NotBlank(message = "Vui lòng chọn nhóm sinh viên.")
    @Size(max = 30, message = "Mã nhóm không được vượt quá 30 ký tự.")
    private String maNhom;

    public String getMaNhom() {
        return maNhom;
    }

    public void setMaNhom(String maNhom) {
        this.maNhom = maNhom;
    }
}