package com.library.model;

import com.library.model.enums.ReservationStatus;

import java.time.LocalDate;

public class Reservation {



    private String reservationId;
    private Book book;
    private Patron patron;
    private LocalDate reservationDate;
    private ReservationStatus status;

    // Constructor
    public Reservation(String reservationId, Book book, Patron patron) {
        this.reservationId = reservationId;
        this.book = book;
        this.patron = patron;
        this.reservationDate = LocalDate.now();
        this.status = ReservationStatus.PENDING;
    }

    // Getters
    public String getReservationId() { return reservationId; }
    public Book getBook() { return book; }
    public Patron getPatron() { return patron; }
    public LocalDate getReservationDate() { return reservationDate; }
    public ReservationStatus getStatus() { return status; }

    // Status updates
    public void fulfil() {
        this.status = ReservationStatus.FULFILLED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean isPending() {
        return this.status == ReservationStatus.PENDING;
    }

    @Override
    public String toString() {
        return String.format("Reservation[%s] Book: '%s' | Patron: %s | Date: %s | Status: %s",
                reservationId,
                book.getTitle(),
                patron.getName(),
                reservationDate,
                status);
    }
}