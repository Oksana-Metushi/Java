import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class WebViews {
    private WebViews() {}

    public static String loginForm(String error, String message) {
        String t = readTemplate("login.html");
        return t.replace("{{TITLE}}", WebHttpUtil.esc("Login"))
                .replace("{{MESSAGE_HTML}}", renderMessage(message))
                .replace("{{ERROR_HTML}}", renderError(error));
    }

    public static String registerForm(String error) {
        String t = readTemplate("register.html");
        return t.replace("{{TITLE}}", WebHttpUtil.esc("Register"))
                .replace("{{ERROR_HTML}}", renderError(error));
    }

    public static String messagePage(String title, String bodyHtml) {
        String t = readTemplate("message.html");
        return t.replace("{{TITLE}}", WebHttpUtil.esc(title))
                .replace("{{BODY}}", bodyHtml == null ? "" : bodyHtml);
    }

    private static String readTemplate(String filename) {
        // Keep it simple: load templates from web/templates/
        try {
            byte[] bytes = Files.readAllBytes(Paths.get("web", "templates", filename));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Fail safe: show a small message instead of crashing the server
            return "<!doctype html><html><body style='font-family: sans-serif'>Template missing: "
                    + WebHttpUtil.esc(filename)
                    + "</body></html>";
        }
    }

    private static String renderMessage(String message) {
        if (message == null || message.isBlank()) return "";
        return "<div class=\"msg-success\">" + WebHttpUtil.esc(message) + "</div>";
    }

    private static String renderError(String error) {
        if (error == null || error.isBlank()) return "";
        return "<div class=\"msg-error\">" + WebHttpUtil.esc(error) + "</div>";
    }
}

