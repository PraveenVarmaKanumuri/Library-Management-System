package com.library.model;

import java.util.ArrayList;
import java.util.List;

public class Patron extends LibraryEntity {

    private String name;
    private String email;
    private String phone;
    private List<LendingRecord> borrowingHistory;

    public Patron(String patronId, String name, String email, String phone) {
        super(patronId);
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.borrowingHistory = new ArrayList<>();
    }

    // Getters
    public String getPatronId() { return getId(); }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public List<LendingRecord> getBorrowingHistory() { return borrowingHistory; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    public void addBorrowingRecord(LendingRecord record) {
        this.borrowingHistory.add(record);
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Patron[%s] %s | Email: %s | Phone: %s | Books Borrowed: %d",
                getPatronId(), name, email, phone, borrowingHistory.size());
    }
}