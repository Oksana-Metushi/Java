import java.util.List;

public interface LibraryRepository {
    Book addBook(String title, String author, int copies);

    boolean removeBook(int bookId); // soft delete

    List<Book> listBooks();

    List<Book> searchBooks(String query);

    Book findBook(int bookId);

    Loan borrowBook(String username, int bookId);

    boolean returnBook(String username, int bookId);

    List<Loan> listLoansForUser(String username);

    List<Loan> listAllLoans();

    List<Loan> listActiveLoans();

    boolean hasAnyBooks();
}

