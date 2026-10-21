package vn.edu.group12.qldt.repository.detai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.group12.qldt.entity.detai.DeTai;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeTaiRepository extends JpaRepository<DeTai, Long> {

    Optional<DeTai> findByMaDeTai(String maDeTai);

    boolean existsByMaDeTai(String maDeTai);

    // Lấy đề tài của 1 đợt cụ thể
    List<DeTai> findByDotDangKyId(Long dotDangKyId);

    // Lấy đề tài của 1 đợt theo trạng thái (ví dụ CONG_BO cho SV xem ở Giai đoạn 2)
    List<DeTai> findByDotDangKyIdAndTrangThai(Long dotDangKyId, String trangThai);

    // Lấy danh sách đề tài do chính 1 Giảng viên đề xuất
    List<DeTai> findByNguoiDeXuatId(Long nguoiDeXuatId);

    // Tìm kiếm và lọc linh hoạt: theo đợt, bộ môn, trạng thái và từ khóa tìm kiếm
    @Query("SELECT d FROM DeTai d WHERE " +
           "(:dotId IS NULL OR d.dotDangKyId = :dotId) AND " +
           "(:boMonId IS NULL OR d.boMonId = :boMonId) AND " +
           "(:trangThai IS NULL OR d.trangThai = :trangThai) AND " +
           "(:keyword IS NULL OR LOWER(d.tenDeTai) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.maDeTai) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<DeTai> filterDeTai(@Param("dotId") Long dotId,
                            @Param("boMonId") Long boMonId,
                            @Param("trangThai") String trangThai,
                            @Param("keyword") String keyword);
}
