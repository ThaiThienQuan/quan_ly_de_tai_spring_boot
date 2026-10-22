package vn.edu.student.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.student.entity.Department;
import vn.edu.student.service.DepartmentService;

@Controller
@RequestMapping("/departments")
public class DepartmentController {
    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("departments", service.findAll());
        return "departments/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("department", new Department());
        model.addAttribute("pageTitle", "Thêm bộ môn");
        return "departments/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("department", service.findById(id));
        model.addAttribute("pageTitle", "Sửa bộ môn");
        return "departments/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("department") Department department, Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            service.save(department);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu bộ môn.");
            return "redirect:/departments";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("department", department);
            model.addAttribute("pageTitle", department.getDepartmentId() == null
                ? "Thêm bộ môn" : "Sửa bộ môn");
            model.addAttribute("errorMessage", exception.getMessage());
            return "departments/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bộ môn.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Không thể xóa bộ môn đang được giảng viên hoặc đề tài sử dụng.");
        }
        return "redirect:/departments";
    }
}
