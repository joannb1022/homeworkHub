package org.example.homeworkhub.testsupport;

import org.example.homeworkhub.assignment.AssignedWork;
import org.example.homeworkhub.assignment.Assignment;
import org.example.homeworkhub.submission.FileType;
import org.example.homeworkhub.submission.SubmissionFile;
import org.example.homeworkhub.user.StudentProfile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public final class TestData {

    private TestData() { }

    public static AssignedWork workWithDeadline(Instant deadline) {
        Assignment assignment = new Assignment(/* title, description, pdfKey, ... */);
        StudentProfile student = new StudentProfile(/* ... */);
        return new AssignedWork(assignment, student, deadline);
    }

    public static SubmissionFile pdf() {
        return new SubmissionFile("key-" + UUID.randomUUID(), "solution.pdf", FileType.PDF, 1000, 1);
    }

    public static SubmissionFile image(int position) {
        return new SubmissionFile("key-" + UUID.randomUUID(), "page" + position + ".jpg", FileType.JPEG, 1000, position);
    }

    public static List<SubmissionFile> images(int count) {
        return IntStream.rangeClosed(1, count).mapToObj(TestData::image).toList();
    }
}