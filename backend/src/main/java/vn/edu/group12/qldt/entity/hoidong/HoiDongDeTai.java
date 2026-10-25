package vn.edu.group12.qldt.entity.hoidong;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "hoi_dong_de_tai")
public class HoiDongDeTai {

    @Id
    @Column(name = "ma_nhom", nullable = false, length = 30)
    private String maNhom;

    @Column(name = "ma_hoi_dong", nullable = false, length = 30)
    private String maHoiDong;

    @Column(name = "ma_nguoi_phan_cong", nullable = false, length = 30)
    private String maNguoiPhanCong;

    @Column(name = "phan_cong_luc", nullable = false)
    private LocalDateTime phanCongLuc;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public HoiDongDeTai() {
    }

    public String getMaNhom() {
        return maNhom;
    }

    public void setMaNhom(String maNhom) {
        this.maNhom = maNhom;
    }

    public String getMaHoiDong() {
        return maHoiDong;
    }

    public void setMaHoiDong(String maHoiDong) {
        this.maHoiDong = maHoiDong;
    }

    public String getMaNguoiPhanCong() {
        return maNguoiPhanCong;
    }

    public void setMaNguoiPhanCong(String maNguoiPhanCong) {
        this.maNguoiPhanCong = maNguoiPhanCong;
    }

    public LocalDateTime getPhanCongLuc() {
        return phanCongLuc;
    }

    public void setPhanCongLuc(LocalDateTime phanCongLuc) {
        this.phanCongLuc = phanCongLuc;
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