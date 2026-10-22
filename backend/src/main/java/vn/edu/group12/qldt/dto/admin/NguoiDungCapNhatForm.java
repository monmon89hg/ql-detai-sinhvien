package vn.edu.group12.qldt.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NguoiDungCapNhatForm {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 120, message = "Email tối đa 120 ký tự")
    private String email;

    @Size(max = 20, message = "Mã bộ môn tối đa 20 ký tự")
    private String maBoMon;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String soDienThoai;

    @Size(max = 50, message = "Lớp hành chính tối đa 50 ký tự")
    private String lopHanhChinh;

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMaBoMon() { return maBoMon; }
    public void setMaBoMon(String maBoMon) { this.maBoMon = maBoMon; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getLopHanhChinh() { return lopHanhChinh; }
    public void setLopHanhChinh(String lopHanhChinh) {
        this.lopHanhChinh = lopHanhChinh;
    }
}