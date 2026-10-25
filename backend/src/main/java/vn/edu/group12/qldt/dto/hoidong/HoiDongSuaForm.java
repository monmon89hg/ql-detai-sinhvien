package vn.edu.group12.qldt.dto.hoidong;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Chỉ nhận các thông tin được phép sửa; không nhận mã đợt hay người tạo. */
public class HoiDongSuaForm {

    @NotBlank(message = "Vui lòng nhập mã hội đồng.")
    @Size(max = 30, message = "Mã hội đồng tối đa 30 ký tự.")
    private String maHoiDong;

    @NotBlank(message = "Vui lòng nhập tên hội đồng.")
    @Size(max = 150, message = "Tên hội đồng tối đa 150 ký tự.")
    private String tenHoiDong;

    @Size(max = 200, message = "Địa điểm tối đa 200 ký tự.")
    private String diaDiem;

    @NotNull(message = "Vui lòng tải lại trang trước khi lưu.")
    @PositiveOrZero(message = "Phiên bản dữ liệu không hợp lệ.")
    private Long version;

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

    public String getDiaDiem() {
        return diaDiem;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
