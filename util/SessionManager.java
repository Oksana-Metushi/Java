import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SessionManager {
    private static final class Session {
        private final String username;

        private Session(String username) {
            this.username = username;
        }
    }

    private final Map<String, Session> sessionsById = new HashMap<>();

    public String createSession(User user) {
        if (user == null) throw new IllegalArgumentException("user required");
        String sessionId = UUID.randomUUID().toString();
        sessionsById.put(sessionId, new Session(user.getUsername()));
        return sessionId;
    }

    public void deleteSession(String sessionId) {
        if (sessionId == null) return;
        sessionsById.remove(sessionId);
    }

    public User getUser(String sessionId, AppContext app) {
        if (sessionId == null || app == null) return null;
        Session s = sessionsById.get(sessionId);
        if (s == null) return null;
        return app.findUser(s.username);
    }
}

