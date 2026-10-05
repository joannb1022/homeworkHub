package org.example.homeworkhub.submission;

import jakarta.persistence.*;
import org.example.homeworkhub.assignment.AssignedWork;
import org.example.homeworkhub.submission.grade.Grade;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "is_late", nullable = false)
    private boolean late;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_work_id", nullable = false, unique = true)
    private AssignedWork assignedWork;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "grade_value")),
            @AttributeOverride(name = "feedback", column = @Column(name = "grade_feedback", length = 2000)),
            @AttributeOverride(name = "gradedAt", column = @Column(name = "graded_at"))
    })
    private Grade grade;

    protected Submission() { }

    public Submission(AssignedWork assignedWork, Instant submittedAt) {
        this.assignedWork = Objects.requireNonNull(assignedWork, "assignedWork");
        this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
        this.late = !assignedWork.isBeforeDeadline(submittedAt);
    }

    public void grade(int value, String feedback, Instant now) {
        this.grade = new Grade(value, feedback, now);
    }

    public boolean isGraded() { return grade != null; }
    public Optional<Grade> findGrade() { return Optional.ofNullable(grade); }

    public Long getId() { return id; }
    public Instant getSubmittedAt() { return submittedAt; }
    public boolean isLate() { return late; }
    public AssignedWork getAssignedWork() { return assignedWork; }
}