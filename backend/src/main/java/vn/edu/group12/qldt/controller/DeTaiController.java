package vn.edu.group12.qldt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.group12.qldt.dto.DeTaiDto;
import vn.edu.group12.qldt.model.DeTai;
import vn.edu.group12.qldt.model.DeTaiGiangVien;
import vn.edu.group12.qldt.model.DotDangKy;
import vn.edu.group12.qldt.service.DeTaiGiangVienService;
import vn.edu.group12.qldt.service.DeTaiService;
import vn.edu.group12.qldt.service.DotDangKyService;

import java.util.List;

@Controller
@RequestMapping("/de-tai")
public class DeTaiController {

    @Autowired
    private DeTaiService deTaiService;

    @Autowired
    private DotDangKyService dotDangKyService;

    @Autowired
    private DeTaiGiangVienService deTaiGiangVienService;

    // 1. Danh sách đề tài (Tìm kiếm & Lọc theo đợt, bộ môn, trạng thái, từ khóa)
    @GetMapping
    public String listDeTai(@RequestParam(value = "dotId", required = false) Long dotId,
                            @RequestParam(value = "boMonId", required = false) Long boMonId,
                            @RequestParam(value = "trangThai", required = false) String trangThai,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            Model model) {
        
        List<DeTai> deTais = deTaiService.filterDeTai(dotId, boMonId, trangThai, keyword);
        List<DotDangKy> dots = dotDangKyService.getAllDotDangKy();

        model.addAttribute("deTais", deTais);
        model.addAttribute("dots", dots);
        model.addAttribute("selectedDotId", dotId);
        model.addAttribute("selectedBoMonId", boMonId);
        model.addAttribute("selectedTrangThai", trangThai);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "topics");
        model.addAttribute("currentUserName", "Nguyễn Văn A");
        return "topics/list";
    }

    // 2. Chi tiết đề tài & Danh sách giảng viên được phân công
    @GetMapping("/chi-tiet/{id}")
    public String viewDeTaiDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            DeTai deTai = deTaiService.getDeTaiById(id);
            List<DeTaiGiangVien> phanCongs = deTaiGiangVienService.getPhanCongByDeTai(id);
            DotDangKy dot = dotDangKyService.getDotDangKyById(deTai.getDotDangKyId());

            model.addAttribute("deTai", deTai);
            model.addAttribute("phanCongs", phanCongs);
            model.addAttribute("dot", dot);
            model.addAttribute("activeMenu", "topics");
            model.addAttribute("currentUserName", "Nguyễn Văn A");
            return "topics/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/de-tai";
        }
    }

    // 3. Giao diện Giảng viên đề xuất đề tài (Giai đoạn 1)
    @GetMapping("/de-xuat")
    public String showProposeForm(Model model) {
        List<DotDangKy> dots = dotDangKyService.getAllDotDangKy();
        model.addAttribute("deTaiDto", new DeTaiDto());
        model.addAttribute("dots", dots);
        model.addAttribute("activeMenu", "propose");
        model.addAttribute("currentUserName", "TS. Trần Văn B (Giảng viên)");
        return "topics/propose";
    }

    // 4. Xử lý nộp đề xuất đề tài
    @PostMapping("/de-xuat")
    public String proposeTopic(@ModelAttribute("deTaiDto") DeTaiDto dto,
                               RedirectAttributes redirectAttributes) {
        try {
            Long giangVienId = 2L; // Giả lập ID Giảng viên đang đăng nhập
            deTaiService.deXuatDeTai(dto, giangVienId);
            redirectAttributes.addFlashAttribute("successMessage", "Đề xuất đề tài thành công! Đề tài đang chờ Trưởng khoa duyệt.");
            return "redirect:/de-tai";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/de-tai/de-xuat";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/de-tai/de-xuat";
        }
    }

    // 5. Giao diện Trưởng khoa xét duyệt, phân công & công bố đề tài
    @GetMapping("/duyet")
    public String showApprovalList(Model model) {
        List<DeTai> deTais = deTaiService.getAllDeTai();
        List<DotDangKy> dots = dotDangKyService.getAllDotDangKy();

        model.addAttribute("deTais", deTais);
        model.addAttribute("dots", dots);
        model.addAttribute("activeMenu", "approve-topic");
        model.addAttribute("currentUserName", "PGS.TS Nguyễn Văn C (Trưởng khoa)");
        return "topics/review";
    }

    // 6. Trưởng khoa duyệt đề tài
    @PostMapping("/{id}/duyet")
    public String approveTopic(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Long approverId = 1L; // ID Trưởng khoa
            deTaiService.duyetDeTai(id, approverId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đề tài thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/de-tai/duyet";
    }

    // 7. Trưởng khoa từ chối đề tài kèm lý do
    @PostMapping("/{id}/tu-choi")
    public String rejectTopic(@PathVariable("id") Long id,
                              @RequestParam("lyDoTuChoi") String lyDoTuChoi,
                              RedirectAttributes redirectAttributes) {
        try {
            Long approverId = 1L;
            deTaiService.tuChoiDeTai(id, lyDoTuChoi, approverId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đề tài!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/de-tai/duyet";
    }

    // 8. Trưởng khoa công bố đề tài
    @PostMapping("/{id}/cong-bo")
    public String publishTopic(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            deTaiService.congBoDeTai(id);
            redirectAttributes.addFlashAttribute("successMessage", "Công bố đề tài thành công! Sinh viên đã có thể xem trong Giai đoạn 2.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/de-tai/duyet";
    }

    // 9. Phân công giảng viên (GVHD, Phản biện, Chấm độc lập)
    @PostMapping("/{id}/phan-cong")
    public String assignLecturer(@PathVariable("id") Long id,
                                 @RequestParam("giangVienId") Long giangVienId,
                                 @RequestParam("loaiPhanCong") String loaiPhanCong,
                                 RedirectAttributes redirectAttributes) {
        try {
            Long approverId = 1L;
            deTaiGiangVienService.phanCong(id, giangVienId, loaiPhanCong, approverId);
            redirectAttributes.addFlashAttribute("successMessage", "Phân công giảng viên thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/de-tai/chi-tiet/" + id;
    }

    // 10. Xóa phân công giảng viên
    @PostMapping("/phan-cong/{phanCongId}/xoa")
    public String removeAssignment(@PathVariable("phanCongId") Long phanCongId,
                                   @RequestParam("deTaiId") Long deTaiId,
                                   RedirectAttributes redirectAttributes) {
        try {
            deTaiGiangVienService.xoaPhanCong(phanCongId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gỡ phân công giảng viên!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/de-tai/chi-tiet/" + deTaiId;
    }
}