package vn.edu.group12.qldt.entity.detai;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "de_tai_giang_vien")
public class DeTaiGiangVien {

    @EmbeddedId
    private DeTaiGiangVienId id;

    @Column(name = "loai_phan_cong", length = 20, nullable = false)
    private String loaiPhanCong; // HUONG_DAN, PHAN_BIEN, CHAM_DOC_LAP

    @Column(name = "ma_nguoi_phan_cong", length = 30, nullable = false)
    private String maNguoiPhanCong;

    @Column(name = "phan_cong_luc", nullable = false)
    private LocalDateTime phanCongLuc = LocalDateTime.now();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public DeTaiGiangVien() {}

    public DeTaiGiangVien(DeTaiGiangVienId id, String loaiPhanCong, String maNguoiPhanCong) {
        this.id = id;
        this.loaiPhanCong = loaiPhanCong;
        this.maNguoiPhanCong = maNguoiPhanCong;
        this.phanCongLuc = LocalDateTime.now();
    }

    public DeTaiGiangVienId getId() { return id; }
    public void setId(DeTaiGiangVienId id) { this.id = id; }

    public String getLoaiPhanCong() { return loaiPhanCong; }
    public void setLoaiPhanCong(String loaiPhanCong) { this.loaiPhanCong = loaiPhanCong; }

    public String getMaNguoiPhanCong() { return maNguoiPhanCong; }
    public void setMaNguoiPhanCong(String maNguoiPhanCong) { this.maNguoiPhanCong = maNguoiPhanCong; }

    public LocalDateTime getPhanCongLuc() { return phanCongLuc; }
    public void setPhanCongLuc(LocalDateTime phanCongLuc) { this.phanCongLuc = phanCongLuc; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    // Convenience delegates
    public String getMaDeTai() {
        return id != null ? id.getMaDeTai() : null;
    }

    public String getMaGiangVien() {
        return id != null ? id.getMaGiangVien() : null;
    }
}
