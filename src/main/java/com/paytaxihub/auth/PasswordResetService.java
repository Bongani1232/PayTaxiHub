package com.paytaxihub.auth;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Process-local reset challenges for the demo. Use a persistent store and delivery provider in production. */
@Service
public class PasswordResetService {
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final int MAX_ATTEMPTS = 5;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, ResetRequest> requests = new ConcurrentHashMap<>();

    public String createCode(String email) {
        String normalizedEmail = normalize(email);
        String code = String.format("%06d", random.nextInt(1_000_000));
        requests.put(normalizedEmail, new ResetRequest(hash(code), Instant.now().plus(TTL), new AtomicInteger()));
        return code;
    }

    public boolean verifyAndConsume(String email, String code) {
        String key = normalize(email);
        ResetRequest request = requests.get(key);
        if (request == null) return false;
        if (Instant.now().isAfter(request.expiresAt())) {
            requests.remove(key, request);
            return false;
        }
        int attempt = request.attempts().incrementAndGet();
        if (attempt > MAX_ATTEMPTS) {
            requests.remove(key, request);
            return false;
        }
        boolean matches = code != null && MessageDigest.isEqual(
                request.codeHash().getBytes(StandardCharsets.UTF_8),
                hash(code.trim()).getBytes(StandardCharsets.UTF_8));
        if (!matches) {
            if (attempt >= MAX_ATTEMPTS) requests.remove(key, request);
            return false;
        }
        return requests.remove(key, request);
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder(digest.length * 2);
            for (byte b : digest) out.append(String.format("%02x", b));
            return out.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private record ResetRequest(String codeHash, Instant expiresAt, AtomicInteger attempts) {}
}
