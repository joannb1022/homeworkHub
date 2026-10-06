package org.example.homeworkhub.submission;

import org.example.homeworkhub.assignment.AssignedWork;
import org.example.homeworkhub.common.error.DeadlinePassedException;
import org.example.homeworkhub.submission.grade.Grade;
import org.example.homeworkhub.testsupport.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SubmissionTest {

    private static final Instant DEADLINE = Instant.parse("2026-10-10T12:00:00Z");
    private static final Instant BEFORE_DEADLINE = DEADLINE.minusSeconds(60);
    private static final Instant AFTER_DEADLINE = DEADLINE.plusSeconds(60);

    private AssignedWork work;

    @BeforeEach
    void setUp() {
        work = TestData.workWithDeadline(DEADLINE);
    }

    @Test
    void isLateWhenSubmittedAfterDeadline() {
        //given
        Submission submission = new Submission(work, List.of(TestData.pdf()), AFTER_DEADLINE);

        assertThat(submission.isLate()).isTrue();
    }

    @Test
    void isNotLateWhenSubmittedBeforeDeadline() {
        Submission submission = new Submission(work, List.of(TestData.pdf()), BEFORE_DEADLINE);

        assertThat(submission.isLate()).isFalse();
    }

    @Test
    void newSubmissionIsNotGraded() {
        Submission submission = new Submission(work, List.of(TestData.pdf()), BEFORE_DEADLINE);

        assertThat(submission.isGraded()).isFalse();
    }

    @Test
    void gradingStoresValueAndFeedback() {
        Submission submission = new Submission(work, List.of(TestData.pdf()), BEFORE_DEADLINE);

        submission.grade(80, "Good work", AFTER_DEADLINE);

        Grade grade = submission.findGrade().orElseThrow();
        assertThat(grade.value()).isEqualTo(80);
        assertThat(grade.findFeedback()).contains("Good work");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    void rejectsGradeOutsideRange(int value) {
        Submission submission = new Submission(work, List.of(TestData.pdf()), BEFORE_DEADLINE);

        assertThatThrownBy(() -> submission.grade(value, null, AFTER_DEADLINE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 100");
    }

    @Test
    void resubmitReplacesFilesAndReturnsOldKeys() {
        //given
        SubmissionFile oldFile = TestData.pdf();
        Submission submission = new Submission(work, List.of(oldFile), BEFORE_DEADLINE);
        Instant later = BEFORE_DEADLINE.plusSeconds(30);

        //when
        List<String> removedKeys = submission.resubmit(TestData.images(3), later);

        //then
        assertThat(removedKeys).containsExactly(oldFile.getStorageKey());
        assertThat(submission.getFiles()).hasSize(3);
        assertThat(submission.getSubmittedAt()).isEqualTo(later);
    }

    @Test
    void cannotResubmitAfterDeadline() {
        Submission submission = new Submission(work, List.of(TestData.pdf()), BEFORE_DEADLINE);

        assertThatThrownBy(() -> submission.resubmit(List.of(TestData.pdf()), AFTER_DEADLINE))
                .isInstanceOf(DeadlinePassedException.class);
    }

    @Test
    void cannotResubmitAfterGrading() {
        Submission submission = new Submission(work, List.of(TestData.pdf()), BEFORE_DEADLINE);
        submission.grade(70, null, BEFORE_DEADLINE.plusSeconds(10));

        assertThatThrownBy(() -> submission.resubmit(List.of(TestData.pdf()), BEFORE_DEADLINE.plusSeconds(20)))
                .isInstanceOf(IllegalStateException.class);
    }
}