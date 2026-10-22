package vn.edu.group12.qldt.entity.hoidong;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import vn.edu.group12.qldt.enums.hoidong.TrangThaiKetQua;

@Entity
@Table(name = "ket_qua")
public class KetQua {

    // Mỗi nhóm có tối đa một kết quả.
    @Id
    @Column(name = "ma_nhom", nullable = false, length = 30)
    private String maNhom;

    @Column(name = "so_phieu", nullable = false)
    private int soPhieu;

    @Column(name = "tong_diem", nullable = false, precision = 8, scale = 2)
    private BigDecimal tongDiem;

    @Column(name = "diem_cuoi", nullable = false, precision = 4, scale = 2)
    private BigDecimal diemCuoi;

    @Column(name = "nhan_xet_tong_hop", columnDefinition = "TEXT")
    private String nhanXetTongHop;

    @Column(name = "ma_nguoi_tong_hop", nullable = false, length = 30)
    private String maNguoiTongHop;

    @Column(name = "tong_hop_luc", nullable = false)
    private LocalDateTime tongHopLuc;

    // DA_TONG_HOP chưa cho xem; CONG_BO mới là kết quả công khai.
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 15)
    private TrangThaiKetQua trangThai = TrangThaiKetQua.DA_TONG_HOP;

    @Column(name = "ma_nguoi_cong_bo", length = 30)
    private String maNguoiCongBo;

    @Column(name = "cong_bo_luc")
    private LocalDateTime congBoLuc;

    // Hai thời điểm này do database tự quản lý.
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // Phát hiện trường hợp hai người cùng sửa một kết quả.
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public KetQua() {
    }

    public String getMaNhom() {
        return maNhom;
    }

    public void setMaNhom(String maNhom) {
        this.maNhom = maNhom;
    }

    public int getSoPhieu() {
        return soPhieu;
    }

    public void setSoPhieu(int soPhieu) {
        this.soPhieu = soPhieu;
    }

    public BigDecimal getTongDiem() {
        return tongDiem;
    }

    public void setTongDiem(BigDecimal tongDiem) {
        this.tongDiem = tongDiem;
    }

    public BigDecimal getDiemCuoi() {
        return diemCuoi;
    }

    public void setDiemCuoi(BigDecimal diemCuoi) {
        this.diemCuoi = diemCuoi;
    }

    public String getNhanXetTongHop() {
        return nhanXetTongHop;
    }

    public void setNhanXetTongHop(String nhanXetTongHop) {
        this.nhanXetTongHop = nhanXetTongHop;
    }

    public String getMaNguoiTongHop() {
        return maNguoiTongHop;
    }

    public void setMaNguoiTongHop(String maNguoiTongHop) {
        this.maNguoiTongHop = maNguoiTongHop;
    }

    public LocalDateTime getTongHopLuc() {
        return tongHopLuc;
    }

    public void setTongHopLuc(LocalDateTime tongHopLuc) {
        this.tongHopLuc = tongHopLuc;
    }

    public TrangThaiKetQua getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiKetQua trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaNguoiCongBo() {
        return maNguoiCongBo;
    }

    public void setMaNguoiCongBo(String maNguoiCongBo) {
        this.maNguoiCongBo = maNguoiCongBo;
    }

    public LocalDateTime getCongBoLuc() {
        return congBoLuc;
    }

    public void setCongBoLuc(LocalDateTime congBoLuc) {
        this.congBoLuc = congBoLuc;
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