package com.library.model;

import java.util.Objects;

public class Book extends LibraryEntity {

    private String title;
    private String author;
    private int publicationYear;

    public Book(String isbn, String title, String author, int publicationYear) {
        super(isbn);
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
    }

    // Getters
    public String getIsbn() { return getId(); }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getPublicationYear() { return publicationYear; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }

    @Override
    public String getDisplayInfo() {
        return String.format("[%s] '%s' by %s (%d)",
                getIsbn(), title, author, publicationYear);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(getIsbn(), book.getIsbn());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIsbn());
    }
}