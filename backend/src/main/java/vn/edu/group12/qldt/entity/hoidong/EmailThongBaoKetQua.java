package vn.edu.group12.qldt.entity.hoidong;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import vn.edu.group12.qldt.enums.hoidong.TrangThaiEmailKetQua;

@Entity
@Table(name = "email_thong_bao_ket_qua")
@IdClass(EmailThongBaoKetQuaId.class)
public class EmailThongBaoKetQua {

    // Khóa chính: một email cho mỗi sinh viên của một nhóm.
    @Id
    @Column(name = "ma_nhom", nullable = false, length = 30)
    private String maNhom;

    @Id
    @Column(name = "ma_sinh_vien", nullable = false, length = 30)
    private String maSinhVien;

    @Column(name = "dia_chi_email", nullable = false, length = 120)
    private String diaChiEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 15)
    private TrangThaiEmailKetQua trangThai = TrangThaiEmailKetQua.CHO_GUI;

    @Column(name = "so_lan_thu", nullable = false)
    private int soLanThu;

    @Column(name = "loi_cuoi", length = 1000)
    private String loiCuoi;

    @Column(name = "lan_thu_cuoi_luc")
    private LocalDateTime lanThuCuoiLuc;

    @Column(name = "gui_luc")
    private LocalDateTime guiLuc;

    // Hai cột này do database tự cập nhật.
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public EmailThongBaoKetQua() {
    }

    public String getMaNhom() {
        return maNhom;
    }

    public void setMaNhom(String maNhom) {
        this.maNhom = maNhom;
    }

    public String getMaSinhVien() {
        return maSinhVien;
    }

    public void setMaSinhVien(String maSinhVien) {
        this.maSinhVien = maSinhVien;
    }

    public String getDiaChiEmail() {
        return diaChiEmail;
    }

    public void setDiaChiEmail(String diaChiEmail) {
        this.diaChiEmail = diaChiEmail;
    }

    public TrangThaiEmailKetQua getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiEmailKetQua trangThai) {
        this.trangThai = trangThai;
    }

    public int getSoLanThu() {
        return soLanThu;
    }

    public void setSoLanThu(int soLanThu) {
        this.soLanThu = soLanThu;
    }

    public String getLoiCuoi() {
        return loiCuoi;
    }

    public void setLoiCuoi(String loiCuoi) {
        this.loiCuoi = loiCuoi;
    }

    public LocalDateTime getLanThuCuoiLuc() {
        return lanThuCuoiLuc;
    }

    public void setLanThuCuoiLuc(LocalDateTime lanThuCuoiLuc) {
        this.lanThuCuoiLuc = lanThuCuoiLuc;
    }

    public LocalDateTime getGuiLuc() {
        return guiLuc;
    }

    public void setGuiLuc(LocalDateTime guiLuc) {
        this.guiLuc = guiLuc;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}