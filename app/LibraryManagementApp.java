import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * JavaFX GUI for Library Management System.
 * Architecture: Stage (window), Scene (content), Scene Graph (hierarchy of nodes).
 */
public final class LibraryManagementApp extends Application {

    private Stage primaryStage;
    private AppContext app;
    private User currentUser;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.app = new AppContext();

        primaryStage.setTitle("Library Management System");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(400);

        showLoginScene();
        primaryStage.show();
    }

    void showLoginScene() {
        currentUser = null;
        Scene loginScene = LoginScene.build(this);
        primaryStage.setScene(loginScene);
    }

    void showRegisterScene() {
        Scene registerScene = RegisterScene.build(this);
        primaryStage.setScene(registerScene);
    }

    void showDashboard() {
        if (currentUser == null) {
            showLoginScene();
            return;
        }
        Scene dashboard;
        switch (currentUser.getRole()) {
            case STUDENT:
                dashboard = StudentScene.build(this);
                break;
            case LIBRARIAN:
                dashboard = LibrarianScene.build(this);
                break;
            case ADMIN:
                dashboard = AdminScene.build(this);
                break;
            default:
                showLoginScene();
                return;
        }
        primaryStage.setScene(dashboard);
    }

    void setCurrentUser(User user) {
        this.currentUser = user;
    }

    User getCurrentUser() {
        return currentUser;
    }

    AppContext getApp() {
        return app;
    }
}
