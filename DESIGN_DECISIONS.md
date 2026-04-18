# Design Decisions

This document explains the architectural and implementation choices made in the Library Management System, including the reasoning behind each decision and the alternatives that were considered and rejected.

---

## Architecture Overview

The system follows a classic **layered architecture**:

```
┌─────────────────────────────────────────────────────┐
│                   UI Layer (Main.java)               │
│         Console menus, handleError() wrapper         │
├─────────────────────────────────────────────────────┤
│              Interface Layer (interfaces/)           │
│   BookService, PatronService, LibraryService, ...    │
├─────────────────────────────────────────────────────┤
│              Service Layer (service/)                │
│  BookServiceImpl, LibraryServiceImpl, ...            │
├─────────────────────────────────────────────────────┤
│               Model Layer (model/)                   │
│        Book, Patron, Branch, LendingRecord, ...      │
└─────────────────────────────────────────────────────┘
```

Each layer depends only on the layer below it through interfaces, never on concrete implementations above or beside it. This makes each layer independently testable and replaceable.

---

## Model Design

### Why `LibraryEntity` as an Abstract Base Class

`Book`, `Patron`, and `Branch` share two attributes: `id` and `createdAt`. Without a base class, these fields and their getters would be copy-pasted into each model class — a textbook duplication problem.

`LibraryEntity` was made **abstract** (not a concrete superclass) for two reasons:

1. It makes no sense to instantiate a "bare" library entity — every entity must be a Book, Patron, or Branch.
2. The abstract `getDisplayInfo()` method **forces** each subclass to define its own display format, ensuring polymorphism is structurally guaranteed rather than accidentally forgotten.

`toString()` in `LibraryEntity` delegates to `getDisplayInfo()`, so passing any entity to `System.out.println()` automatically calls the correct subclass implementation.

### Why `LendingRecord` and `Reservation` Do Not Extend `LibraryEntity`

These are **relational records**, not first-class library entities. They exist to describe the relationship between a `Book` and a `Patron` at a point in time. Giving them an `id` from `LibraryEntity` would be misleading — their IDs come from `IdGenerator` and are semantically different (R001, RES001 vs B001, P001).

---

## Interface Design

### Why Interfaces for All Services

Each service is defined as an interface for three reasons:

1. **Dependency Inversion** — callers (including `Main.java` and other services) depend on the interface, not the implementation. Swapping `PatronServiceImpl` for a database-backed version requires zero changes to any caller.
2. **Testability** — interfaces can be mocked in unit tests without loading the full implementation.
3. **Open/Closed** — new behaviour (e.g. a `NotificationServiceImpl` for email) is added by creating a new class, not by modifying an existing one.

### Why Generic `Searchable<T>`

`BookService` and `PatronService` both needed `searchByName()` and `searchById()`. Without a shared interface, the signatures would be defined twice with only the return type differing.

`Searchable<T>` captures this shared contract once:

```java
public interface Searchable<T> {
    List<T> searchByName(String name);
    T searchById(String id);
}
```

`BookService extends Searchable<Book>` and `PatronService extends Searchable<Patron>` get these methods for free, while still adding their own domain-specific methods. This follows the **DRY principle** without sacrificing type safety.

---

## Design Patterns

### Observer Pattern

The reservation auto-fulfil feature requires `LibraryServiceImpl` to notify `ReservationServiceImpl` when a book is returned — but `LibraryServiceImpl` should not directly reference `ReservationServiceImpl`. That would create tight coupling between two unrelated service domains.

The Observer pattern solves this cleanly:

- `LibraryServiceImpl` only knows about `LibraryObserver` (an interface).
- `ReservationServiceImpl` implements `LibraryObserver` and registers itself.
- When a book is returned, `LibraryServiceImpl` fires `onBookReturned()` to all registered observers.
- `ReservationServiceImpl.onBookReturned()` checks for pending reservations and fulfils the first one.

Adding future observers (email alerts, audit log, SMS) requires only: create a class, implement `LibraryObserver`, register in `Main`. No existing class is touched — this is the **Open/Closed Principle** in action.

### Factory Pattern

Object creation for `Book` and `Patron` involves validation (ISBN format, email format, year range) and ID generation. Placing this logic in constructors would violate **SRP** — a constructor should construct, not validate.

Factories centralise three responsibilities:
1. Input validation via `Validator`
2. ID generation via `IdGenerator` (Patron only)
3. Object construction

This also means any code path that creates a `Book` or `Patron` is guaranteed to go through validation — there is no way to create an invalid object by bypassing the factory.

---

## Data Structure Decisions

### `Map<String, Patron>` vs `List<Patron>`

| | `Map<String, Patron>` | `List<Patron>` |
|---|---|---|
| Lookup by ID | O(1) | O(n) |
| Remove by ID | O(1) | O(n) |
| Iterate all | O(n) | O(n) |

Patron lookup by ID is the most frequent operation (every checkout, return, reservation). A `HashMap` was chosen to keep these constant time. The `patronId` is the natural key.

### `Map<Book, Integer>` in `Branch` vs a `copies` Field on `Book`

