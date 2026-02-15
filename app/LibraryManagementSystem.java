import java.util.List;
import java.util.Scanner;

public final class LibraryManagementSystem {
    private final Scanner scanner = new Scanner(System.in);
    private final AppContext app = new AppContext();

    public static void main(String[] args) {
        new LibraryManagementSystem().run();
    }

    private void run() {
        System.out.println("=== Library Management System ===");
        while (true) {
            User user = loginLoop();
            if (user == null) {
                System.out.println("Goodbye.");
                return;
            }

            switch (user.getRole()) {
                case STUDENT:
                    studentMenu(user);
                    break;
                case LIBRARIAN:
                    librarianMenu(user);
                    break;
                case ADMIN:
                    adminMenu(user);
                    break;
                default:
                    System.out.println("Unknown role.");
            }
        }
    }

    private User loginLoop() {
        while (true) {
            System.out.println();
            System.out.println("1) Login");
            System.out.println("2) Register (Student)");
            System.out.println("0) Exit");
            int choice = readInt("Choose: ");
            if (choice == 0) return null;

            if (choice == 2) {
                registerStudent();
                continue;
            }

            if (choice != 1) {
                System.out.println("Invalid choice.");
                continue;
            }

            String username = readLine("Username: ");
            String password = readLine("Password: ");
            User user = app.auth().authenticate(username, password);
            if (user == null) {
                System.out.println("Login failed. Try again.");
                continue;
            }
            System.out.println("Login successful. Welcome " + user.getUsername() + " (" + user.getRole() + ").");
            return user;
        }
    }

