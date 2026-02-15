# Library Management System (Java + SQLite)

This is a **school-friendly Library Management System** built in Java.

It supports:
- **Authentication** (username + password)
- **Role-based access control** (Student / Librarian / Admin)
- A **Web UI** (recommended), a **console app**, and optional **JavaFX desktop GUI** (requires JDK 8 or JavaFX SDK)
- **SQLite database** persistence (`library.db`)
- **Manual sorting algorithms** (Selection Sort + Bubble Sort) used in the console and in the website (client-side, no page reload)

---

## Features

### Roles
- **Student**
  - View/search books
  - Borrow and return books
  - View own loans
  - Register a new student account (console + web)
- **Librarian**
  - Add books
  - Remove books (only if there are no active loans)
  - View all loans / active loans
- **Admin**
  - Create users
  - Reset user passwords
  - Delete users (demo mode protects the default `admin` user)

### Demo accounts (created automatically)
- **Student**: `student1` / `student123`
- **Librarian**: `librarian1` / `lib123`
- **Admin**: `admin` / `admin123`

---

## Technologies used

- **Java (JDK)**: main language
- **JavaFX GUI**: Stage, Scene, Scene Graph, Panes (VBox, HBox, BorderPane), Shapes (Rectangle), Colors, ImageView, ListView, Buttons; **event handling** with **lambda expressions** and **MouseEvents**
- **Console UI**: `Scanner` input/output
- **Web server** (optional): Java built-in HTTP server; HTML + JavaScript + plain CSS (no Tailwind)
- **Database**:
  - **SQLite** (file-based DB)
  - **JDBC** (database access in Java)
  - SQLite JDBC driver jar in `lib/sqlite-jdbc.jar`

---

## Project structure

Source code is organized into folders (no Java `package` statements to keep it simple):

- `app/` – application entry points and app wiring
  - `WebUiServer.java` (web UI – run with `run-web.bat`)
  - `LibraryManagementSystem.java` (console – run with `run-console.bat`)
  - `LibraryManagementApp.java`, `LoginScene.java`, etc. (JavaFX GUI – optional, needs JDK 8 or JavaFX)
  - `AppContext.java` (creates services + DB)
- `models/` – core objects (`User`, `Book`, `Loan`, `Role`, etc.)
- `services/` – business logic (`AuthService`, `UserService`, `LibraryService`)
- `stores/` – interfaces and in-memory store (`UserStore`, `LibraryRepository`, `InMemoryUserStore`)
- `db/` – SQLite/JDBC implementations (`Database`, `SqliteUserStore`, `SqliteLibraryRepository`)
- `util/` – helpers (`PasswordUtil`, `SessionManager`)
- `web/` – web handlers + HTML rendering helpers
- `lib/` – external jar(s) (`sqlite-jdbc.jar`)
- `bin/` – compiled `.class` files (created by build)
- `library.db` – SQLite database file (created automatically)

---

## Requirements (installation)

1. **Install Java JDK**
   - Install **JDK 17+** (or any modern JDK).
   - Make sure `java` and `javac` work in your terminal:

```bash
java -version
javac -version
```

2. **SQLite**
   - No separate SQLite install needed because the project uses the SQLite JDBC driver.
   - The DB file is `library.db` in the project root.

---

## How to build the project

From the project folder, run:

```bat
build.bat
```

This compiles the console app, web app, and shared code into `bin/`. To build the **JavaFX GUI** as well, run `build-fx.bat` after `build.bat` (or use `run-fx.bat`, which runs both). Building the JavaFX app requires JDK 8 (JavaFX included) or JDK 11+ with the JavaFX SDK.

> Note (PowerShell): run batch files from the current folder using `.\`
>
> Example:
>
> ```powershell
> .\build.bat
> ```

---

## How to run (Web UI – recommended)

```bat
run-web.bat
```

PowerShell: `.\run-web.bat`

Then open **http://localhost:8080**. Demo logins: `student1` / `student123`, `librarian1` / `lib123`, `admin` / `admin123`.

---

## How to run (Console UI)

Run:

```bat
run-console.bat
```

PowerShell version:

```powershell
.\run-console.bat
```

---

## Notes for school project requirements (what concepts are used)

- **OOP**: inheritance (`User` → `Student` / `Librarian` / `Admin`), encapsulation (private fields + getters), separation of concerns (models/services/db/web)
- **RBAC**: `Role` enum controls which menu/actions are available
- **JavaFX GUI**:
  - **Stage** (window), **Scene** (content), **Scene Graph** (hierarchy of nodes)
  - **Panes**: VBox, HBox, BorderPane
  - **Shapes & Colors**: Rectangle, Color
  - **ImageView**: logo on login screen
  - **Event handling**: **MouseEvents** (e.g. setOnMouseClicked, setOnMouseEntered/Exited), **lambda expressions** for button actions (setOnAction)
- **Algorithms + arrays**:
  - Console: selection sort (by title) + bubble sort (by author) using `Book[]`
  - Web: JavaScript sorting of the books table (no reload)
- **Persistence**: users/books/loans saved in SQLite (`library.db`)

