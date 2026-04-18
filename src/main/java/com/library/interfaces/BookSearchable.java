package com.library.interfaces;

import com.library.model.Book;
import java.util.List;

public interface BookSearchable {
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
}