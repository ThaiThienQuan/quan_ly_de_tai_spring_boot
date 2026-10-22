package vn.edu.student.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class NavigationModelAdvice {
    @ModelAttribute("canManagePeriods")
    public boolean canManagePeriods(Authentication authentication) {
        return hasRole(authentication, "ADMIN") || hasRole(authentication, "TRUONG_KHOA");
    }

    @ModelAttribute("canManageGroups")
    public boolean canManageGroups(Authentication authentication) {
        return hasRole(authentication, "ADMIN") || hasRole(authentication, "TRUONG_KHOA")
            || hasRole(authentication, "SINH_VIEN");
    }

    @ModelAttribute("canManageReviews")
    public boolean canManageReviews(Authentication authentication) {
        return hasRole(authentication, "ADMIN") || hasRole(authentication, "TRUONG_KHOA")
            || hasRole(authentication, "GIANG_VIEN") || hasRole(authentication, "CHU_TICH_HOI_DONG");
    }

    @ModelAttribute("canViewResults")
    public boolean canViewResults(Authentication authentication) {
        return hasRole(authentication, "SINH_VIEN");
    }

    @ModelAttribute("canManageUsers")
    public boolean canManageUsers(Authentication authentication) {
        return hasRole(authentication, "ADMIN");
    }

    @ModelAttribute("currentUsername")
    public String currentUsername(Authentication authentication) {
        return authentication == null ? "" : authentication.getName();
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
