package vn.edu.group12.qldt.repository.detai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.group12.qldt.entity.detai.DeTai;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeTaiRepository extends JpaRepository<DeTai, String> {

    Optional<DeTai> findByMaDeTai(String maDeTai);

    boolean existsByMaDeTai(String maDeTai);

    // Lấy đề tài của 1 đợt cụ thể
    List<DeTai> findByMaDot(String maDot);

    // Lấy đề tài của 1 đợt theo trạng thái (ví dụ CONG_BO cho SV xem ở Giai đoạn 2)
    List<DeTai> findByMaDotAndTrangThai(String maDot, String trangThai);

    // Lấy danh sách đề tài do chính 1 Giảng viên đề xuất
    List<DeTai> findByMaNguoiDeXuat(String maNguoiDeXuat);

    // Đếm số lượng đăng ký của sinh viên đối với đề tài này
    @Query(value = "SELECT COUNT(*) FROM dang_ky_de_tai WHERE ma_de_tai = :maDeTai", nativeQuery = true)
    long countDangKyByMaDeTai(@Param("maDeTai") String maDeTai);

    // Tìm kiếm và lọc linh hoạt: theo đợt, bộ môn, trạng thái và từ khóa tìm kiếm
    @Query("SELECT d FROM DeTai d WHERE " +
           "(:maDot IS NULL OR :maDot = '' OR d.maDot = :maDot) AND " +
           "(:maBoMon IS NULL OR :maBoMon = '' OR d.maBoMon = :maBoMon) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR d.trangThai = :trangThai) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(d.tenDeTai) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.maDeTai) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<DeTai> filterDeTai(@Param("maDot") String maDot,
                            @Param("maBoMon") String maBoMon,
                            @Param("trangThai") String trangThai,
                            @Param("keyword") String keyword);
}
