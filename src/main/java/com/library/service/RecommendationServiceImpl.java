package com.library.service;

import com.library.interfaces.PatronService;
import com.library.interfaces.RecommendationService;
import com.library.model.Book;
import com.library.model.Branch;
import com.library.model.LendingRecord;
import com.library.model.Patron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final List<Branch> branches;
    private final PatronService patronService;

    public RecommendationServiceImpl(List<Branch> branches, PatronService patronService) {
        this.branches = branches;
        this.patronService = patronService;
    }

    @Override
    public List<Book> recommendBooks(String patronId) {
        // Get patron via PatronService
        Patron patron = patronService.searchById(patronId);

        logger.info("Generating recommendations for patron '{}'", patron.getName());

        // Step 1 — Get borrowing history
        List<LendingRecord> history = patron.getBorrowingHistory();

        if (history.isEmpty()) {
            logger.info("No history for '{}' — returning all available books", patron.getName());
            return getAllAvailableBooks();
        }

        // Step 2 — Collect read authors and ISBNs
        Set<String> readAuthors = new HashSet<>();
        Set<String> readIsbns = new HashSet<>();

        for (LendingRecord record : history) {
            readAuthors.add(record.getBook().getAuthor().toLowerCase());
            readIsbns.add(record.getBook().getIsbn());
        }

        // Step 3 — Recommend by same author not yet read
        List<Book> recommendations = new ArrayList<>();

        for (Book book : getAllAvailableBooks()) {
            boolean sameAuthor = readAuthors.contains(book.getAuthor().toLowerCase());
            boolean alreadyRead = readIsbns.contains(book.getIsbn());
            if (sameAuthor && !alreadyRead) {
                recommendations.add(book);
            }
        }

        // Step 4 — Fill up to 5 recommendations with other books
        if (recommendations.size() < 3) {
            for (Book book : getAllAvailableBooks()) {
                if (!recommendations.contains(book)
                        && !readIsbns.contains(book.getIsbn())) {
                    recommendations.add(book);
                }
                if (recommendations.size() >= 5) break;
            }
        }

        logger.info("Generated {} recommendations for patron '{}'",
                recommendations.size(), patron.getName());
        return recommendations;
    }

    // Private helper
    private List<Book> getAllAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Branch branch : branches) {
            for (Book book : branch.getAvailableBooks()) {
                if (!available.contains(book)) {
                    available.add(book);
                }
            }
        }
        return available;
    }
}