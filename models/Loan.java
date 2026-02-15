import java.time.LocalDate;

public final class Loan {
    private final int id;
    private final String username;
    private final int bookId;
    private final LocalDate loanDate;
    private LocalDate returnDate; // null = active

    public Loan(int id, String username, int bookId, LocalDate loanDate) {
        this(id, username, bookId, loanDate, null);
    }

    public Loan(int id, String username, int bookId, LocalDate loanDate, LocalDate returnDate) {
        if (id <= 0) throw new IllegalArgumentException("id must be > 0");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username required");
        if (bookId <= 0) throw new IllegalArgumentException("bookId must be > 0");
        if (loanDate == null) throw new IllegalArgumentException("loanDate required");
        this.id = id;
        this.username = username.trim();
        this.bookId = bookId;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getBookId() {
        return bookId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isActive() {
        return returnDate == null;
    }

    public void markReturned(LocalDate returnedOn) {
        if (returnedOn == null) throw new IllegalArgumentException("returnedOn required");
        if (returnDate != null) return; // already returned
        returnDate = returnedOn;
    }

    @Override
    public String toString() {
        return "Loan#" + id + " | user=" + username + " | bookId=" + bookId
                + " | loanDate=" + loanDate + " | returnDate=" + (returnDate == null ? "-" : returnDate);
    }
}

