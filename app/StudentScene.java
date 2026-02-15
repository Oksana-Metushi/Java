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
 * Student dashboard: list books, search, borrow, return, view my loans.
 * Uses Panes, Shapes, Colors, ListView, lambda event handling and MouseEvents.
 */
public final class StudentScene {

    static Scene build(LibraryManagementApp app) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color: #0f172a;");

        // Top bar (Shape + Pane)
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 12, 12, 12));
        topBar.setStyle("-fx-background-color: #1e293b;");
        Rectangle accent = new Rectangle(4, 28);
        accent.setFill(Color.web("#f97316"));
        Text heading = new Text("Student - " + app.getCurrentUser().getUsername());
        heading.setFill(Color.WHITE);
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        topBar.getChildren().addAll(accent, heading);
        root.setTop(topBar);

        VBox center = new VBox(10);
        center.setPadding(new Insets(10));

        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(220);
        listView.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by title or author");
        searchField.setMaxWidth(300);

        HBox buttonRow = new HBox(8);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button listBtn = new Button("List Books");
        Button searchBtn = new Button("Search");
        Button borrowBtn = new Button("Borrow by ID");
        Button returnBtn = new Button("Return by ID");
        Button myLoansBtn = new Button("My Loans");
        Button logoutBtn = new Button("Logout");

        FxHelper.styleButton(listBtn, "#3b82f6");
        FxHelper.styleButton(searchBtn, "#3b82f6");
        FxHelper.styleButton(borrowBtn, "#22c55e");
        FxHelper.styleButton(returnBtn, "#eab308");
        FxHelper.styleButton(myLoansBtn, "#8b5cf6");
        FxHelper.styleButton(logoutBtn, "#64748b");

        listBtn.setOnAction(e -> {
            List<Book> books = app.getApp().library().listBooks();
            listView.getItems().clear();
            for (Book b : books) listView.getItems().add(b.toString());
        });

        searchBtn.setOnAction(e -> {
            String q = searchField.getText().trim();
            List<Book> books = app.getApp().library().searchBooks(q);
            listView.getItems().clear();
            for (Book b : books) listView.getItems().add(b.toString());
        });

        borrowBtn.setOnAction(e -> {
            String idStr = FxHelper.showInputDialog("Borrow book", "Enter book ID:");
            if (idStr == null) return;
            try {
                int bookId = Integer.parseInt(idStr.trim());
                Loan loan = app.getApp().library().borrowBook(app.getCurrentUser().getUsername(), bookId);
                FxHelper.showInfo(loan != null ? "Borrowed: " + loan : "Borrow failed (not found or unavailable).");
                if (loan != null) myLoansBtn.fire();
            } catch (NumberFormatException ex) {
                FxHelper.showInfo("Please enter a valid book ID.");
            }
        });

        returnBtn.setOnAction(e -> {
            String idStr = FxHelper.showInputDialog("Return book", "Enter book ID:");
            if (idStr == null) return;
            try {
                int bookId = Integer.parseInt(idStr.trim());
                boolean ok = app.getApp().library().returnBook(app.getCurrentUser().getUsername(), bookId);
                FxHelper.showInfo(ok ? "Returned successfully." : "Return failed (no active loan).");
                if (ok) myLoansBtn.fire();
            } catch (NumberFormatException ex) {
                FxHelper.showInfo("Please enter a valid book ID.");
            }
        });

        myLoansBtn.setOnAction(e -> {
            List<Loan> loans = app.getApp().library().listLoansForUser(app.getCurrentUser().getUsername());
            listView.getItems().clear();
            if (loans.isEmpty()) listView.getItems().add("No loans.");
            else for (Loan l : loans) listView.getItems().add(l.toString());
        });

        logoutBtn.setOnAction(e -> app.showLoginScene());

        buttonRow.getChildren().addAll(listBtn, searchBtn, borrowBtn, returnBtn, myLoansBtn, logoutBtn);
        center.getChildren().addAll(searchField, listView, buttonRow);
        root.setCenter(center);

        return new Scene(root, 700, 450);
    }
}
