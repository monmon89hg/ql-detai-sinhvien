package vn.edu.group12.qldt.controller.nhom;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.edu.group12.qldt.service.nhom.*;

@Controller
@RequestMapping("/dang-ky-de-tai")
public class DangKyDeTaiController {
    private final DangKyDeTaiService service;
    private final NhomBaoCaoViewService views;

    public DangKyDeTaiController(DangKyDeTaiService service, NhomBaoCaoViewService views) {
        this.service = service;
        this.views = views;
    }

    @GetMapping
    String danhSach(
            @RequestParam(defaultValue = "") String tuKhoa,
            @RequestParam(defaultValue = "") String maDot,
            @RequestParam(defaultValue = "0") int trang,
            Model model) {
        model.addAllAttributes(views.dangKy(tuKhoa, maDot, trang));
        return "registrations/list";
    }

    @PostMapping
    String dangKy(@RequestParam String maDeTai, RedirectAttributes f) {
        service.dangKy(maDeTai);
        f.addFlashAttribute("successMessage", "Đã gửi đăng ký đề tài để xét duyệt.");
        return "redirect:/dang-ky-de-tai";
    }

    @GetMapping("/duyet")
    String duyet(
            @RequestParam(defaultValue = "") String tuKhoa,
            @RequestParam(defaultValue = "") String trangThai,
            @RequestParam(defaultValue = "0") int trang,
            Model model) {
        model.addAllAttributes(views.duyetDangKy(tuKhoa, trangThai, trang));
        return "registrations/review";
    }

    @PostMapping("/{maNhom}/duyet")
    String duyet(@PathVariable String maNhom, RedirectAttributes f) {
        service.duyet(maNhom);
        f.addFlashAttribute("successMessage", "Đã duyệt đăng ký đề tài.");
        return "redirect:/dang-ky-de-tai/duyet";
    }

    @PostMapping("/{maNhom}/tu-choi")
    String tuChoi(
            @PathVariable String maNhom, @RequestParam String lyDoTuChoi, RedirectAttributes f) {
        service.tuChoi(maNhom, lyDoTuChoi);
        f.addFlashAttribute("successMessage", "Đã từ chối đăng ký và lưu lý do.");
        return "redirect:/dang-ky-de-tai/duyet";
    }
}
