package vn.edu.group12.qldt.repository.admin;

import vn.edu.group12.qldt.entity.admin.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ThongBaoRepository
        extends JpaRepository<ThongBao, String> {

    Page<ThongBao> findAllByOrderByCongBoLucDesc(Pageable pageable);

    @Query("""
            select distinct tb
            from ThongBao tb
            where tb.trangThai = 'CONG_BO'
              and tb.congBoLuc <= :thoiDiem
              and (
                    tb.phamVi = 'TAT_CA'
                    or (
                        tb.phamVi = 'CA_NHAN'
                        and :maNguoiDung member of tb.maNguoiNhanIds
                    )
              )
            """)
    Page<ThongBao> findInboxForUser(
            @Param("maNguoiDung") String maNguoiDung,
            @Param("thoiDiem") LocalDateTime thoiDiem,
            Pageable pageable
    );
}
