public final class AppContext {
    private final Database db = new Database("library.db");
    private final UserStore userStore;
    private final AuthService authService;
    private final UserService userService;
    private final LibraryService libraryService;

    public AppContext() {
        db.init();
        this.userStore = new SqliteUserStore(db);
        this.authService = new AuthService(userStore);
        this.userService = new UserService(userStore);
        this.libraryService = new LibraryService(new SqliteLibraryRepository(db));
        seedDemoData();
    }

    private void seedDemoData() {
        // Ensure staff accounts exist even if a student registers first.
        if (userStore.findByUsername("admin") == null) {
            userService.createUser("admin", "admin123", Role.ADMIN);
        }
        if (userStore.findByUsername("librarian1") == null) {
            userService.createUser("librarian1", "lib123", Role.LIBRARIAN);
        }
        // Optional demo student (won't overwrite if already created).
        if (userStore.findByUsername("student1") == null) {
            userService.createUser("student1", "student123", Role.STUDENT);
        }

        // Demo books
        libraryService.seedDemoData();
    }

    public AuthService auth() {
        return authService;
    }

    public UserService users() {
        return userService;
    }

    public LibraryService library() {
        return libraryService;
    }

    public User findUser(String username) {
        return userStore.findByUsername(username);
    }
}

