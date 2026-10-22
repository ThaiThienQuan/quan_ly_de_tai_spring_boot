package vn.edu.student.controller;

import vn.edu.student.entity.Topic;
import vn.edu.student.repository.LecturerRepository;
import vn.edu.student.repository.UserAccountRepository;
import vn.edu.student.service.TopicService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/topics")
public class TopicController {
    private final TopicService topicService;
    private final LecturerRepository lecturerRepository;
    private final UserAccountRepository userAccountRepository;

    public TopicController(TopicService topicService, LecturerRepository lecturerRepository,
                           UserAccountRepository userAccountRepository) {
        this.topicService = topicService;
        this.lecturerRepository = lecturerRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        boolean student = hasRole(authentication, "SINH_VIEN");
        model.addAttribute("topics", student ? topicService.findPublished() : topicService.findAll());
        model.addAttribute("canCreate", hasRole(authentication, "GIANG_VIEN") || hasRole(authentication, "ADMIN"));
        model.addAttribute("canReview", hasRole(authentication, "TRUONG_KHOA") || hasRole(authentication, "ADMIN"));
        return "topics/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("topic", new Topic());
        model.addAttribute("lecturers", lecturerRepository.findAll());
        return "topics/form";
    }

    @PostMapping
    public String create(@ModelAttribute Topic topic, Authentication authentication,
                         @RequestParam(required = false) Long coSupervisorId,
                         Model model, RedirectAttributes redirectAttributes) {
        try {
            Long userId = userAccountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."))
                .getUserId();
            Long lecturerId = lecturerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản này chưa có hồ sơ giảng viên."))
                .getLecturerId();
            topic.setCreatedByLecturerId(lecturerId);
            topicService.create(topic, coSupervisorId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi đề tài chờ phê duyệt.");
            return "redirect:/topics";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("topic", topic);
            model.addAttribute("lecturers", lecturerRepository.findAll());
            model.addAttribute("errorMessage", exception.getMessage());
            return "topics/form";
        }
    }

    @PostMapping("/{topicId}/approve")
    public String approve(@PathVariable Long topicId, RedirectAttributes attributes) {
        try { topicService.approve(topicId); attributes.addFlashAttribute("successMessage", "Đã duyệt đề tài."); }
        catch (IllegalArgumentException exception) { attributes.addFlashAttribute("errorMessage", exception.getMessage()); }
        return "redirect:/topics";
    }

    @PostMapping("/{topicId}/publish")
    public String publish(@PathVariable Long topicId, RedirectAttributes attributes) {
        try { topicService.publish(topicId); attributes.addFlashAttribute("successMessage", "Đã công bố đề tài."); }
        catch (IllegalArgumentException exception) { attributes.addFlashAttribute("errorMessage", exception.getMessage()); }
        return "redirect:/topics";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
