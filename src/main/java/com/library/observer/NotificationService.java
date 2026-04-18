package com.library.observer;

import com.library.model.Book;
import com.library.model.Patron;
import com.library.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationService implements LibraryObserver {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Override
    public void onBookCheckedOut(Book book, Patron patron) {
        String message = String.format("Book '%s' checked out by %s",
                book.getTitle(), patron.getName());
        System.out.println(message);
        logger.info(message);
    }

    @Override
    public void onBookReturned(Book book, Patron patron) {
        String message = String.format("Book '%s' returned by %s",
                book.getTitle(), patron.getName());
        System.out.println(message);
        logger.info(message);
    }

    @Override
    public void onBookReserved(Book book, Patron patron) {
        String message = String.format("Book '%s' reserved by %s",
                book.getTitle(), patron.getName());
        System.out.println(message);
        logger.info(message);
    }

    @Override
    public void onReservationFulfilled(Reservation reservation) {
        String message = String.format("Reservation fulfilled. Book '%s' is now available for %s",
                reservation.getBook().getTitle(),
                reservation.getPatron().getName());
        System.out.println(message);
        logger.info(message);
    }

    @Override
    public void onBookTransferred(Book book, String fromBranch, String toBranch) {
        String message = String.format("Book '%s' transferred from %s to %s",
                book.getTitle(), fromBranch, toBranch);
        System.out.println(message);
        logger.info(message);
    }
}