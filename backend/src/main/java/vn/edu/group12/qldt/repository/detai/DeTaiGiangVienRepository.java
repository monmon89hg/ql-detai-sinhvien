package vn.edu.group12.qldt.repository.detai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.group12.qldt.entity.detai.DeTaiGiangVien;
import vn.edu.group12.qldt.entity.detai.DeTaiGiangVienId;

import java.util.List;

@Repository
public interface DeTaiGiangVienRepository extends JpaRepository<DeTaiGiangVien, DeTaiGiangVienId> {

    // Lấy tất cả phân công của 1 đề tài
    List<DeTaiGiangVien> findById_MaDeTai(String maDeTai);

    // Lấy phân công theo loại: HUONG_DAN, PHAN_BIEN, CHAM_DOC_LAP
    List<DeTaiGiangVien> findById_MaDeTaiAndLoaiPhanCong(String maDeTai, String loaiPhanCong);

    // Đếm số lượng GVHD của 1 đề tài (quy tắc: tối đa 2 GVHD)
    long countById_MaDeTaiAndLoaiPhanCong(String maDeTai, String loaiPhanCong);

    // Kiểm tra xem giảng viên đã được phân công vào đề tài này chưa
    boolean existsById_MaDeTaiAndId_MaGiangVien(String maDeTai, String maGiangVien);

    // Kiểm tra xem giảng viên đã có vai trò cụ thể này trong đề tài chưa
    boolean existsById_MaDeTaiAndId_MaGiangVienAndLoaiPhanCong(String maDeTai, String maGiangVien, String loaiPhanCong);

    // Xóa phân công của một giảng viên trong một đề tài
    void deleteById_MaDeTaiAndId_MaGiangVien(String maDeTai, String maGiangVien);

    // Đếm tổng số phân công của một đề tài
    long countById_MaDeTai(String maDeTai);

    // Xóa tất cả phân công của một đề tài
    void deleteById_MaDeTai(String maDeTai);
}
