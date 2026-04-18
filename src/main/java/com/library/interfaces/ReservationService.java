package com.library.interfaces;

import com.library.model.Book;
import com.library.model.Patron;
import com.library.model.Reservation;
import java.util.List;

public interface ReservationService {
    Reservation reserveBook(String isbn, String patronId);
    void cancelReservation(String reservationId);
    List<Reservation> getReservationsForBook(String isbn);
    List<Reservation> getReservationsForPatron(String patronId);
}