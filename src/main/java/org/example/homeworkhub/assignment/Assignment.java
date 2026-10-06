package org.example.homeworkhub.assignment;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Long pdfFileKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "owner_user_id", nullable = false) //TODO how to name it better
//    private User owner;

    public Assignment(String title, String description, Long pdfFileKey) {
        this.title = title;
        this.description = description;
        this.pdfFileKey = pdfFileKey;
//        this.owner = owner;
    }

    public Assignment() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getPdfFileKey() {
        return pdfFileKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

//    public User getOwner() {
//        return owner;
//    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
