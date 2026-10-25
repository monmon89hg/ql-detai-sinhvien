package vn.edu.group12.qldt.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class NguoiDungForm {

    @NotBlank(message = "Mã người dùng không được để trống")
    @Size(max = 30, message = "Mã người dùng tối đa 30 ký tự")
    private String maNguoiDung;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 120, message = "Email tối đa 120 ký tự")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 72, message = "Mật khẩu phải dài từ 8 đến 72 ký tự")
    private String matKhau;

    @NotBlank(message = "Loại người dùng không được để trống")
    @Pattern(
            regexp = "(?i)(SV|GV|CAN_BO)",
            message = "Loại người dùng phải là SV, GV hoặc CAN_BO"
    )
    private String loaiNguoiDung;

    @Size(max = 20, message = "Mã bộ môn tối đa 20 ký tự")
    private String maBoMon;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String soDienThoai;

    @Size(max = 50, message = "Lớp hành chính tối đa 50 ký tự")
    private String lopHanhChinh;

    @NotEmpty(message = "Hãy chọn ít nhất một vai trò")
    private List<
            @NotBlank(message = "Mã vai trò không được để trống")
            @Size(max = 30, message = "Mã vai trò tối đa 30 ký tự")
            String
    > maVaiTros = new ArrayList<>();

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getLoaiNguoiDung() {
        return loaiNguoiDung;
    }

    public void setLoaiNguoiDung(String loaiNguoiDung) {
        this.loaiNguoiDung = loaiNguoiDung;
    }

    public String getMaBoMon() {
        return maBoMon;
    }

    public void setMaBoMon(String maBoMon) {
        this.maBoMon = maBoMon;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getLopHanhChinh() {
        return lopHanhChinh;
    }

    public void setLopHanhChinh(String lopHanhChinh) {
        this.lopHanhChinh = lopHanhChinh;
    }

    public List<String> getMaVaiTros() {
        return maVaiTros;
    }

    public void setMaVaiTros(List<String> maVaiTros) {
        this.maVaiTros = maVaiTros;
    }
}