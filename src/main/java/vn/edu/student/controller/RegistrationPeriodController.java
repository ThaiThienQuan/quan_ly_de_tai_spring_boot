package vn.edu.student.controller;

import vn.edu.student.entity.RegistrationPeriod;
import vn.edu.student.repository.UserAccountRepository;
import vn.edu.student.service.RegistrationPeriodService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/periods")
public class RegistrationPeriodController {
    private final RegistrationPeriodService service;
    private final UserAccountRepository userRepository;
    public RegistrationPeriodController(RegistrationPeriodService service, UserAccountRepository userRepository) {
        this.service = service; this.userRepository = userRepository;
    }
    @GetMapping
    public String list(Model model) {
        model.addAttribute("periods", service.findAll());
        return "periods/list";
    }
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("period", new RegistrationPeriod());
        return "periods/form";
    }
    @PostMapping
    public String create(@ModelAttribute RegistrationPeriod period, Authentication authentication,
                         RedirectAttributes redirectAttributes, Model model) {
        try {
            Long creatorId = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản đăng nhập."))
                .getUserId();
            service.create(period, creatorId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo đợt đăng ký ở trạng thái bản nháp.");
            return "redirect:/periods";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("period", period);
            model.addAttribute("errorMessage", exception.getMessage());
            return "periods/form";
        }
    }
    @PostMapping("/{periodId}/publish")
    public String publish(@PathVariable Long periodId, RedirectAttributes redirectAttributes) {
        try {
            service.publish(periodId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã công bố đợt đăng ký.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/periods";
    }
}
