package vn.edu.group12.qldt.entity.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "nguoi_dung")
public class NguoiDung extends AuditedEntity {

    @Id
    @Column(name = "ma_nguoi_dung", length = 30, nullable = false)
    private String maNguoiDung;

    @Column(name = "ho_ten", length = 100, nullable = false)
    private String hoTen;

    @Column(name = "email", length = 120, nullable = false, unique = true)
    private String email;

    @Column(name = "mat_khau_hash", length = 255, nullable = false)
    private String matKhauHash;

    @Column(name = "loai_nguoi_dung", length = 10, nullable = false)
    private String loaiNguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bo_mon", referencedColumnName = "ma_bo_mon")
    private BoMon boMon;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "lop_hanh_chinh", length = 50)
    private String lopHanhChinh;

    @Column(name = "trang_thai", length = 15, nullable = false)
    private String trangThai = "HOAT_DONG";

    @Column(name = "buoc_doi_mat_khau", nullable = false)
    private Boolean buocDoiMatKhau = true;

    @Column(name = "lan_dang_nhap_cuoi")
    private LocalDateTime lanDangNhapCuoi;

    public NguoiDung() {
    }

    public String getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(String maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhauHash() {
        return matKhauHash;
    }

    public void setMatKhauHash(String matKhauHash) {
        this.matKhauHash = matKhauHash;
    }

    public String getLoaiNguoiDung() {
        return loaiNguoiDung;
    }

    public void setLoaiNguoiDung(String loaiNguoiDung) {
        this.loaiNguoiDung = loaiNguoiDung;
    }

    public BoMon getBoMon() {
        return boMon;
    }

    public void setBoMon(BoMon boMon) {
        this.boMon = boMon;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getLopHanhChinh() {
        return lopHanhChinh;
    }

    public void setLopHanhChinh(String lopHanhChinh) {
        this.lopHanhChinh = lopHanhChinh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Boolean getBuocDoiMatKhau() {
        return buocDoiMatKhau;
    }

    public void setBuocDoiMatKhau(Boolean buocDoiMatKhau) {
        this.buocDoiMatKhau = buocDoiMatKhau;
    }

    public LocalDateTime getLanDangNhapCuoi() {
        return lanDangNhapCuoi;
    }

    public void setLanDangNhapCuoi(LocalDateTime lanDangNhapCuoi) {
        this.lanDangNhapCuoi = lanDangNhapCuoi;
    }
}