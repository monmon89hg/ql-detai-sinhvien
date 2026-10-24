package vn.edu.group12.qldt.enums.hoidong;

public enum TrangThaiHoiDong {
    NHAP("Nháp"),
    SAN_SANG("Sẵn sàng"),
    DANG_CHAM("Đang chấm"),
    DA_KHOA("Đã khóa");

    private final String tenHienThi;

    TrangThaiHoiDong(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
}
