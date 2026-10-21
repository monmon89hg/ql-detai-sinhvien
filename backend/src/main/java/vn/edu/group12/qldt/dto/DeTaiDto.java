package vn.edu.group12.qldt.dto;

public class DeTaiDto {

    private String maDeTai;
    private Long dotDangKyId;
    private Long boMonId;
    private String tenDeTai;
    private String noiDung;
    private String yeuCau;

    public DeTaiDto() {}

    public String getMaDeTai() { return maDeTai; }
    public void setMaDeTai(String maDeTai) { this.maDeTai = maDeTai; }

    public Long getDotDangKyId() { return dotDangKyId; }
    public void setDotDangKyId(Long dotDangKyId) { this.dotDangKyId = dotDangKyId; }

    public Long getBoMonId() { return boMonId; }
    public void setBoMonId(Long boMonId) { this.boMonId = boMonId; }

    public String getTenDeTai() { return tenDeTai; }
    public void setTenDeTai(String tenDeTai) { this.tenDeTai = tenDeTai; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getYeuCau() { return yeuCau; }
    public void setYeuCau(String yeuCau) { this.yeuCau = yeuCau; }
}