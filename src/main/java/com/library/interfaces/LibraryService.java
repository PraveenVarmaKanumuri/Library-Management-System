package com.library.interfaces;

import com.library.model.Book;
import com.library.model.Branch;
import com.library.model.LendingRecord;
import com.library.model.Patron;
import com.library.observer.LibraryObserver;
import java.util.List;

public interface LibraryService {
    // Observer registration
    void addObserver(LibraryObserver observer);

    // Lending
    LendingRecord checkoutBook(String isbn, String patronId, String branchId);
    LendingRecord returnBook(String recordId);

    // Inventory
    List<Book> getAvailableBooks(String branchId);
    List<Book> getBorrowedBooks(String branchId);
    List<LendingRecord> getAllLendingRecords();
}