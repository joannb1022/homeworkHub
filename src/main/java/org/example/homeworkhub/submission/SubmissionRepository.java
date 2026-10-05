package org.example.homeworkhub.submission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Page<Submission> findByAssignmentId(Long assignmentId, Pageable pageable);
    Slice<Submission> findByStudentId(Long studentId, Pageable pageable);
}
