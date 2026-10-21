package vn.edu.group12.qldt.entity.hoidong;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import vn.edu.group12.qldt.enums.hoidong.LoaiPhieuCham;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiPhieuCham;

@Entity
@Table(name = "phieu_cham_diem")
public class PhieuChamDiem {

    // Một phiếu được xác định bằng mã nhóm và mã giảng viên.
    @EmbeddedId
    private PhieuChamDiemId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_phieu", nullable = false, length = 20)
    private LoaiPhieuCham loaiPhieu;

    // Dùng cho phiếu PHAN_BIEN hoặc CHAM_DOC_LAP.
    @Column(name = "ma_de_tai_phan_cong", length = 30)
    private String maDeTaiPhanCong;

    // Dùng cho phiếu HOI_DONG, phải trùng mã nhóm trong khóa chính.
    @Column(name = "ma_nhom_hoi_dong", length = 30)
    private String maNhomHoiDong;

    // BigDecimal phù hợp với cột DECIMAL, tránh sai số kiểu double.
    @Column(name = "diem", precision = 4, scale = 2)
    private BigDecimal diem;

    @Column(name = "nhan_xet", columnDefinition = "TEXT")
    private String nhanXet;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 15)
    private TrangThaiPhieuCham trangThai = TrangThaiPhieuCham.NHAP;

    // Chỉ có giá trị khi phiếu được xác nhận nộp.
    @Column(name = "nop_luc")
    private LocalDateTime nopLuc;

    // Database tự quản lý thời điểm tạo và cập nhật.
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // Phát hiện xung đột khi cùng một phiếu bị sửa đồng thời.
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public PhieuChamDiem() {
    }

    public PhieuChamDiemId getId() {
        return id;
    }

    public void setId(PhieuChamDiemId id) {
        this.id = id;
    }

    public LoaiPhieuCham getLoaiPhieu() {
        return loaiPhieu;
    }

    public void setLoaiPhieu(LoaiPhieuCham loaiPhieu) {
        this.loaiPhieu = loaiPhieu;
    }

    public String getMaDeTaiPhanCong() {
        return maDeTaiPhanCong;
    }

    public void setMaDeTaiPhanCong(String maDeTaiPhanCong) {
        this.maDeTaiPhanCong = maDeTaiPhanCong;
    }

    public String getMaNhomHoiDong() {
        return maNhomHoiDong;
    }

    public void setMaNhomHoiDong(String maNhomHoiDong) {
        this.maNhomHoiDong = maNhomHoiDong;
    }

    public BigDecimal getDiem() {
        return diem;
    }

    public void setDiem(BigDecimal diem) {
        this.diem = diem;
    }

    public String getNhanXet() {
        return nhanXet;
    }

    public void setNhanXet(String nhanXet) {
        this.nhanXet = nhanXet;
    }

    public TrangThaiPhieuCham getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiPhieuCham trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNopLuc() {
        return nopLuc;
    }

    public void setNopLuc(LocalDateTime nopLuc) {
        this.nopLuc = nopLuc;
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
