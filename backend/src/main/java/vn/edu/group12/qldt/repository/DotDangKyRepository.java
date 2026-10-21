package vn.edu.group12.qldt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.group12.qldt.model.DotDangKy;

import java.util.List;
import java.util.Optional;

@Repository
public interface DotDangKyRepository extends JpaRepository<DotDangKy, Long> {

    Optional<DotDangKy> findByMaDot(String maDot);

    boolean existsByMaDot(String maDot);

    // Lấy danh sách đợt sắp xếp theo thời gian tạo mới nhất
    List<DotDangKy> findAllByOrderByCreatedAtDesc();

    // Tìm các đợt đang mở (đã công bố)
    List<DotDangKy> findByTrangThai(String trangThai);
}