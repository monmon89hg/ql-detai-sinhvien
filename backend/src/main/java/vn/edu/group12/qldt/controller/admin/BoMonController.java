package vn.edu.group12.qldt.controller.admin;

import jakarta.validation.Valid;
import vn.edu.group12.qldt.dto.admin.BoMonCapNhatForm;
import vn.edu.group12.qldt.dto.admin.BoMonForm;
import vn.edu.group12.qldt.service.admin.BoMonService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/bo-mon")
@PreAuthorize("hasRole('ADMIN')")
public class BoMonController {

    private final BoMonService boMonService;

    public BoMonController(BoMonService boMonService) {
        this.boMonService = boMonService;
    }

    @GetMapping
    public String danhSach(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<?> boMons = boMonService.layDanhSach(
                PageRequest.of(
                        Math.max(page, 0),
                        20,
                        Sort.by(Sort.Direction.ASC, "maBoMon")
                )
        );

        model.addAttribute("boMons", boMons);
        return "admin/bo-mon/list";
    }

    @GetMapping("/new")
    public String formTao(Model model) {
        model.addAttribute("form", new BoMonForm());
        model.addAttribute("isEdit", false);
        return "admin/bo-mon/form";
    }

    @PostMapping
    public String tao(
            @Valid @ModelAttribute("form") BoMonForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/bo-mon/form";
        }

        try {
            boMonService.taoBoMon(form);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã tạo bộ môn."
            );
            return "redirect:/admin/bo-mon";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("form", exception.getMessage());
            model.addAttribute("isEdit", false);
            return "admin/bo-mon/form";
        }
    }

    @GetMapping("/{maBoMon}/edit")
    public String formCapNhat(
            @PathVariable String maBoMon,
            Model model
    ) {
        model.addAttribute(
                "form",
                boMonService.layFormCapNhat(maBoMon)
        );
        model.addAttribute("maBoMon", maBoMon);
        model.addAttribute("isEdit", true);
        return "admin/bo-mon/form";
    }

    @PostMapping("/{maBoMon}")
    public String capNhat(
            @PathVariable String maBoMon,
            @Valid @ModelAttribute("form") BoMonCapNhatForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("maBoMon", maBoMon);
            model.addAttribute("isEdit", true);
            return "admin/bo-mon/form";
        }

        try {
            boMonService.capNhatBoMon(maBoMon, form);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã cập nhật bộ môn."
            );
            return "redirect:/admin/bo-mon";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("form", exception.getMessage());
            model.addAttribute("maBoMon", maBoMon);
            model.addAttribute("isEdit", true);
            return "admin/bo-mon/form";
        }
    }

    @PostMapping("/{maBoMon}/trang-thai")
    public String capNhatTrangThai(
            @PathVariable String maBoMon,
            @RequestParam boolean hoatDong,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boMonService.capNhatTrangThai(maBoMon, hoatDong);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã cập nhật trạng thái bộ môn."
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/bo-mon";
    }
}