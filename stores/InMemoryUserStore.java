import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InMemoryUserStore implements UserStore {
    private final Map<String, User> usersByUsername = new HashMap<>();

    public InMemoryUserStore() {}

    @Override
    public void add(User user) {
        if (user == null) throw new IllegalArgumentException("user required");
        String key = user.getUsername().toLowerCase();
        if (usersByUsername.containsKey(key)) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        usersByUsername.put(key, user);
    }

    @Override
    public User findByUsername(String username) {
        if (username == null) return null;
        return usersByUsername.get(username.trim().toLowerCase());
    }

    @Override
    public boolean delete(String username) {
        if (username == null) return false;
        return usersByUsername.remove(username.trim().toLowerCase()) != null;
    }

    @Override
    public List<User> listAll() {
        return new ArrayList<>(usersByUsername.values());
    }

    @Override
    public boolean hasAnyUsers() {
        return !usersByUsername.isEmpty();
    }
}

