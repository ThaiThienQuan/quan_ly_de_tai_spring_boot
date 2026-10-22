package vn.edu.student.service;

import vn.edu.student.entity.Council;

import java.util.List;

public record LecturerCouncilView(Council council, String periodName, List<Member> members) {
    public record Member(String lecturerCode, String fullName, String role) {
    }
}
