package com.library.interfaces;

import com.library.model.Book;
import com.library.model.Branch;
import java.util.List;

public interface BranchService {
    void addBranch(Branch branch);
    void removeBranch(String branchId);
    Branch getBranchById(String branchId);
    List<Branch> getAllBranches();
    void transferBook(Book book, String fromBranchId, String toBranchId, int copies);
}