package vn.edu.group12.qldt.entity.hoidong;

import java.io.Serializable;
import java.util.Objects;

// Một sinh viên chỉ có một email kết quả cho mỗi nhóm.
public class EmailThongBaoKetQuaId implements Serializable {

    private String maNhom;
    private String maSinhVien;

    public EmailThongBaoKetQuaId() {
    }

    public EmailThongBaoKetQuaId(String maNhom, String maSinhVien) {
        this.maNhom = maNhom;
        this.maSinhVien = maSinhVien;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EmailThongBaoKetQuaId that)) {
            return false;
        }
        return Objects.equals(maNhom, that.maNhom)
            && Objects.equals(maSinhVien, that.maSinhVien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNhom, maSinhVien);
    }
}