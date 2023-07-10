package com.github.brickwall2900.diary;

public class DiaryException extends RuntimeException {
    public DiaryException() {
    }

    public DiaryException(String message) {
        super(message);
    }

    public DiaryException(String message, Throwable cause) {
        super(message, cause);
    }

    public DiaryException(Throwable cause) {
        super(cause);
    }
}
