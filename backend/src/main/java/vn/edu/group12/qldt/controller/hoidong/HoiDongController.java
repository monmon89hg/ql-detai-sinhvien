package vn.edu.group12.qldt.controller.hoidong;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.edu.group12.qldt.service.hoidong.HoiDongService;
import vn.edu.group12.qldt.dto.hoidong.HoiDongForm;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import vn.edu.group12.qldt.service.hoidong.ThanhVienHoiDongService;
import vn.edu.group12.qldt.dto.hoidong.ThanhVienHoiDongForm;
import vn.edu.group12.qldt.entity.hoidong.HoiDong;
import vn.edu.group12.qldt.enums.hoidong.ChucDanhHoiDong;
import vn.edu.group12.qldt.enums.hoidong.TrangThaiHoiDong;
import vn.edu.group12.qldt.service.hoidong.PhanCongHoiDongService;
import vn.edu.group12.qldt.dto.hoidong.PhanCongHoiDongForm;
import vn.edu.group12.qldt.service.hoidong.NguoiDungHienThiService;


@Controller
public class HoiDongController {

    private final HoiDongService hoiDongService;
    private final ThanhVienHoiDongService thanhVienHoiDongService;
    // Xử lý danh sách và nghiệp vụ phân công nhóm cho hội đồng.
    private final PhanCongHoiDongService phanCongHoiDongService;
    private final NguoiDungHienThiService nguoiDungHienThiService;

    public HoiDongController(
            HoiDongService hoiDongService,
            ThanhVienHoiDongService thanhVienHoiDongService,
            PhanCongHoiDongService phanCongHoiDongService,
            NguoiDungHienThiService nguoiDungHienThiService) {

        this.hoiDongService = hoiDongService;
        this.thanhVienHoiDongService = thanhVienHoiDongService;
        this.phanCongHoiDongService = phanCongHoiDongService;
        this.nguoiDungHienThiService = nguoiDungHienThiService;
    }

    @GetMapping("/hoi-dong")
    public String danhSach(
            Model model,
            Principal principal,
            HttpServletRequest request) {

        model.addAttribute(
            "currentUserName",
            nguoiDungHienThiService.layHoTenHienTai()
        );

        model.addAttribute(
            "danhSachHoiDong",
            hoiDongService.layDanhSach()
        );

        model.addAttribute(
            "coQuyenTaoHoiDong",
            request.isUserInRole("TRUONG_KHOA")
        );

        return "assessment/hoi-dong/list";
    }

    @GetMapping("/hoi-dong/{maHoiDong}")
    public String chiTiet(
            @PathVariable("maHoiDong") String maHoiDong,
            Model model,
            Principal principal,
            HttpServletRequest request) {

        try {
            HoiDong hoiDong = hoiDongService.layChiTiet(maHoiDong);

            model.addAttribute("hoiDong", hoiDong);

            // Luôn hiển thị các nhóm đã được giao cho hội đồng.
            model.addAttribute(
                "danhSachNhomDaPhanCong",
                phanCongHoiDongService.layDanhSachDaPhanCong(maHoiDong)
            );

            // Chỉ trưởng khoa được phân công khi hội đồng sẵn sàng.
            boolean coQuyenPhanCong =
                request.isUserInRole("TRUONG_KHOA")
                    && hoiDong.getTrangThai() == TrangThaiHoiDong.SAN_SANG;

            model.addAttribute("coQuyenPhanCong", coQuyenPhanCong);

            if (coQuyenPhanCong) {
                model.addAttribute(
                    "danhSachNhomCoThePhanCong",
                    phanCongHoiDongService
                        .layDanhSachCoThePhanCong(maHoiDong)
                );

                // Giữ lựa chọn cũ nếu lần gửi form trước có lỗi.
                if (!model.containsAttribute("phanCongHoiDongForm")) {
                    model.addAttribute(
                        "phanCongHoiDongForm",
                        new PhanCongHoiDongForm()
                    );
                }
            }


            model.addAttribute(
                "danhSachThanhVien",
                thanhVienHoiDongService.layDanhSach(maHoiDong)
            );

            boolean coQuyenThemThanhVien =
                request.isUserInRole("TRUONG_KHOA")
                    && hoiDong.getTrangThai() == TrangThaiHoiDong.NHAP;

            model.addAttribute(
                "coQuyenThemThanhVien",
                coQuyenThemThanhVien
            );

            model.addAttribute(
                "coQuyenXoaThanhVien",
                coQuyenThemThanhVien
            );

            model.addAttribute(
                "coQuyenXacNhanSanSang",
                coQuyenThemThanhVien
            );

            if (coQuyenThemThanhVien) {
                if (!model.containsAttribute("thanhVienHoiDongForm")) {
                    model.addAttribute(
                        "thanhVienHoiDongForm",
                        new ThanhVienHoiDongForm()
                    );
                }

                model.addAttribute(
                    "danhSachGiangVien",
                    thanhVienHoiDongService.layDanhSachGiangVien()
                );

                model.addAttribute(
                    "danhSachChucDanh",
                    ChucDanhHoiDong.values()
                );
            }
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Hội đồng không tồn tại.",
                ex
            );
        }

        model.addAttribute(
            "currentUserName",
            nguoiDungHienThiService.layHoTenHienTai()
        );