A `copies` field on `Book` would break the multi-branch model: the same `Book` object exists in multiple branches with different copy counts. Storing copies on the `Book` would mean either duplicating `Book` objects (one per branch) or using a single integer that represents the total across all branches — both wrong.

`Branch` holds two maps: `totalCopies` and `availableCopies`, keyed by the `Book` object. This accurately models the real world: a book's availability is a property of *where it is*, not *what it is*. `Book.equals()` and `hashCode()` are based on ISBN, so the same logical book works as a map key across branches.

### Shared `List<Branch>` Across Services

`BookServiceImpl`, `LibraryServiceImpl`, `BranchServiceImpl`, `ReservationServiceImpl`, and `RecommendationServiceImpl` all receive the same `List<Branch>` reference via their constructors. This means:

- A branch added via `BranchServiceImpl.addBranch()` is immediately visible to all other services.
- No synchronisation or cache invalidation is needed.
- There is a single source of truth for branch state.

This works because the system is single-threaded. In a multi-threaded environment, this shared mutable state would require a `ConcurrentHashMap` or a dedicated repository layer.

### `Set<String>` in `RecommendationServiceImpl`

The recommendation algorithm needs to answer two questions per candidate book:
- Has the patron already read a book by this author?
- Has the patron already read this book (by ISBN)?

Using a `List` for both would require `O(n)` contains checks per candidate. Using `HashSet<String>` for `readAuthors` and `readIsbns` reduces each membership check to **O(1)**, and naturally deduplicates (an author appearing in 10 borrowed books is stored once).

---

## Exception Design

### Why `RuntimeException`

All custom exceptions — `BookNotFoundException`, `PatronNotFoundException`, `BookNotAvailableException` — extend `RuntimeException` (unchecked). The alternative is checked exceptions (`Exception`), but:

- Checked exceptions force every caller to declare or catch them, polluting service interfaces and menu methods with noise that adds no real safety.
- These exceptions represent **programmer errors or invalid user input**, not recoverable states the caller should silently handle.
- `handleError()` in `Main.java` catches the general `Exception`, so all error paths are handled uniformly at the UI boundary.

### Static Factory Methods on Exceptions

`BookNotFoundException.withIsbn(isbn, branchName)` and `PatronNotFoundException.withId(patronId)` are static factory methods that format the error message consistently. Without them, every throw site would either duplicate the message format string or pass an inconsistently formatted string.

---

## Error Handling Design

### `handleError()` Lambda Wrapper

```java
private static void handleError(Runnable action) {
    try {
        action.run();
    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
        System.out.println("Please try again.");
    }
}
```

Every menu action is wrapped:

```java
case 1 -> handleError(() -> checkoutBook());
```

This design:
- **Prevents crashes** — no unhandled exception can reach `main()` and terminate the program
- **Keeps menu methods clean** — service methods throw naturally; no try/catch noise in business logic
- **Centralises error messaging** — the "Please try again." prompt appears consistently without duplication
- **Preserves stack traces in logs** — the logger in each service logs `ERROR` before throwing, so the cause is recorded even though the console shows a friendly message

---

## Alternatives Considered and Rejected

| Decision | Chosen | Rejected | Reason for Rejection |
|---|---|---|---|
| **Storage** | In-memory `HashMap`/`ArrayList` | Database (SQLite, H2) | Out of scope; adds JDBC/JPA complexity without demonstrating Java OOP concepts |
| **Implementation naming** | `*Impl` suffix (e.g. `BookServiceImpl`) | `I*` prefix on interface (e.g. `IBookService`) | `I` prefix is a C#/.NET convention; in Java, the interface carries the clean name and the implementation is the "special" case |
| **Enum location** | `com.library.model.enums` sub-package | Inner class of `Reservation` | Sub-package is more scalable — future enums (`BookGenre`, `PatronType`) have a natural home without bloating any single class |
| **Update method signature** | `updateBook(String isbn, Book updatedBook)` | `updateBook(String isbn, String title, String author, int year)` | Passing a whole object is extensible — adding a field doesn't change the method signature; individual parameters require a new overload per field combination |
| **Search design** | Generic `Searchable<T>` interface | Two separate `BookSearchable` and `PatronSearchable` interfaces | Duplication with no added expressiveness; the generic version is type-safe and DRY |
| **Copy tracking** | `Map<Book, Integer>` in `Branch` | `int copies` field in `Book` | A `copies` field on `Book` cannot represent per-branch availability; the same book object exists in multiple branches with different counts |

---

## Logging Strategy

SLF4J is used as the logging facade with Logback as the implementation. The three-level strategy:

| Level | When used |
|---|---|
| `INFO` | Successful business operations — book added, patron checked out, reservation fulfilled |
| `WARN` | Business rule violations that are not errors — e.g. attempting to reserve an available book |
| `ERROR` | Exception conditions logged immediately before throwing — book not found, patron not found, checkout unavailable |

Logging at `ERROR` level before throwing (rather than at the catch site in `Main`) means the full context (which service, which IDs) is captured in the log even though the console only shows a friendly message.
