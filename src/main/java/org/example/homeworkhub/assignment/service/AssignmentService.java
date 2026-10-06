package org.example.homeworkhub.assignment.service;

import org.example.homeworkhub.assignment.Assignment;
import org.example.homeworkhub.assignment.dto.AssignmentResponse;
import org.example.homeworkhub.assignment.dto.CreateAssignmentRequest;
import org.example.homeworkhub.assignment.repos.AssignmentRepository;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    private AssignmentRepository assignmentRepository;

    public AssignmentService(AssignmentRepository repository) {
        this.assignmentRepository = repository;
    }

    //TODO add file handling (check if pdf, save it)
    public AssignmentResponse create(CreateAssignmentRequest request) {
        Assignment saved = assignmentRepository.save(new Assignment(
                request.title(),
                request.description(),
                55L // TODO add file handling
        ));

        return AssignmentResponse.from(saved);
    }

    public void createAssignedWorkForStudent(Assignment assignment, Long studentId) {

    }

}
