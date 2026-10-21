package vn.edu.group12.qldt.entity.hoidong;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Khóa ghép xác định phiếu chấm của một giảng viên cho một nhóm.
@Embeddable
public class PhieuChamDiemId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ma_nhom", nullable = false, length = 30)
    private String maNhom;

    @Column(name = "ma_giang_vien", nullable = false, length = 30)
    private String maGiangVien;

    // JPA cần constructor không tham số.
    public PhieuChamDiemId() {
    }

    public PhieuChamDiemId(String maNhom, String maGiangVien) {
        this.maNhom = maNhom;
        this.maGiangVien = maGiangVien;
    }

    public String getMaNhom() {
        return maNhom;
    }

    public void setMaNhom(String maNhom) {
        this.maNhom = maNhom;
    }

    public String getMaGiangVien() {
        return maGiangVien;
    }

    public void setMaGiangVien(String maGiangVien) {
        this.maGiangVien = maGiangVien;
    }

    // Hai khóa bằng nhau khi cả mã nhóm và mã giảng viên đều bằng nhau.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PhieuChamDiemId other)) {
            return false;
        }

        return Objects.equals(maNhom, other.maNhom)
            && Objects.equals(maGiangVien, other.maGiangVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNhom, maGiangVien);
    }
}