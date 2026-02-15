import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public final class WebUiServer {
    private static final String COOKIE_NAME = "SESSIONID";

    private final AppContext app = new AppContext();
    private final SessionManager sessions = new SessionManager();

    public static void main(String[] args) throws Exception {
        new WebUiServer().start(8080);
    }

    private void start(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        WebHandlers h = new WebHandlers(app, sessions, COOKIE_NAME);
        // Static files (images, etc.)
        server.createContext("/assets/", h::handleAssets);
        server.createContext("/", h::handleRoot);
        server.createContext("/login", h::handleLogin);
        server.createContext("/register", h::handleRegister);
        server.createContext("/logout", h::handleLogout);
        server.createContext("/dashboard", h::handleDashboard);

        server.createContext("/student", h::handleStudent);
        server.createContext("/student/borrow", h::handleStudentBorrow);
        server.createContext("/student/return", h::handleStudentReturn);

        server.createContext("/librarian", h::handleLibrarian);
        server.createContext("/librarian/addBook", h::handleLibrarianAddBook);
        server.createContext("/librarian/removeBook", h::handleLibrarianRemoveBook);

        server.createContext("/admin", h::handleAdmin);
        server.createContext("/admin/createUser", h::handleAdminCreateUser);
        server.createContext("/admin/resetPassword", h::handleAdminResetPassword);
        server.createContext("/admin/deleteUser", h::handleAdminDeleteUser);

        // JSON API used by the static dashboard pages
        server.createContext("/api/me", h::handleApiMe);

        server.createContext("/api/student/books", h::handleApiStudentBooks);
        server.createContext("/api/student/loans", h::handleApiStudentLoans);

        server.createContext("/api/librarian/books", h::handleApiLibrarianBooks);
        server.createContext("/api/librarian/loans/active", h::handleApiLibrarianActiveLoans);

        server.createContext("/api/admin/users", h::handleApiAdminUsers);

        server.setExecutor(null);
        server.start();

        System.out.println("Web UI running at http://localhost:" + port);
        System.out.println("Demo logins: student1/student123 | librarian1/lib123 | admin/admin123");
    }
}

