import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Login screen: Panes, ImageView, Colors, lambda event handling.
 */
public final class LoginScene {

    static Scene build(LibraryManagementApp app) {
        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setSpacing(12);
        root.setStyle("-fx-background-color: #1e293b;");

        // Decorative shape (Rectangle) - JavaFX Shapes
        Rectangle topBar = new Rectangle(400, 4);
        topBar.setFill(Color.web("#f97316"));
        topBar.setArcWidth(8);
        topBar.setArcHeight(8);

        // Image (ImageView) - logo from assets
        ImageView logoView = new ImageView();
        try {
            Image img = new Image("file:assets/images/logo.png", 120, 0, true, true);
            logoView.setImage(img);
        } catch (Exception e) {
            logoView.setVisible(false);
        }
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);

        Text title = new Text("Login");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(280);
        usernameField.setPrefHeight(36);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(280);
        passwordField.setPrefHeight(36);

        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.web("#fca5a5"));
        messageLabel.setWrapText(true);

        Button loginBtn = new Button("Log In");
        loginBtn.setDefaultButton(true);
        loginBtn.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold;");
        loginBtn.setPrefWidth(280);
        loginBtn.setPrefHeight(36);

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter username and password.");
                return;
            }
            User user = app.getApp().auth().authenticate(username, password);
            if (user == null) {
                messageLabel.setText("Invalid username or password.");
                return;
            }
            app.setCurrentUser(user);
            app.showDashboard();
        });

        Button registerLink = new Button("New student? Sign up");
        registerLink.setStyle("-fx-background-color: transparent; -fx-text-fill: #93c5fd; -fx-underline: true;");
        registerLink.setOnAction(e -> app.showRegisterScene());

        Label demoHint = new Label("Demo: student1/student123 | librarian1/lib123 | admin/admin123");
        demoHint.setTextFill(Color.web("#94a3b8"));
        demoHint.setStyle("-fx-font-size: 11px;");

        root.getChildren().addAll(topBar, logoView, title, usernameField, passwordField,
                messageLabel, loginBtn, demoHint, registerLink);

        return new Scene(root, 500, 520);
    }
}
