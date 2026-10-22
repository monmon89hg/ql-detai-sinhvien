package vn.edu.group12.qldt.entity.detai;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DeTaiGiangVienId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ma_de_tai", length = 30, nullable = false)
    private String maDeTai;

    @Column(name = "ma_giang_vien", length = 30, nullable = false)
    private String maGiangVien;

    public DeTaiGiangVienId() {}

    public DeTaiGiangVienId(String maDeTai, String maGiangVien) {
        this.maDeTai = maDeTai;
        this.maGiangVien = maGiangVien;
    }

    public String getMaDeTai() { return maDeTai; }
    public void setMaDeTai(String maDeTai) { this.maDeTai = maDeTai; }

    public String getMaGiangVien() { return maGiangVien; }
    public void setMaGiangVien(String maGiangVien) { this.maGiangVien = maGiangVien; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DeTaiGiangVienId other)) return false;
        return Objects.equals(maDeTai, other.maDeTai)
            && Objects.equals(maGiangVien, other.maGiangVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDeTai, maGiangVien);
    }
}
