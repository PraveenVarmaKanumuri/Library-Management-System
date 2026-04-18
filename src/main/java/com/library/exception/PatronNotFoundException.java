package com.library.exception;

public class PatronNotFoundException extends RuntimeException {

    public PatronNotFoundException(String message) {
        super(message);
    }

    public static PatronNotFoundException withId(String patronId) {
        return new PatronNotFoundException(
                String.format("Patron with ID '%s' not found", patronId)
        );
    }
}