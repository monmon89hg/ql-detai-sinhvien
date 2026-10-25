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
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import vn.edu.group12.qldt.service.hoidong.NguoiDungHienThiService;
import vn.edu.group12.qldt.service.hoidong.XuatExcelKetQuaService;
import vn.edu.group12.qldt.dto.hoidong.NhomKetQuaHienThi;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/ket-qua")
public class KetQuaHoiDongController {

    private final TongHopKetQuaService tongHopKetQuaService;
    private final NguoiDungHienThiService nguoiDungHienThiService;
    private final XuatExcelKetQuaService xuatExcelKetQuaService;
    private final vn.edu.group12.qldt.service.hoidong.MoChamService moChamService;

    public KetQuaHoiDongController(
            TongHopKetQuaService tongHopKetQuaService,
            NguoiDungHienThiService nguoiDungHienThiService,
            XuatExcelKetQuaService xuatExcelKetQuaService,
            vn.edu.group12.qldt.service.hoidong.MoChamService moChamService) {
        this.tongHopKetQuaService = tongHopKetQuaService;
        this.moChamService = moChamService;
        this.nguoiDungHienThiService = nguoiDungHienThiService;
        this.xuatExcelKetQuaService = xuatExcelKetQuaService;
    }

    // Chủ tịch tổng hợp; Trưởng khoa và ADMIN chỉ xem danh sách, trạng thái.
    @GetMapping("/tong-hop")
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_KHOA', 'ADMIN')")
    public String danhSachTongHop(
            Model model,
            Principal principal,
            HttpServletRequest request) {

        boolean laAdmin = request.isUserInRole("ADMIN");
        boolean laTruongKhoa = request.isUserInRole("TRUONG_KHOA");
        boolean chiXem = laAdmin || laTruongKhoa;
        model.addAttribute("chiXem", chiXem);

        model.addAttribute("maDot", request.getParameter("maDot") == null ? "" : request.getParameter("maDot"));
        model.addAttribute("loai", request.getParameter("loai") == null ? "" : request.getParameter("loai"));
        model.addAttribute("danhSachDot", moChamService.danhSachDot());
        model.addAttribute("danhSachNhom", layDanhSachTongHop(request));

        model.addAttribute("currentUserName", nguoiDungHienThiService.layHoTenHienTai());
        return "assessment/ket-qua/tong-hop-list";
    }

