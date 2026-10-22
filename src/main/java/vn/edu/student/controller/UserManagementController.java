package vn.edu.student.controller;

import vn.edu.student.service.UserManagementService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserManagementController {
    private final UserManagementService userService;

    public UserManagementController(UserManagementService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", List.of("ADMIN", "TRUONG_KHOA", "CHU_TICH_HOI_DONG", "GIANG_VIEN", "SINH_VIEN"));
        return "users/list";
    }

    @GetMapping("/{userId}/password")
    public String passwordPage(@PathVariable Long userId, Model model, RedirectAttributes attributes) {
        try {
            model.addAttribute("account", userService.findById(userId));
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/users";
        }
        return "users/reset-password";
    }

    @PostMapping("/{userId}")
    public String update(@PathVariable Long userId, @RequestParam String fullName,
                         @RequestParam String email, @RequestParam(required = false) String phone,
                         @RequestParam String role, @RequestParam String status, Authentication authentication,
                         RedirectAttributes attributes) {
        try {
            userService.updateAccount(userId, fullName, email, phone, role, status, authentication.getName());
            attributes.addFlashAttribute("successMessage", "Đã cập nhật thông tin và quyền tài khoản.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{userId}/password")
    public String resetPassword(@PathVariable Long userId, @RequestParam String newPassword,
                                @RequestParam String confirmPassword, RedirectAttributes attributes) {
        try {
            userService.resetPassword(userId, newPassword, confirmPassword);
            attributes.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu cho tài khoản.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{userId}/delete")
    public String delete(@PathVariable Long userId, Authentication authentication,
                         RedirectAttributes attributes) {
        try {
            userService.deleteUser(userId, authentication.getName());
            attributes.addFlashAttribute("successMessage", "Đã xóa tài khoản.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            attributes.addFlashAttribute("errorMessage",
                "Không thể xóa tài khoản vì đang được dữ liệu khác tham chiếu. Hãy khóa tài khoản thay thế.");
        }
        return "redirect:/users";
    }
}
