package com.paytaxihub.auth;

import com.paytaxihub.entity.User;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthSessionService {
    private static final Duration TTL = Duration.ofHours(8);
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public String create(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(user.getUserId(), user.getRole().name(), Instant.now().plus(TTL)));
        return token;
    }

    public Session validate(String token) {
        if (token == null || token.isBlank()) return null;
        Session s = sessions.get(token);
        if (s == null) return null;
        if (Instant.now().isAfter(s.expiresAt())) { sessions.remove(token); return null; }
        return s;
    }

    public void revoke(String token) { if (token != null) sessions.remove(token); }

    /** Revoke every active session after a password reset or account-security event. */
    public void revokeAllForUser(Integer userId) {
        if (userId == null) return;
        sessions.entrySet().removeIf(entry -> userId.equals(entry.getValue().userId()));
    }
    public record Session(Integer userId, String role, Instant expiresAt) {}
}
