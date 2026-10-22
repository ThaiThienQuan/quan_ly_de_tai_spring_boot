package vn.edu.student.controller;

import vn.edu.student.service.TopicService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final TopicService topicService;
    public HomeController(TopicService topicService) {
        this.topicService = topicService;
    }
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        model.addAttribute("topics", topicService.findPublished());
        boolean admin = hasRole(authentication, "ADMIN");
        boolean facultyLead = hasRole(authentication, "TRUONG_KHOA");
        boolean lecturer = hasRole(authentication, "GIANG_VIEN") || hasRole(authentication, "CHU_TICH_HOI_DONG");
        boolean student = hasRole(authentication, "SINH_VIEN");
        model.addAttribute("canManagePeriods", admin || facultyLead);
        model.addAttribute("canManageGroups", admin || facultyLead || student);
        model.addAttribute("canManageReviews", admin || facultyLead || lecturer);
        model.addAttribute("canViewResults", student);
        model.addAttribute("canManageUsers", admin);
        model.addAttribute("canViewNotifications", true);
        return "home";
    }
    @GetMapping("/login")
    public String login() { return "login"; }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
