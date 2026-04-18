package com.library.interfaces;

import com.library.model.LendingRecord;
import com.library.model.Patron;
import java.util.List;

public interface PatronService extends Searchable<Patron> {
    void addPatron(Patron patron);
    void removePatron(String patronId);
    void updatePatron(String patronId, Patron updatedPatron);
    List<Patron> getAllPatrons();
    List<LendingRecord> getBorrowingHistory(String patronId);
}