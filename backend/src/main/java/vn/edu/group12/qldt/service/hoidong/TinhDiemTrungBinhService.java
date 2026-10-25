package vn.edu.group12.qldt.service.hoidong;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.group12.qldt.dto.hoidong.KetQuaTinhDiem;
import vn.edu.group12.qldt.entity.hoidong.PhieuChamDiem;

@Service
public class TinhDiemTrungBinhService {

    private final KiemTraDuPhieuChamService kiemTraDuPhieuChamService;

    public TinhDiemTrungBinhService(
            KiemTraDuPhieuChamService kiemTraDuPhieuChamService) {
        this.kiemTraDuPhieuChamService = kiemTraDuPhieuChamService;
    }

    @Transactional(readOnly = true)
    public KetQuaTinhDiem tinhDiem(String maNhom) {
        // Chỉ tính khi tất cả phiếu bắt buộc đã nộp hợp lệ.
        List<PhieuChamDiem> phieuDaNop =
            kiemTraDuPhieuChamService.layPhieuDaNopKhiDayDu(maNhom);

        return tinhTuPhieu(phieuDaNop);
    }

    public record DiemXemTruoc(KetQuaTinhDiem diem, String lyDoChuaDu) {}

    @Transactional(readOnly = true)
    public DiemXemTruoc xemTruoc(String maNhom) {
        var trangThai = kiemTraDuPhieuChamService.xemTrangThai(maNhom);
        if (trangThai.lyDoChuaDu() != null) {
            return new DiemXemTruoc(null, trangThai.lyDoChuaDu());
        }
        try {
            return new DiemXemTruoc(tinhTuPhieu(trangThai.phieuDaNop()), null);
        } catch (IllegalArgumentException ex) {
            return new DiemXemTruoc(null, ex.getMessage());
        }
    }

    private KetQuaTinhDiem tinhTuPhieu(List<PhieuChamDiem> phieuDaNop) {
        if (phieuDaNop.isEmpty()) {
            throw new IllegalArgumentException(
                "Không có phiếu chấm để tính điểm."
            );
        }

        BigDecimal tongDiem = BigDecimal.ZERO;

        for (PhieuChamDiem phieu : phieuDaNop) {
            BigDecimal diem = phieu.getDiem();

            // Kiểm tra thêm để không tính từ điểm sai dữ liệu.
            if (diem == null
                    || diem.compareTo(BigDecimal.ZERO) < 0
                    || diem.compareTo(BigDecimal.TEN) > 0) {
                throw new IllegalArgumentException(
                    "Phiếu chấm có điểm không hợp lệ."
                );
            }

            tongDiem = tongDiem.add(diem);
        }

        // Cột tong_diem trong database có đúng 2 chữ số thập phân.
        tongDiem = tongDiem.setScale(2, RoundingMode.UNNECESSARY);

        // Làm tròn điểm cuối đến 2 chữ số, khớp ROUND(..., 2) của MySQL.
        BigDecimal diemCuoi = tongDiem.divide(
            BigDecimal.valueOf(phieuDaNop.size()),
            2,
            RoundingMode.HALF_UP
        );

        return new KetQuaTinhDiem(
            phieuDaNop.size(),
            tongDiem,
            diemCuoi
        );
    }
}
