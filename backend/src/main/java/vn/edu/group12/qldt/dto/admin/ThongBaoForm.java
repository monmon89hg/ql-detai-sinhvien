package vn.edu.group12.qldt.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ThongBaoForm {

    @NotBlank(message = "Mã thông báo không được để trống")
    @Size(max = 30, message = "Mã thông báo tối đa 30 ký tự")
    private String maThongBao;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String tieuDe;

    @NotBlank(message = "Nội dung không được để trống")
    private String noiDung;

    @Size(max = 30, message = "Mã đợt tối đa 30 ký tự")
    private String maDot;

    @NotBlank(message = "Phạm vi không được để trống")
    @Pattern(
            regexp = "TAT_CA|CA_NHAN",
            message = "Phạm vi không hợp lệ"
    )
    private String phamVi = "TAT_CA";

    private List<String> maNguoiNhan = new ArrayList<>();

    public String getMaThongBao() { return maThongBao; }
    public void setMaThongBao(String maThongBao) {
        this.maThongBao = maThongBao;
    }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getMaDot() { return maDot; }
    public void setMaDot(String maDot) { this.maDot = maDot; }

    public String getPhamVi() { return phamVi; }
    public void setPhamVi(String phamVi) { this.phamVi = phamVi; }

    public List<String> getMaNguoiNhan() { return maNguoiNhan; }
    public void setMaNguoiNhan(List<String> maNguoiNhan) {
        this.maNguoiNhan = maNguoiNhan == null ? new ArrayList<>() : maNguoiNhan;
    }
}
