package vn.edu.group12.qldt.dto.hoidong;

// Chỉ lấy các thông tin cần hiển thị trên danh sách nhóm được chấm.
public interface NhomChamDiemHienThi {

    // Thông tin nhóm sinh viên.
    String getMaNhom();

    String getTenNhom();

    // Đề tài mà nhóm đăng ký.
    String getMaDeTai();

    String getTenDeTai();

    // Hội đồng được phân công chấm nhóm.
    String getMaHoiDong();

    String getTenHoiDong();
    String getLoaiPhieu();
    String getLoaiDeTai();
}
