package vn.edu.student.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.student.entity.Department;
import vn.edu.student.repository.DepartmentRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {
    @Mock
    private DepartmentRepository repository;

    @InjectMocks
    private DepartmentService service;

    @Test
    void createsDepartmentWithNormalizedFieldsAndActiveStatus() {
        when(repository.save(any(Department.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        Department input = department(null, "  CNTT  ", "  Công nghệ thông tin  ");
        input.setDescription("  Khoa học máy tính  ");

        Department saved = service.save(input);

        assertEquals("CNTT", saved.getDepartmentCode());
        assertEquals("Công nghệ thông tin", saved.getDepartmentName());
        assertEquals("Khoa học máy tính", saved.getDescription());
        assertEquals("ACTIVE", saved.getStatus());
        verify(repository).existsByDepartmentCodeIgnoreCase("CNTT");
        verify(repository).save(saved);
    }

    @Test
    void rejectsMissingCodeOrNameBeforeSaving() {
        Department missingCode = department(null, " ", "Tên");
        assertThrows(IllegalArgumentException.class, () -> service.save(missingCode));

        Department missingName = department(null, "CNTT", " ");
        assertThrows(IllegalArgumentException.class, () -> service.save(missingName));

        Department overlongCode = department(null, "C".repeat(21), "Tên");
        assertThrows(IllegalArgumentException.class, () -> service.save(overlongCode));

        Department overlongName = department(null, "CNTT", "T".repeat(101));
        assertThrows(IllegalArgumentException.class, () -> service.save(overlongName));

        Department overlongDescription = department(null, "CNTT", "Tên");
        overlongDescription.setDescription("D".repeat(501));
        assertThrows(IllegalArgumentException.class, () -> service.save(overlongDescription));

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsDuplicateDepartmentCode() {
        when(repository.existsByDepartmentCodeIgnoreCase("CNTT")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.save(department(null, "CNTT", "Công nghệ thông tin")));

        assertEquals("Mã bộ môn đã tồn tại.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void updatesExistingDepartmentWithoutReplacingItsStatus() {
        Department existing = department(3L, "OLD", "Tên cũ");
        existing.setStatus("INACTIVE");
        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Department.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Department updated = service.save(department(3L, "NEW", "Tên mới"));

        assertSame(existing, updated);
        assertEquals("NEW", updated.getDepartmentCode());
        assertEquals("Tên mới", updated.getDepartmentName());
        assertEquals("INACTIVE", updated.getStatus());
        verify(repository).existsByDepartmentCodeIgnoreCaseAndDepartmentIdNot("NEW", 3L);
        verify(repository).save(existing);
    }

    @Test
    void deletesExistingDepartment() {
        Department existing = department(3L, "CNTT", "Công nghệ thông tin");
        when(repository.findById(3L)).thenReturn(Optional.of(existing));

        service.delete(3L);

        verify(repository).delete(existing);
    }

    @Test
    void reportsMissingDepartmentWhenEditingOrDeleting() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.save(department(404L, "CNTT", "Công nghệ thông tin")));
        assertThrows(IllegalArgumentException.class, () -> service.delete(404L));
        verify(repository, never()).delete(any(Department.class));
    }

    private Department department(Long id, String code, String name) {
        Department department = new Department();
        department.setDepartmentId(id);
        department.setDepartmentCode(code);
        department.setDepartmentName(name);
        return department;
    }
}
