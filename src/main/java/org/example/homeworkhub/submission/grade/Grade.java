package org.example.homeworkhub.submission.grade;

import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Embeddable
public record Grade(int value, String feedback, Instant gradedAt) {

    public Grade {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }

        Objects.requireNonNull(gradedAt, "gradedAt");
        if (feedback != null && feedback.isBlank()) {
            feedback = null;
        }
    }

    public static Grade of(int value, String feedback, Instant now) {
        return new Grade(value, feedback, now);
    }

    public Optional<String> findFeedback() {
        return Optional.ofNullable(feedback);
    }
}