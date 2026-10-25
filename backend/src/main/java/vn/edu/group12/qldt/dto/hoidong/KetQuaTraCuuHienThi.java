package vn.edu.group12.qldt.dto.hoidong;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Các cột hiển thị trên trang tra cứu kết quả đã công bố.
public interface KetQuaTraCuuHienThi {

    String getMaDot();

    String getTenDot();

    String getMaNhom();

    String getTenNhom();

    String getMaDeTai();

    String getTenDeTai();

    BigDecimal getDiemCuoi();

    String getNhanXetTongHop();

    LocalDateTime getCongBoLuc();
}