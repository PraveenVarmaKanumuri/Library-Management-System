package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.interfaces.BookService;
import com.library.model.Book;
import com.library.model.Branch;
import com.library.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final List<Branch> branches;

    public BookServiceImpl(List<Branch> branches) {
        this.branches = branches;
    }

    @Override
    public void addBook(Book book, Branch branch, int copies) {
        Validator.validateCopies(copies);
        branch.addBook(book, copies);
        logger.info("Added {} copies of '{}' to branch '{}'",
                copies, book.getTitle(), branch.getName());
    }

    @Override
    public void removeBook(String isbn) {
        Validator.validateIsbn(isbn);
        Book book = findBookByIsbn(isbn);
        if (book == null) {
            logger.error("Remove failed — book not found with ISBN '{}'", isbn);
            throw BookNotFoundException.withIsbn(isbn, "any branch");
        }
        for (Branch branch : branches) {
            branch.removeBook(book);
        }
        logger.info("Removed book with ISBN '{}' from all branches", isbn);
    }

    @Override
    public void updateBook(String isbn, Book updatedBook) {
        Validator.validateIsbn(isbn);
        Book existing = findBookByIsbn(isbn);
        if (existing == null) {
            logger.error("Update failed — book not found with ISBN '{}'", isbn);
            throw BookNotFoundException.withIsbn(isbn, "any branch");
        }
        existing.setTitle(updatedBook.getTitle());
        existing.setAuthor(updatedBook.getAuthor());
        existing.setPublicationYear(updatedBook.getPublicationYear());
        logger.info("Updated book with ISBN '{}'", isbn);
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> allBooks = new ArrayList<>();
        for (Branch branch : branches) {
            for (Book book : branch.getAllBooks()) {
                if (!allBooks.contains(book)) {
                    allBooks.add(book);
                }
            }
        }
        return allBooks;
    }

    @Override
    public List<Book> searchByName(String query) {
        Validator.validateNotEmpty(query, "Search query");
        List<Book> results = new ArrayList<>();
        String lower = query.toLowerCase();
        for (Book book : getAllBooks()) {
            if (book.getTitle().toLowerCase().contains(lower)
                    || book.getAuthor().toLowerCase().contains(lower)
                    || book.getIsbn().toLowerCase().contains(lower)) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public List<Book> searchByTitle(String title) {
        Validator.validateNotEmpty(title, "Title");
        List<Book> results = new ArrayList<>();
        for (Book book : getAllBooks()) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        Validator.validateNotEmpty(author, "Author");
        List<Book> results = new ArrayList<>();
        for (Book book : getAllBooks()) {
            if (book.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                results.add(book);
            }
        }
        return results;
    }

    @Override
    public Book searchById(String id) {
        Validator.validateIsbn(id);
        Book book = findBookByIsbn(id);
        if (book == null) {
            logger.error("Search failed — book not found with ISBN '{}'", id);
            throw BookNotFoundException.withIsbn(id, "any branch");
        }
        return book;
    }

    // Private helper
    private Book findBookByIsbn(String isbn) {
        for (Branch branch : branches) {
            for (Book book : branch.getAllBooks()) {
                if (book.getIsbn().equalsIgnoreCase(isbn)) {
                    return book;
                }
            }
        }
        return null;
    }
}