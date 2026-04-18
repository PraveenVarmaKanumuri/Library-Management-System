package com.library;

import com.library.factory.BookFactory;
import com.library.factory.PatronFactory;
import com.library.interfaces.*;
import com.library.model.*;
import com.library.observer.NotificationService;
import com.library.service.*;
import com.library.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    // Shared state
    private static final List<Branch> branches = new ArrayList<>();
    private static final List<com.library.observer.LibraryObserver> observers = new ArrayList<>();

    // Services
    private static PatronService patronService;
    private static BookService bookService;
    private static BranchService branchService;
    private static LibraryService libraryService;
    private static ReservationService reservationService;
    private static RecommendationService recommendationService;

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeServices();
        loadSampleData();
        showMainMenu();
    }

    // --- Initialize all services ---
    private static void initializeServices() {
        NotificationService notificationService = new NotificationService();
        observers.add(notificationService);

        patronService = new PatronServiceImpl();
        bookService = new BookServiceImpl(branches);
        branchService = new BranchServiceImpl(branches, observers);
        libraryService = new LibraryServiceImpl(branches, patronService);
        reservationService = new ReservationServiceImpl(branches, patronService, observers);
        recommendationService = new RecommendationServiceImpl(branches, patronService);

        libraryService.addObserver(notificationService);
        libraryService.addObserver((com.library.observer.LibraryObserver) reservationService);
    }

    // --- Load sample data ---
    private static void loadSampleData() {
        Branch mainBranch = new Branch(IdGenerator.generateBranchId(), "Main Branch", "123 Main St");
        Branch cityBranch = new Branch(IdGenerator.generateBranchId(), "City Branch", "456 City Ave");
        branchService.addBranch(mainBranch);
        branchService.addBranch(cityBranch);

        Book book1 = BookFactory.create("ISBN001", "Clean Code", "Robert Martin", 2008);
        Book book2 = BookFactory.create("ISBN002", "The Pragmatic Programmer", "Andrew Hunt", 1999);
        Book book3 = BookFactory.create("ISBN003", "Refactoring", "Robert Martin", 2018);
        Book book4 = BookFactory.create("ISBN004", "Design Patterns", "Gang of Four", 1994);

        bookService.addBook(book1, mainBranch, 3);
        bookService.addBook(book2, mainBranch, 2);
        bookService.addBook(book3, cityBranch, 2);
        bookService.addBook(book4, cityBranch, 1);

        Patron patron1 = PatronFactory.create("John Doe", "john@email.com", "1234567890");
        Patron patron2 = PatronFactory.create("Jane Smith", "jane@email.com", "0987654321");
        patronService.addPatron(patron1);
        patronService.addPatron(patron2);

        System.out.println("Sample data loaded.");
        System.out.println("Branches: Main Branch (" + mainBranch.getBranchId() + "), City Branch (" + cityBranch.getBranchId() + ")");
        System.out.println("Patrons: John Doe (" + patron1.getPatronId() + "), Jane Smith (" + patron2.getPatronId() + ")");
        System.out.println("Books: ISBN001, ISBN002 in Main Branch | ISBN003, ISBN004 in City Branch");
    }

    // --- Error handler ---
    private static void handleError(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Please try again.");
        }
    }

    // --- Selectors ---
    private static Branch selectBranch() {
        List<Branch> allBranches = branchService.getAllBranches();
        if (allBranches.isEmpty()) {
            throw new IllegalStateException("No branches available");
        }
        System.out.println("\nAvailable Branches:");
        for (int i = 0; i < allBranches.size(); i++) {
            System.out.println((i + 1) + ". " + allBranches.get(i));
        }
        System.out.print("Select branch: ");
        int choice = readInt();
        if (choice < 1 || choice > allBranches.size()) {
            throw new IllegalArgumentException("Invalid branch selection");
        }
        return allBranches.get(choice - 1);
    }

    private static Patron selectPatron() {
        List<Patron> allPatrons = patronService.getAllPatrons();
        if (allPatrons.isEmpty()) {
            throw new IllegalStateException("No patrons available");
        }
        System.out.println("\nAvailable Patrons:");
        for (int i = 0; i < allPatrons.size(); i++) {
            System.out.println((i + 1) + ". " + allPatrons.get(i));
        }
        System.out.print("Select patron: ");
        int choice = readInt();
        if (choice < 1 || choice > allPatrons.size()) {
            throw new IllegalArgumentException("Invalid patron selection");
        }
        return allPatrons.get(choice - 1);
    }

    private static Book selectBook() {
        List<Book> allBooks = bookService.getAllBooks();
        if (allBooks.isEmpty()) {
            throw new IllegalStateException("No books available");
        }
        System.out.println("\nAvailable Books:");
        for (int i = 0; i < allBooks.size(); i++) {
            System.out.println((i + 1) + ". " + allBooks.get(i));
        }
        System.out.print("Select book: ");
        int choice = readInt();
        if (choice < 1 || choice > allBooks.size()) {
            throw new IllegalArgumentException("Invalid book selection");
        }
        return allBooks.get(choice - 1);
    }

    private static LendingRecord selectLendingRecord() {
        List<LendingRecord> allRecords = libraryService.getAllLendingRecords();
        List<LendingRecord> activeRecords = new ArrayList<>();
        for (LendingRecord record : allRecords) {
            if (!record.isReturned()) {
                activeRecords.add(record);
            }
        }
        if (activeRecords.isEmpty()) {
            throw new IllegalStateException("No active lending records");
        }
        System.out.println("\nActive Lending Records:");
        for (int i = 0; i < activeRecords.size(); i++) {
            System.out.println((i + 1) + ". " + activeRecords.get(i));
        }
        System.out.print("Select record: ");
        int choice = readInt();
        if (choice < 1 || choice > activeRecords.size()) {
            throw new IllegalArgumentException("Invalid record selection");
        }
        return activeRecords.get(choice - 1);
    }

    private static Reservation selectReservation() {
        System.out.println("\nSelect Patron first:");
        Patron patron = selectPatron();
        List<Reservation> patronReservations = reservationService
                .getReservationsForPatron(patron.getPatronId());
        if (patronReservations.isEmpty()) {
            throw new IllegalStateException("No reservations found for this patron");
        }
        System.out.println("\nReservations:");
        for (int i = 0; i < patronReservations.size(); i++) {
            System.out.println((i + 1) + ". " + patronReservations.get(i));
        }
        System.out.print("Select reservation: ");
        int choice = readInt();
        if (choice < 1 || choice > patronReservations.size()) {
            throw new IllegalArgumentException("Invalid reservation selection");
        }
        return patronReservations.get(choice - 1);
    }

    // --- Main Menu ---
    private static void showMainMenu() {
        while (true) {
            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Book Management");
            System.out.println("2. Patron Management");
            System.out.println("3. Lending");
            System.out.println("4. Branch Management");
            System.out.println("5. Reservations");
            System.out.println("6. Recommendations");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            switch (readInt()) {
                case 1 -> showBookMenu();
                case 2 -> showPatronMenu();
                case 3 -> showLendingMenu();
                case 4 -> showBranchMenu();
                case 5 -> showReservationMenu();
                case 6 -> showRecommendationMenu();
                case 0 -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // --- Book Menu ---
    private static void showBookMenu() {
        System.out.println("\n--- Book Management ---");
        System.out.println("1. Add Book");
        System.out.println("2. Remove Book");
        System.out.println("3. Update Book");
        System.out.println("4. List All Books");
        System.out.println("5. Search Books");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        switch (readInt()) {
            case 1 -> addBook();
            case 2 -> removeBook();
            case 3 -> updateBook();
            case 4 -> listAllBooks();
            case 5 -> searchBooks();
            case 0 -> {}
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void addBook() {
        handleError(() -> {
            System.out.print("ISBN: ");
            String isbn = scanner.nextLine();
            System.out.print("Title: ");
            String title = scanner.nextLine();
            System.out.print("Author: ");
            String author = scanner.nextLine();
            System.out.print("Publication Year: ");
            int year = readInt();
            System.out.print("Copies: ");
            int copies = readInt();
            Branch branch = selectBranch();
            Book book = BookFactory.create(isbn, title, author, year);
            bookService.addBook(book, branch, copies);
            System.out.println("Book added successfully.");
        });
    }

    private static void removeBook() {
        handleError(() -> {
            Book book = selectBook();
            bookService.removeBook(book.getIsbn());
            System.out.println("Book removed successfully.");
        });
    }

    private static void updateBook() {
        handleError(() -> {
            Book book = selectBook();
            System.out.print("New Title (" + book.getTitle() + "): ");
            String title = scanner.nextLine();
            System.out.print("New Author (" + book.getAuthor() + "): ");
            String author = scanner.nextLine();
            System.out.print("New Year (" + book.getPublicationYear() + "): ");
            int year = readInt();
            Book updated = BookFactory.create(book.getIsbn(), title, author, year);
            bookService.updateBook(book.getIsbn(), updated);
            System.out.println("Book updated successfully.");
        });
    }

    private static void listAllBooks() {
        handleError(() -> {
            List<Book> books = bookService.getAllBooks();
            if (books.isEmpty()) {
                System.out.println("No books found.");
                return;
            }
            System.out.println("\nAll Books:");
            books.forEach(System.out::println);
        });
    }

    private static void searchBooks() {
        handleError(() -> {
            System.out.println("\n--- Search Books ---");
            System.out.println("1. By Title");
            System.out.println("2. By Author");
            System.out.println("3. By ISBN");
            System.out.print("Choose: ");

            switch (readInt()) {
                case 1 -> {
                    System.out.print("Title: ");
                    List<Book> results = bookService.searchByTitle(scanner.nextLine());
                    if (results.isEmpty()) System.out.println("No books found.");
                    else results.forEach(System.out::println);
                }
                case 2 -> {
                    System.out.print("Author: ");
                    List<Book> results = bookService.searchByAuthor(scanner.nextLine());
                    if (results.isEmpty()) System.out.println("No books found.");
                    else results.forEach(System.out::println);
                }
                case 3 -> {
                    System.out.print("ISBN: ");
                    System.out.println(bookService.searchById(scanner.nextLine()));
                }
                default -> System.out.println("Invalid choice.");
            }
        });
    }

    // --- Patron Menu ---
    private static void showPatronMenu() {
        System.out.println("\n--- Patron Management ---");
        System.out.println("1. Add Patron");
        System.out.println("2. Remove Patron");
        System.out.println("3. Update Patron");
        System.out.println("4. List All Patrons");
        System.out.println("5. View Borrowing History");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        switch (readInt()) {
            case 1 -> addPatron();
            case 2 -> removePatron();
            case 3 -> updatePatron();
            case 4 -> listAllPatrons();
            case 5 -> viewBorrowingHistory();
            case 0 -> {}
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void addPatron() {
        handleError(() -> {
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Phone: ");
            String phone = scanner.nextLine();
            Patron patron = PatronFactory.create(name, email, phone);
            patronService.addPatron(patron);
            System.out.println("Patron added. ID: " + patron.getPatronId());
        });
    }

    private static void removePatron() {
        handleError(() -> {
            Patron patron = selectPatron();
            patronService.removePatron(patron.getPatronId());
            System.out.println("Patron removed.");
        });
    }

    private static void updatePatron() {
        handleError(() -> {
            Patron patron = selectPatron();
            System.out.print("New Name (" + patron.getName() + "): ");
            String name = scanner.nextLine();
            System.out.print("New Email (" + patron.getEmail() + "): ");
            String email = scanner.nextLine();
            System.out.print("New Phone (" + patron.getPhone() + "): ");
            String phone = scanner.nextLine();
            Patron updated = PatronFactory.create(name, email, phone);
            patronService.updatePatron(patron.getPatronId(), updated);
            System.out.println("Patron updated.");
        });
    }

    private static void listAllPatrons() {
        handleError(() -> {
            List<Patron> patrons = patronService.getAllPatrons();
            if (patrons.isEmpty()) {
                System.out.println("No patrons found.");
                return;
            }
            System.out.println("\nAll Patrons:");
            patrons.forEach(System.out::println);
        });
    }

    private static void viewBorrowingHistory() {
        handleError(() -> {
            Patron patron = selectPatron();
            List<LendingRecord> history = patronService
                    .getBorrowingHistory(patron.getPatronId());
            if (history.isEmpty()) {
                System.out.println("No borrowing history.");
                return;
            }
            System.out.println("\nBorrowing History:");
            history.forEach(System.out::println);
        });
    }

    // --- Lending Menu ---
    private static void showLendingMenu() {
        System.out.println("\n--- Lending ---");
        System.out.println("1. Checkout Book");
        System.out.println("2. Return Book");
        System.out.println("3. View All Lending Records");
        System.out.println("4. View Available Books");
        System.out.println("5. View Borrowed Books");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        switch (readInt()) {
            case 1 -> checkoutBook();
            case 2 -> returnBook();
            case 3 -> viewLendingRecords();
            case 4 -> viewAvailableBooks();
            case 5 -> viewBorrowedBooks();
            case 0 -> {}
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void checkoutBook() {
        handleError(() -> {
            Book book = selectBook();
            Patron patron = selectPatron();
            Branch branch = selectBranch();
            LendingRecord record = libraryService.checkoutBook(
                    book.getIsbn(), patron.getPatronId(), branch.getBranchId());
            System.out.println("Checkout successful. Record ID: " + record.getRecordId());
        });
    }

    private static void returnBook() {
        handleError(() -> {
            LendingRecord record = selectLendingRecord();
            LendingRecord returned = libraryService.returnBook(record.getRecordId());
            System.out.println("Return successful. Book: " + returned.getBook().getTitle());
        });
    }

    private static void viewLendingRecords() {
        handleError(() -> {
            List<LendingRecord> records = libraryService.getAllLendingRecords();
            if (records.isEmpty()) {
                System.out.println("No lending records.");
                return;
            }
            System.out.println("\nAll Lending Records:");
            records.forEach(System.out::println);
        });
    }

    private static void viewAvailableBooks() {
        handleError(() -> {
            Branch branch = selectBranch();
            List<Book> books = libraryService.getAvailableBooks(branch.getBranchId());
            if (books.isEmpty()) {
                System.out.println("No available books.");
                return;
            }
            System.out.println("\nAvailable Books:");
            books.forEach(System.out::println);
        });
    }

    private static void viewBorrowedBooks() {
        handleError(() -> {
            Branch branch = selectBranch();
            List<Book> books = libraryService.getBorrowedBooks(branch.getBranchId());
            if (books.isEmpty()) {
                System.out.println("No borrowed books.");
                return;
            }
            System.out.println("\nBorrowed Books:");
            books.forEach(System.out::println);
        });
    }

    // --- Branch Menu ---
    private static void showBranchMenu() {
        System.out.println("\n--- Branch Management ---");
        System.out.println("1. Add Branch");
        System.out.println("2. Remove Branch");
        System.out.println("3. List All Branches");
        System.out.println("4. Transfer Book");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        switch (readInt()) {
            case 1 -> addBranch();
            case 2 -> removeBranch();
            case 3 -> listAllBranches();
            case 4 -> transferBook();
            case 0 -> {}
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void addBranch() {
        handleError(() -> {
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Address: ");
            String address = scanner.nextLine();
            Branch branch = new Branch(IdGenerator.generateBranchId(), name, address);
            branchService.addBranch(branch);
            System.out.println("Branch added. ID: " + branch.getBranchId());
        });
    }

    private static void removeBranch() {
        handleError(() -> {
            Branch branch = selectBranch();
            branchService.removeBranch(branch.getBranchId());
            System.out.println("Branch removed.");
        });
    }

    private static void listAllBranches() {
        handleError(() -> {
            List<Branch> allBranches = branchService.getAllBranches();
            if (allBranches.isEmpty()) {
                System.out.println("No branches found.");
                return;
            }
            System.out.println("\nAll Branches:");
            allBranches.forEach(System.out::println);
        });
    }

    private static void transferBook() {
        handleError(() -> {
            Book book = selectBook();
            System.out.println("Select FROM branch:");
            Branch fromBranch = selectBranch();
            System.out.println("Select TO branch:");
            Branch toBranch = selectBranch();
            System.out.print("Copies: ");
            int copies = readInt();
            branchService.transferBook(book, fromBranch.getBranchId(),
                    toBranch.getBranchId(), copies);
            System.out.println("Transfer successful.");
        });
    }

    // --- Reservation Menu ---
    private static void showReservationMenu() {
        System.out.println("\n--- Reservations ---");
        System.out.println("1. Reserve Book");
        System.out.println("2. Cancel Reservation");
        System.out.println("3. View Reservations for Book");
        System.out.println("4. View Reservations for Patron");
        System.out.println("0. Back");
        System.out.print("Choose: ");

        switch (readInt()) {
            case 1 -> reserveBook();
            case 2 -> cancelReservation();
            case 3 -> viewReservationsForBook();
            case 4 -> viewReservationsForPatron();
            case 0 -> {}
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void reserveBook() {
        handleError(() -> {
            Book book = selectBook();
            Patron patron = selectPatron();
            Reservation reservation = reservationService.reserveBook(
                    book.getIsbn(), patron.getPatronId());
            System.out.println("Reserved. ID: " + reservation.getReservationId());
        });
    }

    private static void cancelReservation() {
        handleError(() -> {
            Reservation reservation = selectReservation();
            reservationService.cancelReservation(reservation.getReservationId());
            System.out.println("Reservation cancelled.");
        });
    }

    private static void viewReservationsForBook() {
        handleError(() -> {
            Book book = selectBook();
            List<Reservation> reservations = reservationService
                    .getReservationsForBook(book.getIsbn());
            if (reservations.isEmpty()) {
                System.out.println("No reservations found.");
                return;
            }
            System.out.println("\nReservations:");
            reservations.forEach(System.out::println);
        });
    }

    private static void viewReservationsForPatron() {
        handleError(() -> {
            Patron patron = selectPatron();
            List<Reservation> reservations = reservationService
                    .getReservationsForPatron(patron.getPatronId());
            if (reservations.isEmpty()) {
                System.out.println("No reservations found.");
                return;
            }
            System.out.println("\nReservations:");
            reservations.forEach(System.out::println);
        });
    }

    // --- Recommendation Menu ---
    private static void showRecommendationMenu() {
        handleError(() -> {
            System.out.println("\n--- Recommendations ---");
            Patron patron = selectPatron();
            List<Book> recommendations = recommendationService
                    .recommendBooks(patron.getPatronId());
            if (recommendations.isEmpty()) {
                System.out.println("No recommendations available.");
                return;
            }
            System.out.println("\nRecommended Books:");
            recommendations.forEach(System.out::println);
        });
    }

    // --- Helpers ---
    private static int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}