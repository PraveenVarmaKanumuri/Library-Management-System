package com.library.service;

import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.interfaces.BranchService;
import com.library.model.Book;
import com.library.model.Branch;
import com.library.observer.LibraryObserver;
import com.library.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BranchServiceImpl implements BranchService {

    private static final Logger logger = LoggerFactory.getLogger(BranchServiceImpl.class);

    private final List<Branch> branches;
    private final List<LibraryObserver> observers;

    public BranchServiceImpl(List<Branch> branches, List<LibraryObserver> observers) {
        this.branches = branches;
        this.observers = observers;
    }

    @Override
    public void addBranch(Branch branch) {
        Validator.validateNotEmpty(branch.getName(), "Branch name");
        Validator.validateNotEmpty(branch.getAddress(), "Branch address");
        branches.add(branch);
        logger.info("Added new branch: {}", branch.getName());
    }

    @Override
    public void removeBranch(String branchId) {
        Branch branch = getBranchById(branchId);
        branches.remove(branch);
        logger.info("Removed branch: {}", branch.getName());
    }

    @Override
    public Branch getBranchById(String branchId) {
        for (Branch branch : branches) {
            if (branch.getBranchId().equalsIgnoreCase(branchId)) {
                return branch;
            }
        }
        logger.error("Branch not found with ID '{}'", branchId);
        throw new BookNotFoundException("Branch not found: " + branchId);
    }

    @Override
    public List<Branch> getAllBranches() {
        return new ArrayList<>(branches);
    }

    @Override
    public void transferBook(Book book, String fromBranchId, String toBranchId, int copies) {
        // Find branches
        Branch fromBranch = getBranchById(fromBranchId);
        Branch toBranch = getBranchById(toBranchId);

        // Check enough copies available
        if (fromBranch.getAvailableCopies(book) < copies) {
            logger.error("Transfer failed — not enough copies of '{}' available in '{}'",
                    book.getTitle(), fromBranch.getName());
            throw new BookNotAvailableException(
                    "Not enough available copies to transfer in branch: " + fromBranch.getName());
        }

        // Transfer
        fromBranch.removeBook(book, copies);
        toBranch.addBook(book, copies);

        // Notify observers
        observers.forEach(o -> o.onBookTransferred(book, fromBranch.getName(), toBranch.getName()));

        logger.info("Transferred {} copies of '{}' from '{}' to '{}'",
                copies, book.getTitle(), fromBranch.getName(), toBranch.getName());
    }
}