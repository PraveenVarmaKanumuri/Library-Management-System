package com.library.interfaces;

import com.library.model.Book;
import com.library.model.Branch;
import java.util.List;

public interface BookService extends Searchable<Book> {
    void addBook(Book book, Branch branch, int copies);
    void removeBook(String isbn);
    void updateBook(String isbn, Book updatedBook);
    List<Book> getAllBooks();
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
}
