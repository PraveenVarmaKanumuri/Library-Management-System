package com.library.service;

import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.interfaces.LibraryService;
import com.library.interfaces.PatronService;
import com.library.model.*;
import com.library.observer.LibraryObserver;
import com.library.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryServiceImpl implements LibraryService {

    private static final Logger logger = LoggerFactory.getLogger(LibraryServiceImpl.class);

    private final List<Branch> branches;
    private final PatronService patronService;
    private final Map<String, LendingRecord> lendingRecords;
    private final List<LibraryObserver> observers;

    public LibraryServiceImpl(List<Branch> branches, PatronService patronService) {
        this.branches = branches;
        this.patronService = patronService;
        this.lendingRecords = new HashMap<>();
        this.observers = new ArrayList<>();
    }

    // Register observer
    public void addObserver(LibraryObserver observer) {
        observers.add(observer);
    }

    @Override
    public LendingRecord checkoutBook(String isbn, String patronId, String branchId) {
        // Find branch
        Branch branch = findBranchById(branchId);

        // Find book
        Book book = findBookByIsbn(isbn, branch);

        // Find patron — delegated to PatronService
        Patron patron = patronService.searchById(patronId);

        // Check availability
        if (!branch.isBookAvailable(book)) {
            logger.error("Checkout failed — '{}' not available at '{}'",
                    book.getTitle(), branch.getName());
            throw new BookNotAvailableException(book.getTitle(), branch.getName());
        }

        // Checkout
        branch.checkoutBook(book);

        // Create lending record
        String recordId = IdGenerator.generateRecordId();
        LendingRecord record = new LendingRecord(recordId, book, patron);
        lendingRecords.put(recordId, record);

        // Add to patron history
        patron.addBorrowingRecord(record);

        // Notify observers
        observers.forEach(o -> o.onBookCheckedOut(book, patron));

        logger.info("Book '{}' checked out by patron '{}'",
                book.getTitle(), patron.getName());
        return record;
    }

    @Override
    public LendingRecord returnBook(String recordId) {
        // Find record
        LendingRecord record = lendingRecords.get(recordId);
        if (record == null) {
            logger.error("Return failed — lending record not found: '{}'", recordId);
            throw new BookNotFoundException("Lending record not found: " + recordId);
        }

        // Find branch book belongs to and return it
        Branch branch = findBranchForBook(record.getBook());
        branch.returnBook(record.getBook());

        // Mark as returned
        record.markAsReturned();

        // Notify observers
        observers.forEach(o -> o.onBookReturned(record.getBook(), record.getPatron()));

        logger.info("Book '{}' returned by patron '{}'",
                record.getBook().getTitle(), record.getPatron().getName());
        return record;
    }

    @Override
    public List<Book> getAvailableBooks(String branchId) {
        Branch branch = findBranchById(branchId);
        return branch.getAvailableBooks();
    }

    @Override
    public List<Book> getBorrowedBooks(String branchId) {
        Branch branch = findBranchById(branchId);
        List<Book> borrowed = new ArrayList<>();
        for (Book book : branch.getAllBooks()) {
            if (!branch.isBookAvailable(book)) {
                borrowed.add(book);
            }
        }
        return borrowed;
    }

    @Override
    public List<LendingRecord> getAllLendingRecords() {
        return new ArrayList<>(lendingRecords.values());
    }

    // Private helpers
    private Branch findBranchById(String branchId) {
        for (Branch branch : branches) {
            if (branch.getBranchId().equalsIgnoreCase(branchId)) {
                return branch;
            }
        }
        logger.error("Branch not found with ID '{}'", branchId);
        throw new BookNotFoundException("Branch not found: " + branchId);
    }

    private Book findBookByIsbn(String isbn, Branch branch) {
        for (Book book : branch.getAllBooks()) {
            if (book.getIsbn().equalsIgnoreCase(isbn)) {
                return book;
            }
        }
        logger.error("Book not found with ISBN '{}' in branch '{}'", isbn, branch.getName());
        throw BookNotFoundException.withIsbn(isbn, branch.getName());
    }

    private Branch findBranchForBook(Book book) {
        for (Branch branch : branches) {
            if (branch.getAllBooks().contains(book)) {
                return branch;
            }
        }
        logger.error("No branch found holding book '{}'", book.getTitle());
        throw new BookNotFoundException("Branch not found for book: " + book.getTitle());
    }
}