package vn.edu.group12.qldt.repository.hoidong;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.edu.group12.qldt.entity.hoidong.EmailThongBaoKetQua;
import vn.edu.group12.qldt.entity.hoidong.EmailThongBaoKetQuaId;

public interface EmailThongBaoKetQuaRepository
        extends JpaRepository<EmailThongBaoKetQua, EmailThongBaoKetQuaId> {

    // Khóa một email để hai lượt xử lý không cùng gửi cho một sinh viên.
    @Query(
        value = """
            SELECT *
            FROM email_thong_bao_ket_qua
            WHERE trang_thai IN ('CHO_GUI', 'LOI')
                AND so_lan_thu < 5
                AND (
                    lan_thu_cuoi_luc IS NULL
                    OR lan_thu_cuoi_luc <
                        DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 MINUTE)
                )
            ORDER BY created_at, ma_nhom, ma_sinh_vien
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    Optional<EmailThongBaoKetQua> khoaEmailCanGui();
}