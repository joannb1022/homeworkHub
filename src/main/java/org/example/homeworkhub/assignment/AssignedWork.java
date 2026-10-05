package org.example.homeworkhub.assignment;

import jakarta.persistence.*;
import org.example.homeworkhub.user.StudentProfile;

import java.time.Instant;

@Entity
public class AssignedWork {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private StudentProfile studentProfile;

    @Column(nullable = false)
    private Instant deadline;

    @Column(name= "assigned_at", nullable = false)
    private Instant assignedAt;

    @PrePersist
    protected void onCreate() {
        this.assignedAt = Instant.now();
    }

    public boolean isBeforeDeadline(Instant now){
        return now.isBefore(deadline);
    }
}