    private void registerStudent() {
        System.out.println();
        System.out.println("=== Student Registration ===");
        String username = readLine("Choose a username: ");
        String password = readLine("Choose a password: ");
        try {
            app.users().createUser(username, password, Role.STUDENT);
            System.out.println("Registration successful. You can now login.");
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    // ===== STUDENT =====
    private void studentMenu(User user) {
        while (true) {
            System.out.println();
            System.out.println("=== Student Menu ===");
            System.out.println("1) List books");
            System.out.println("2) Search books");
            System.out.println("3) Borrow book");
            System.out.println("4) Return book");
            System.out.println("5) View my loans");
            System.out.println("0) Logout");

            int choice = readInt("Choose: ");
            switch (choice) {
                case 1:
                    listBooks();
                    break;
                case 2:
                    searchBooks();
                    break;
                case 3:
                    borrowBook(user);
                    break;
                case 4:
                    returnBook(user);
                    break;
                case 5:
                    viewMyLoans(user);
                    break;
                case 0:
                    System.out.println("Logged out.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ===== LIBRARIAN =====
    private void librarianMenu(User user) {
        while (true) {
            System.out.println();
            System.out.println("=== Librarian Menu ===");
            System.out.println("1) List books");
            System.out.println("2) Add book");
            System.out.println("3) Remove book (only if no active loans)");
            System.out.println("4) View all loans");
            System.out.println("5) View active loans");
            System.out.println("0) Logout");

            int choice = readInt("Choose: ");
            switch (choice) {
                case 1:
                    listBooks();
                    break;
                case 2:
                    addBook();
                    break;
                case 3:
                    removeBook();
                    break;
                case 4:
                    viewAllLoans();
                    break;
                case 5:
                    viewActiveLoans();
                    break;
                case 0:
                    System.out.println("Logged out.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ===== ADMIN =====
    private void adminMenu(User user) {
        while (true) {
            System.out.println();
            System.out.println("=== Admin Menu ===");
            System.out.println("1) List users");
            System.out.println("2) Create user");
            System.out.println("3) Reset user password");
            System.out.println("4) Delete user");
            System.out.println("0) Logout");

            int choice = readInt("Choose: ");
            switch (choice) {
                case 1:
                    listUsers();
                    break;
                case 2:
                    createUser();
                    break;
                case 3:
                    resetPassword();
                    break;
                case 4:
                    deleteUser();
                    break;
                case 0:
                    System.out.println("Logged out.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ===== Shared actions =====
    private void listBooks() {
        List<Book> books = app.library().listBooks();
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        books = applyBookSortForConsole(books);
        System.out.println("--- Books ---");
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private void searchBooks() {
        String q = readLine("Search (title/author): ");
        List<Book> books = app.library().searchBooks(q);
        if (books.isEmpty()) {
            System.out.println("No matching books.");
            return;
        }
        books = applyBookSortForConsole(books);
        System.out.println("--- Results ---");
        for (Book b : books) {
            System.out.println(b);
        }
    }

    // Uses ARRAYS + manual sorting algorithms (Selection/Bubble)
    private List<Book> applyBookSortForConsole(List<Book> books) {
        System.out.println();
        System.out.println("Sort books?");
        System.out.println("1) By Title (Selection Sort)");
        System.out.println("2) By Author (Bubble Sort)");
        System.out.println("0) No sorting");
        int choice = readInt("Choose: ");

        if (choice != 1 && choice != 2) return books;

        Book[] arr = books.toArray(new Book[0]); // Array usage
        if (choice == 1) {
            selectionSortBooksByTitle(arr);
        } else {
            bubbleSortBooksByAuthor(arr);
        }

        // Convert back to List
        java.util.ArrayList<Book> out = new java.util.ArrayList<Book>();
        for (int i = 0; i < arr.length; i++) {
            out.add(arr[i]);
        }
        return out;
    }

    private void selectionSortBooksByTitle(Book[] arr) {
        for (int i = 0; i < arr.length; i++) {
            int minIndex = i;
            for (int j = i + 1; j < arr.length; j++) {
                String a = arr[j].getTitle();
                String b = arr[minIndex].getTitle();
                if (a.compareToIgnoreCase(b) < 0) {
                    minIndex = j;
                }
            }
            Book tmp = arr[i];
            arr[i] = arr[minIndex];
            arr[minIndex] = tmp;
        }
    }

    private void bubbleSortBooksByAuthor(Book[] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                String a = arr[j].getAuthor();
                String b = arr[j + 1].getAuthor();
                if (a.compareToIgnoreCase(b) > 0) {
                    Book tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
                }
            }
        }
    }

    private void borrowBook(User user) {
        int bookId = readInt("Book id to borrow: ");
        Loan loan = app.library().borrowBook(user.getUsername(), bookId);
        if (loan == null) {
            System.out.println("Borrow failed (book not found, unavailable, or already borrowed by you).");
            return;
        }
        System.out.println("Borrowed successfully. " + loan);
    }

    private void returnBook(User user) {
        int bookId = readInt("Book id to return: ");
        boolean ok = app.library().returnBook(user.getUsername(), bookId);
        System.out.println(ok ? "Returned successfully." : "Return failed (no active loan found).");
    }

    private void viewMyLoans(User user) {
        List<Loan> loans = app.library().listLoansForUser(user.getUsername());
        if (loans.isEmpty()) {
            System.out.println("You have no loans.");
            return;
        }
        System.out.println("--- My Loans ---");
        for (Loan l : loans) {
            System.out.println(l);
        }
    }

    private void addBook() {
        String title = readLine("Title: ");
        String author = readLine("Author: ");
        int copies = readInt("Copies: ");
        try {
            Book b = app.library().addBook(title, author, copies);
            System.out.println("Added: " + b);
        } catch (Exception e) {
            System.out.println("Could not add book: " + e.getMessage());
        }
    }

    private void removeBook() {
        int bookId = readInt("Book id to remove: ");
        boolean ok = app.library().removeBook(bookId);
        System.out.println(ok ? "Removed." : "Remove failed (book missing or has active loans).");
    }

    private void viewAllLoans() {
        List<Loan> loans = app.library().listAllLoans();
        if (loans.isEmpty()) {
            System.out.println("No loans found.");
            return;
        }
        System.out.println("--- All Loans ---");
        for (Loan l : loans) {
            System.out.println(l);
        }
    }

    private void viewActiveLoans() {
        List<Loan> loans = app.library().listActiveLoans();
        if (loans.isEmpty()) {
            System.out.println("No active loans.");
            return;
        }
        System.out.println("--- Active Loans ---");
        for (Loan l : loans) {
            System.out.println(l);
        }
    }

    private void listUsers() {
        List<User> users = app.users().listUsers();
        if (users.isEmpty()) {
            System.out.println("No users.");
            return;
        }
        System.out.println("--- Users ---");
        for (User u : users) {
            System.out.println(u);
        }
    }

    private void createUser() {
        String username = readLine("New username: ");
        String password = readLine("New password: ");
        Role role = readRole();
        try {
            User u = app.users().createUser(username, password, role);
            System.out.println("Created: " + u);
        } catch (Exception e) {
            System.out.println("Could not create user: " + e.getMessage());
        }
    }

    private void resetPassword() {
        String username = readLine("Username to reset: ");
        String password = readLine("New password: ");
        boolean ok = app.users().resetPassword(username, password);
        System.out.println(ok ? "Password updated." : "User not found.");
    }

    private void deleteUser() {
        String username = readLine("Username to delete: ");
        if ("admin".equalsIgnoreCase(username.trim())) {
            System.out.println("Refusing to delete the default admin in demo mode.");
            return;
        }
        boolean ok = app.users().deleteUser(username);
        System.out.println(ok ? "Deleted." : "User not found.");
    }

    // ===== Input helpers =====
    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private int readInt(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private Role readRole() {
        while (true) {
            System.out.println("Role:");
            System.out.println("1) Student");
            System.out.println("2) Librarian");
            System.out.println("3) Admin");
            int choice = readInt("Choose role: ");
            switch (choice) {
                case 1:
                    return Role.STUDENT;
                case 2:
                    return Role.LIBRARIAN;
                case 3:
                    return Role.ADMIN;
                default:
                    System.out.println("Invalid role choice.");
            }
        }
    }
}

