package com.library.factory;

import com.library.model.Book;
import com.library.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookFactory {

    private static final Logger logger = LoggerFactory.getLogger(BookFactory.class);

    public static Book create(String isbn, String title, String author, int publicationYear) {
        // Validate inputs
        Validator.validateIsbn(isbn);
        Validator.validateNotEmpty(title, "Title");
        Validator.validateNotEmpty(author, "Author");
        Validator.validateYear(publicationYear);

        logger.info("Creating new book: {} by {}", title, author);
        return new Book(isbn, title, author, publicationYear);
    }
}