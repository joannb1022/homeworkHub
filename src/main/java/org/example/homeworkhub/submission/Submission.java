package org.example.homeworkhub.submission;

import jakarta.persistence.*;
import org.example.homeworkhub.assignment.AssignedWork;
import org.example.homeworkhub.common.error.DeadlinePassedException;
import org.example.homeworkhub.submission.grade.Grade;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private List<SubmissionFile> files = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "grade_value")),
            @AttributeOverride(name = "feedback", column = @Column(name = "grade_feedback", length = 2000)),
            @AttributeOverride(name = "gradedAt", column = @Column(name = "graded_at"))
    })
    private Grade grade;

    public Submission(AssignedWork assignedWork, List<SubmissionFile> files, Instant submittedAt) {
        this.assignedWork = Objects.requireNonNull(assignedWork, "assignedWork");
        this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
        this.late = !assignedWork.isBeforeDeadline(submittedAt);
        applyFiles(files);
    }

    protected Submission() {
    }

    public List<String> resubmit(List<SubmissionFile> newFiles, Instant now) {
        if (isGraded()) {
            throw new IllegalStateException("A graded submission cannot be resubmitted");
        }
        if (!assignedWork.isBeforeDeadline(now)) {
            throw new DeadlinePassedException("The deadline has passed");
        }
        List<String> oldKeys = files.stream().map(SubmissionFile::getStorageKey).toList();
        applyFiles(newFiles);
        this.submittedAt = now;
        this.late = false;
        return oldKeys;
    }

    public void grade(int value, String feedback, Instant now) {
        this.grade = new Grade(value, feedback, now);
    }

    public boolean isGraded() {
        return grade != null;
    }

    public Optional<Grade> findGrade() {
        return Optional.ofNullable(grade);
    }


    private void applyFiles(List<SubmissionFile> newFiles) {
        validateFiles(newFiles);
        files.clear();
        for (SubmissionFile f : newFiles) {
            f.attachTo(this);
            files.add(f);
        }
    }

    private void validateFiles(List<SubmissionFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("A submission needs at least one file");
        }

        boolean hasPdf = files.stream().anyMatch(f -> f.getFileType().isPDF());
        if (hasPdf && files.size() > 1) {
            throw new IllegalArgumentException("A PDF submission can have only one file");
        }
        Set<Integer> positions = files.stream().map(SubmissionFile::getPosition).collect(Collectors.toSet());
        boolean complete = positions.size() == files.size()
                && positions.stream().allMatch(p -> p >= 1 && p <= files.size());
        if (!complete) {
            throw new IllegalArgumentException("File positions must be exactly 1 to " + files.size());
        }
    }

    public Long getId() {
        return id;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public boolean isLate() {
        return late;
    }

    public AssignedWork getAssignedWork() {
        return assignedWork;
    }

    public List<SubmissionFile> getFiles() {
        return files;
    }

}