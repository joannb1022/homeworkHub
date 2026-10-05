package org.example.homeworkhub.assignment;

import jakarta.persistence.*;
import org.example.homeworkhub.user.User;

import java.time.Instant;

@Entity
public class Assignment {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Long pdfFileKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false) //TODO how to name it better
    private User owner;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
