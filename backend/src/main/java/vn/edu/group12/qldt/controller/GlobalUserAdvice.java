package vn.edu.group12.qldt.controller;

import java.security.Principal;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import vn.edu.group12.qldt.entity.admin.NguoiDung;
import vn.edu.group12.qldt.repository.admin.NguoiDungRepository;

/**
 * Cung cấp Họ và tên thật của người dùng đang đăng nhập cho toàn bộ giao diện hệ thống.
 */
@ControllerAdvice
@Component("userAdvice")
public class GlobalUserAdvice {

    private final NguoiDungRepository nguoiDungRepository;

    public GlobalUserAdvice(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @ModelAttribute("currentUserName")
    public String currentUserName(Principal principal) {
        return resolveName(principal != null ? principal.getName() : null);
    }

    public String resolveName(String identifier) {
        if (identifier == null || identifier.isBlank() || "Người dùng".equals(identifier) || "Tài khoản".equals(identifier)) {
            return "Tài khoản";
        }
        String clean = identifier.trim();
        return nguoiDungRepository.findByEmailIgnoreCase(clean)
                .or(() -> nguoiDungRepository.findById(clean))
                .map(NguoiDung::getHoTen)
                .orElse(clean);
    }
}
