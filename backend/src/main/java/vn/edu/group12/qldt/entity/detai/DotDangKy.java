package vn.edu.group12.qldt.entity.detai;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dot_dang_ky")
public class DotDangKy {

    @Id
    @Column(name = "ma_dot", length = 30, nullable = false)
    private String maDot;

    @Column(name = "ten_dot", length = 150, nullable = false)
    private String tenDot;

    @Column(name = "loai_de_tai", length = 15, nullable = false)
    private String loaiDeTai; // MON_HOC, NCKH, TLCN, KLTN

    @Column(name = "bat_dau_gv", nullable = false)
    private LocalDateTime batDauGv;

    @Column(name = "ket_thuc_gv", nullable = false)
    private LocalDateTime ketThucGv;

    @Column(name = "bat_dau_sv", nullable = false)
    private LocalDateTime batDauSv;

    @Column(name = "ket_thuc_sv", nullable = false)
    private LocalDateTime ketThucSv;

    @Column(name = "han_nop_bao_cao")
    private LocalDateTime hanNopBaoCao;

    @Column(name = "han_nop_diem_pb")
    private LocalDateTime hanNopDiemPb;

    @Column(name = "ngay_bao_cao_hd")
    private LocalDate ngayBaoCaoHd;

    @Column(name = "trang_thai", length = 15, nullable = false)
    private String trangThai = "NHAP"; // NHAP, CONG_BO, DONG

    @Column(name = "ma_nguoi_tao", length = 30, nullable = false)
    private String maNguoiTao;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public DotDangKy() {}

    public String getMaDot() { return maDot; }
    public void setMaDot(String maDot) { this.maDot = maDot; }

    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }

    public String getLoaiDeTai() { return loaiDeTai; }
    public void setLoaiDeTai(String loaiDeTai) { this.loaiDeTai = loaiDeTai; }

    public LocalDateTime getBatDauGv() { return batDauGv; }
    public void setBatDauGv(LocalDateTime batDauGv) { this.batDauGv = batDauGv; }

    public LocalDateTime getKetThucGv() { return ketThucGv; }
    public void setKetThucGv(LocalDateTime ketThucGv) { this.ketThucGv = ketThucGv; }

    public LocalDateTime getBatDauSv() { return batDauSv; }
    public void setBatDauSv(LocalDateTime batDauSv) { this.batDauSv = batDauSv; }

    public LocalDateTime getKetThucSv() { return ketThucSv; }
    public void setKetThucSv(LocalDateTime ketThucSv) { this.ketThucSv = ketThucSv; }

    public LocalDateTime getHanNopBaoCao() { return hanNopBaoCao; }
    public void setHanNopBaoCao(LocalDateTime hanNopBaoCao) { this.hanNopBaoCao = hanNopBaoCao; }

    public LocalDateTime getHanNopDiemPb() { return hanNopDiemPb; }
    public void setHanNopDiemPb(LocalDateTime hanNopDiemPb) { this.hanNopDiemPb = hanNopDiemPb; }

    public LocalDate getNgayBaoCaoHd() { return ngayBaoCaoHd; }
    public void setNgayBaoCaoHd(LocalDate ngayBaoCaoHd) { this.ngayBaoCaoHd = ngayBaoCaoHd; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getMaNguoiTao() { return maNguoiTao; }
    public void setMaNguoiTao(String maNguoiTao) { this.maNguoiTao = maNguoiTao; }

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
            case "NHAP" -> "Bản nháp";
            case "DONG" -> "Đã đóng";
            default -> trangThai;
        };
    }
}
