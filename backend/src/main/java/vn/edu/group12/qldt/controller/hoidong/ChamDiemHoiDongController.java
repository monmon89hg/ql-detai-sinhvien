package vn.edu.group12.qldt.controller.hoidong;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import vn.edu.group12.qldt.service.hoidong.ChamDiemHoiDongService;
import vn.edu.group12.qldt.service.hoidong.AdminXemChamDiemService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import vn.edu.group12.qldt.dto.hoidong.PhieuChamDiemForm;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiPhieuCham;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/cham-diem")
public class ChamDiemHoiDongController {

    private final ChamDiemHoiDongService chamDiemHoiDongService;
    private final AdminXemChamDiemService adminXemChamDiemService;

    public ChamDiemHoiDongController(
            ChamDiemHoiDongService chamDiemHoiDongService,
            AdminXemChamDiemService adminXemChamDiemService) {
        this.chamDiemHoiDongService = chamDiemHoiDongService;
        this.adminXemChamDiemService = adminXemChamDiemService;
    }

    // Mở danh sách nhóm phù hợp với vai trò đang đăng nhập.
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'ADMIN')")
    @GetMapping
    public String danhSach(
            Model model,
            Principal principal,
            HttpServletRequest request) {

        boolean laAdmin = request.isUserInRole("ADMIN");
        model.addAttribute("laAdmin", laAdmin);

        if (laAdmin) {
            // ADMIN xem các nhóm đã được phân công hội đồng.
            model.addAttribute(
                "danhSachNhom",
                adminXemChamDiemService.layDanhSachNhom()
            );
        } else {
            // Giảng viên chỉ thấy nhóm mình được phép chấm.
            model.addAttribute(
                "danhSachNhom",
                chamDiemHoiDongService.layDanhSachNhomDuocCham()
            );
        }

        model.addAttribute(
            "currentUserName",
            principal != null ? principal.getName() : "Người dùng"
        );

        return "assessment/cham-diem/list";
    }

    // ADMIN xem mọi phiếu đã lưu; giảng viên mở phiếu của mình.
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'ADMIN')")
    @GetMapping("/{maNhom}")
    public String moPhieu(
            @PathVariable("maNhom") String maNhom,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        if (request.isUserInRole("ADMIN")) {
            try {
                model.addAttribute(
                    "nhom",
                    adminXemChamDiemService.layThongTinNhom(maNhom)
                );
                model.addAttribute(
                    "danhSachPhieu",
                    adminXemChamDiemService.layPhieuChamTheoNhom(maNhom)
                );
                model.addAttribute(
                    "currentUserName",
                    principal != null ? principal.getName() : "Người dùng"
                );

                return "assessment/cham-diem/admin-detail";
            } catch (EntityNotFoundException ex) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ex.getMessage(),
                    ex
                );
            }
        }

        try {
            // Luồng cũ của giảng viên: kiểm tra nhóm được phép chấm.
            model.addAttribute(
                "nhom",
                chamDiemHoiDongService.layThongTinNhom(maNhom)
            );

            TrangThaiPhieuCham trangThaiPhieu =
                chamDiemHoiDongService.layTrangThaiPhieu(maNhom);

            model.addAttribute("trangThaiPhieu", trangThaiPhieu);
            model.addAttribute(
                "coTheChinhSua",
                trangThaiPhieu == TrangThaiPhieuCham.NHAP
            );

            if (!model.containsAttribute("phieuChamDiemForm")) {
                model.addAttribute(
                    "phieuChamDiemForm",
                    chamDiemHoiDongService.layFormChamDiem(maNhom)
                );
            }

            model.addAttribute(
                "currentUserName",
                principal != null ? principal.getName() : "Người dùng"
            );

            return "assessment/cham-diem/form";

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                ex.getMessage()
            );
            return "redirect:/cham-diem";
        }
    }

    @PreAuthorize("hasRole('GIANG_VIEN')")
    @PostMapping("/{maNhom}/luu-nhap")
    public String luuNhap(
            @PathVariable("maNhom") String maNhom,
            @Valid @ModelAttribute("phieuChamDiemForm")
                PhieuChamDiemForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // Điền mã nhóm vào đường dẫn chuyển hướng ở cuối hàm.
        redirectAttributes.addAttribute("maNhom", maNhom);

        // Dữ liệu sai định dạng hoặc không đạt điều kiện của Form.
        if (bindingResult.hasErrors()) {
            giuLaiFormVaLoi(form, bindingResult, redirectAttributes);

            return "redirect:/cham-diem/{maNhom}";
        }

        try {
            chamDiemHoiDongService.luuNhap(maNhom, form);

            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Lưu nháp phiếu chấm thành công."
            );

        } catch (IllegalArgumentException ex) {
            // Lỗi nghiệp vụ, ví dụ phiếu đã được nộp.
            giuLaiFormVaLoi(form, bindingResult, redirectAttributes);

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                ex.getMessage()
            );

        } catch (DataIntegrityViolationException
                | OptimisticLockingFailureException ex) {

            // Không hiển thị chi tiết lỗi SQL ra giao diện.
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Không thể lưu do dữ liệu bị xung đột hoặc không hợp lệ. "
                    + "Vui lòng kiểm tra lại phiếu trước khi thao tác tiếp."
            );
        }

        return "redirect:/cham-diem/{maNhom}";
    }

    @PreAuthorize("hasRole('GIANG_VIEN')")
    @PostMapping("/{maNhom}/nop")
    public String nopPhieu(
            @PathVariable("maNhom") String maNhom,
            @Valid @ModelAttribute("phieuChamDiemForm")
                PhieuChamDiemForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("maNhom", maNhom);

        // Khi nộp chính thức, điểm và nhận xét đều bắt buộc.
        // Không thêm lỗi "chưa nhập" nếu điểm đã có lỗi chuyển đổi.
        if (!bindingResult.hasFieldErrors("diem")
                && form.getDiem() == null) {

            bindingResult.rejectValue(
                "diem",
                "diem.required",
                "Bạn phải nhập điểm trước khi nộp phiếu."
            );
        }

        if (form.getNhanXet() == null
                || form.getNhanXet().isBlank()) {

            bindingResult.rejectValue(
                "nhanXet",
                "nhanXet.required",
                "Bạn phải nhập nhận xét trước khi nộp phiếu."
            );
        }

        // Quay lại form, giữ nội dung nhập và hiển thị lỗi từng trường.
        if (bindingResult.hasErrors()) {
            giuLaiFormVaLoi(form, bindingResult, redirectAttributes);

            return "redirect:/cham-diem/{maNhom}";
        }

        try {
            chamDiemHoiDongService.nopPhieu(maNhom, form);

            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Nộp phiếu chấm thành công. Phiếu không còn được chỉnh sửa."
            );

        } catch (IllegalArgumentException ex) {
            giuLaiFormVaLoi(form, bindingResult, redirectAttributes);

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                ex.getMessage()
            );

        } catch (DataIntegrityViolationException
                | OptimisticLockingFailureException ex) {

            // Đọc lại dữ liệu từ database sau khi chuyển hướng.
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Không thể nộp do dữ liệu bị xung đột hoặc không hợp lệ. "
                    + "Vui lòng kiểm tra lại trạng thái và nội dung phiếu."
            );
        }

        return "redirect:/cham-diem/{maNhom}";
    }

    // Giữ nội dung đã nhập và lỗi Validation qua lần chuyển hướng.
    private void giuLaiFormVaLoi(
            PhieuChamDiemForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
            "phieuChamDiemForm",
            form
        );

        redirectAttributes.addFlashAttribute(
            BindingResult.MODEL_KEY_PREFIX + "phieuChamDiemForm",
            bindingResult
        );
    }

}