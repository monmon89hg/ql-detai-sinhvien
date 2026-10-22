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

@Controller
@RequestMapping("/ket-qua")
public class TraCuuKetQuaController {

    private final TraCuuKetQuaService traCuuKetQuaService;

    public TraCuuKetQuaController(
            TraCuuKetQuaService traCuuKetQuaService) {
        this.traCuuKetQuaService = traCuuKetQuaService;
    }

    // Sinh viên chỉ tra cứu các đợt có kết quả đã công bố của chính mình.
    @GetMapping("/cua-toi")
    @PreAuthorize("hasRole('SINH_VIEN')")
    public String cuaToi(
            @RequestParam(required = false) String maDot,
            Model model,
            Principal principal) {

        List<DotDangKyLuaChon> danhSachDot =
                traCuuKetQuaService.layDotCuaToi();

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
                        "Bạn không có quyền xem kết quả của đợt này.");
            }
        }

        model.addAttribute("danhSachDot", danhSachDot);
        model.addAttribute("maDotDuocChon", maDotDuocChon);
        model.addAttribute(
                "danhSachKetQua",
                traCuuKetQuaService.traCuuTheoDot(maDotDuocChon));
        model.addAttribute("currentUserName", principal.getName());

        return "assessment/ket-qua/cua-toi";
    }
}