package vn.edu.group12.qldt.entity.hoidong;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import vn.edu.group12.qldt.enums.hoidong.ChucDanhHoiDong;

@Entity
@Table(name = "thanh_vien_hoi_dong")
public class ThanhVienHoiDong {

    @EmbeddedId
    private ThanhVienHoiDongId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "chuc_danh", nullable = false, length = 15)
    private ChucDanhHoiDong chucDanh;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public ThanhVienHoiDong() {
    }

    public ThanhVienHoiDongId getId() {
        return id;
    }

    public void setId(ThanhVienHoiDongId id) {
        this.id = id;
    }

    public ChucDanhHoiDong getChucDanh() {
        return chucDanh;
    }

    public void setChucDanh(ChucDanhHoiDong chucDanh) {
        this.chucDanh = chucDanh;
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