package vn.edu.group12.qldt.repository.admin;

import vn.edu.group12.qldt.entity.admin.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;

public interface ThongBaoRepository
        extends JpaRepository<ThongBao, String> {

    Page<ThongBao> findAllByOrderByCongBoLucDesc(Pageable pageable);

    Page<ThongBao>
    findAllByTrangThaiAndPhamViInAndCongBoLucLessThanEqualOrderByCongBoLucDesc(
            String trangThai,
            Collection<String> phamVi,
            LocalDateTime thoiDiem,
            Pageable pageable
    );
}