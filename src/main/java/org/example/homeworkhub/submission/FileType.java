package org.example.homeworkhub.submission;

public enum FileType {
    PDF, JPEG, PNG;

    boolean isImage() {
        return this == FileType.JPEG || this == FileType.PNG;
    }

    boolean isPDF() {
        return this == FileType.PDF;
    }
}
