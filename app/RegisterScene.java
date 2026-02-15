import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Student registration: Panes, Shapes, Colors, lambda event handling.
 */
public final class RegisterScene {

    static Scene build(LibraryManagementApp app) {
        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setSpacing(12);
        root.setStyle("-fx-background-color: #1e293b;");

        Rectangle topBar = new Rectangle(400, 4);
        topBar.setFill(Color.web("#22c55e"));
        topBar.setArcWidth(8);
        topBar.setArcHeight(8);

        Text title = new Text("Register as Student");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username (3-20 letters/numbers/underscore)");
        usernameField.setMaxWidth(320);
        usernameField.setPrefHeight(36);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password (at least 4 characters)");
        passwordField.setMaxWidth(320);
        passwordField.setPrefHeight(36);

        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.web("#fca5a5"));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(320);

        Button registerBtn = new Button("Register");
        registerBtn.setDefaultButton(true);
        registerBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold;");
        registerBtn.setPrefWidth(320);
        registerBtn.setPrefHeight(36);

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter username and password.");
                return;
            }
            try {
                app.getApp().users().createUser(username, password, Role.STUDENT);
                messageLabel.setTextFill(Color.web("#86efac"));
                messageLabel.setText("Registration successful. You can now log in.");
                usernameField.clear();
                passwordField.clear();
            } catch (Exception ex) {
                messageLabel.setTextFill(Color.web("#fca5a5"));
                messageLabel.setText(ex.getMessage());
            }
        });

        Button backLink = new Button("Back to Login");
        backLink.setStyle("-fx-background-color: transparent; -fx-text-fill: #93c5fd;");
        backLink.setOnAction(e -> app.showLoginScene());

        root.getChildren().addAll(topBar, title, usernameField, passwordField,
                messageLabel, registerBtn, backLink);

        return new Scene(root, 500, 420);
    }
}
