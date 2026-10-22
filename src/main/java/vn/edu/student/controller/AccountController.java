package vn.edu.student.controller;

import vn.edu.student.entity.UserAccount;
import vn.edu.student.repository.UserAccountRepository;
import vn.edu.student.service.AccountService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {
    private final AccountService accountService;
    private final UserAccountRepository userRepository;

    public AccountController(AccountService accountService, UserAccountRepository userRepository) {
        this.accountService = accountService;
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String fullName,
                           @RequestParam String email, @RequestParam(required = false) String phone,
                           @RequestParam String studentCode, @RequestParam String className,
                           @RequestParam String major, @RequestParam String password,
                           @RequestParam String confirmPassword, RedirectAttributes attributes) {
        try {
            accountService.registerStudent(username, fullName, email, phone, studentCode,
                className, major, password, confirmPassword);
            attributes.addFlashAttribute("successMessage", "Đăng ký thành công. Bạn có thể đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/account")
    public String account(Authentication authentication, Model model) {
        model.addAttribute("account", findCurrentAccount(authentication));
        return "account/profile";
    }

    @PostMapping("/account")
    public String updateAccount(Authentication authentication, @RequestParam String fullName,
                                @RequestParam String email, @RequestParam(required = false) String phone,
                                RedirectAttributes attributes) {
        try {
            accountService.updateProfile(authentication.getName(), fullName, email, phone);
            attributes.addFlashAttribute("successMessage", "Thông tin tài khoản đã được cập nhật.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/account";
    }

    @GetMapping("/account/password")
    public String passwordPage() {
        return "account/password";
    }

    @PostMapping("/account/password")
    public String updatePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes attributes) {
        try {
            accountService.changePassword(authentication.getName(), currentPassword,
                newPassword, confirmPassword);
            attributes.addFlashAttribute("successMessage", "Mật khẩu đã được đổi thành công.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/account/password";
    }

    private UserAccount findCurrentAccount(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản đang đăng nhập."));
    }
}
