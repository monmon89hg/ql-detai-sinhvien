package vn.edu.group12.qldt.controller.detai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.group12.qldt.dto.detai.DotDangKyDto;
import vn.edu.group12.qldt.entity.detai.DotDangKy;
import vn.edu.group12.qldt.service.detai.DotDangKyService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dot-dang-ky")
public class DotDangKyController {

    @Autowired
    private DotDangKyService dotDangKyService;

    // 1. Danh sách đợt đăng ký
    @GetMapping
    public String listDotDangKy(Model model) {
        List<DotDangKy> list = dotDangKyService.getAllDotDangKy();
        
        // Tạo map lưu thông tin giai đoạn hiện tại của từng đợt để hiển thị trực quan
        Map<Long, String> giaiDoanMap = new HashMap<>();
        for (DotDangKy dot : list) {
            giaiDoanMap.put(dot.getId(), dotDangKyService.getGiaiDoanHienTai(dot));
        }

        model.addAttribute("dots", list);
        model.addAttribute("giaiDoanMap", giaiDoanMap);
        model.addAttribute("activeMenu", "rounds");
        model.addAttribute("currentUserName", "Trưởng khoa / Quản trị");
        return "registration-rounds/list";
    }

    // 2. Form tạo mới đợt đăng ký
    @GetMapping("/tao")
    public String showCreateForm(Model model) {
        model.addAttribute("dotDto", new DotDangKyDto());
        model.addAttribute("activeMenu", "rounds");
        model.addAttribute("currentUserName", "Trưởng khoa / Quản trị");
        return "registration-rounds/form";
    }

    // 3. Xử lý lưu tạo mới
    @PostMapping("/tao")
    public String createDotDangKy(@ModelAttribute("dotDto") DotDangKyDto dto,
                                  RedirectAttributes redirectAttributes) {
        try {
            Long nguoiTaoId = 1L; // Mặc định ID Trưởng khoa/Admin
            dotDangKyService.createDotDangKy(dto, nguoiTaoId);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo mới đợt đăng ký thành công!");
            return "redirect:/dot-dang-ky";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dot-dang-ky/tao";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/dot-dang-ky/tao";
        }
    }

    // 4. Form chỉnh sửa đợt đăng ký
    @GetMapping("/sua/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            DotDangKy dot = dotDangKyService.getDotDangKyById(id);
            DotDangKyDto dto = new DotDangKyDto();
            dto.setMaDot(dot.getMaDot());
            dto.setTenDot(dot.getTenDot());
            dto.setLoaiDeTai(dot.getLoaiDeTai());
            dto.setBatDauGv(dot.getBatDauGv());
            dto.setKetThucGv(dot.getKetThucGv());
            dto.setBatDauSv(dot.getBatDauSv());
            dto.setKetThucSv(dot.getKetThucSv());
            dto.setHanNopBaoCao(dot.getHanNopBaoCao());
            dto.setHanNopDiemPb(dot.getHanNopDiemPb());
            dto.setNgayBaoCaoHd(dot.getNgayBaoCaoHd());
            dto.setTrangThai(dot.getTrangThai());

            model.addAttribute("dotDto", dto);
            model.addAttribute("dotId", id);
            model.addAttribute("activeMenu", "rounds");
            model.addAttribute("currentUserName", "Trưởng khoa / Quản trị");
            return "registration-rounds/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dot-dang-ky";
        }
    }

    // 5. Xử lý cập nhật đợt đăng ký
    @PostMapping("/sua/{id}")
    public String updateDotDangKy(@PathVariable("id") Long id,
                                  @ModelAttribute("dotDto") DotDangKyDto dto,
                                  RedirectAttributes redirectAttributes) {
        try {
            dotDangKyService.updateDotDangKy(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đợt đăng ký thành công!");
            return "redirect:/dot-dang-ky";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dot-dang-ky/sua/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/dot-dang-ky/sua/" + id;
        }
    }

    // 6. Công bố đợt đăng ký
    @PostMapping("/{id}/cong-bo")
    public String congBoDot(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            dotDangKyService.congBoDot(id);
            redirectAttributes.addFlashAttribute("successMessage", "Công bố đợt đăng ký thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dot-dang-ky";
    }

    // 7. Đóng đợt đăng ký
    @PostMapping("/{id}/dong")
    public String dongDot(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            dotDangKyService.dongDot(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đóng đợt đăng ký thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dot-dang-ky";
    }
}
