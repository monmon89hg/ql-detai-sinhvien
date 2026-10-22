package vn.edu.group12.qldt.entity.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class NguoiDungVaiTroId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ma_nguoi_dung", length = 30, nullable = false)
    private String maNguoiDung;

    @Column(name = "ma_vai_tro", length = 30, nullable = false)
    private String maVaiTro;

    public NguoiDungVaiTroId() {
    }

    public NguoiDungVaiTroId(String maNguoiDung, String maVaiTro) {
        this.maNguoiDung = maNguoiDung;
        this.maVaiTro = maVaiTro;
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getMaVaiTro() {
        return maVaiTro;
    }

    public void setMaVaiTro(String maVaiTro) {
        this.maVaiTro = maVaiTro;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof NguoiDungVaiTroId that)) {
            return false;
        }
        return Objects.equals(maNguoiDung, that.maNguoiDung)
                && Objects.equals(maVaiTro, that.maVaiTro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNguoiDung, maVaiTro);
    }
}