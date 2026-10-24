package vn.edu.group12.qldt.controller.detai;

import vn.edu.group12.qldt.service.auth.CurrentUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.group12.qldt.dto.detai.DeTaiDto;
import vn.edu.group12.qldt.entity.detai.DeTai;
import vn.edu.group12.qldt.entity.detai.DeTaiGiangVien;
import vn.edu.group12.qldt.entity.detai.DotDangKy;
import vn.edu.group12.qldt.service.detai.DeTaiGiangVienService;
import vn.edu.group12.qldt.service.detai.DeTaiService;
import vn.edu.group12.qldt.service.detai.DotDangKyService;
import vn.edu.group12.qldt.service.detai.LichIcsService;

import java.security.Principal;
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

    @Autowired
    private LichIcsService lichIcsService;

    // 1. Danh sách đề tài (Tìm kiếm & Lọc theo đợt, bộ môn, trạng thái, từ khóa)
    @GetMapping
    public String listDeTai(@RequestParam(value = "maDot", required = false) String maDot,
                            @RequestParam(value = "maBoMon", required = false) String maBoMon,
                            @RequestParam(value = "trangThai", required = false) String trangThai,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            Principal principal,
                            Model model) {
        
        List<DeTai> deTais = deTaiService.filterDeTai(maDot, maBoMon, trangThai, keyword);
        List<DotDangKy> dots = dotDangKyService.getAllDotDangKy();

        model.addAttribute("deTais", deTais);
        model.addAttribute("dots", dots);
        model.addAttribute("selectedMaDot", maDot);
        model.addAttribute("selectedMaBoMon", maBoMon);
        model.addAttribute("selectedTrangThai", trangThai);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "topics");
        model.addAttribute("currentUserName", principal != null ? principal.getName() : "Người dùng");
        return "topics/list";
    }

    // 2. Chi tiết đề tài & Danh sách giảng viên được phân công
    @GetMapping("/chi-tiet/{maDeTai}")
    public String viewDeTaiDetail(@PathVariable("maDeTai") String maDeTai,
                                  Principal principal,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            DeTai deTai = deTaiService.getDeTaiByMa(maDeTai);
            List<DeTaiGiangVien> phanCongs = deTaiGiangVienService.getPhanCongByDeTai(maDeTai);
            DotDangKy dot = dotDangKyService.getDotDangKyByMa(deTai.getMaDot());

            model.addAttribute("deTai", deTai);
            model.addAttribute("phanCongs", phanCongs);
            model.addAttribute("dot", dot);
            model.addAttribute("activeMenu", "topics");
            model.addAttribute("currentUserName", principal != null ? principal.getName() : "Người dùng");
            return "topics/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/de-tai";
        }
    }

    // 3. Giao diện Giảng viên đề xuất đề tài (Giai đoạn 1)
    @GetMapping("/de-xuat")
    public String showProposeForm(Principal principal, Model model) {
        List<DotDangKy> dots = dotDangKyService.getAllDotDangKy();
        model.addAttribute("deTaiDto", new DeTaiDto());
        model.addAttribute("dots", dots);
        model.addAttribute("activeMenu", "propose");
        model.addAttribute("currentUserName", principal != null ? principal.getName() : "TS. Trần Văn B (Giảng viên)");
        return "topics/propose";
    }

    // 4. Xử lý nộp đề xuất đề tài
    @PostMapping("/de-xuat")
    public String proposeTopic(@ModelAttribute("deTaiDto") DeTaiDto dto,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            String maGiangVien = CurrentUser.maNguoiDung();
            deTaiService.deXuatDeTai(dto, maGiangVien);
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

    // 4.1 Form chỉnh sửa đề tài
    @GetMapping("/sua/{maDeTai}")
    public String showEditForm(@PathVariable("maDeTai") String maDeTai,
                               Principal principal,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            DeTai deTai = deTaiService.getDeTaiByMa(maDeTai);
            List<DotDangKy> dots = dotDangKyService.getAllDotDangKy();

            DeTaiDto dto = new DeTaiDto();
            dto.setMaDeTai(deTai.getMaDeTai());
            dto.setMaDot(deTai.getMaDot());
            dto.setMaBoMon(deTai.getMaBoMon());
            dto.setTenDeTai(deTai.getTenDeTai());
            dto.setNoiDung(deTai.getNoiDung());
            dto.setYeuCau(deTai.getYeuCau());

            model.addAttribute("deTaiDto", dto);
            model.addAttribute("deTai", deTai);
            model.addAttribute("dots", dots);
            model.addAttribute("activeMenu", "topics");
            model.addAttribute("currentUserName", principal != null ? principal.getName() : "Người dùng");
            return "topics/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/de-tai";
        }
    }

    // 4.2 Xử lý cập nhật đề tài
    @PostMapping("/sua/{maDeTai}")
    public String updateTopic(@PathVariable("maDeTai") String maDeTai,
                              @ModelAttribute("deTaiDto") DeTaiDto dto,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            String maNguoiCapNhat = CurrentUser.maNguoiDung();
            deTaiService.capNhatDeTai(maDeTai, dto, maNguoiCapNhat);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đề tài thành công!");
            return "redirect:/de-tai/chi-tiet/" + maDeTai;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/de-tai/sua/" + maDeTai;
        }
    }

    // 4.3 Cơ chế ngừng nhận đăng ký đề tài (thay vì xóa vật lý)
    @PostMapping("/{maDeTai}/ngung-nhan")
    public String stopAcceptingTopic(@PathVariable("maDeTai") String maDeTai,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            String maNguoiThucHien = CurrentUser.maNguoiDung();
            deTaiService.ngungNhanDeTai(maDeTai, maNguoiThucHien);
            redirectAttributes.addFlashAttribute("successMessage", "Đã chuyển đề tài sang trạng thái NGỪNG NHẬN đăng ký!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/de-tai/chi-tiet/" + maDeTai;
    }

    // 4.4 Cơ chế xóa hoặc hủy đề tài (chống xóa vật lý khi có dữ liệu liên quan)
    @PostMapping("/{maDeTai}/xoa")
    public String deleteOrCancelTopic(@PathVariable("maDeTai") String maDeTai,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            String maNguoiThucHien = CurrentUser.maNguoiDung();
            String msg = deTaiService.xoaHoacHuyDeTai(maDeTai, maNguoiThucHien);
            redirectAttributes.addFlashAttribute("successMessage", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/de-tai";
    }

    // 5. Giao diện Trưởng khoa xét duyệt, phân công & công bố đề tài
    @GetMapping("/duyet")
    public String showApprovalList(Principal principal, Model model) {
        List<DeTai> deTais = deTaiService.getAllDeTai();
        List<DotDangKy> dots = dotDangKyService.getAllDotDangKy();

        model.addAttribute("deTais", deTais);
        model.addAttribute("dots", dots);
        model.addAttribute("activeMenu", "approve-topic");
        model.addAttribute("currentUserName", principal != null ? principal.getName() : "PGS.TS Nguyễn Văn C (Trưởng khoa)");
        return "topics/review";
    }

    // 6. Trưởng khoa duyệt đề tài
    @PostMapping("/{maDeTai}/duyet")
    public String approveTopic(@PathVariable("maDeTai") String maDeTai,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            String maNguoiDuyet = CurrentUser.maNguoiDung();
            deTaiService.duyetDeTai(maDeTai, maNguoiDuyet);
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đề tài thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/de-tai/duyet";
    }

    // 7. Trưởng khoa từ chối đề tài kèm lý do
    @PostMapping("/{maDeTai}/tu-choi")
    public String rejectTopic(@PathVariable("maDeTai") String maDeTai,
                              @RequestParam("lyDoTuChoi") String lyDoTuChoi,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            String maNguoiDuyet = CurrentUser.maNguoiDung();
            deTaiService.tuChoiDeTai(maDeTai, lyDoTuChoi, maNguoiDuyet);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đề tài!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/de-tai/duyet";
    }

    // 8. Trưởng khoa công bố đề tài
    @PostMapping("/{maDeTai}/cong-bo")
    public String publishTopic(@PathVariable("maDeTai") String maDeTai, RedirectAttributes redirectAttributes) {
        try {
            deTaiService.congBoDeTai(maDeTai);
            redirectAttributes.addFlashAttribute("successMessage", "Công bố đề tài thành công! Sinh viên đã có thể xem trong Giai đoạn 2.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/de-tai/duyet";
    }

    // 9. Phân công giảng viên (GVHD, Phản biện, Chấm độc lập)
    @PostMapping("/{maDeTai}/phan-cong")
    public String assignLecturer(@PathVariable("maDeTai") String maDeTai,
                                 @RequestParam("maGiangVien") String maGiangVien,
                                 @RequestParam("loaiPhanCong") String loaiPhanCong,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            String maNguoiPhanCong = CurrentUser.maNguoiDung();
            deTaiGiangVienService.phanCong(maDeTai, maGiangVien, loaiPhanCong, maNguoiPhanCong);
            redirectAttributes.addFlashAttribute("successMessage", "Phân công giảng viên thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/de-tai/chi-tiet/" + maDeTai;
    }

    // 10. Xóa phân công giảng viên
    @PostMapping("/phan-cong/{maGiangVien}/xoa")
    public String removeAssignment(@PathVariable("maGiangVien") String maGiangVien,
                                   @RequestParam("maDeTai") String maDeTai,
                                   RedirectAttributes redirectAttributes) {
        try {
            deTaiGiangVienService.xoaPhanCong(maDeTai, maGiangVien);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gỡ phân công giảng viên!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/de-tai/chi-tiet/" + maDeTai;
    }

    // 11. Xuất lịch hạn nộp và các mốc thời gian vào Google Calendar (.ics)
    @GetMapping(value = "/lich/{maDot}.ics", produces = "text/calendar;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<byte[]> exportCalendarTopic(@PathVariable("maDot") String maDot) {
        String icsContent = lichIcsService.taoLichIcsChoDot(maDot);
        byte[] bytes = icsContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"lich-dot-" + maDot + ".ics\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=UTF-8")
                .body(bytes);
    }
}
