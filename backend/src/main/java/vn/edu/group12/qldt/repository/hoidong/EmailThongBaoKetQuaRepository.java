package vn.edu.group12.qldt.repository.hoidong;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.edu.group12.qldt.entity.hoidong.EmailThongBaoKetQua;
import vn.edu.group12.qldt.entity.hoidong.EmailThongBaoKetQuaId;
import java.time.LocalDateTime;
import org.springframework.data.repository.query.Param;

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
                        :nguongThuLai
                )
            ORDER BY created_at, ma_nhom, ma_sinh_vien
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """,
        nativeQuery = true
    )
    Optional<EmailThongBaoKetQua> khoaEmailCanGui(
        @Param("nguongThuLai") LocalDateTime nguongThuLai
    );
}