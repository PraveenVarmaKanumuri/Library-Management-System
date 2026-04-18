package com.library.service;

import com.library.exception.PatronNotFoundException;
import com.library.interfaces.PatronService;
import com.library.model.LendingRecord;
import com.library.model.Patron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatronServiceImpl implements PatronService {

    private static final Logger logger = LoggerFactory.getLogger(PatronServiceImpl.class);

    private final Map<String, Patron> patrons;

    public PatronServiceImpl() {
        this.patrons = new HashMap<>();
    }

    @Override
    public void addPatron(Patron patron) {
        patrons.put(patron.getPatronId(), patron);
        logger.info("Added new patron: {} with ID: {}",
                patron.getName(), patron.getPatronId());
    }

    @Override
    public void removePatron(String patronId) {
        Patron patron = getPatronById(patronId);
        patrons.remove(patronId);
        logger.info("Removed patron: {}", patron.getName());
    }

    @Override
    public void updatePatron(String patronId, Patron updatedPatron) {
        Patron existing = getPatronById(patronId);
        existing.setName(updatedPatron.getName());
        existing.setEmail(updatedPatron.getEmail());
        existing.setPhone(updatedPatron.getPhone());
        logger.info("Updated patron with ID: {}", patronId);
    }

    @Override
    public List<Patron> getAllPatrons() {
        return new ArrayList<>(patrons.values());
    }

    @Override
    public List<Patron> searchByName(String name) {
        List<Patron> results = new ArrayList<>();
        for (Patron patron : patrons.values()) {
            if (patron.getName().toLowerCase().contains(name.toLowerCase())) {
                results.add(patron);
            }
        }
        return results;
    }

    @Override
    public Patron searchById(String id) {
        return getPatronById(id);
    }

    @Override
    public List<LendingRecord> getBorrowingHistory(String patronId) {
        Patron patron = getPatronById(patronId);
        return patron.getBorrowingHistory();
    }

    // Helper — get patron by ID or throw
    public Patron getPatronById(String patronId) {
        Patron patron = patrons.get(patronId);
        if (patron == null) {
            logger.error("Patron not found with ID '{}'", patronId);
            throw PatronNotFoundException.withId(patronId);
        }
        return patron;
    }
}