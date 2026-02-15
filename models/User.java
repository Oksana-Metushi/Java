public abstract class User implements Comparable<User> {
    private final String username;
    private String passwordHash;
    private final Role role;

    protected User(String username, String rawPassword, Role role) {
        this(username, rawPassword, role, false);
    }

    protected User(String username, String passwordOrHash, Role role, boolean passwordIsHash) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username required");
        if (passwordOrHash == null) throw new IllegalArgumentException("password required");
        if (role == null) throw new IllegalArgumentException("role required");
        this.username = username.trim();
        this.passwordHash = passwordIsHash ? passwordOrHash : PasswordUtil.hash(passwordOrHash);
        this.role = role;
    }

    public final String getUsername() {
        return username;
    }

    public final Role getRole() {
        return role;
    }

    public final String getPasswordHash() {
        return passwordHash;
    }

    public final boolean checkPassword(String rawPassword) {
        return PasswordUtil.matches(rawPassword, passwordHash);
    }

    public final void setPassword(String newRawPassword) {
        if (newRawPassword == null) throw new IllegalArgumentException("password required");
        this.passwordHash = PasswordUtil.hash(newRawPassword);
    }

    @Override
    public String toString() {
        return "User{username='" + username + "', role=" + role + "}";
    }

    @Override
    public int compareTo(User other) {
        if (other == null) return 1;
        return this.username.compareToIgnoreCase(other.username);
    }
}

