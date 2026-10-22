package vn.edu.student.controller;

import vn.edu.student.service.ReviewService;
import vn.edu.student.service.RegistrationPeriodService;
import vn.edu.student.service.TopicService;
import vn.edu.student.repository.LecturerRepository;
import vn.edu.student.entity.ReviewResult;
import vn.edu.student.entity.Evaluation;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final RegistrationPeriodService periodService;
    private final TopicService topicService;
    private final LecturerRepository lecturerRepository;

    public ReviewController(ReviewService reviewService, RegistrationPeriodService periodService,
                            TopicService topicService, LecturerRepository lecturerRepository) {
        this.reviewService = reviewService;
        this.periodService = periodService;
        this.topicService = topicService;
        this.lecturerRepository = lecturerRepository;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        boolean manager = hasRole(authentication, "ADMIN") || hasRole(authentication, "TRUONG_KHOA");
        model.addAttribute("manager", manager);
        model.addAttribute("councils", reviewService.findAllCouncils());
        model.addAttribute("assignments", manager ? reviewService.findAllAssignments()
            : reviewService.findAssignmentsForLecturer(authentication.getName()));
        model.addAttribute("periods", periodService.findAll());
        model.addAttribute("topics", topicService.findPublished());
        model.addAttribute("lecturers", lecturerRepository.findAll());
        return "reviews/list";
    }

    @PostMapping("/councils")
    public String createCouncil(@RequestParam Long periodId, @RequestParam String councilCode,
                                @RequestParam String councilName, @RequestParam LocalDateTime reportDate,
                                @RequestParam Long chairmanId, @RequestParam Long secretaryId,
                                @RequestParam(required = false) List<Long> otherLecturerIds,
                                RedirectAttributes attributes) {
        try {
            reviewService.createCouncil(periodId, councilCode, councilName, reportDate,
                chairmanId, secretaryId, otherLecturerIds);
            attributes.addFlashAttribute("successMessage", "Đã lập hội đồng và ghi nhận các vai trò.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/reviews";
    }

    @PostMapping("/assignments")
    public String assign(@RequestParam Long councilId, @RequestParam Long topicId,
                         @RequestParam Long lecturerId, RedirectAttributes attributes) {
        try {
            reviewService.assignReviewer(councilId, topicId, lecturerId);
            attributes.addFlashAttribute("successMessage", "Đã phân công giảng viên phản biện.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/reviews";
    }

    @PostMapping("/assignments/{assignmentId}/evaluation")
    public String evaluate(Authentication authentication, @PathVariable Long assignmentId,
                           @RequestParam BigDecimal score, @RequestParam(required = false) String comment,
                           RedirectAttributes attributes) {
        try {
            reviewService.submitEvaluation(authentication.getName(), assignmentId, score, comment);
            attributes.addFlashAttribute("successMessage", "Đã lưu điểm và nhận xét phản biện.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/reviews";
    }

    @PostMapping("/councils/{councilId}/topics/{topicId}/publish")
    public String publishResult(Authentication authentication, @PathVariable Long councilId,
                                @PathVariable Long topicId, RedirectAttributes attributes) {
        try {
            reviewService.publishResult(authentication.getName(), councilId, topicId);
            attributes.addFlashAttribute("successMessage", "Đã tổng hợp và công bố điểm trung bình.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/reviews";
    }

    @GetMapping("/results")
    public String results(Authentication authentication, Model model) {
        List<ReviewResult> results = reviewService.findPublishedResultsForStudent(authentication.getName());
        Map<Long, List<Evaluation>> evaluationsByResult = new LinkedHashMap<>();
        for (ReviewResult result : results) {
            evaluationsByResult.put(result.getResultId(),
                reviewService.findEvaluationsForPublishedResult(authentication.getName(), result.getResultId()));
        }
        model.addAttribute("results", results);
        model.addAttribute("evaluationsByResult", evaluationsByResult);
        return "reviews/results";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
