package vn.edu.group12.qldt.dto.hoidong;

import java.math.BigDecimal;

// Các cột cần hiển thị khi chọn nhóm để tổng hợp hoặc công bố.
public interface NhomKetQuaHienThi {

    String getMaNhom();
    String getTenNhom();

    String getMaDeTai();
    String getTenDeTai();

    String getMaHoiDong();
    String getTenHoiDong();

    // Chưa tổng hợp thì hai giá trị này có thể là null.
    String getTrangThaiKetQua();
    BigDecimal getDiemCuoi();
}