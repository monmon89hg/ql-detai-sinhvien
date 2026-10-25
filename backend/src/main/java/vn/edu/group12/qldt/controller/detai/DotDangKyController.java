package vn.edu.group12.qldt.controller.detai;

import vn.edu.group12.qldt.service.auth.CurrentUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.group12.qldt.dto.detai.DotDangKyDto;
import vn.edu.group12.qldt.entity.detai.DotDangKy;
import vn.edu.group12.qldt.service.detai.DotDangKyService;
import vn.edu.group12.qldt.service.detai.LichIcsService;
import org.springframework.security.access.prepost.PreAuthorize;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dot-dang-ky")
public class DotDangKyController {

    @Autowired
    private DotDangKyService dotDangKyService;

    @Autowired
    private LichIcsService lichIcsService;

    // 1. Danh sách đợt đăng ký
    @GetMapping
    @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'ADMIN')")
    public String listDotDangKy(Model model, Principal principal) {
        List<DotDangKy> list = dotDangKyService.getAllDotDangKy();
        
        // Tạo map lưu thông tin giai đoạn hiện tại của từng đợt để hiển thị trực quan
        Map<String, String> giaiDoanMap = new HashMap<>();
        for (DotDangKy dot : list) {
            giaiDoanMap.put(dot.getMaDot(), dotDangKyService.getGiaiDoanHienTai(dot));
        }

        model.addAttribute("dots", list);
        model.addAttribute("giaiDoanMap", giaiDoanMap);
        model.addAttribute("activeMenu", "rounds");
        model.addAttribute("currentUserName", principal != null ? principal.getName() : "Trưởng khoa / Quản trị");
        return "registration-rounds/list";
    }

    // 2. Form tạo mới đợt đăng ký
    @GetMapping("/tao")
    public String showCreateForm(Model model, Principal principal) {
        model.addAttribute("dotDto", new DotDangKyDto());
        model.addAttribute("activeMenu", "rounds");
        model.addAttribute("currentUserName", principal != null ? principal.getName() : "Trưởng khoa / Quản trị");
        return "registration-rounds/form";
    }

    // 3. Xử lý lưu tạo mới
    @PostMapping("/tao")
    public String createDotDangKy(@ModelAttribute("dotDto") DotDangKyDto dto,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            String maNguoiTao = CurrentUser.maNguoiDung();
            dotDangKyService.createDotDangKy(dto, maNguoiTao);
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
    @GetMapping("/sua/{maDot}")
    public String showEditForm(@PathVariable("maDot") String maDot, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            DotDangKy dot = dotDangKyService.getDotDangKyByMa(maDot);
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
            model.addAttribute("maDot", maDot);
            model.addAttribute("activeMenu", "rounds");
            model.addAttribute("currentUserName", principal != null ? principal.getName() : "Trưởng khoa / Quản trị");
            return "registration-rounds/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dot-dang-ky";
        }
    }

    // 5. Xử lý cập nhật
    @PostMapping("/sua/{maDot}")
    public String updateDotDangKy(@PathVariable("maDot") String maDot,
                                  @ModelAttribute("dotDto") DotDangKyDto dto,
                                  RedirectAttributes redirectAttributes) {
        try {
            dto.setMaDot(maDot);
            dotDangKyService.updateDotDangKy(maDot, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đợt đăng ký thành công!");
            return "redirect:/dot-dang-ky";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dot-dang-ky/sua/" + maDot;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/dot-dang-ky/sua/" + maDot;
        }
    }

    // 6. Công bố đợt đăng ký
    @PostMapping("/{maDot}/cong-bo")
    public String publishDot(@PathVariable("maDot") String maDot, RedirectAttributes redirectAttributes) {
        try {
            dotDangKyService.congBoDot(maDot);
            redirectAttributes.addFlashAttribute("successMessage", "Đã công bố đợt đăng ký!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dot-dang-ky";
    }

    // 7. Đóng đợt đăng ký
    @PostMapping("/{maDot}/dong")
    public String closeDot(@PathVariable("maDot") String maDot, RedirectAttributes redirectAttributes) {
        try {
            dotDangKyService.dongDot(maDot);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đóng đợt đăng ký!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dot-dang-ky";
    }

    // 8. Xuất lịch hạn nộp và các mốc thời gian vào Google Calendar / Apple Calendar (.ics)
    @GetMapping(value = "/{maDot}/lich.ics", produces = "text/calendar;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<byte[]> exportCalendar(@PathVariable("maDot") String maDot) {
        String icsContent = lichIcsService.taoLichIcsChoDot(maDot);
        byte[] bytes = icsContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"lich-dot-" + maDot + ".ics\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=UTF-8")
                .body(bytes);
    }
}
