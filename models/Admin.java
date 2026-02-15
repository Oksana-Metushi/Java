public final class Admin extends User {
    public Admin(String username, String rawPassword) {
        super(username, rawPassword, Role.ADMIN);
    }

    Admin(String username, String passwordHash, boolean passwordIsHash) {
        super(username, passwordHash, Role.ADMIN, passwordIsHash);
    }
}

