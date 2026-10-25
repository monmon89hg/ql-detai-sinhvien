package vn.edu.group12.qldt.entity.detai;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "de_tai")
public class DeTai {

    @Id
    @Column(name = "ma_de_tai", length = 30, nullable = false)
    private String maDeTai;

    @Column(name = "ma_dot", length = 30, nullable = false)
    private String maDot;

    @Column(name = "ma_bo_mon", length = 20, nullable = false)
    private String maBoMon;

    @Column(name = "ten_de_tai", length = 255, nullable = false)
    private String tenDeTai;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "yeu_cau", columnDefinition = "TEXT")
    private String yeuCau;

    @Column(name = "ma_nguoi_de_xuat", length = 30, nullable = false)
    private String maNguoiDeXuat;

    @Column(name = "trang_thai", length = 20, nullable = false)
    private String trangThai = "NHAP"; // NHAP, CHO_DUYET, DA_DUYET, TU_CHOI, CONG_BO, NGUNG_NHAN

    @Column(name = "ma_nguoi_duyet", length = 30)
    private String maNguoiDuyet;

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

    public String getMaDeTai() { return maDeTai; }
    public void setMaDeTai(String maDeTai) { this.maDeTai = maDeTai; }

    public String getMaDot() { return maDot; }
    public void setMaDot(String maDot) { this.maDot = maDot; }

    public String getMaBoMon() { return maBoMon; }
    public void setMaBoMon(String maBoMon) { this.maBoMon = maBoMon; }

    public String getTenDeTai() { return tenDeTai; }
    public void setTenDeTai(String tenDeTai) { this.tenDeTai = tenDeTai; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getYeuCau() { return yeuCau; }
    public void setYeuCau(String yeuCau) { this.yeuCau = yeuCau; }

    public String getMaNguoiDeXuat() { return maNguoiDeXuat; }
    public void setMaNguoiDeXuat(String maNguoiDeXuat) { this.maNguoiDeXuat = maNguoiDeXuat; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getMaNguoiDuyet() { return maNguoiDuyet; }
    public void setMaNguoiDuyet(String maNguoiDuyet) { this.maNguoiDuyet = maNguoiDuyet; }

    public LocalDateTime getDuyetLuc() { return duyetLuc; }
    public void setDuyetLuc(LocalDateTime duyetLuc) { this.duyetLuc = duyetLuc; }

    public String getLyDoTuChoi() { return lyDoTuChoi; }
    public void setLyDoTuChoi(String lyDoTuChoi) { this.lyDoTuChoi = lyDoTuChoi; }

    public LocalDateTime getCongBoLuc() { return congBoLuc; }
    public void setCongBoLuc(LocalDateTime congBoLuc) { this.congBoLuc = congBoLuc; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getTenTrangThaiHienThi() {
        if (trangThai == null) return "";
        return switch (trangThai) {
            case "CONG_BO" -> "Đã công bố";
            case "CHO_DUYET" -> "Chờ duyệt";
            case "DA_DUYET" -> "Đã duyệt";
            case "TU_CHOI" -> "Bị từ chối";
            case "NHAP" -> "Bản nháp";
            case "NGUNG_NHAN" -> "Ngừng nhận";
            default -> trangThai;
        };
    }
}
