package com.library.model;

import java.time.LocalDate;

public class LendingRecord {

    private String recordId;
    private Book book;
    private Patron patron;
    private LocalDate checkoutDate;
    private LocalDate returnDate;
    private boolean isReturned;

    // Constructor
    public LendingRecord(String recordId, Book book, Patron patron) {
        this.recordId = recordId;
        this.book = book;
        this.patron = patron;
        this.checkoutDate = LocalDate.now();
        this.returnDate = null;
        this.isReturned = false;
    }

    // Getters
    public String getRecordId() { return recordId; }
    public Book getBook() { return book; }
    public Patron getPatron() { return patron; }
    public LocalDate getCheckoutDate() { return checkoutDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public boolean isReturned() { return isReturned; }

    // Called when book is returned
    public void markAsReturned() {
        this.isReturned = true;
        this.returnDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return String.format("Record[%s] Book: %s | Patron: %s | Checked out: %s | Returned: %s",
                recordId,
                book.getTitle(),
                patron.getName(),
                checkoutDate,
                isReturned ? returnDate.toString() : "Not yet returned");
    }
}