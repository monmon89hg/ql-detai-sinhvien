package vn.edu.group12.qldt.dto.hoidong;

import java.math.BigDecimal;

/**
 * Ba giá trị được tính từ cùng một danh sách phiếu chấm đã nộp.
 * Chúng tương ứng với các cột so_phieu, tong_diem, diem_cuoi.
 */
public record KetQuaTinhDiem(
        int soPhieu,
        BigDecimal tongDiem,
        BigDecimal diemCuoi
) {
}