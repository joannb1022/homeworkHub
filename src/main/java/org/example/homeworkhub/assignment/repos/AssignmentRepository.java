package org.example.homeworkhub.assignment.repos;

import org.example.homeworkhub.assignment.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Page<Assignment> findByAssignmentId(Long assignmentId, Pageable pageable);
    Slice<Assignment> findByOwner(Long owner, Pageable pageable);
}
