package org.example.homeworkhub.submission;

import org.example.homeworkhub.assignment.AssignedWork;
import org.example.homeworkhub.assignment.Assignment;
import org.example.homeworkhub.user.StudentProfile;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;


class SubmissionTest {

    Assignment assignment = new Assignment();
    StudentProfile studentProfile = new StudentProfile();

    @Test
    void shouldBeLateWhenSubmittedAfterDeadline() {
        AssignedWork assignedWork = new AssignedWork(assignment, studentProfile, Instant.now().minusSeconds(60));
        Instant submittedAt = Instant.now();
        Submission submission = new Submission(assignedWork, submittedAt);

        assertThat(submission.isLate()).isTrue();
    }

    @Test
    void shouldNotBeLateWhenSubmittedBeforeDeadline() {
        AssignedWork assignedWork = new AssignedWork(assignment, studentProfile, Instant.now().plusSeconds(60));
        Submission submission = new Submission(assignedWork, Instant.now());

        assertThat(submission.isLate()).isFalse();
    }

    @Test
    void gradeCannotBeNegativeNumber() {
        StudentProfile studentProfile = new StudentProfile();
        AssignedWork assignedWork = new AssignedWork(assignment, studentProfile, Instant.now().plusSeconds(60));
        Submission submission = new Submission(assignedWork, Instant.now());

        assertThatThrownBy(() -> submission.grade(-1, "", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Grade must be between 0 and 100");    }
}