package vn.edu.group12.qldt.entity.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bo_mon")
public class BoMon extends AuditedEntity {

    @Id
    @Column(name = "ma_bo_mon", length = 20, nullable = false)
    private String maBoMon;

    @Column(name = "ten_bo_mon", length = 150, nullable = false)
    private String tenBoMon;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "hoat_dong", nullable = false)
    private Boolean hoatDong = true;

    public BoMon() {
    }

    public String getMaBoMon() {
        return maBoMon;
    }

    public void setMaBoMon(String maBoMon) {
        this.maBoMon = maBoMon;
    }

    public String getTenBoMon() {
        return tenBoMon;
    }

    public void setTenBoMon(String tenBoMon) {
        this.tenBoMon = tenBoMon;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Boolean getHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(Boolean hoatDong) {
        this.hoatDong = hoatDong;
    }
}