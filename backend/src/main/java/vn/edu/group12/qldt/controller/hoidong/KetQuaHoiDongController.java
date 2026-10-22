package vn.edu.group12.qldt.controller.hoidong;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.edu.group12.qldt.service.hoidong.TongHopKetQuaService;
import java.security.Principal;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/ket-qua")
public class KetQuaHoiDongController {

    private final TongHopKetQuaService tongHopKetQuaService;

    public KetQuaHoiDongController(
            TongHopKetQuaService tongHopKetQuaService) {
        this.tongHopKetQuaService = tongHopKetQuaService;
    }

    // Trang tổng hợp dành cho chủ tịch hội đồng.
    @GetMapping("/tong-hop")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String danhSachTongHop(Model model, Principal principal) {
        model.addAttribute(
            "danhSachNhom",
            tongHopKetQuaService.layDanhSachNhomChoChuTich()
        );
        model.addAttribute("currentUserName", principal.getName());

        return "assessment/ket-qua/tong-hop-list";
    }

    // Trang quản lý, công bố kết quả dành cho trưởng khoa.
    @GetMapping("/quan-ly")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String danhSachQuanLy(Model model, Principal principal) {
        model.addAttribute(
            "danhSachNhom",
            tongHopKetQuaService.layDanhSachNhomChoTruongKhoa()
        );
        model.addAttribute("currentUserName", principal.getName());

        return "assessment/ket-qua/quan-ly-list";
    }

    // Chủ tịch hội đồng gửi yêu cầu tổng hợp kết quả của một nhóm.
    @PostMapping("/{maNhom}/tong-hop")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String tongHop(
            @PathVariable String maNhom,
            @RequestParam(required = false) String nhanXetTongHop,
            RedirectAttributes redirectAttributes) {

        try {
            tongHopKetQuaService.tongHop(maNhom, nhanXetTongHop);
            redirectAttributes.addFlashAttribute(
                "successMessage", "Tổng hợp kết quả thành công."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                "errorMessage", ex.getMessage()
            );
        } catch (DataIntegrityViolationException
                | OptimisticLockingFailureException ex) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Dữ liệu đã thay đổi hoặc không hợp lệ. Vui lòng thử lại."
            );
        }

        return "redirect:/ket-qua/tong-hop";
    }

    // Trưởng khoa công bố kết quả đã được tổng hợp.
    @PostMapping("/{maNhom}/cong-bo")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String congBo(
            @PathVariable String maNhom,
            RedirectAttributes redirectAttributes) {

        try {
            tongHopKetQuaService.congBo(maNhom);
            redirectAttributes.addFlashAttribute(
                "successMessage", "Công bố kết quả thành công."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                "errorMessage", ex.getMessage()
            );
        } catch (DataIntegrityViolationException
                | OptimisticLockingFailureException ex) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Dữ liệu đã thay đổi hoặc không hợp lệ. Vui lòng thử lại."
            );
        }

        return "redirect:/ket-qua/quan-ly";
    }
}