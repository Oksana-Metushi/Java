import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WebHttpUtil {
    private WebHttpUtil() {}

    public static void sendHtml(HttpExchange ex, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        Headers h = ex.getResponseHeaders();
        h.set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    public static void sendBytes(HttpExchange ex, int status, byte[] bytes, String contentType) throws IOException {
        if (bytes == null) bytes = new byte[0];
        Headers h = ex.getResponseHeaders();
        if (contentType != null && !contentType.isBlank()) {
            h.set("Content-Type", contentType);
        }
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    public static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = (json == null ? "" : json).getBytes(StandardCharsets.UTF_8);
        sendBytes(ex, status, bytes, "application/json; charset=utf-8");
    }

    public static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().set("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    public static Map<String, String> readForm(HttpExchange ex) throws IOException {
        String body;
        try (InputStream in = ex.getRequestBody()) {
            body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        return parseUrlEncoded(body);
    }

    public static Map<String, String> parseUrlEncoded(String s) {
        Map<String, String> out = new HashMap<>();
        if (s == null || s.isBlank()) return out;
        for (String p : s.split("&")) {
            if (p.isEmpty()) continue;
            String[] kv = p.split("=", 2);
            out.put(urlDecode(kv[0]), kv.length > 1 ? urlDecode(kv[1]) : "");
        }
        return out;
    }

    public static String readQueryParam(URI uri, String key) {
        if (uri == null || key == null) return null;
        String q = uri.getRawQuery();
        if (q == null || q.isBlank()) return null;
        Map<String, String> params = parseUrlEncoded(q);
        return params.get(key);
    }

    public static String readCookie(HttpExchange ex, String name) {
        if (name == null) return null;
        List<String> cookies = ex.getRequestHeaders().get("Cookie");
        if (cookies == null) return null;
        for (String header : cookies) {
            if (header == null) continue;
            for (String part : header.split(";")) {
                String t = part.trim();
                int idx = t.indexOf('=');
                if (idx <= 0) continue;
                if (name.equals(t.substring(0, idx).trim()))
                    return t.substring(idx + 1).trim();
            }
        }
        return null;
    }

    public static void setCookie(HttpExchange ex, String name, String value) {
        ex.getResponseHeaders().add("Set-Cookie", name + "=" + value + "; HttpOnly; Path=/; SameSite=Lax");
    }

    public static void clearCookie(HttpExchange ex, String name) {
        ex.getResponseHeaders().add("Set-Cookie", name + "=; Max-Age=0; Path=/; SameSite=Lax");
    }

    public static int parseInt(String s) {
        if (s == null) return -1;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String url(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    public static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String jsonEsc(String s) {
        if (s == null) return "";
        // Minimal JSON string escaper (enough for this project)
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}

