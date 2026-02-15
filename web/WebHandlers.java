import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public final class WebHandlers {
    private final AppContext app;
    private final SessionManager sessions;
    private final String cookieName;

    public WebHandlers(AppContext app, SessionManager sessions, String cookieName) {
        if (app == null) throw new IllegalArgumentException("app required");
        if (sessions == null) throw new IllegalArgumentException("sessions required");
        if (cookieName == null || cookieName.isBlank()) throw new IllegalArgumentException("cookieName required");
        this.app = app;
        this.sessions = sessions;
        this.cookieName = cookieName;
    }

    public void handleAssets(HttpExchange ex) throws IOException {
        // Very small static file handler for /assets/*
        String reqPath = ex.getRequestURI() == null ? "" : ex.getRequestURI().getPath();
        if (reqPath == null || !reqPath.startsWith("/assets/")) {
            WebHttpUtil.sendHtml(ex, 404, "Not found");
            return;
        }

        // Prevent path traversal
        Path base = Paths.get("assets").toAbsolutePath().normalize();
        String relRaw = reqPath.substring("/assets/".length()); // e.g. images/logo.png
        if (relRaw.contains("..") || relRaw.contains(":") || relRaw.contains("\\")) {
            WebHttpUtil.sendHtml(ex, 400, "Bad path");
            return;
        }

        Path file = base.resolve(relRaw.replace("/", java.io.File.separator)).normalize();
        if (!file.startsWith(base) || !Files.exists(file) || Files.isDirectory(file)) {
            WebHttpUtil.sendHtml(ex, 404, "Not found");
            return;
        }

        String contentType = guessContentType(file.getFileName().toString());
        byte[] bytes = Files.readAllBytes(file);
        WebHttpUtil.sendBytes(ex, 200, bytes, contentType);
    }

    public void handleRoot(HttpExchange ex) throws IOException {
        User u = requireUserOrNull(ex);
        WebHttpUtil.redirect(ex, u == null ? "/login" : "/dashboard");
    }

    public void handleLogin(HttpExchange ex) throws IOException {
        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            Map<String, String> form = WebHttpUtil.readForm(ex);
            String username = form.getOrDefault("username", "");
            String password = form.getOrDefault("password", "");
            User user = app.auth().authenticate(username, password);
            if (user == null) {
                WebHttpUtil.sendHtml(ex, 200, WebViews.loginForm("Invalid username/password.", null));
                return;
            }
            String sessionId = sessions.createSession(user);
            WebHttpUtil.setCookie(ex, cookieName, sessionId);
            WebHttpUtil.redirect(ex, "/dashboard");
            return;
        }

        String msg = WebHttpUtil.readQueryParam(ex.getRequestURI(), "msg");
        WebHttpUtil.sendHtml(ex, 200, WebViews.loginForm(null, msg));
    }

    public void handleRegister(HttpExchange ex) throws IOException {
        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            Map<String, String> form = WebHttpUtil.readForm(ex);
            String username = form.getOrDefault("username", "");
            String password = form.getOrDefault("password", "");
            try {
                app.users().createUser(username, password, Role.STUDENT);
                WebHttpUtil.redirect(ex, "/login?msg=" + WebHttpUtil.url("Registration successful. Please login."));
            } catch (Exception e) {
                WebHttpUtil.sendHtml(ex, 200, WebViews.registerForm("Registration failed: " + e.getMessage()));
            }
            return;
        }

        WebHttpUtil.sendHtml(ex, 200, WebViews.registerForm(null));
    }

    public void handleLogout(HttpExchange ex) throws IOException {
        String sessionId = WebHttpUtil.readCookie(ex, cookieName);
        sessions.deleteSession(sessionId);
        WebHttpUtil.clearCookie(ex, cookieName);
        WebHttpUtil.redirect(ex, "/login");
    }

    public void handleDashboard(HttpExchange ex) throws IOException {
        User user = requireUser(ex);
        String path = dashboardPath(user.getRole());
        if (path == null) {
            WebHttpUtil.sendHtml(ex, 403, WebViews.messagePage("Forbidden", "<p>Unknown role.</p>"));
            return;
        }
        WebHttpUtil.redirect(ex, path);
    }

    private static String dashboardPath(Role r) {
        if (r == Role.STUDENT) return "/student";
        if (r == Role.LIBRARIAN) return "/librarian";
        if (r == Role.ADMIN) return "/admin";
        return null;
    }

    // ===== Student =====
    public void handleStudent(HttpExchange ex) throws IOException {
        requireRole(ex, Role.STUDENT);
        sendTemplate(ex, "student.html");
    }

    public void handleStudentBorrow(HttpExchange ex) throws IOException {
        User user = requireRole(ex, Role.STUDENT);
        requireMethod(ex, "POST");
        Map<String, String> form = WebHttpUtil.readForm(ex);
        int bookId = WebHttpUtil.parseInt(form.get("bookId"));
        Loan loan = (bookId <= 0) ? null : app.library().borrowBook(user.getUsername(), bookId);
        String msg = (loan == null) ? "Borrow failed." : "Borrowed successfully (loan " + loan.getId() + ").";
        WebHttpUtil.redirect(ex, "/student?msg=" + WebHttpUtil.url(msg));
    }

    public void handleStudentReturn(HttpExchange ex) throws IOException {
        User user = requireRole(ex, Role.STUDENT);
        requireMethod(ex, "POST");
        Map<String, String> form = WebHttpUtil.readForm(ex);
        int bookId = WebHttpUtil.parseInt(form.get("bookId"));
        boolean ok = bookId > 0 && app.library().returnBook(user.getUsername(), bookId);
        WebHttpUtil.redirect(ex, "/student?msg=" + WebHttpUtil.url(ok ? "Returned successfully." : "Return failed."));
    }

    // ===== Librarian =====
    public void handleLibrarian(HttpExchange ex) throws IOException {
        requireRole(ex, Role.LIBRARIAN);
        sendTemplate(ex, "librarian.html");
    }

    public void handleLibrarianAddBook(HttpExchange ex) throws IOException {
        requireRole(ex, Role.LIBRARIAN);
        requireMethod(ex, "POST");
        Map<String, String> form = WebHttpUtil.readForm(ex);
        String title = form.getOrDefault("title", "");
        String author = form.getOrDefault("author", "");
        int copies = WebHttpUtil.parseInt(form.get("copies"));
        try {
            Book b = app.library().addBook(title, author, copies);
            WebHttpUtil.redirect(ex, "/librarian?msg=" + WebHttpUtil.url("Added book #" + b.getId()));
        } catch (Exception e) {
            WebHttpUtil.redirect(ex, "/librarian?msg=" + WebHttpUtil.url("Add failed: " + e.getMessage()));
        }
    }

    public void handleLibrarianRemoveBook(HttpExchange ex) throws IOException {
        requireRole(ex, Role.LIBRARIAN);
        requireMethod(ex, "POST");
        Map<String, String> form = WebHttpUtil.readForm(ex);
        int bookId = WebHttpUtil.parseInt(form.get("bookId"));
        boolean ok = bookId > 0 && app.library().removeBook(bookId);
        WebHttpUtil.redirect(ex, "/librarian?msg=" + WebHttpUtil.url(ok ? "Removed." : "Remove failed."));
    }

    // ===== Admin =====
    public void handleAdmin(HttpExchange ex) throws IOException {
        requireRole(ex, Role.ADMIN);
        sendTemplate(ex, "admin.html");
    }

    // ===== API (JSON) =====
    public void handleApiMe(HttpExchange ex) throws IOException {
        User u = requireUserOrNull(ex);
        if (u == null) {
            WebHttpUtil.sendJson(ex, 401, "{\"error\":\"unauthorized\"}");
            return;
        }
        String json = "{"
                + "\"username\":\"" + WebHttpUtil.jsonEsc(u.getUsername()) + "\","
                + "\"role\":\"" + WebHttpUtil.jsonEsc(u.getRole().toString()) + "\""
                + "}";
        WebHttpUtil.sendJson(ex, 200, json);
    }

    public void handleApiStudentBooks(HttpExchange ex) throws IOException {
        apiRequireRole(ex, Role.STUDENT);
        String q = WebHttpUtil.readQueryParam(ex.getRequestURI(), "q");
        List<Book> books = (q == null || q.isBlank()) ? app.library().listBooks() : app.library().searchBooks(q);
        WebHttpUtil.sendJson(ex, 200, booksToJson(books));
    }

    public void handleApiStudentLoans(HttpExchange ex) throws IOException {
        User u = apiRequireRole(ex, Role.STUDENT);
        List<Loan> loans = app.library().listLoansForUser(u.getUsername());
        WebHttpUtil.sendJson(ex, 200, loansToJson(loans));
    }

    public void handleApiLibrarianBooks(HttpExchange ex) throws IOException {
        apiRequireRole(ex, Role.LIBRARIAN);
        List<Book> books = app.library().listBooks();
        WebHttpUtil.sendJson(ex, 200, booksToJson(books));
    }

    public void handleApiLibrarianActiveLoans(HttpExchange ex) throws IOException {
        apiRequireRole(ex, Role.LIBRARIAN);
        List<Loan> loans = app.library().listActiveLoans();
        WebHttpUtil.sendJson(ex, 200, loansToJson(loans));
    }

    public void handleApiAdminUsers(HttpExchange ex) throws IOException {
        apiRequireRole(ex, Role.ADMIN);
        List<User> users = app.users().listUsers();
        WebHttpUtil.sendJson(ex, 200, usersToJson(users));
    }

    public void handleAdminCreateUser(HttpExchange ex) throws IOException {
        requireRole(ex, Role.ADMIN);
        requireMethod(ex, "POST");
        Map<String, String> form = WebHttpUtil.readForm(ex);
        String username = form.getOrDefault("username", "");
        String password = form.getOrDefault("password", "");
        String roleRaw = form.getOrDefault("role", "STUDENT");
        try {
            Role role = Role.valueOf(roleRaw.trim().toUpperCase());
            User u = app.users().createUser(username, password, role);
            WebHttpUtil.redirect(ex, "/admin?msg=" + WebHttpUtil.url("Created " + u.getUsername() + " (" + u.getRole() + ")."));
        } catch (Exception e) {
            WebHttpUtil.redirect(ex, "/admin?msg=" + WebHttpUtil.url("Create failed: " + e.getMessage()));
        }
    }

    public void handleAdminResetPassword(HttpExchange ex) throws IOException {
        requireRole(ex, Role.ADMIN);
        requireMethod(ex, "POST");
        Map<String, String> form = WebHttpUtil.readForm(ex);
        String username = form.getOrDefault("username", "");
        String password = form.getOrDefault("password", "");
        boolean ok = app.users().resetPassword(username, password);
        WebHttpUtil.redirect(ex, "/admin?msg=" + WebHttpUtil.url(ok ? "Password updated." : "User not found."));
    }

    public void handleAdminDeleteUser(HttpExchange ex) throws IOException {
        requireRole(ex, Role.ADMIN);
        requireMethod(ex, "POST");
        Map<String, String> form = WebHttpUtil.readForm(ex);
        String username = form.getOrDefault("username", "");
        if ("admin".equalsIgnoreCase(username.trim())) {
            WebHttpUtil.redirect(ex, "/admin?msg=" + WebHttpUtil.url("Refusing to delete the default admin in demo mode."));
            return;
        }
        boolean ok = app.users().deleteUser(username);
        WebHttpUtil.redirect(ex, "/admin?msg=" + WebHttpUtil.url(ok ? "Deleted." : "User not found."));
    }

    // ===== Auth helpers =====
    private User requireUser(HttpExchange ex) throws IOException {
        User u = requireUserOrNull(ex);
        if (u == null) {
            WebHttpUtil.redirect(ex, "/login");
            throw new StopHandling();
        }
        return u;
    }

    private User requireUserOrNull(HttpExchange ex) {
        String sessionId = WebHttpUtil.readCookie(ex, cookieName);
        return sessions.getUser(sessionId, app);
    }

    private User requireRole(HttpExchange ex, Role role) throws IOException {
        User u = requireUser(ex);
        if (u.getRole() != role) {
            WebHttpUtil.sendHtml(ex, 403, WebViews.messagePage("Forbidden", "<p>You do not have access to this page.</p>"));
            throw new StopHandling();
        }
        return u;
    }

    private User apiRequireRole(HttpExchange ex, Role role) throws IOException {
        User u = requireUserOrNull(ex);
        if (u == null) {
            WebHttpUtil.sendJson(ex, 401, "{\"error\":\"unauthorized\"}");
            throw new StopHandling();
        }
        if (u.getRole() != role) {
            WebHttpUtil.sendJson(ex, 403, "{\"error\":\"forbidden\"}");
            throw new StopHandling();
        }
        return u;
    }

    private void requireMethod(HttpExchange ex, String method) throws IOException {
        if (!method.equalsIgnoreCase(ex.getRequestMethod())) {
            WebHttpUtil.sendHtml(ex, 405, WebViews.messagePage("Method Not Allowed", "<p>Use " + WebHttpUtil.esc(method) + ".</p>"));
            throw new StopHandling();
        }
    }

    static final class StopHandling extends RuntimeException {}

    private void sendTemplate(HttpExchange ex, String filename) throws IOException {
        // Send a static HTML template from web/templates/
        if (filename == null || filename.isBlank()) {
            WebHttpUtil.sendHtml(ex, 500, "Template missing");
            return;
        }
        // Basic safety: no slashes
        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            WebHttpUtil.sendHtml(ex, 400, "Bad template name");
            return;
        }
        Path p = Paths.get("web", "templates", filename);
        if (!Files.exists(p)) {
            WebHttpUtil.sendHtml(ex, 404, "Template not found");
            return;
        }
        byte[] bytes = Files.readAllBytes(p);
        WebHttpUtil.sendBytes(ex, 200, bytes, "text/html; charset=utf-8");
    }

    private static String booksToJson(List<Book> books) {
        StringBuilder b = new StringBuilder();
        b.append("[");
        if (books != null) {
            for (int i = 0; i < books.size(); i++) {
                if (i > 0) b.append(",");
                Book bk = books.get(i);
                b.append("{")
                        .append("\"id\":").append(bk.getId()).append(",")
                        .append("\"title\":\"").append(WebHttpUtil.jsonEsc(bk.getTitle())).append("\",")
                        .append("\"author\":\"").append(WebHttpUtil.jsonEsc(bk.getAuthor())).append("\",")
                        .append("\"availableCopies\":").append(bk.getAvailableCopies()).append(",")
                        .append("\"totalCopies\":").append(bk.getTotalCopies())
                        .append("}");
            }
        }
        b.append("]");
        return b.toString();
    }

    private static String loansToJson(List<Loan> loans) {
        StringBuilder b = new StringBuilder();
        b.append("[");
        if (loans != null) {
            for (int i = 0; i < loans.size(); i++) {
                if (i > 0) b.append(",");
                Loan l = loans.get(i);
                String returnDate = l.getReturnDate() == null ? "null" : ("\"" + WebHttpUtil.jsonEsc(String.valueOf(l.getReturnDate())) + "\"");
                b.append("{")
                        .append("\"id\":").append(l.getId()).append(",")
                        .append("\"username\":\"").append(WebHttpUtil.jsonEsc(l.getUsername())).append("\",")
                        .append("\"bookId\":").append(l.getBookId()).append(",")
                        .append("\"loanDate\":\"").append(WebHttpUtil.jsonEsc(String.valueOf(l.getLoanDate()))).append("\",")
                        .append("\"returnDate\":").append(returnDate)
                        .append("}");
            }
        }
        b.append("]");
        return b.toString();
    }

    private static String usersToJson(List<User> users) {
        StringBuilder b = new StringBuilder();
        b.append("[");
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                if (i > 0) b.append(",");
                User u = users.get(i);
                b.append("{")
                        .append("\"username\":\"").append(WebHttpUtil.jsonEsc(u.getUsername())).append("\",")
                        .append("\"role\":\"").append(WebHttpUtil.jsonEsc(u.getRole().toString())).append("\"")
                        .append("}");
            }
        }
        b.append("]");
        return b.toString();
    }

    private static String guessContentType(String filename) {
        if (filename == null) return "application/octet-stream";
        String f = filename.toLowerCase();
        if (f.endsWith(".png")) return "image/png";
        if (f.endsWith(".jpg") || f.endsWith(".jpeg")) return "image/jpeg";
        if (f.endsWith(".gif")) return "image/gif";
        if (f.endsWith(".webp")) return "image/webp";
        if (f.endsWith(".svg")) return "image/svg+xml";
        if (f.endsWith(".css")) return "text/css; charset=utf-8";
        if (f.endsWith(".js")) return "application/javascript; charset=utf-8";
        return "application/octet-stream";
    }
}

