import java.util.List;

public final class LibraryService {
    private final LibraryRepository repo;

    public LibraryService(LibraryRepository repo) {
        if (repo == null) throw new IllegalArgumentException("repo required");
        this.repo = repo;
    }

    public Book addBook(String title, String author, int copies) {
        return repo.addBook(title, author, copies);
    }

    public boolean removeBook(int bookId) {
        return repo.removeBook(bookId);
    }

    public List<Book> listBooks() {
        return repo.listBooks();
    }

    public List<Book> searchBooks(String query) {
        return repo.searchBooks(query);
    }

    public Book findBook(int bookId) {
        return repo.findBook(bookId);
    }

    public Loan borrowBook(String username, int bookId) {
        return repo.borrowBook(username, bookId);
    }

    public boolean returnBook(String username, int bookId) {
        return repo.returnBook(username, bookId);
    }

    public List<Loan> listLoansForUser(String username) {
        return repo.listLoansForUser(username);
    }

    public List<Loan> listAllLoans() {
        return repo.listAllLoans();
    }

    public List<Loan> listActiveLoans() {
        return repo.listActiveLoans();
    }

    public void seedDemoData() {
        if (repo.hasAnyBooks()) return;
        repo.addBook("Clean Code", "Robert C. Martin", 3);
        repo.addBook("Effective Java", "Joshua Bloch", 2);
        repo.addBook("Introduction to Algorithms", "CLRS", 1);
    }
}

