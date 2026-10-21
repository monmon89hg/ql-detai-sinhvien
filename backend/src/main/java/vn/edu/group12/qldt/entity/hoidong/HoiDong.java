package vn.edu.group12.qldt.entity.hoidong;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import vn.edu.group12.qldt.enums.hoidong.TrangThaiHoiDong;

@Entity
@Table(name = "hoi_dong")
public class HoiDong {

    @Id
    @Column(name = "ma_hoi_dong", nullable = false, length = 30)
    private String maHoiDong;

    @Column(name = "ten_hoi_dong", nullable = false, length = 150)
    private String tenHoiDong;

    @Column(name = "ma_dot", nullable = false, length = 30)
    private String maDot;

    @Column(name = "dia_diem", length = 200)
    private String diaDiem;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 15)
    private TrangThaiHoiDong trangThai = TrangThaiHoiDong.NHAP;

    @Column(name = "ma_nguoi_tao", nullable = false, length = 30)
    private String maNguoiTao;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public HoiDong() {
    }

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

    public String getMaDot() {
        return maDot;
    }

    public void setMaDot(String maDot) {
        this.maDot = maDot;
    }

    public String getDiaDiem() {
        return diaDiem;
    }

    public void setDiaDiem(String diaDiem) {
        this.diaDiem = diaDiem;
    }

    public TrangThaiHoiDong getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiHoiDong trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaNguoiTao() {
        return maNguoiTao;
    }

    public void setMaNguoiTao(String maNguoiTao) {
        this.maNguoiTao = maNguoiTao;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}