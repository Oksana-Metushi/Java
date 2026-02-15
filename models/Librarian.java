public final class Librarian extends User {
    public Librarian(String username, String rawPassword) {
        super(username, rawPassword, Role.LIBRARIAN);
    }

    Librarian(String username, String passwordHash, boolean passwordIsHash) {
        super(username, passwordHash, Role.LIBRARIAN, passwordIsHash);
    }
}

