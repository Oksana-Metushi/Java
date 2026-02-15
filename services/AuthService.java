public final class AuthService {
    private final UserStore userStore;

    public AuthService(UserStore userStore) {
        if (userStore == null) throw new IllegalArgumentException("userStore required");
        this.userStore = userStore;
    }

    public User authenticate(String username, String password) {
        User user = userStore.findByUsername(username);
        if (user == null) return null;
        return user.checkPassword(password) ? user : null;
    }
}

