package com.library.observer;

import com.library.model.Book;
import com.library.model.Patron;
import com.library.model.Reservation;

public interface LibraryObserver {
    void onBookCheckedOut(Book book, Patron patron);
    void onBookReturned(Book book, Patron patron);
    void onBookReserved(Book book, Patron patron);
    void onReservationFulfilled(Reservation reservation);
    void onBookTransferred(Book book, String fromBranch, String toBranch);
}