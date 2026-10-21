package vn.edu.group12.qldt.entity.detai;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "de_tai_giang_vien")
public class DeTaiGiangVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "de_tai_id", nullable = false)
    private Long deTaiId;

    @Column(name = "giang_vien_id", nullable = false)
    private Long giangVienId;

    @Column(name = "loai_phan_cong", length = 20, nullable = false)
    private String loaiPhanCong; // HUONG_DAN, PHAN_BIEN, CHAM_DOC_LAP

    @Column(name = "nguoi_phan_cong_id", nullable = false)
    private Long nguoiPhanCongId;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDeTaiId() { return deTaiId; }
    public void setDeTaiId(Long deTaiId) { this.deTaiId = deTaiId; }

    public Long getGiangVienId() { return giangVienId; }
    public void setGiangVienId(Long giangVienId) { this.giangVienId = giangVienId; }

    public String getLoaiPhanCong() { return loaiPhanCong; }
    public void setLoaiPhanCong(String loaiPhanCong) { this.loaiPhanCong = loaiPhanCong; }

    public Long getNguoiPhanCongId() { return nguoiPhanCongId; }
    public void setNguoiPhanCongId(Long nguoiPhanCongId) { this.nguoiPhanCongId = nguoiPhanCongId; }

    public LocalDateTime getPhanCongLuc() { return phanCongLuc; }
    public void setPhanCongLuc(LocalDateTime phanCongLuc) { this.phanCongLuc = phanCongLuc; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
