package vn.edu.group12.qldt.service.hoidong;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.dto.hoidong.PhieuChamYeuCau;
import vn.edu.group12.qldt.entity.hoidong.HoiDongDeTai;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiem;
import vn.edu.group12.qldt.entity.hoidong.ThanhVienHoiDong;
import vn.edu.group12.qldt.enums.hoidong.ChucDanhHoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiPhieuCham;
import vn.edu.group12.qldt.repository.hoidong.HoiDongDeTaiRepository;
import vn.edu.group12.qldt.repository.hoidong.PhieuChamDiemRepository;
import vn.edu.group12.qldt.repository.hoidong.ThanhVienHoiDongRepository;

@Service
public class KiemTraDuPhieuChamService {

    private final PhieuChamDiemRepository phieuChamDiemRepository;
    private final HoiDongDeTaiRepository hoiDongDeTaiRepository;
    private final ThanhVienHoiDongRepository thanhVienHoiDongRepository;

    public KiemTraDuPhieuChamService(
            PhieuChamDiemRepository phieuChamDiemRepository,
            HoiDongDeTaiRepository hoiDongDeTaiRepository,
            ThanhVienHoiDongRepository thanhVienHoiDongRepository) {

        this.phieuChamDiemRepository = phieuChamDiemRepository;
        this.hoiDongDeTaiRepository = hoiDongDeTaiRepository;
        this.thanhVienHoiDongRepository = thanhVienHoiDongRepository;
    }

    // Trả về các phiếu đã nộp khi mọi người chấm bắt buộc đều hoàn thành.
    @Transactional(readOnly = true)
    public List<PhieuChamDiem> layPhieuDaNopKhiDayDu(String maNhom) {
        if (maNhom == null || maNhom.isBlank()) {
            throw new IllegalArgumentException("Mã nhóm không được để trống.");
        }

        String loaiDot = phieuChamDiemRepository
            .timLoaiDotTheoMaNhom(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Nhóm chưa có đăng ký đề tài."
            ));

        List<PhieuChamYeuCau> yeuCau = phieuChamDiemRepository
            .layPhieuChamYeuCau(maNhom);

        // Theo dữ liệu hiện tại: một người phản biện hoặc chấm độc lập.
        long soPhieuNgoaiHoiDong = yeuCau.stream()
            .filter(phieu -> !"HOI_DONG".equals(phieu.getLoaiPhieu()))
            .count();

        if (soPhieuNgoaiHoiDong != 1) {
            throw new IllegalArgumentException(
                "Đăng ký phải có đúng một giảng viên phản biện "
                    + "hoặc chấm độc lập được phân công."
            );
        }

        if ("KLTN".equals(loaiDot)) {
            kiemTraHoiDongVaThanhVien(maNhom, yeuCau);
        }

        // Khóa chính phiếu chấm không chứa loại phiếu. Một giảng viên
        // không thể đồng thời nộp hai loại phiếu cho cùng nhóm.
        Set<String> maGiangVienDaGap = new HashSet<>();

        for (PhieuChamYeuCau phieu : yeuCau) {
            if (phieuChamDiemRepository.demPhanCongHuongDan(maNhom, phieu.getMaGiangVien()) > 0)
                throw new IllegalArgumentException("Người chấm không được đồng thời hướng dẫn đề tài của nhóm.");
            if (!maGiangVienDaGap.add(phieu.getMaGiangVien())) {
                throw new IllegalArgumentException(
                    "Một giảng viên đang được yêu cầu nộp nhiều loại phiếu "
                        + "cho cùng nhóm. Cần kiểm tra lại phân công."
                );
            }
        }

        List<PhieuChamDiem> phieuThucTe = phieuChamDiemRepository
            .findById_MaNhomOrderById_MaGiangVienAsc(maNhom);

        // Đối chiếu theo cả mã GV và loại phiếu; phiếu NHAP không được tính.
        for (PhieuChamYeuCau phieuCanCo : yeuCau) {
            boolean daNopDungPhieu = phieuThucTe.stream().anyMatch(
                phieu -> phieuCanCo.getMaGiangVien().equals(
                        phieu.getId().getMaGiangVien()
                    )
                    && phieu.getLoaiPhieu() != null
                    && phieuCanCo.getLoaiPhieu().equals(
                        phieu.getLoaiPhieu().name()
                    )
                    && phieu.getTrangThai() == TrangThaiPhieuCham.DA_NOP
                    && phieu.getDiem() != null
                    && phieu.getNhanXet() != null
                    && !phieu.getNhanXet().isBlank()
                    && phieu.getNopLuc() != null
            );

            if (!daNopDungPhieu) {
                throw new IllegalArgumentException(
                    "Chưa có phiếu đã nộp hợp lệ của giảng viên "
                        + phieuCanCo.getMaGiangVien()
                        + " (loại " + phieuCanCo.getLoaiPhieu() + ")."
                );
            }
        }

        // Chặn cả phiếu ngoài danh sách phân công dự kiến.
        if (phieuThucTe.size() != yeuCau.size()) {
            throw new IllegalArgumentException(
                "Số phiếu của nhóm không khớp danh sách người chấm bắt buộc."
            );
        }

        return List.copyOf(phieuThucTe);
    }

    private void kiemTraHoiDongVaThanhVien(
            String maNhom,
            List<PhieuChamYeuCau> yeuCau) {

        HoiDongDeTai phanCong = hoiDongDeTaiRepository
            .findById(maNhom)
            .orElseThrow(() -> new IllegalArgumentException(
                "Khóa luận chưa được phân công hội đồng."
            ));

        List<ThanhVienHoiDong> thanhVien = thanhVienHoiDongRepository
            .findById_MaHoiDongOrderById_MaGiangVienAsc(
                phanCong.getMaHoiDong()
            );

        long soPhieuHoiDong = yeuCau.stream()
            .filter(phieu -> "HOI_DONG".equals(phieu.getLoaiPhieu()))
            .count();

        boolean coChuTich = thanhVien.stream().anyMatch(
            tv -> tv.getChucDanh() == ChucDanhHoiDong.CHU_TICH
        );

        boolean coThuKy = thanhVien.stream().anyMatch(
            tv -> tv.getChucDanh() == ChucDanhHoiDong.THU_KY
        );

        if (thanhVien.size() < 3
                || thanhVien.size() > 5
                || !coChuTich
                || !coThuKy
                || soPhieuHoiDong != thanhVien.size()) {

            throw new IllegalArgumentException(
                "Hội đồng phải có 3–5 thành viên, gồm chủ tịch, thư ký "
                    + "và một phiếu yêu cầu cho mỗi thành viên."
            );
        }
    }
}
