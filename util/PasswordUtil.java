import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtil {
    private PasswordUtil() {}

    public static String hash(String password) {
        if (password == null) throw new IllegalArgumentException("password cannot be null");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // Should never happen for SHA-256 in standard JRE
            throw new RuntimeException(e);
        }
    }

    public static boolean matches(String password, String passwordHash) {
        if (password == null || passwordHash == null) return false;
        return hash(password).equals(passwordHash);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

