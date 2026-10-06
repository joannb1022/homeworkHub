package org.example.homeworkhub.submission;

import jakarta.persistence.*;

@Entity
@Table(name = "submission_files")
public class SubmissionFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private int position;

    protected SubmissionFile() {
    }

    public SubmissionFile(String storageKey,
                          String originalFilename, FileType fileType,
                          long sizeBytes, int position) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("storageKey must not be blank");
        }
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("originalFilename must not be blank");
        }
        if (sizeBytes <= 0) {
            throw new IllegalArgumentException("sizeBytes must be positive");
        }
        if (position < 1) {
            throw new IllegalArgumentException("position must be at least 1");
        }

        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.sizeBytes = sizeBytes;
        this.position = position;
    }

    public void attachTo(Submission submission) {
        this.submission = submission;
    }

    public FileType getFileType() {
        return fileType;
    }

    public int getPosition() {
        return position;
    }

    public String getStorageKey() {
        return storageKey;
    }
}

