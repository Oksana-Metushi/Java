import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.List;

/**
 * Librarian dashboard: list books, add/remove book, view loans.
 * Panes, Shapes, Colors, ListView, lambda and MouseEvent handling.
 */
public final class LibrarianScene {

    static Scene build(LibraryManagementApp app) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color: #0f172a;");

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 12, 12, 12));
        topBar.setStyle("-fx-background-color: #1e293b;");
        Rectangle accent = new Rectangle(4, 28);
        accent.setFill(Color.web("#8b5cf6"));
        Text heading = new Text("Librarian - " + app.getCurrentUser().getUsername());
        heading.setFill(Color.WHITE);
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        topBar.getChildren().addAll(accent, heading);
        root.setTop(topBar);

        VBox center = new VBox(10);
        center.setPadding(new Insets(10));

        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(240);
        listView.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white;");

        HBox buttonRow = new HBox(8);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button listBooksBtn = new Button("List Books");
        Button addBookBtn = new Button("Add Book");
        Button removeBookBtn = new Button("Remove Book");
        Button allLoansBtn = new Button("All Loans");
        Button activeLoansBtn = new Button("Active Loans");
        Button logoutBtn = new Button("Logout");

        FxHelper.styleButton(listBooksBtn, "#3b82f6");
        FxHelper.styleButton(addBookBtn, "#22c55e");
        FxHelper.styleButton(removeBookBtn, "#ef4444");
        FxHelper.styleButton(allLoansBtn, "#8b5cf6");
        FxHelper.styleButton(activeLoansBtn, "#8b5cf6");
        FxHelper.styleButton(logoutBtn, "#64748b");

        listBooksBtn.setOnAction(e -> {
            List<Book> books = app.getApp().library().listBooks();
            listView.getItems().clear();
            for (Book b : books) listView.getItems().add(b.toString());
        });

        addBookBtn.setOnAction(e -> {
            String title = FxHelper.showInputDialog("Add Book", "Title:");
            if (title == null || title.isBlank()) return;
            String author = FxHelper.showInputDialog("Add Book", "Author:");
            if (author == null || author.isBlank()) return;
            String copiesStr = FxHelper.showInputDialog("Add Book", "Number of copies:");
            if (copiesStr == null) return;
            try {
                int copies = Integer.parseInt(copiesStr.trim());
                Book b = app.getApp().library().addBook(title.trim(), author.trim(), copies);
                FxHelper.showInfo("Added: " + b);
                listBooksBtn.fire();
            } catch (Exception ex) {
                FxHelper.showInfo("Error: " + ex.getMessage());
            }
        });

        removeBookBtn.setOnAction(e -> {
            String idStr = FxHelper.showInputDialog("Remove Book", "Book ID to remove:");
            if (idStr == null) return;
            try {
                int bookId = Integer.parseInt(idStr.trim());
                boolean ok = app.getApp().library().removeBook(bookId);
                FxHelper.showInfo(ok ? "Removed." : "Remove failed (not found or has active loans).");
                listBooksBtn.fire();
            } catch (NumberFormatException ex) {
                FxHelper.showInfo("Please enter a valid book ID.");
            }
        });

        allLoansBtn.setOnAction(e -> {
            List<Loan> loans = app.getApp().library().listAllLoans();
            listView.getItems().clear();
            if (loans.isEmpty()) listView.getItems().add("No loans.");
            else for (Loan l : loans) listView.getItems().add(l.toString());
        });

        activeLoansBtn.setOnAction(e -> {
            List<Loan> loans = app.getApp().library().listActiveLoans();
            listView.getItems().clear();
            if (loans.isEmpty()) listView.getItems().add("No active loans.");
            else for (Loan l : loans) listView.getItems().add(l.toString());
        });

        logoutBtn.setOnAction(e -> app.showLoginScene());

        buttonRow.getChildren().addAll(listBooksBtn, addBookBtn, removeBookBtn, allLoansBtn, activeLoansBtn, logoutBtn);
        center.getChildren().addAll(listView, buttonRow);
        root.setCenter(center);

        return new Scene(root, 700, 450);
    }
}
