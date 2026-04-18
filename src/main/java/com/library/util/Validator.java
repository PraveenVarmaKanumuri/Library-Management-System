package com.library.util;

public class Validator {

    // Validate string not null or empty
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    // Validate publication year
    public static void validateYear(int year) {
        int currentYear = java.time.Year.now().getValue();
        if (year < 1000 || year > currentYear) {
            throw new IllegalArgumentException(
                    "Publication year must be between 1000 and " + currentYear);
        }
    }

    // Validate copies count
    public static void validateCopies(int copies) {
        if (copies <= 0) {
            throw new IllegalArgumentException("Copies must be greater than 0");
        }
    }

    // Validate email format
    public static void validateEmail(String email) {
        validateNotEmpty(email, "Email");
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // Validate phone — must be digits only, 10 characters
    public static void validatePhone(String phone) {
        validateNotEmpty(phone, "Phone");
        if (!phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must be 10 digits");
        }
    }

    // Validate ISBN format — alphanumeric, not empty
    public static void validateIsbn(String isbn) {
        validateNotEmpty(isbn, "ISBN");
        if (isbn.trim().length() < 3) {
            throw new IllegalArgumentException("ISBN must be at least 3 characters");
        }
    }
}