import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;

/**
 * Shared helpers for JavaFX scenes (school project - keep it simple).
 */
public final class FxHelper {

    private FxHelper() {}

    public static void styleButton(Button b, String color) {
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");
        b.setOnMouseEntered(me -> b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-cursor: hand;"));
        b.setOnMouseExited(me -> b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;"));
    }

    public static String showInputDialog(String title, String prompt) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle(title);
        d.setHeaderText(null);
        d.setContentText(prompt);
        return d.showAndWait().orElse(null);
    }

    public static void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
