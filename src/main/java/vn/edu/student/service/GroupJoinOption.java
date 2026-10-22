package vn.edu.student.service;

import vn.edu.student.entity.GroupMember;
import vn.edu.student.entity.ProjectGroup;

import java.util.List;

public record GroupJoinOption(ProjectGroup group, int memberCount, List<GroupMember> members,
                              boolean canJoin, String unavailableReason) {
}
