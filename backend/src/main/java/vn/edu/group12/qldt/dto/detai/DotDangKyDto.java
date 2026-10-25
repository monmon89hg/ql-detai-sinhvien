package vn.edu.group12.qldt.dto.detai;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DotDangKyDto {

    private String maDot;
    private String tenDot;
    private String loaiDeTai; // MON_HOC, NCKH, TLCN, KLTN
    private LocalDateTime batDauGv;
    private LocalDateTime ketThucGv;
    private LocalDateTime batDauSv;
    private LocalDateTime ketThucSv;
    private LocalDateTime hanNopBaoCao;
    private LocalDateTime hanNopDiemPb;
    private LocalDate ngayBaoCaoHd;
    private String trangThai; // NHAP, CONG_BO, DONG

    public DotDangKyDto() {}

    public String getMaDot() { return maDot; }
    public void setMaDot(String maDot) { this.maDot = maDot; }

    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }

    public String getLoaiDeTai() { return loaiDeTai; }
    public void setLoaiDeTai(String loaiDeTai) { this.loaiDeTai = loaiDeTai; }

    public LocalDateTime getBatDauGv() { return batDauGv; }
    public void setBatDauGv(LocalDateTime batDauGv) { this.batDauGv = batDauGv; }

    public LocalDateTime getKetThucGv() { return ketThucGv; }
    public void setKetThucGv(LocalDateTime ketThucGv) { this.ketThucGv = ketThucGv; }

    public LocalDateTime getBatDauSv() { return batDauSv; }
    public void setBatDauSv(LocalDateTime batDauSv) { this.batDauSv = batDauSv; }

    public LocalDateTime getKetThucSv() { return ketThucSv; }
    public void setKetThucSv(LocalDateTime ketThucSv) { this.ketThucSv = ketThucSv; }

    public LocalDateTime getHanNopBaoCao() { return hanNopBaoCao; }
    public void setHanNopBaoCao(LocalDateTime hanNopBaoCao) { this.hanNopBaoCao = hanNopBaoCao; }

    public LocalDateTime getHanNopDiemPb() { return hanNopDiemPb; }
    public void setHanNopDiemPb(LocalDateTime hanNopDiemPb) { this.hanNopDiemPb = hanNopDiemPb; }

    public LocalDate getNgayBaoCaoHd() { return ngayBaoCaoHd; }
    public void setNgayBaoCaoHd(LocalDate ngayBaoCaoHd) { this.ngayBaoCaoHd = ngayBaoCaoHd; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
