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
@Table(name = "thong_bao")
public class ThongBao extends AuditedEntity {

    @Id
    @Column(name = "ma_thong_bao", length = 30, nullable = false)
    private String maThongBao;

    @Column(name = "tieu_de", length = 200, nullable = false)
    private String tieuDe;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ma_nguoi_dang", referencedColumnName = "ma_nguoi_dung",
            nullable = false)
    private NguoiDung nguoiDang;

    @Column(name = "ma_dot", length = 30)
    private String maDot;

    @Column(name = "pham_vi", length = 15, nullable = false)
    private String phamVi = "TAT_CA";

    @Column(name = "trang_thai", length = 15, nullable = false)
    private String trangThai = "NHAP";

    @Column(name = "cong_bo_luc")
    private LocalDateTime congBoLuc;

    public ThongBao() {
    }

    public String getMaThongBao() {
        return maThongBao;
    }

    public void setMaThongBao(String maThongBao) {
        this.maThongBao = maThongBao;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public NguoiDung getNguoiDang() {
        return nguoiDang;
    }

    public void setNguoiDang(NguoiDung nguoiDang) {
        this.nguoiDang = nguoiDang;
    }

    public String getMaDot() {
        return maDot;
    }

    public void setMaDot(String maDot) {
        this.maDot = maDot;
    }

    public String getPhamVi() {
        return phamVi;
    }

    public void setPhamVi(String phamVi) {
        this.phamVi = phamVi;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getCongBoLuc() {
        return congBoLuc;
    }

    public void setCongBoLuc(LocalDateTime congBoLuc) {
        this.congBoLuc = congBoLuc;
    }
}