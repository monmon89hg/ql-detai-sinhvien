package vn.edu.group12.qldt.entity.detai;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "de_tai")
public class DeTai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_de_tai", length = 30, nullable = false, unique = true)
    private String maDeTai;

    @Column(name = "dot_dang_ky_id", nullable = false)
    private Long dotDangKyId;

    @Column(name = "bo_mon_id", nullable = false)
    private Long boMonId;

    @Column(name = "ten_de_tai", length = 255, nullable = false)
    private String tenDeTai;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "yeu_cau", columnDefinition = "TEXT")
    private String yeuCau;

    @Column(name = "nguoi_de_xuat_id", nullable = false)
    private Long nguoiDeXuatId;

    @Column(name = "trang_thai", length = 20, nullable = false)
    private String trangThai = "NHAP"; // NHAP, CHO_DUYET, DA_DUYET, TU_CHOI, CONG_BO, NGUNG_NHAN

    @Column(name = "nguoi_duyet_id")
    private Long nguoiDuyetId;

    @Column(name = "duyet_luc")
    private LocalDateTime duyetLuc;

    @Column(name = "ly_do_tu_choi", length = 1000)
    private String lyDoTuChoi;

    @Column(name = "cong_bo_luc")
    private LocalDateTime congBoLuc;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public DeTai() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMaDeTai() { return maDeTai; }
    public void setMaDeTai(String maDeTai) { this.maDeTai = maDeTai; }

    public Long getDotDangKyId() { return dotDangKyId; }
    public void setDotDangKyId(Long dotDangKyId) { this.dotDangKyId = dotDangKyId; }

    public Long getBoMonId() { return boMonId; }
    public void setBoMonId(Long boMonId) { this.boMonId = boMonId; }

    public String getTenDeTai() { return tenDeTai; }
    public void setTenDeTai(String tenDeTai) { this.tenDeTai = tenDeTai; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getYeuCau() { return yeuCau; }
    public void setYeuCau(String yeuCau) { this.yeuCau = yeuCau; }

    public Long getNguoiDeXuatId() { return nguoiDeXuatId; }
    public void setNguoiDeXuatId(Long nguoiDeXuatId) { this.nguoiDeXuatId = nguoiDeXuatId; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Long getNguoiDuyetId() { return nguoiDuyetId; }
    public void setNguoiDuyetId(Long nguoiDuyetId) { this.nguoiDuyetId = nguoiDuyetId; }

    public LocalDateTime getDuyetLuc() { return duyetLuc; }
    public void setDuyetLuc(LocalDateTime duyetLuc) { this.duyetLuc = duyetLuc; }

    public String getLyDoTuChoi() { return lyDoTuChoi; }
    public void setLyDoTuChoi(String lyDoTuChoi) { this.lyDoTuChoi = lyDoTuChoi; }

    public LocalDateTime getCongBoLuc() { return congBoLuc; }
    public void setCongBoLuc(LocalDateTime congBoLuc) { this.congBoLuc = congBoLuc; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
