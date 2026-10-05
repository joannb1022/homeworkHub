package org.example.homeworkhub.common.error;

public abstract class HomeworkHubException extends RuntimeException {
    protected HomeworkHubException(String message) { super(message); }
}

