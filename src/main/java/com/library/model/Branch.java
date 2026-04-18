package com.library.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Branch extends LibraryEntity {

    private String name;
    private String address;
    private Map<Book, Integer> totalCopies;
    private Map<Book, Integer> availableCopies;

    public Branch(String branchId, String name, String address) {
        super(branchId);
        this.name = name;
        this.address = address;
        this.totalCopies = new HashMap<>();
        this.availableCopies = new HashMap<>();
    }

    // Getters
    public String getBranchId() { return getId(); }
    public String getName() { return name; }
    public String getAddress() { return address; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }

    // Add copies of a book to this branch
    public void addBook(Book book, int copies) {
        totalCopies.merge(book, copies, Integer::sum);
        availableCopies.merge(book, copies, Integer::sum);
    }

    // Remove specific number of copies
    public void removeBook(Book book, int copies) {
        int currentTotal = totalCopies.getOrDefault(book, 0);
        int currentAvailable = availableCopies.getOrDefault(book, 0);

        if (copies >= currentTotal) {
            totalCopies.remove(book);
            availableCopies.remove(book);
        } else {
            totalCopies.put(book, currentTotal - copies);
            availableCopies.put(book, Math.max(0, currentAvailable - copies));
        }
    }

    // Remove book entirely from branch
    public void removeBook(Book book) {
        totalCopies.remove(book);
        availableCopies.remove(book);
    }

    // Checkout one copy
    public boolean checkoutBook(Book book) {
        int available = availableCopies.getOrDefault(book, 0);
        if (available > 0) {
            availableCopies.put(book, available - 1);
            return true;
        }
        return false;
    }

    // Return one copy
    public void returnBook(Book book) {
        int available = availableCopies.getOrDefault(book, 0);
        int total = totalCopies.getOrDefault(book, 0);
        if (available < total) {
            availableCopies.put(book, available + 1);
        }
    }

    // Check availability
    public boolean isBookAvailable(Book book) {
        return availableCopies.getOrDefault(book, 0) > 0;
    }

    // Get available copies count
    public int getAvailableCopies(Book book) {
        return availableCopies.getOrDefault(book, 0);
    }

    // Get total copies count
    public int getTotalCopies(Book book) {
        return totalCopies.getOrDefault(book, 0);
    }

    // Get all available books
    public List<Book> getAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Map.Entry<Book, Integer> entry : availableCopies.entrySet()) {
            if (entry.getValue() > 0) {
                available.add(entry.getKey());
            }
        }
        return available;
    }

    // Get all books in branch
    public List<Book> getAllBooks() {
        return new ArrayList<>(totalCopies.keySet());
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Branch[%s] %s | Address: %s | Unique Titles: %d",
                getBranchId(), name, address, totalCopies.size());
    }
}