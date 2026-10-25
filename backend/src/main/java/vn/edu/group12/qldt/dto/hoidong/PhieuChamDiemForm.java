package vn.edu.group12.qldt.dto.hoidong;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

public class PhieuChamDiemForm {

    // Cho phép để trống khi lưu nháp.
    // Nếu nhập điểm thì phải nằm trong khoảng 0–10.
    @DecimalMin(value = "0.00", message = "Điểm phải từ 0 đến 10.")
    @DecimalMax(value = "10.00", message = "Điểm phải từ 0 đến 10.")
    @Digits(
        integer = 2,
        fraction = 2,
        message = "Điểm chỉ được có tối đa 2 chữ số phần thập phân."
    )
    private BigDecimal diem;

    // Khi nộp chính thức, Service sẽ yêu cầu nhận xét không được trống.
    private String nhanXet;

    public PhieuChamDiemForm() {
    }

    public BigDecimal getDiem() {
        return diem;
    }

    public void setDiem(BigDecimal diem) {
        this.diem = diem;
    }

    public String getNhanXet() {
        return nhanXet;
    }

    public void setNhanXet(String nhanXet) {
        this.nhanXet = nhanXet;
    }
}