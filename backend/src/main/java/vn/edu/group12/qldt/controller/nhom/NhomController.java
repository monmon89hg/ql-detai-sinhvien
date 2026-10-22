package vn.edu.group12.qldt.controller.nhom;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.edu.group12.qldt.domain.nhom.NghiepVuNhomException;
import vn.edu.group12.qldt.dto.nhom.TaoNhomForm;
import vn.edu.group12.qldt.service.nhom.*;

@Controller
@RequestMapping("/nhom")
public class NhomController {
    private final NhomService service;
    private final NhomBaoCaoViewService views;

    public NhomController(NhomService service, NhomBaoCaoViewService views) {
        this.service = service;
        this.views = views;
    }

    @InitBinder("taoNhomForm")
    void gioiHan(org.springframework.web.bind.WebDataBinder binder) {
        binder.setAllowedFields("tenNhom", "maDot");
    }

    @GetMapping
    String danhSach(
            @RequestParam(defaultValue = "") String tuKhoa,
            @RequestParam(defaultValue = "") String maDot,
            @RequestParam(defaultValue = "") String trangThai,
            @RequestParam(defaultValue = "0") int trang,
            Model model) {
        model.addAllAttributes(views.danhSachNhom(tuKhoa, maDot, trangThai, trang));
        return "groups/list";
    }

    @GetMapping("/tao")
    String form(Model model) {
        model.addAllAttributes(views.formTaoNhom());
        model.addAttribute("taoNhomForm", new TaoNhomForm());
        return "groups/form";
    }

    @PostMapping("/tao")
    String tao(
            @Valid @ModelAttribute("taoNhomForm") TaoNhomForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        model.addAllAttributes(views.formTaoNhom());
        if (errors.hasErrors()) return "groups/form";
        try {
            service.tao(form);
        } catch (NghiepVuNhomException ex) {
            if (ex.getLoai() != NghiepVuNhomException.Loai.DU_LIEU
                    && ex.getLoai() != NghiepVuNhomException.Loai.XUNG_DOT) throw ex;
            model.addAttribute("errorMessage", ex.getMessage());
            return "groups/form";
        }
        flash.addFlashAttribute("successMessage", "Đã tạo nhóm và xác nhận bạn là nhóm trưởng.");
        return "redirect:/nhom/cua-toi";
    }

    @GetMapping("/cua-toi")
    String cuaToi(Model model) {
        model.addAllAttributes(views.nhomCuaToi());
        return "groups/cua-toi";
    }

    @GetMapping("/{maNhom}")
    String chiTiet(@PathVariable String maNhom, Model model) {
        model.addAllAttributes(views.chiTietNhom(maNhom));
        return "groups/detail";
    }

    @PostMapping("/{maNhom}/yeu-cau")
    String yeuCau(@PathVariable String maNhom, RedirectAttributes f) {
        service.yeuCau(maNhom);
        f.addFlashAttribute("successMessage", "Đã gửi yêu cầu tham gia.");
        return "redirect:/nhom/" + maNhom;
    }

    @PostMapping("/{maNhom}/yeu-cau/{maSinhVien}/duyet")
    String duyet(
            @PathVariable String maNhom, @PathVariable String maSinhVien, RedirectAttributes f) {
        service.xuLyYeuCau(maNhom, maSinhVien, true);
        f.addFlashAttribute("successMessage", "Đã duyệt thành viên.");
        return "redirect:/nhom/cua-toi";
    }

    @PostMapping("/{maNhom}/yeu-cau/{maSinhVien}/tu-choi")
    String tuChoi(
            @PathVariable String maNhom, @PathVariable String maSinhVien, RedirectAttributes f) {
        service.xuLyYeuCau(maNhom, maSinhVien, false);
        f.addFlashAttribute("successMessage", "Đã từ chối yêu cầu.");
        return "redirect:/nhom/cua-toi";
    }

    @PostMapping("/{maNhom}/thanh-vien/{maSinhVien}/xoa")
    String xoa(@PathVariable String maNhom, @PathVariable String maSinhVien, RedirectAttributes f) {
        service.xoaThanhVien(maNhom, maSinhVien);
        f.addFlashAttribute(
                "successMessage", "Đã kết thúc tư cách thành viên; lịch sử được giữ nguyên.");
        return "redirect:/nhom/cua-toi";
    }

    @PostMapping("/{maNhom}/chot")
    String chot(@PathVariable String maNhom, RedirectAttributes f) {
        service.chot(maNhom);
        f.addFlashAttribute("successMessage", "Đã chốt danh sách thành viên.");
        return "redirect:/nhom/cua-toi";
    }
}
