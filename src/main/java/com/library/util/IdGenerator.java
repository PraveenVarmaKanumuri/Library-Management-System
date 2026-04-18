package com.library.util;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {

    private static final AtomicInteger patronCounter = new AtomicInteger(0);
    private static final AtomicInteger recordCounter = new AtomicInteger(0);
    private static final AtomicInteger reservationCounter = new AtomicInteger(0);
    private static final AtomicInteger branchCounter = new AtomicInteger(0);

    public static String generatePatronId() {
        return String.format("P%03d", patronCounter.incrementAndGet());
    }

    public static String generateRecordId() {
        return String.format("R%03d", recordCounter.incrementAndGet());
    }

    public static String generateReservationId() {
        return String.format("RES%03d", reservationCounter.incrementAndGet());
    }

    public static String generateBranchId() {
        return String.format("B%03d", branchCounter.incrementAndGet());
    }
}