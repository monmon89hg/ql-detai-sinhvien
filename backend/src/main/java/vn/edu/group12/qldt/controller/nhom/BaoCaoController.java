package vn.edu.group12.qldt.controller.nhom;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.edu.group12.qldt.service.nhom.*;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/bao-cao")
public class BaoCaoController {
    private final BaoCaoService service;
    private final NhomBaoCaoViewService views;

    public BaoCaoController(BaoCaoService service, NhomBaoCaoViewService views) {
        this.service = service;
        this.views = views;
    }

    @GetMapping
    String danhSach(
            @RequestParam(required = false) String maNhom,
            @RequestParam(defaultValue = "0") int trang,
            @RequestParam(defaultValue = "") String tuKhoa,
            @RequestParam(defaultValue = "") String maDot,
            @RequestParam(defaultValue = "") String trangThaiNop,
            @RequestParam(defaultValue = "0") int trangNhom,
            Model model) {
        model.addAllAttributes(views.baoCao(
                maNhom, trang, tuKhoa, maDot, trangThaiNop, trangNhom));
        return "reports/list";
    }

    @GetMapping("/nop")
    String form(Model model) {
        model.addAllAttributes(views.baoCao(null, 0));
        return "reports/form";
    }

    @PostMapping("/nop")
    String nop(@RequestParam MultipartFile tepBaoCao, RedirectAttributes f) {
        var bc = service.nop(tepBaoCao);
        f.addFlashAttribute("successMessage", "Đã lưu báo cáo phiên bản " + bc.phienBan() + ".");
        return "redirect:/bao-cao";
    }

    @GetMapping("/{maNhom}/{phienBan}/tai")
    ResponseEntity<byte[]> tai(@PathVariable String maNhom, @PathVariable int phienBan) {
        var tep = service.tai(maNhom, phienBan);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(tep.bytes().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(tep.baoCao().tenFileGoc(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(tep.bytes());
    }
}
