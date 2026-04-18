package com.library.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message) {
        super(message);
    }

    public static BookNotFoundException withIsbn(String isbn, String branchName) {
        return new BookNotFoundException(
                String.format("Book with ISBN '%s' not found in branch '%s'", isbn, branchName)
        );
    }
}