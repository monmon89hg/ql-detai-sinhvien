package vn.edu.group12.qldt.entity.admin;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "nguoi_dung_vai_tro")
public class NguoiDungVaiTro extends AuditedEntity {

    @EmbeddedId
    private NguoiDungVaiTroId id = new NguoiDungVaiTroId();

    @MapsId("maNguoiDung")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ma_nguoi_dung", referencedColumnName = "ma_nguoi_dung",
            nullable = false)
    private NguoiDung nguoiDung;

    @MapsId("maVaiTro")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ma_vai_tro", referencedColumnName = "ma_vai_tro",
            nullable = false)
    private VaiTro vaiTro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_cap", referencedColumnName = "ma_nguoi_dung")
    private NguoiDung nguoiCap;

    public NguoiDungVaiTro() {
    }

    public NguoiDungVaiTroId getId() {
        return id;
    }

    public void setId(NguoiDungVaiTroId id) {
        this.id = id;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(VaiTro vaiTro) {
        this.vaiTro = vaiTro;
    }

    public NguoiDung getNguoiCap() {
        return nguoiCap;
    }

    public void setNguoiCap(NguoiDung nguoiCap) {
        this.nguoiCap = nguoiCap;
    }
}