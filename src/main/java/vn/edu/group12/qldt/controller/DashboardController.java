package vn.edu.group12.qldt.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("currentUserName", principal.getName());
        model.addAttribute(
            "successMessage",
            "Dashboard đã nhận dữ liệu từ backend!"
        );

        return "shared/dashboard";
    }
}