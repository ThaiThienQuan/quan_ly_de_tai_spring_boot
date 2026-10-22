package vn.edu.student.controller;

import vn.edu.student.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        boolean manager = authentication.getAuthorities().stream().anyMatch(authority ->
            authority.getAuthority().equals("ROLE_ADMIN") || authority.getAuthority().equals("ROLE_TRUONG_KHOA"));
        model.addAttribute("manager", manager);
        model.addAttribute("notifications", notificationService.findForUser(authentication.getName(), manager));
        model.addAttribute("roles", List.of("TRUONG_KHOA", "CHU_TICH_HOI_DONG", "GIANG_VIEN", "SINH_VIEN"));
        return "notifications/list";
    }

    @PostMapping
    public String create(Authentication authentication, @RequestParam String title, @RequestParam String content,
                         @RequestParam(defaultValue = "SYSTEM") String type,
                         @RequestParam List<String> targetRoles, RedirectAttributes attributes) {
        try {
            notificationService.create(authentication.getName(), title, content, type, targetRoles);
            attributes.addFlashAttribute("successMessage", "Đã lưu thông báo ở trạng thái bản nháp.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/notifications";
    }

    @PostMapping("/{notificationId}/publish")
    public String publish(@PathVariable Long notificationId, RedirectAttributes attributes) {
        try {
            notificationService.publish(notificationId);
            attributes.addFlashAttribute("successMessage", "Đã đăng tải thông báo.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/notifications";
    }
}
