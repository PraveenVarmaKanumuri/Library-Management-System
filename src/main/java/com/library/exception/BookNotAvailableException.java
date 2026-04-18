package com.library.exception;

public class BookNotAvailableException extends RuntimeException {

    public BookNotAvailableException(String message) {
        super(message);
    }

    public BookNotAvailableException(String title, String branchName) {
        super(String.format("Book '%s' is not available in branch '%s'", title, branchName));
    }
}