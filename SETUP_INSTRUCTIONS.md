# Setup Instructions

Step-by-step guide to clone, build, run, and use the Library Management System.

---

## Prerequisites

| Tool | Minimum Version | Download |
|---|---|---|
| **Java JDK** | 25 | [https://jdk.java.net/25/](https://jdk.java.net/25/) |
| **Apache Maven** | 3.8+ | [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) |
| **IntelliJ IDEA** | Community 2023+ | [https://www.jetbrains.com/idea/download/](https://www.jetbrains.com/idea/download/) |
| **Git** | Any recent version | [https://git-scm.com/downloads](https://git-scm.com/downloads) |

Verify your environment before starting:

```bash
java -version      # should print: openjdk 25 ...
mvn -version       # should print: Apache Maven 3.x.x ...
git --version      # should print: git version 2.x.x
```

---

## Step 1 — Clone the Repository

```bash
git clone <repository-url>
cd library-management-system
```

---

## Step 2 — Open in IntelliJ IDEA

1. Launch IntelliJ IDEA
2. Click **File → Open**
3. Navigate to and select the `library-management-system` folder (the one containing `pom.xml`)
4. Click **OK**
5. IntelliJ will detect the Maven project and begin importing — watch the progress bar at the bottom
6. Wait until **"Maven project imported"** or **"Build completed"** appears in the status bar (this downloads `slf4j-api` and `logback-classic` automatically)

> If IntelliJ does not detect the Maven project automatically: right-click `pom.xml` → **Add as Maven Project**

---

## Step 3 — Build the Project

### Using IntelliJ

- **Build → Build Project** (or press `Ctrl+F9` / `Cmd+F9`)
- The **Build** panel at the bottom should show: `Build completed successfully`

### Using Maven Terminal

```bash
mvn clean install
```

Expected output ends with:
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXX s
```

---

## Step 4 — Run the Application

### Using IntelliJ

1. Navigate to `src/main/java/com/library/Main.java`
2. Click the green **▶ Run** button in the left gutter next to `public static void main`
3. The console opens in the **Run** panel at the bottom

### Using Maven Terminal

```bash
mvn exec:java -Dexec.mainClass="com.library.Main"
```

### Expected Startup Output

```
Sample data loaded.
Branches: Main Branch (B001), City Branch (B002)
Patrons: John Doe (P001), Jane Smith (P002)
Books: ISBN001, ISBN002 in Main Branch | ISBN003, ISBN004 in City Branch

===== LIBRARY MANAGEMENT SYSTEM =====
1. Book Management
2. Patron Management
3. Lending
4. Branch Management
5. Reservations
6. Recommendations
0. Exit
Choose:
```

---

## Package Structure

```
src/
└── main/
    ├── java/com/library/
    │   ├── model/              Domain entities: Book, Patron, Branch, LendingRecord, Reservation
    │   │   └── enums/          ReservationStatus enum (PENDING, FULFILLED, CANCELLED)
    │   ├── interfaces/         Service contracts: BookService, PatronService, LibraryService, etc.
    │   ├── service/            Business logic implementations for each interface
    │   ├── observer/           LibraryObserver interface + NotificationService implementation
    │   ├── factory/            BookFactory and PatronFactory with input validation
    │   ├── exception/          BookNotFoundException, PatronNotFoundException, BookNotAvailableException
    │   ├── util/               Validator (input rules) and IdGenerator (P001, R001, RES001, B001)
    │   └── Main.java           Entry point, console menus, error handling
    └── resources/
        └── logback.xml         Logging config: console + rolling file output
```

---

## Dependencies

Declared in `pom.xml` — downloaded automatically by Maven on first build.

| Dependency | Version | Purpose |
|---|---|---|
| `slf4j-api` | 2.0.13 | Logging facade — `Logger` and `LoggerFactory` used in every service |
| `logback-classic` | 1.5.18 | Logging implementation — writes to console and `logs/library.log` |

No other external dependencies. All data structures (`HashMap`, `ArrayList`, `HashSet`) are from the Java standard library.

---

## How to Use the System

### Menu Navigation

The system uses numbered menus. Type the number and press **Enter** at any prompt.
- `0` always returns to the previous menu or exits
- Invalid input shows `"Invalid choice. Try again."` — the program never crashes

### Sample Data Available on Startup

You do not need to add any data to test the system. The following is loaded automatically:

| ID | Type | Details |
|---|---|---|
| B001 | Branch | Main Branch |
| B002 | Branch | City Branch |
| ISBN001 | Book | *Clean Code* by Robert Martin — 3 copies @ Main |
| ISBN002 | Book | *The Pragmatic Programmer* by Andrew Hunt — 2 copies @ Main |
| ISBN003 | Book | *Refactoring* by Robert Martin — 2 copies @ City |
| ISBN004 | Book | *Design Patterns* by Gang of Four — 1 copy @ City |
| P001 | Patron | John Doe |
| P002 | Patron | Jane Smith |

---

## Example Flows

### Flow 1 — Checkout a Book

```
Main Menu → 3 (Lending) → 1 (Checkout Book)

Select book:    1 (Clean Code)
Select patron:  1 (John Doe)
Select branch:  1 (Main Branch)

Output: Checkout successful. Record ID: R001
```

The available copy count for *Clean Code* at Main Branch decreases by 1.

---

### Flow 2 — Return a Book

```
Main Menu → 3 (Lending) → 2 (Return Book)

Active lending records are listed.
Select record:  1 (R001)

Output: Return successful. Book: Clean Code
```

The available copy count is restored. Any pending reservations for this book are automatically fulfilled.

---

### Flow 3 — Reserve a Book and See Auto-Fulfil

This flow requires a book to have zero available copies.

**Step A — Checkout all copies of Design Patterns (1 copy @ City Branch)**
```
Main Menu → 3 (Lending) → 1 (Checkout Book)
Select book: 4 (Design Patterns), patron: 1 (John Doe), branch: 2 (City Branch)
```

**Step B — Reserve the now-unavailable book**
```
Main Menu → 5 (Reservations) → 1 (Reserve Book)
Select book: 4 (Design Patterns), patron: 2 (Jane Smith)

Output: Reserved. ID: RES001
```

**Step C — Return the book**
```
Main Menu → 3 (Lending) → 2 (Return Book)
Select active record for Design Patterns

Output: Return successful. Book: Design Patterns
Console also shows: [NOTIFICATION] Reservation fulfilled for Jane Smith — Design Patterns
```

The Observer pattern automatically fulfilled Jane Smith's reservation the moment the book was returned.

---

### Flow 4 — Get Recommendations After Borrowing

This flow uses the checkout from Flow 1 (John Doe borrowed *Clean Code* by Robert Martin).

```
Main Menu → 6 (Recommendations)
Select patron: 1 (John Doe)

Output: Recommended Books:
  [ISBN003] 'Refactoring' by Robert Martin (2018)
```

*Refactoring* is recommended because it shares the same author (Robert Martin) as *Clean Code*, which John Doe has already read. *Clean Code* itself is excluded (already read). Books by other authors may also appear as fill-up recommendations.

---

## Logs

### Console Logs

Appear in real time during the session. Format:

```
HH:mm:ss.SSS LEVEL  [logger] - message
```

Example:
```
10:32:14.201 INFO  [LibraryServiceImpl] - Book 'Clean Code' checked out by patron 'John Doe'
10:32:19.445 ERROR [PatronServiceImpl]  - Patron not found with ID 'P999'
```

### File Logs

Written to `logs/library.log` in the project root directory. The file is created automatically on first run.

- **Rolling policy** — a new file is created each day
- **Retention** — files older than 30 days are deleted automatically
- **Format** — same as console but with full date: `yyyy-MM-dd HH:mm:ss.SSS`

```
library-management-system/
└── logs/
    ├── library.log              ← current session
    ├── library.2025-01-14.log   ← yesterday's log
    └── library.2025-01-13.log   ← day before
```

---

## Common Issues

| Issue | Symptom | Fix |
|---|---|---|
| **Dependencies not downloaded** | `ClassNotFoundException: org.slf4j.Logger` at startup | Run `mvn clean install` to force Maven to download all dependencies |
| **Wrong Java version** | Compile error: `error: source release 25 requires target release 25` | Install JDK 25 and set it as the project SDK in IntelliJ: **File → Project Structure → SDK** |
| **Main class not found** (Maven exec) | `Could not find or load main class com.library.Main` | Ensure you ran `mvn clean install` first before `mvn exec:java` |
| **`logs/` folder not created** | No `library.log` file appears | The folder is created automatically by Logback on first run — ensure the process has write permission in the project root |
| **IntelliJ not recognising source root** | Red imports, cannot find classes | Right-click `src/main/java` → **Mark Directory as → Sources Root** |
| **Build fails on Windows with path spaces** | Maven error with path | Ensure the project is cloned to a path with no spaces (e.g. `C:\projects\library-management-system`) |
