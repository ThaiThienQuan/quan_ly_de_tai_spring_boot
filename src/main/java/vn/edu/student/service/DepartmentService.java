package vn.edu.student.service;

import org.springframework.stereotype.Service;
import vn.edu.student.entity.Department;
import vn.edu.student.repository.DepartmentRepository;

import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    public List<Department> findAll() {
        return repository.findAll();
    }

    public Department findById(Long departmentId) {
        return repository.findById(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ môn."));
    }

    public Department save(Department input) {
        String code = normalize(input.getDepartmentCode());
        String name = normalize(input.getDepartmentName());
        String description = normalize(input.getDescription());
        validate(code, name, description);

        boolean duplicateCode = input.getDepartmentId() == null
            ? repository.existsByDepartmentCodeIgnoreCase(code)
            : repository.existsByDepartmentCodeIgnoreCaseAndDepartmentIdNot(code, input.getDepartmentId());
        if (duplicateCode) {
            throw new IllegalArgumentException("Mã bộ môn đã tồn tại.");
        }

        Department department = input.getDepartmentId() == null
            ? new Department()
            : findById(input.getDepartmentId());
        department.setDepartmentCode(code);
        department.setDepartmentName(name);
        department.setDescription(description);
        return repository.save(department);
    }

    public void delete(Long departmentId) {
        repository.delete(findById(departmentId));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validate(String code, String name, String description) {
        if (code == null) {
            throw new IllegalArgumentException("Mã bộ môn không được để trống.");
        }
        if (code.length() > 20) {
            throw new IllegalArgumentException("Mã bộ môn không được vượt quá 20 ký tự.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Tên bộ môn không được để trống.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Tên bộ môn không được vượt quá 100 ký tự.");
        }
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("Mô tả không được vượt quá 500 ký tự.");
        }
    }
}
