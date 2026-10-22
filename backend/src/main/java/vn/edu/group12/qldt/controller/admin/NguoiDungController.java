package vn.edu.group12.qldt.controller.admin;

import vn.edu.group12.qldt.dto.admin.NguoiDungCapNhatForm;
import vn.edu.group12.qldt.dto.admin.NguoiDungForm;
import vn.edu.group12.qldt.entity.admin.NguoiDung;
import vn.edu.group12.qldt.repository.admin.BoMonRepository;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;
import vn.edu.group12.qldt.repository.admin.VaiTroRepository;
import vn.edu.group12.qldt.service.admin.NguoiDungService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class NguoiDungController {

    private final NguoiDungService nguoiDungService;
    private final NguoiDungRepository nguoiDungRepository;
    private final BoMonRepository boMonRepository;
    private final VaiTroRepository vaiTroRepository;

    public NguoiDungController(
            NguoiDungService nguoiDungService,
            NguoiDungRepository nguoiDungRepository,
            BoMonRepository boMonRepository,
            VaiTroRepository vaiTroRepository
    ) {
        this.nguoiDungService = nguoiDungService;
        this.nguoiDungRepository = nguoiDungRepository;
        this.boMonRepository = boMonRepository;
        this.vaiTroRepository = vaiTroRepository;
    }

    @GetMapping
    public String danhSach(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<?> users = nguoiDungService.layDanhSach(
                PageRequest.of(
                        Math.max(page, 0),
                        20,
                        Sort.by(Sort.Direction.ASC, "maNguoiDung")
                )
        );

        model.addAttribute("users", users);
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String formTao(Model model) {
        model.addAttribute("form", new NguoiDungForm());
        model.addAttribute("isEdit", false);
        napDanhMuc(model);
        return "admin/users/form";
    }

    @PostMapping
    public String tao(
            @Valid @ModelAttribute("form") NguoiDungForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            napDanhMuc(model);
            return "admin/users/form";
        }

        try {
            NguoiDung nguoiCap = timNguoiDungDangNhap(authentication);
            nguoiDungService.taoTaiKhoan(form, nguoiCap);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã tạo tài khoản."
            );
            return "redirect:/admin/users";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("form", exception.getMessage());
            model.addAttribute("isEdit", false);
            napDanhMuc(model);
            return "admin/users/form";
        }
    }

    @GetMapping("/{maNguoiDung}/edit")
    public String formCapNhat(
            @PathVariable String maNguoiDung,
            Model model
    ) {
        model.addAttribute(
                "form",
                nguoiDungService.layFormCapNhat(maNguoiDung)
        );
        model.addAttribute("maNguoiDung", maNguoiDung);
        model.addAttribute("isEdit", true);
        napDanhMuc(model);
        return "admin/users/form";
    }

    @PostMapping("/{maNguoiDung}")
    public String capNhat(
            @PathVariable String maNguoiDung,
            @Valid @ModelAttribute("form") NguoiDungCapNhatForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("maNguoiDung", maNguoiDung);
            model.addAttribute("isEdit", true);
            napDanhMuc(model);
            return "admin/users/form";
        }

        try {
            nguoiDungService.capNhatThongTin(maNguoiDung, form);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã cập nhật tài khoản."
            );
            return "redirect:/admin/users";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("form", exception.getMessage());
            model.addAttribute("maNguoiDung", maNguoiDung);
            model.addAttribute("isEdit", true);
            napDanhMuc(model);
            return "admin/users/form";
        }
    }

    @PostMapping("/{maNguoiDung}/trang-thai")
    public String capNhatTrangThai(
            @PathVariable String maNguoiDung,
            @RequestParam String trangThai,
            RedirectAttributes redirectAttributes
    ) {
        try {
            nguoiDungService.capNhatTrangThai(maNguoiDung, trangThai);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã cập nhật trạng thái tài khoản."
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/users";
    }

    private void napDanhMuc(Model model) {
        model.addAttribute(
                "boMons",
                boMonRepository.findAllByHoatDongTrueOrderByTenBoMonAsc()
        );
        model.addAttribute(
                "vaiTros",
                vaiTroRepository.findAll(
                        Sort.by(Sort.Direction.ASC, "tenVaiTro")
                )
        );
    }

    private NguoiDung timNguoiDungDangNhap(
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalArgumentException(
                    "Không xác định được người dùng đang đăng nhập"
            );
        }

        return nguoiDungRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy tài khoản đang đăng nhập"
                        ));
    }
}