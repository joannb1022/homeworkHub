package org.example.homeworkhub.assignment;

import jakarta.persistence.*;
import org.example.homeworkhub.user.StudentProfile;

import java.time.Instant;

@Entity
public class AssignedWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile studentProfile;

    @Column(nullable = false)
    private Instant deadline;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    protected AssignedWork() {
    }

    public AssignedWork(Assignment assignment, StudentProfile studentProfile, Instant deadline) {
        this.assignment = assignment;
        this.studentProfile = studentProfile;
        this.deadline = deadline;
    }

    @PrePersist
    protected void onCreate() {
        if (this.assignedAt == null) {
            this.assignedAt = Instant.now();
        }
    }

    public boolean isBeforeDeadline(Instant now) {
        return now.isBefore(deadline);
    }
}