package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.interfaces.ReservationService;
import com.library.interfaces.PatronService;
import com.library.model.Book;
import com.library.model.Branch;
import com.library.model.Patron;
import com.library.model.Reservation;
import com.library.observer.LibraryObserver;
import com.library.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationServiceImpl implements ReservationService, LibraryObserver {

    private static final Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final List<Branch> branches;
    private final PatronService patronService;
    private final List<LibraryObserver> observers;
    private final Map<String, Reservation> reservations;

    public ReservationServiceImpl(List<Branch> branches,
                                  PatronService patronService,
                                  List<LibraryObserver> observers) {
        this.branches = branches;
        this.patronService = patronService;
        this.observers = observers;
        this.reservations = new HashMap<>();
    }

    // --- ReservationService implementation ---
    @Override
    public Reservation reserveBook(String isbn, String patronId) {
        Patron patron = patronService.searchById(patronId);
        Book book = findBookByIsbn(isbn);

        if (isBookAvailableAnywhere(book)) {
            logger.warn("Reservation rejected — '{}' is still available, no need to reserve",
                    book.getTitle());
            throw new IllegalStateException(
                    "Book '" + book.getTitle() + "' is available — no need to reserve");
        }

        String reservationId = IdGenerator.generateReservationId();
        Reservation reservation = new Reservation(reservationId, book, patron);
        reservations.put(reservationId, reservation);

        observers.forEach(o -> o.onBookReserved(book, patron));

        logger.info("Book '{}' reserved by patron '{}'",
                book.getTitle(), patron.getName());
        return reservation;
    }

    @Override
    public void cancelReservation(String reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservation.cancel();
        logger.info("Reservation '{}' cancelled", reservationId);
    }

    @Override
    public List<Reservation> getReservationsForBook(String isbn) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations.values()) {
            if (reservation.getBook().getIsbn().equalsIgnoreCase(isbn)
                    && reservation.isPending()) {
                result.add(reservation);
            }
        }
        return result;
    }

    @Override
    public List<Reservation> getReservationsForPatron(String patronId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation reservation : reservations.values()) {
            if (reservation.getPatron().getPatronId().equalsIgnoreCase(patronId)
                    && reservation.isPending()) {
                result.add(reservation);
            }
        }
        return result;
    }

    // --- LibraryObserver implementation ---
    @Override
    public void onBookReturned(Book book, Patron patron) {
        // When book is returned — automatically fulfil first pending reservation
        for (Reservation reservation : reservations.values()) {
            if (reservation.getBook().equals(book) && reservation.isPending()) {
                reservation.fulfil();
                observers.forEach(o -> o.onReservationFulfilled(reservation));
                logger.info("Reservation fulfilled for book '{}' patron '{}'",
                        book.getTitle(), reservation.getPatron().getName());
                break;
            }
        }
    }

    @Override
    public void onBookCheckedOut(Book book, Patron patron) {
        // No action needed
    }

    @Override
    public void onBookReserved(Book book, Patron patron) {
        // No action needed
    }

    @Override
    public void onReservationFulfilled(Reservation reservation) {
        // No action needed
    }

    @Override
    public void onBookTransferred(Book book, String fromBranch, String toBranch) {
        // No action needed
    }

    // --- Private helpers ---
    private Reservation getReservationById(String reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            logger.error("Reservation not found with ID '{}'", reservationId);
            throw new BookNotFoundException("Reservation not found: " + reservationId);
        }
        return reservation;
    }

    private Book findBookByIsbn(String isbn) {
        for (Branch branch : branches) {
            for (Book book : branch.getAllBooks()) {
                if (book.getIsbn().equalsIgnoreCase(isbn)) {
                    return book;
                }
            }
        }
        logger.error("Book not found with ISBN '{}' in any branch", isbn);
        throw BookNotFoundException.withIsbn(isbn, "any branch");
    }

    private boolean isBookAvailableAnywhere(Book book) {
        for (Branch branch : branches) {
            if (branch.isBookAvailable(book)) {
                return true;
            }
        }
        return false;
    }
}