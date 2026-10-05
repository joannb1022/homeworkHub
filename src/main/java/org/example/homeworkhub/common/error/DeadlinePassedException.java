package org.example.homeworkhub.common.error;

public class DeadlinePassedException extends HomeworkHubException {
    protected DeadlinePassedException(String message) {
        super(message);
    }
}
