public final class Student extends User {
    public Student(String username, String rawPassword) {
        super(username, rawPassword, Role.STUDENT);
    }

    Student(String username, String passwordHash, boolean passwordIsHash) {
        super(username, passwordHash, Role.STUDENT, passwordIsHash);
    }
}

