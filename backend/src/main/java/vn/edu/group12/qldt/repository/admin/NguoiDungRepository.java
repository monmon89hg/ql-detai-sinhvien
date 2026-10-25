package vn.edu.group12.qldt.repository.admin;

import vn.edu.group12.qldt.entity.admin.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {

    Optional<NguoiDung> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select distinct nd
            from NguoiDung nd, NguoiDungVaiTro ndvt
            where ndvt.nguoiDung = nd
              and nd.trangThai = 'HOAT_DONG'
              and ndvt.vaiTro.maVaiTro in ('SINH_VIEN', 'GIANG_VIEN', 'TRUONG_KHOA')
            order by nd.hoTen asc
            """)
    List<NguoiDung> findActiveNotificationRecipients();

    @Query("""
            select distinct nd
            from NguoiDung nd, NguoiDungVaiTro ndvt
            where ndvt.nguoiDung = nd
              and nd.trangThai = 'HOAT_DONG'
              and nd.maNguoiDung in :maNguoiDungs
              and ndvt.vaiTro.maVaiTro in ('SINH_VIEN', 'GIANG_VIEN', 'TRUONG_KHOA')
            """)
    List<NguoiDung> findActiveNotificationRecipientsByIds(
            @Param("maNguoiDungs") Collection<String> maNguoiDungs
    );
}
