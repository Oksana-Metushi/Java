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
 * Admin dashboard: list users, create user, reset password, delete user.
 * Panes, Shapes, Colors, ListView, lambda and MouseEvent handling.
 */
public final class AdminScene {

    static Scene build(LibraryManagementApp app) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color: #0f172a;");

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 12, 12, 12));
        topBar.setStyle("-fx-background-color: #1e293b;");
        Rectangle accent = new Rectangle(4, 28);
        accent.setFill(Color.web("#ef4444"));
        Text heading = new Text("Admin - " + app.getCurrentUser().getUsername());
        heading.setFill(Color.WHITE);
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        topBar.getChildren().addAll(accent, heading);
        root.setTop(topBar);

        VBox center = new VBox(10);
        center.setPadding(new Insets(10));

        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(260);
        listView.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white;");

        HBox buttonRow = new HBox(8);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button listUsersBtn = new Button("List Users");
        Button createUserBtn = new Button("Create User");
        Button resetPwBtn = new Button("Reset Password");
        Button deleteUserBtn = new Button("Delete User");
        Button logoutBtn = new Button("Logout");

        FxHelper.styleButton(listUsersBtn, "#3b82f6");
        FxHelper.styleButton(createUserBtn, "#22c55e");
        FxHelper.styleButton(resetPwBtn, "#eab308");
        FxHelper.styleButton(deleteUserBtn, "#ef4444");
        FxHelper.styleButton(logoutBtn, "#64748b");

        listUsersBtn.setOnAction(e -> {
            List<User> users = app.getApp().users().listUsers();
            listView.getItems().clear();
            for (User u : users) listView.getItems().add(u.toString());
        });

        createUserBtn.setOnAction(e -> {
            String username = FxHelper.showInputDialog("Create User", "Username:");
            if (username == null || username.isBlank()) return;
            String password = FxHelper.showInputDialog("Create User", "Password:");
            if (password == null || password.isBlank()) return;
            String roleStr = FxHelper.showInputDialog("Create User", "Role (STUDENT / LIBRARIAN / ADMIN):");
            if (roleStr == null) return;
            Role role;
            try {
                role = Role.valueOf(roleStr.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                FxHelper.showInfo("Invalid role. Use STUDENT, LIBRARIAN, or ADMIN.");
                return;
            }
            try {
                User u = app.getApp().users().createUser(username.trim(), password, role);
                FxHelper.showInfo("Created: " + u);
                listUsersBtn.fire();
            } catch (Exception ex) {
                FxHelper.showInfo("Error: " + ex.getMessage());
            }
        });

        resetPwBtn.setOnAction(e -> {
            String username = FxHelper.showInputDialog("Reset Password", "Username:");
            if (username == null || username.isBlank()) return;
            String newPassword = FxHelper.showInputDialog("Reset Password", "New password:");
            if (newPassword == null || newPassword.isBlank()) return;
            boolean ok = app.getApp().users().resetPassword(username.trim(), newPassword);
            FxHelper.showInfo(ok ? "Password updated." : "User not found.");
        });

        deleteUserBtn.setOnAction(e -> {
            String username = FxHelper.showInputDialog("Delete User", "Username to delete:");
            if (username == null || username.isBlank()) return;
            if ("admin".equalsIgnoreCase(username.trim())) {
                FxHelper.showInfo("Cannot delete the default admin.");
                return;
            }
            boolean ok = app.getApp().users().deleteUser(username.trim());
            FxHelper.showInfo(ok ? "Deleted." : "User not found.");
            listUsersBtn.fire();
        });

        logoutBtn.setOnAction(e -> app.showLoginScene());

        buttonRow.getChildren().addAll(listUsersBtn, createUserBtn, resetPwBtn, deleteUserBtn, logoutBtn);
        center.getChildren().addAll(listView, buttonRow);
        root.setCenter(center);

        return new Scene(root, 700, 450);
    }
}
