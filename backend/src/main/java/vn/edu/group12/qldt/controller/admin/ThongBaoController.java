package vn.edu.group12.qldt.controller.admin;

import jakarta.validation.Valid;
import vn.edu.group12.qldt.dto.admin.ThongBaoDanhSachDto;
import vn.edu.group12.qldt.dto.admin.ThongBaoForm;
import vn.edu.group12.qldt.entity.admin.NguoiDung;
import vn.edu.group12.qldt.service.admin.ThongBaoService;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/thong-bao")
public class ThongBaoController {

    private final ThongBaoService thongBaoService;
    private final NguoiDungRepository nguoiDungRepository;

    public ThongBaoController(
            ThongBaoService thongBaoService,
            NguoiDungRepository nguoiDungRepository
    ) {
        this.thongBaoService = thongBaoService;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    // Danh sách thông báo đã công bố cho người dùng hiện tại
    @GetMapping
    public String danhSachNguoiDung(
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication,
            Model model
    ) {
        List<String> maVaiTros = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_")
                        ? authority.substring(5)
                        : authority)
                .toList();

        Page<ThongBaoDanhSachDto> thongBaos =
                thongBaoService.layThongBaoChoNguoiDung(
                        maVaiTros,
                        PageRequest.of(
                                Math.max(page, 0),
                                20,
                                Sort.by(Sort.Direction.DESC, "congBoLuc")
                        )
                );

        model.addAttribute("thongBaos", thongBaos);
        return "thong-bao/list";
    }

    // Danh sách quản trị, gồm cả bản nháp và thông báo đã ẩn
    @GetMapping("/quan-ly")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String danhSachQuanTri(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<ThongBaoDanhSachDto> thongBaos =
                thongBaoService.layDanhSachQuanTri(
                        PageRequest.of(
                                Math.max(page, 0),
                                20,
                                Sort.by(Sort.Direction.DESC, "congBoLuc")
                        )
                );

        model.addAttribute("thongBaos", thongBaos);
        return "admin/thong-bao/list";
    }

    @GetMapping("/quan-ly/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String formTao(Model model) {
        model.addAttribute("form", new ThongBaoForm());
        model.addAttribute("isEdit", false);
        return "admin/thong-bao/form";
    }

    @PostMapping("/quan-ly")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String tao(
            @Valid @ModelAttribute("form") ThongBaoForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/thong-bao/form";
        }

        try {
            NguoiDung nguoiDang = nguoiDungRepository
                    .findByEmailIgnoreCase(authentication.getName())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Không tìm thấy tài khoản đang đăng nhập"
                            ));

            thongBaoService.taoBanNhap(form, nguoiDang);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã tạo bản nháp thông báo."
            );
            return "redirect:/thong-bao/quan-ly";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("form", exception.getMessage());
            model.addAttribute("isEdit", false);
            return "admin/thong-bao/form";
        }
    }

    @GetMapping("/quan-ly/{maThongBao}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String formCapNhat(
            @PathVariable String maThongBao,
            Model model
    ) {
        model.addAttribute(
                "form",
                thongBaoService.layFormCapNhat(maThongBao)
        );
        model.addAttribute("maThongBao", maThongBao);
        model.addAttribute("isEdit", true);
        return "admin/thong-bao/form";
    }

    @PostMapping("/quan-ly/{maThongBao}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String capNhat(
            @PathVariable String maThongBao,
            @Valid @ModelAttribute("form") ThongBaoForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("maThongBao", maThongBao);
            model.addAttribute("isEdit", true);
            return "admin/thong-bao/form";
        }

        try {
            thongBaoService.capNhatBanNhap(maThongBao, form);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã cập nhật bản nháp."
            );
            return "redirect:/thong-bao/quan-ly";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("form", exception.getMessage());
            model.addAttribute("maThongBao", maThongBao);
            model.addAttribute("isEdit", true);
            return "admin/thong-bao/form";
        }
    }

    @PostMapping("/quan-ly/{maThongBao}/cong-bo")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String congBo(
            @PathVariable String maThongBao,
            RedirectAttributes redirectAttributes
    ) {
        try {
            thongBaoService.congBo(maThongBao);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã công bố thông báo."
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/thong-bao/quan-ly";
    }

    @PostMapping("/quan-ly/{maThongBao}/an")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
    public String an(
            @PathVariable String maThongBao,
            RedirectAttributes redirectAttributes
    ) {
        try {
            thongBaoService.anThongBao(maThongBao);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã ẩn thông báo."
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/thong-bao/quan-ly";
    }
}