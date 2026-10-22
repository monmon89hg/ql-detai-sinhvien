package vn.edu.group12.qldt.service.detai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.group12.qldt.entity.detai.DeTaiGiangVien;
import vn.edu.group12.qldt.entity.detai.DeTaiGiangVienId;
import vn.edu.group12.qldt.repository.detai.DeTaiGiangVienRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeTaiGiangVienService {

    @Autowired
    private DeTaiGiangVienRepository deTaiGiangVienRepository;

    public List<DeTaiGiangVien> getPhanCongByDeTai(String maDeTai) {
        return deTaiGiangVienRepository.findById_MaDeTai(maDeTai);
    }

    public long countGvhd(String maDeTai) {
        return deTaiGiangVienRepository.countById_MaDeTaiAndLoaiPhanCong(maDeTai, "HUONG_DAN");
    }

    @Transactional
    public DeTaiGiangVien phanCong(String maDeTai, String maGiangVien, String loaiPhanCong, String maNguoiPhanCong) {
        // 1. Kiểm tra nếu đã được phân công chính vai trò này rồi
        if (deTaiGiangVienRepository.existsById_MaDeTaiAndId_MaGiangVienAndLoaiPhanCong(maDeTai, maGiangVien, loaiPhanCong)) {
            throw new IllegalArgumentException("Giảng viên đã được phân công vai trò " + loaiPhanCong + " cho đề tài này rồi!");
        }

        // 2. Ràng buộc: Mỗi đề tài tối đa 2 GVHD
        if ("HUONG_DAN".equals(loaiPhanCong)) {
            long currentGvhd = countGvhd(maDeTai);
            if (currentGvhd >= 2) {
                throw new IllegalArgumentException("Mỗi đề tài chỉ được phân công tối đa 2 Giảng viên hướng dẫn!");
            }
        }

        // 3. RÀNG BUỘC NGHIỆP VỤ BẢO VỆ TÍNH KHÁCH QUAN:
        // Giảng viên hướng dẫn KHÔNG được làm Phản biện hoặc Chấm độc lập đề tài của chính mình!
        if ("PHAN_BIEN".equals(loaiPhanCong) || "CHAM_DOC_LAP".equals(loaiPhanCong)) {
            boolean isGvhd = deTaiGiangVienRepository.existsById_MaDeTaiAndId_MaGiangVienAndLoaiPhanCong(maDeTai, maGiangVien, "HUONG_DAN");
            if (isGvhd) {
                throw new IllegalArgumentException("Vi phạm quy chế: Giảng viên hướng dẫn không được phân công chấm hoặc phản biện đề tài này!");
            }
        }

        DeTaiGiangVienId id = new DeTaiGiangVienId(maDeTai, maGiangVien);
        DeTaiGiangVien phanCong = new DeTaiGiangVien(id, loaiPhanCong, maNguoiPhanCong);

        return deTaiGiangVienRepository.save(phanCong);
    }

    @Transactional
    public void xoaPhanCong(String maDeTai, String maGiangVien) {
        deTaiGiangVienRepository.deleteById_MaDeTaiAndId_MaGiangVien(maDeTai, maGiangVien);
    }
}
