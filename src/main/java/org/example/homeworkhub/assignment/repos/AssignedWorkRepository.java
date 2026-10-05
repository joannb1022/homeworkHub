package org.example.homeworkhub.assignment.repos;

import org.example.homeworkhub.assignment.AssignedWork;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignedWorkRepository extends JpaRepository<AssignedWork, Long> {
    Page<AssignedWork> findByAssignmentId(Long assignmentId, Pageable pageable);
    Slice<AssignedWork> findByOwner(Long owner, Pageable pageable);
}
