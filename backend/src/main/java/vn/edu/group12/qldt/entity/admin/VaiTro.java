package vn.edu.group12.qldt.entity.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vai_tro")
public class VaiTro extends AuditedEntity {

    @Id
    @Column(name = "ma_vai_tro", length = 30, nullable = false)
    private String maVaiTro;

    @Column(name = "ten_vai_tro", length = 100, nullable = false)
    private String tenVaiTro;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    protected VaiTro() {
    }

    public String getMaVaiTro() {
        return maVaiTro;
    }

    public void setMaVaiTro(String maVaiTro) {
        this.maVaiTro = maVaiTro;
    }

    public String getTenVaiTro() {
        return tenVaiTro;
    }

    public void setTenVaiTro(String tenVaiTro) {
        this.tenVaiTro = tenVaiTro;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}