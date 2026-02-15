import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class UserService {
    private final UserStore userStore;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    public UserService(UserStore userStore) {
        if (userStore == null) throw new IllegalArgumentException("userStore required");
        this.userStore = userStore;
    }

    public User createUser(String username, String password, Role role) {
        validateUsernameAndPassword(username, password);
        if (role == null) throw new IllegalArgumentException("role required");
        User u;
        switch (role) {
            case STUDENT:
                u = new Student(username, password);
                break;
            case LIBRARIAN:
                u = new Librarian(username, password);
                break;
            case ADMIN:
                u = new Admin(username, password);
                break;
            default:
                throw new IllegalStateException("Unexpected role: " + role);
        }
        userStore.add(u);
        return u;
    }

    public boolean deleteUser(String username) {
        return userStore.delete(username);
    }

    public boolean resetPassword(String username, String newPassword) {
        User u = userStore.findByUsername(username);
        if (u == null) return false;
        validatePasswordOnly(newPassword);
        u.setPassword(newPassword);
        if (userStore instanceof SqliteUserStore) {
            SqliteUserStore sqlite = (SqliteUserStore) userStore;
            return sqlite.updatePasswordHash(u.getUsername(), u.getPasswordHash());
        }
        return true;
    }

    public List<User> listUsers() {
        List<User> users = userStore.listAll();
        Collections.sort(users);
        return users;
    }

    private static void validateUsernameAndPassword(String username, String password) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username required");
        String u = username.trim();
        if (!USERNAME_PATTERN.matcher(u).matches()) {
            throw new IllegalArgumentException("Username must be 3-20 chars: letters, numbers, underscore");
        }
        validatePasswordOnly(password);
    }

    private static void validatePasswordOnly(String password) {
        if (password == null) throw new IllegalArgumentException("password required");
        if (password.trim().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
    }
}

