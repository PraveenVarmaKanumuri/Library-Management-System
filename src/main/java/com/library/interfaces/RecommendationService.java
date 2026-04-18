package com.library.interfaces;

import com.library.model.Book;
import com.library.model.Patron;
import java.util.List;

public interface RecommendationService {
    List<Book> recommendBooks(String patronId);
}