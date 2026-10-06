package org.example.homeworkhub.assignment.dto;

import org.example.homeworkhub.assignment.Assignment;

public record AssignmentResponse(Long id, String title, String description) {

    public static AssignmentResponse from(Assignment assignment) {
        return new AssignmentResponse(assignment.getId(), assignment.getTitle(), assignment.getDescription());
    }
}
