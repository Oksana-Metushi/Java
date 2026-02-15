import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private final String url;

    public Database(String dbFilePath) {
        if (dbFilePath == null || dbFilePath.isBlank()) throw new IllegalArgumentException("dbFilePath required");
        this.url = "jdbc:sqlite:" + dbFilePath;
    }

    public Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    public void init() {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.execute(
                    "CREATE TABLE IF NOT EXISTS users ("
                            + " username TEXT PRIMARY KEY COLLATE NOCASE,"
                            + " password_hash TEXT NOT NULL,"
                            + " role TEXT NOT NULL"
                            + ")"
            );

            st.execute(
                    "CREATE TABLE IF NOT EXISTS books ("
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + " title TEXT NOT NULL,"
                            + " author TEXT NOT NULL,"
                            + " total_copies INTEGER NOT NULL,"
                            + " available_copies INTEGER NOT NULL,"
                            + " is_deleted INTEGER NOT NULL DEFAULT 0"
                            + ")"
            );

            st.execute(
                    "CREATE TABLE IF NOT EXISTS loans ("
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + " username TEXT NOT NULL COLLATE NOCASE,"
                            + " book_id INTEGER NOT NULL,"
                            + " loan_date TEXT NOT NULL,"
                            + " return_date TEXT NULL,"
                            + " FOREIGN KEY(username) REFERENCES users(username) ON DELETE CASCADE,"
                            + " FOREIGN KEY(book_id) REFERENCES books(id) ON DELETE RESTRICT"
                            + ")"
            );

            st.execute(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_loans_active_unique "
                            + "ON loans(username, book_id) "
                            + "WHERE return_date IS NULL"
            );

            st.execute("CREATE INDEX IF NOT EXISTS idx_loans_user ON loans(username)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_loans_book ON loans(book_id)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_books_deleted ON books(is_deleted)");
        } catch (SQLException e) {
            throw new RuntimeException("DB init failed: " + e.getMessage(), e);
        }
    }
}

