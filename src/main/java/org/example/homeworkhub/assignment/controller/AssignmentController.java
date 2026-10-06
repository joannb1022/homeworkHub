package org.example.homeworkhub.assignment.controller;

import jakarta.validation.Valid;
import org.example.homeworkhub.assignment.dto.AssignmentResponse;
import org.example.homeworkhub.assignment.dto.CreateAssignmentRequest;
import org.example.homeworkhub.assignment.service.AssignmentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    //TODO add security to get user (ONLY TEACHER CAN CREATE ASSIGNMENT)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentResponse> create(@Valid @ModelAttribute CreateAssignmentRequest request) {
            AssignmentResponse created = assignmentService.create(request);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(created.id())
                    .toUri();
            return ResponseEntity.created(location).body(created); // 201 + Location header
        }
    }