    @GetMapping("/tong-hop/xuat-excel")
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'TRUONG_KHOA', 'ADMIN')")
    public ResponseEntity<byte[]> xuatDanhSachTongHop(HttpServletRequest request)
            throws IOException {
        return taiExcel(
            xuatExcelKetQuaService.xuat(layDanhSachTongHop(request), false),
            "tong-hop-ket-qua.xlsx"
        );
    }

    // Trưởng khoa quản lý và công bố; ADMIN chỉ xem kết quả.
    @GetMapping("/quan-ly")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'ADMIN')")
    public String danhSachQuanLy(
            Model model,
            Principal principal,
            HttpServletRequest request) {

        boolean laAdmin = request.isUserInRole("ADMIN");
        model.addAttribute("laAdmin", laAdmin);

        model.addAttribute("maDot", request.getParameter("maDot") == null ? "" : request.getParameter("maDot"));
        model.addAttribute("loai", request.getParameter("loai") == null ? "" : request.getParameter("loai"));
        model.addAttribute("danhSachDot", moChamService.danhSachDot());
        model.addAttribute("danhSachNhom", layDanhSachQuanLy(request));

        model.addAttribute("currentUserName", nguoiDungHienThiService.layHoTenHienTai());
        return "assessment/ket-qua/quan-ly-list";
    }

    @GetMapping("/quan-ly/xuat-excel")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA', 'ADMIN')")
    public ResponseEntity<byte[]> xuatDanhSachQuanLy(HttpServletRequest request)
            throws IOException {
        return taiExcel(
            xuatExcelKetQuaService.xuat(layDanhSachQuanLy(request), true),
            "quan-ly-ket-qua.xlsx"
        );
    }

    private List<NhomKetQuaHienThi> layDanhSachTongHop(HttpServletRequest request) {
        if (request.getParameter("maDot")!=null || request.getParameter("loai")!=null)
            return tongHopKetQuaService.locDanhSach(request.getParameter("maDot"),request.getParameter("loai"));
        if (request.isUserInRole("ADMIN")) {
            return tongHopKetQuaService.layDanhSachNhomChoAdmin();
        }
        if (request.isUserInRole("TRUONG_KHOA")) {
            return tongHopKetQuaService.layDanhSachNhomChoTruongKhoa();
        }
        return tongHopKetQuaService.layDanhSachNhomChoChuTich();
    }

    private List<NhomKetQuaHienThi> layDanhSachQuanLy(HttpServletRequest request) {
        if (request.getParameter("maDot")!=null || request.getParameter("loai")!=null)
            return tongHopKetQuaService.locDanhSach(request.getParameter("maDot"),request.getParameter("loai"));
        return request.isUserInRole("ADMIN")
            ? tongHopKetQuaService.layDanhSachNhomChoAdmin()
            : tongHopKetQuaService.layDanhSachNhomChoTruongKhoa();
    }

    private ResponseEntity<byte[]> taiExcel(byte[] noiDung, String tenFile) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + tenFile + "\"")
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .cacheControl(CacheControl.noStore())
            .body(noiDung);
    }

    // Người được phân công tổng hợp gửi yêu cầu xác nhận kết quả của nhóm.
    @PostMapping("/{maNhom}/tong-hop")
    @PreAuthorize("hasRole('GIANG_VIEN') and !hasRole('TRUONG_KHOA')")
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
    @GetMapping("/{maNhom}/chi-tiet")
    @PreAuthorize("hasAnyRole('GIANG_VIEN','TRUONG_KHOA','ADMIN')")
    public String chiTiet(@PathVariable String maNhom, Model model, HttpServletRequest request) {
        model.addAttribute("coTheDocBaoCao", !request.isUserInRole("ADMIN"));
        model.addAttribute("chiTiet", moChamService.chiTiet(maNhom));
        model.addAttribute("laTruongKhoa", request.isUserInRole("TRUONG_KHOA") && !request.isUserInRole("ADMIN"));
        model.addAttribute("currentUserName", nguoiDungHienThiService.layHoTenHienTai());
        return "assessment/ket-qua/chi-tiet";
    }

    @PostMapping("/{maNhom}/mo-cham")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String moCham(@PathVariable String maNhom, RedirectAttributes flash) {
        try { moChamService.moCham(maNhom); flash.addFlashAttribute("successMessage", "Đã mở chấm và khóa dữ liệu đầu vào của nhóm."); }
        catch (IllegalArgumentException | org.springframework.dao.DataAccessException ex) {
            flash.addFlashAttribute("errorMessage", ex instanceof IllegalArgumentException ? ex.getMessage() : "Dữ liệu vừa thay đổi. Vui lòng tải lại trang.");
        }
        return "redirect:/ket-qua/quan-ly";
    }

    @PostMapping("/{maNhom}/email/{maSinhVien}/thu-lai")
    @PreAuthorize("hasRole('TRUONG_KHOA')")
    public String thuLaiEmail(@PathVariable String maNhom, @PathVariable String maSinhVien, RedirectAttributes flash) {
        try { moChamService.thuLaiEmail(maNhom,maSinhVien); flash.addFlashAttribute("successMessage", "Đã đưa thư gửi lỗi vào hàng đợi thử lại."); }
        catch (IllegalArgumentException ex) { flash.addFlashAttribute("errorMessage",ex.getMessage()); }
        return "redirect:/ket-qua/"+maNhom+"/chi-tiet";
    }

}
