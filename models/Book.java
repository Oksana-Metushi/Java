public final class Book {
    private final int id;
    private final String title;
    private final String author;
    private int totalCopies;
    private int availableCopies;

    public Book(int id, String title, String author, int totalCopies) {
        this(id, title, author, totalCopies, totalCopies);
    }

    public Book(int id, String title, String author, int totalCopies, int availableCopies) {
        if (id <= 0) throw new IllegalArgumentException("id must be > 0");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title required");
        if (author == null || author.isBlank()) throw new IllegalArgumentException("author required");
        if (totalCopies <= 0) throw new IllegalArgumentException("totalCopies must be > 0");
        if (availableCopies < 0 || availableCopies > totalCopies) throw new IllegalArgumentException("invalid availableCopies");
        this.id = id;
        this.title = title.trim();
        this.author = author.trim();
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public boolean borrowCopy() {
        if (availableCopies <= 0) return false;
        availableCopies--;
        assert availableCopies >= 0 : "availableCopies cannot be negative";
        return true;
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
        assert availableCopies <= totalCopies : "availableCopies cannot exceed totalCopies";
    }

    public void addCopies(int count) {
        if (count <= 0) throw new IllegalArgumentException("count must be > 0");
        totalCopies += count;
        availableCopies += count;
    }

    @Override
    public String toString() {
        return "#" + id + " | " + title + " | " + author + " | available " + availableCopies + "/" + totalCopies;
    }
}

