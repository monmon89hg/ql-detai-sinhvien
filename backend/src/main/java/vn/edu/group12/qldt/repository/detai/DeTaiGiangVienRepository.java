package vn.edu.group12.qldt.repository.detai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.group12.qldt.entity.detai.DeTaiGiangVien;

import java.util.List;

@Repository
public interface DeTaiGiangVienRepository extends JpaRepository<DeTaiGiangVien, Long> {

    // Lấy tất cả phân công của 1 đề tài
    List<DeTaiGiangVien> findByDeTaiId(Long deTaiId);

    // Lấy phân công theo loại: HUONG_DAN, PHAN_BIEN, CHAM_DOC_LAP
    List<DeTaiGiangVien> findByDeTaiIdAndLoaiPhanCong(Long deTaiId, String loaiPhanCong);

    // Đếm số lượng GVHD của 1 đề tài (quy tắc: tối đa 2 GVHD)
    long countByDeTaiIdAndLoaiPhanCong(Long deTaiId, String loaiPhanCong);

    // Kiểm tra xem giảng viên đã được phân công vào đề tài này chưa
    boolean existsByDeTaiIdAndGiangVienId(Long deTaiId, Long giangVienId);

    // Kiểm tra xem giảng viên đã có vai trò cụ thể này trong đề tài chưa
    boolean existsByDeTaiIdAndGiangVienIdAndLoaiPhanCong(Long deTaiId, Long giangVienId, String loaiPhanCong);

    // Xóa một phân công cụ thể
    void deleteByDeTaiIdAndGiangVienIdAndLoaiPhanCong(Long deTaiId, Long giangVienId, String loaiPhanCong);
}
