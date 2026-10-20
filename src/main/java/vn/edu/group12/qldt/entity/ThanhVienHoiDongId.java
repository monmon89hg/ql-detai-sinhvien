package vn.edu.group12.qldt.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ThanhVienHoiDongId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ma_hoi_dong", nullable = false, length = 30)
    private String maHoiDong;

    @Column(name = "ma_giang_vien", nullable = false, length = 30)
    private String maGiangVien;

    public ThanhVienHoiDongId() {
    }

    public ThanhVienHoiDongId(
            String maHoiDong,
            String maGiangVien) {
        this.maHoiDong = maHoiDong;
        this.maGiangVien = maGiangVien;
    }

    public String getMaHoiDong() {
        return maHoiDong;
    }

    public void setMaHoiDong(String maHoiDong) {
        this.maHoiDong = maHoiDong;
    }

    public String getMaGiangVien() {
        return maGiangVien;
    }

    public void setMaGiangVien(String maGiangVien) {
        this.maGiangVien = maGiangVien;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ThanhVienHoiDongId other)) {
            return false;
        }

        return Objects.equals(maHoiDong, other.maHoiDong)
            && Objects.equals(maGiangVien, other.maGiangVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHoiDong, maGiangVien);
    }
}