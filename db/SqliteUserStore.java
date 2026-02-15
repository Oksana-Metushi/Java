import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class SqliteUserStore implements UserStore {
    private final Database db;

    public SqliteUserStore(Database db) {
        if (db == null) throw new IllegalArgumentException("db required");
        this.db = db;
    }

    @Override
    public void add(User user) {
        if (user == null) throw new IllegalArgumentException("user required");
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(username, password_hash, role) VALUES(?,?,?)")) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            // likely duplicate username
            throw new IllegalArgumentException("Username already exists: " + user.getUsername(), e);
        }
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.isBlank()) return null;
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT username, password_hash, role FROM users WHERE username = ? LIMIT 1")) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return toUser(rs.getString("username"), rs.getString("password_hash"), rs.getString("role"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(String username) {
        if (username == null || username.isBlank()) return false;
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE username = ?")) {
            ps.setString(1, username.trim());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> listAll() {
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT username, password_hash, role FROM users ORDER BY username")) {
            List<User> out = new ArrayList<>();
            while (rs.next()) {
                out.add(toUser(rs.getString("username"), rs.getString("password_hash"), rs.getString("role")));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasAnyUsers() {
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1 FROM users LIMIT 1")) {
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updatePasswordHash(String username, String newPasswordHash) {
        if (username == null || username.isBlank()) return false;
        if (newPasswordHash == null || newPasswordHash.isBlank()) return false;
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash = ? WHERE username = ?")) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, username.trim());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static User toUser(String username, String passwordHash, String roleRaw) {
        Role role = Role.valueOf(roleRaw.trim().toUpperCase());
        switch (role) {
            case STUDENT:
                return new Student(username, passwordHash, true);
            case LIBRARIAN:
                return new Librarian(username, passwordHash, true);
            case ADMIN:
                return new Admin(username, passwordHash, true);
            default:
                throw new IllegalArgumentException("Unknown role: " + roleRaw);
        }
    }
}

