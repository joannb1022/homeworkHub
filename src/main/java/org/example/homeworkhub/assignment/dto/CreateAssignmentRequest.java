package org.example.homeworkhub.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CreateAssignmentRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotNull MultipartFile file) { }