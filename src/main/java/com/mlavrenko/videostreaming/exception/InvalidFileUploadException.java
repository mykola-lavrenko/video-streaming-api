package com.mlavrenko.videostreaming.exception;

public class InvalidFileUploadException extends RuntimeException {
    public InvalidFileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
