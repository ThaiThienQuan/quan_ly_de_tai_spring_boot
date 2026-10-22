package vn.edu.student.controller;

import vn.edu.student.entity.ProjectGroup;
import vn.edu.student.entity.GroupMember;
import vn.edu.student.entity.ProjectReport;
import vn.edu.student.service.ProjectGroupService;
import vn.edu.student.service.RegistrationPeriodService;
import vn.edu.student.service.TopicService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/groups")
public class ProjectGroupController {
    private final ProjectGroupService groupService;
    private final RegistrationPeriodService periodService;
    private final TopicService topicService;

    public ProjectGroupController(ProjectGroupService groupService, RegistrationPeriodService periodService,
                                  TopicService topicService) {
        this.groupService = groupService;
        this.periodService = periodService;
        this.topicService = topicService;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        boolean manager = hasRole(authentication, "ADMIN") || hasRole(authentication, "TRUONG_KHOA");
        List<ProjectGroup> groups = manager ? groupService.findAllGroups()
            : groupService.findGroupsForUser(authentication.getName());
        Map<Long, List<GroupMember>> membersByGroup = new LinkedHashMap<>();
        Map<Long, List<ProjectReport>> reportsByGroup = new LinkedHashMap<>();
        Set<Long> leaderGroupIds = manager ? Set.of() : groups.stream()
            .filter(group -> groupService.isLeaderOf(authentication.getName(), group.getGroupId()))
            .map(ProjectGroup::getGroupId).collect(Collectors.toSet());
        if (manager) {
            for (ProjectGroup group : groups) membersByGroup.put(group.getGroupId(), groupService.findMembers(group.getGroupId()));
        }
        if (!manager) {
            for (ProjectGroup group : groups) {
                membersByGroup.put(group.getGroupId(), groupService.findMembers(group.getGroupId()));
                reportsByGroup.put(group.getGroupId(),
                    groupService.findReportsForGroup(group.getGroupId(), authentication.getName()));
            }
        }
        model.addAttribute("groups", groups);
        model.addAttribute("periods", periodService.findAll());
        model.addAttribute("topics", topicService.findPublished());
        model.addAttribute("pendingRegistrations", manager ? groupService.findPendingRegistrations() : List.of());
        model.addAttribute("membersByGroup", membersByGroup);
        model.addAttribute("reportsByGroup", reportsByGroup);
        model.addAttribute("leaderGroupIds", leaderGroupIds);
        model.addAttribute("manager", manager);
        return "groups/list";
    }

    @PostMapping
    public String create(Authentication authentication, @RequestParam Long periodId,
                         @RequestParam String groupCode, @RequestParam String groupName,
                         @RequestParam(required = false) List<String> memberCodes,
                         RedirectAttributes attributes) {
        try {
            groupService.createGroup(authentication.getName(), periodId, groupCode, groupName, memberCodes);
            attributes.addFlashAttribute("successMessage", "Đã tạo nhóm và ghi nhận nhóm trưởng.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/groups";
    }

    @PostMapping("/{groupId}/register")
    public String register(Authentication authentication, @PathVariable Long groupId,
                           @RequestParam Long topicId, RedirectAttributes attributes) {
        try {
            groupService.registerTopic(authentication.getName(), groupId, topicId);
            attributes.addFlashAttribute("successMessage", "Đã gửi đăng ký đề tài, chờ Trưởng khoa phê duyệt.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/groups";
    }

    @PostMapping("/{groupId}/reports")
    public String submitReport(Authentication authentication, @PathVariable Long groupId,
                               @RequestParam String title, @RequestParam String fileUrl,
                               @RequestParam(required = false) String description,
                               RedirectAttributes attributes) {
        try {
            groupService.submitReport(authentication.getName(), groupId, title, fileUrl, description);
            attributes.addFlashAttribute("successMessage", "Đã nộp báo cáo phiên bản mới.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/groups";
    }

    @PostMapping("/registrations/{registrationId}/approve")
    public String approveRegistration(Authentication authentication, @PathVariable Long registrationId,
                                      RedirectAttributes attributes) {
        try {
            groupService.reviewRegistration(authentication.getName(), registrationId, true, null);
            attributes.addFlashAttribute("successMessage", "Đã phê duyệt đăng ký đề tài.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/groups";
    }

    @PostMapping("/registrations/{registrationId}/reject")
    public String rejectRegistration(Authentication authentication, @PathVariable Long registrationId,
                                     @RequestParam String reason, RedirectAttributes attributes) {
        try {
            groupService.reviewRegistration(authentication.getName(), registrationId, false, reason);
            attributes.addFlashAttribute("successMessage", "Đã từ chối đăng ký đề tài.");
        } catch (IllegalArgumentException exception) {
            attributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/groups";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
