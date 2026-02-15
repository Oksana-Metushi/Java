import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class SqliteLibraryRepository implements LibraryRepository {
    private final Database db;

    public SqliteLibraryRepository(Database db) {
        if (db == null) throw new IllegalArgumentException("db required");
        this.db = db;
    }

    @Override
    public Book addBook(String title, String author, int copies) {
        // validate via Book constructor after insert
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title required");
        if (author == null || author.isBlank()) throw new IllegalArgumentException("author required");
        if (copies <= 0) throw new IllegalArgumentException("copies must be > 0");

        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO books(title, author, total_copies, available_copies, is_deleted) VALUES(?,?,?,?,0)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title.trim());
            ps.setString(2, author.trim());
            ps.setInt(3, copies);
            ps.setInt(4, copies);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No id generated");
                int id = keys.getInt(1);
                return new Book(id, title, author, copies, copies);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeBook(int bookId) {
        if (bookId <= 0) return false;
        try (Connection c = db.connect()) {
            // active loans?
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT 1 FROM loans WHERE book_id = ? AND return_date IS NULL LIMIT 1")) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return false;
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE books SET is_deleted = 1 WHERE id = ? AND is_deleted = 0")) {
                ps.setInt(1, bookId);
                return ps.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Book> listBooks() {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, title, author, total_copies, available_copies FROM books WHERE is_deleted = 0 ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            List<Book> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")
                ));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Book> searchBooks(String query) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, title, author, total_copies, available_copies " +
                             "FROM books " +
                             "WHERE is_deleted = 0 AND (LOWER(title) LIKE ? OR LOWER(author) LIKE ?) " +
                             "ORDER BY id")) {
            String like = "%" + q + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Book> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("total_copies"),
                            rs.getInt("available_copies")
                    ));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Book findBook(int bookId) {
        if (bookId <= 0) return null;
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, title, author, total_copies, available_copies FROM books WHERE id = ? AND is_deleted = 0 LIMIT 1")) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Loan borrowBook(String username, int bookId) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username required");
        if (bookId <= 0) return null;

        String u = username.trim();
        LocalDate today = LocalDate.now();

        try (Connection c = db.connect()) {
            c.setAutoCommit(false);
            try {
                // book exists, not deleted, available > 0
                int available;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT available_copies FROM books WHERE id = ? AND is_deleted = 0 LIMIT 1")) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return null;
                        }
                        available = rs.getInt("available_copies");
                        if (available <= 0) {
                            c.rollback();
                            return null;
                        }
                    }
                }

                // insert loan (unique partial index prevents double-borrow of same book)
                int loanId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO loans(username, book_id, loan_date, return_date) VALUES(?,?,?,NULL)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, u);
                    ps.setInt(2, bookId);
                    ps.setString(3, today.toString());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No loan id generated");
                        loanId = keys.getInt(1);
                    }
                }

                // decrement available copies
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0")) {
                    ps.setInt(1, bookId);
                    if (ps.executeUpdate() != 1) {
                        c.rollback();
                        return null;
                    }
                }

                c.commit();
                return new Loan(loanId, u, bookId, today);
            } catch (SQLException e) {
                c.rollback();
                // duplicate active loan -> treat as failed borrow
                return null;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean returnBook(String username, int bookId) {
        if (username == null || username.isBlank()) return false;
        if (bookId <= 0) return false;

        String u = username.trim();
        LocalDate today = LocalDate.now();

        try (Connection c = db.connect()) {
            c.setAutoCommit(false);
            try {
                Integer loanId = null;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT id FROM loans WHERE username = ? AND book_id = ? AND return_date IS NULL LIMIT 1")) {
                    ps.setString(1, u);
                    ps.setInt(2, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) loanId = rs.getInt("id");
                    }
                }
                if (loanId == null) {
                    c.rollback();
                    return false;
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE loans SET return_date = ? WHERE id = ? AND return_date IS NULL")) {
                    ps.setString(1, today.toString());
                    ps.setInt(2, loanId);
                    if (ps.executeUpdate() != 1) {
                        c.rollback();
                        return false;
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE books " +
                                "SET available_copies = CASE WHEN available_copies < total_copies THEN available_copies + 1 ELSE available_copies END " +
                                "WHERE id = ?")) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }

                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                return false;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Loan> listLoansForUser(String username) {
        if (username == null || username.isBlank()) return List.of();
        String u = username.trim();
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, username, book_id, loan_date, return_date FROM loans WHERE username = ? ORDER BY id")) {
            ps.setString(1, u);
            try (ResultSet rs = ps.executeQuery()) {
                List<Loan> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(toLoan(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Loan> listAllLoans() {
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, username, book_id, loan_date, return_date FROM loans ORDER BY id")) {
            List<Loan> out = new ArrayList<>();
            while (rs.next()) out.add(toLoan(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Loan> listActiveLoans() {
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, username, book_id, loan_date, return_date FROM loans WHERE return_date IS NULL ORDER BY id")) {
            List<Loan> out = new ArrayList<>();
            while (rs.next()) out.add(toLoan(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasAnyBooks() {
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1 FROM books WHERE is_deleted = 0 LIMIT 1")) {
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Loan toLoan(ResultSet rs) throws SQLException {
        LocalDate loanDate = LocalDate.parse(rs.getString("loan_date"));
        String returnRaw = rs.getString("return_date");
        LocalDate returnDate = (returnRaw == null) ? null : LocalDate.parse(returnRaw);
        return new Loan(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getInt("book_id"),
                loanDate,
                returnDate
        );
    }
}