        return "assessment/hoi-dong/detail";
    }

    @GetMapping("/hoi-dong/tao")
    public String hienThiFormTao(Model model, Principal principal) {
        model.addAttribute(
            "currentUserName",
            nguoiDungHienThiService.layHoTenHienTai()
        );

        model.addAttribute("hoiDongForm", new HoiDongForm());

        model.addAttribute(
            "danhSachDot",
            hoiDongService.layDanhSachDot()
        );

        return "assessment/hoi-dong/form";
    }

    @PostMapping("/hoi-dong/tao")
    public String taoHoiDong(
            @Valid @ModelAttribute("hoiDongForm") HoiDongForm form,
            BindingResult bindingResult,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        model.addAttribute(
            "currentUserName",
            nguoiDungHienThiService.layHoTenHienTai()
        );

        model.addAttribute(
            "danhSachDot",
            hoiDongService.layDanhSachDot()
        );

        if (bindingResult.hasErrors()) {
            return "assessment/hoi-dong/form";
        }

        try {
            hoiDongService.taoHoiDong(form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "assessment/hoi-dong/form";
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute(
                "errorMessage",
                "Không thể lưu vì dữ liệu bị trùng hoặc dữ liệu tham chiếu "
                    + "đã thay đổi. Vui lòng kiểm tra và thử lại."
            );
            return "assessment/hoi-dong/form";
        }

        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Tạo hội đồng thành công."
        );

        return "redirect:/hoi-dong";
    }

    @PostMapping("/hoi-dong/{maHoiDong}/thanh-vien")
    public String themThanhVien(
            @PathVariable("maHoiDong") String maHoiDong,
            @Valid @ModelAttribute("thanhVienHoiDongForm")
                ThanhVienHoiDongForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("maHoiDong", maHoiDong);

        if (!bindingResult.hasErrors()) {
            try {
                thanhVienHoiDongService.themThanhVien(maHoiDong, form);

                redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thêm thành viên thành công."
                );

                return "redirect:/hoi-dong/{maHoiDong}";
            } catch (EntityNotFoundException ex) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Hội đồng không tồn tại.",
                    ex
                );
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
                );
            } catch (DataIntegrityViolationException ex) {
                redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể thêm thành viên vì dữ liệu bị trùng "
                        + "hoặc dữ liệu tham chiếu đã thay đổi."
                );
            }
        }

        redirectAttributes.addFlashAttribute(
            "thanhVienHoiDongForm",
            form
        );

        redirectAttributes.addFlashAttribute(
            BindingResult.MODEL_KEY_PREFIX + "thanhVienHoiDongForm",
            bindingResult
        );

        return "redirect:/hoi-dong/{maHoiDong}";
    }

    @PostMapping("/hoi-dong/{maHoiDong}/thanh-vien/{maGiangVien}/xoa")
    public String xoaThanhVien(
            @PathVariable("maHoiDong") String maHoiDong,
            @PathVariable("maGiangVien") String maGiangVien,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("maHoiDong", maHoiDong);

        try {
            thanhVienHoiDongService.xoaThanhVien(maHoiDong, maGiangVien);

            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Xóa thành viên thành công."
            );
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ex
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                ex.getMessage()
            );
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Không thể xóa thành viên vì có dữ liệu liên quan "
                    + "đang ràng buộc hoặc đã thay đổi."
            );
        }

        return "redirect:/hoi-dong/{maHoiDong}";
    }

    @PostMapping("/hoi-dong/{maHoiDong}/san-sang")
    public String chuyenSangSanSang(
            @PathVariable("maHoiDong") String maHoiDong,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("maHoiDong", maHoiDong);

        try {
            hoiDongService.chuyenSangSanSang(maHoiDong);

            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Hội đồng đã chuyển sang trạng thái sẵn sàng."
            );
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ex
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                ex.getMessage()
            );
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Không thể cập nhật trạng thái vì dữ liệu liên quan "
                    + "đã thay đổi hoặc không hợp lệ."
            );
        }

        return "redirect:/hoi-dong/{maHoiDong}";
    }

    @PostMapping("/hoi-dong/{maHoiDong}/phan-cong")
    public String phanCongHoiDong(
            @PathVariable("maHoiDong") String maHoiDong,
            @Valid @ModelAttribute("phanCongHoiDongForm")
                PhanCongHoiDongForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // Dùng mã này để tạo URL quay lại đúng hội đồng.
        redirectAttributes.addAttribute("maHoiDong", maHoiDong);

        if (!bindingResult.hasErrors()) {
            try {
                phanCongHoiDongService.phanCong(maHoiDong, form);

                redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Phân công hội đồng cho nhóm thành công."
                );

                return "redirect:/hoi-dong/{maHoiDong}";

            } catch (EntityNotFoundException ex) {
                // Hội đồng không tồn tại: trả HTTP 404.
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ex.getMessage(),
                    ex
                );

            } catch (IllegalArgumentException ex) {
                // Nhóm hoặc hội đồng không đáp ứng điều kiện nghiệp vụ.
                redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
                );

            } catch (DataIntegrityViolationException ex) {
                // Database chặn dữ liệu trùng hoặc tham chiếu không hợp lệ.
                redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể phân công vì dữ liệu đã thay đổi "
                        + "hoặc nhóm đã được phân công. "
                        + "Vui lòng tải lại trang và kiểm tra."
                );
            }
        }

        // Khi lỗi, giữ lại nhóm đã chọn và các lỗi Validation.
        redirectAttributes.addFlashAttribute(
            "phanCongHoiDongForm",
            form
        );

        redirectAttributes.addFlashAttribute(
            BindingResult.MODEL_KEY_PREFIX + "phanCongHoiDongForm",
            bindingResult
        );

        return "redirect:/hoi-dong/{maHoiDong}";
    }

}