package vn.edu.group12.qldt.controller.hoidong;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.edu.group12.qldt.dto.hoidong.DotDangKyLuaChon;
import vn.edu.group12.qldt.service.hoidong.TraCuuKetQuaService;
import vn.edu.group12.qldt.service.hoidong.AdminTraCuuKetQuaService;
import jakarta.servlet.http.HttpServletRequest;
import vn.edu.group12.qldt.service.hoidong.NguoiDungHienThiService;

@Controller
@RequestMapping("/ket-qua")
public class TraCuuKetQuaController {

    private final TraCuuKetQuaService traCuuKetQuaService;
    private final AdminTraCuuKetQuaService adminTraCuuKetQuaService;
    private final NguoiDungHienThiService nguoiDungHienThiService;

    public TraCuuKetQuaController(
            TraCuuKetQuaService traCuuKetQuaService,
            AdminTraCuuKetQuaService adminTraCuuKetQuaService,
            NguoiDungHienThiService nguoiDungHienThiService) {
        this.traCuuKetQuaService = traCuuKetQuaService;
        this.adminTraCuuKetQuaService = adminTraCuuKetQuaService;
        this.nguoiDungHienThiService = nguoiDungHienThiService;
    }

    // Sinh viên xem nhóm mình; Trưởng khoa và ADMIN xem kết quả đã công bố.
    @GetMapping("/cua-toi")
    @PreAuthorize("hasAnyRole('SINH_VIEN', 'TRUONG_KHOA', 'ADMIN')")
    public String cuaToi(
            @RequestParam(required = false) String maDot,
            Model model,
            Principal principal,
            HttpServletRequest request) {

        boolean xemTatCa = request.isUserInRole("ADMIN")
            || request.isUserInRole("TRUONG_KHOA");

        List<DotDangKyLuaChon> danhSachDot = xemTatCa
            ? adminTraCuuKetQuaService.layDotCoKetQuaCongBo()
            : traCuuKetQuaService.layDotCuaToi();

        String maDotDuocChon;
        if (maDot == null || maDot.isBlank()) {
            maDotDuocChon = danhSachDot.isEmpty()
                ? null
                : danhSachDot.get(0).getMaDot();
        } else {
            maDotDuocChon = maDot.strip();

            boolean coQuyenXemDot = danhSachDot.stream()
                .anyMatch(dot -> dot.getMaDot().equals(maDotDuocChon));

            if (!coQuyenXemDot) {
                throw new AccessDeniedException(
                    "Bạn không có quyền xem kết quả của đợt này."
                );
            }
        }

        model.addAttribute("xemTatCa", xemTatCa);
        model.addAttribute("danhSachDot", danhSachDot);
        model.addAttribute("maDotDuocChon", maDotDuocChon);

        model.addAttribute(
            "danhSachKetQua",
            xemTatCa
                ? adminTraCuuKetQuaService.traCuuTheoDot(maDotDuocChon)
                : traCuuKetQuaService.traCuuTheoDot(maDotDuocChon)
        );

        model.addAttribute("currentUserName", nguoiDungHienThiService.layHoTenHienTai());

        return "assessment/ket-qua/cua-toi";
    }
}
