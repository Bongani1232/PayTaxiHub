package com.paytaxihub;

import com.paytaxihub.auth.PasswordResetService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetServiceTest {
    @Test void codeIsCaseInsensitiveByEmailAndCanOnlyBeConsumedOnce() {
        PasswordResetService service = new PasswordResetService();
        String code = service.createCode("Person@example.com");
        assertTrue(service.verifyAndConsume(" person@EXAMPLE.com ", code));
        assertFalse(service.verifyAndConsume("person@example.com", code));
    }

    @Test void invalidAttemptsEventuallyInvalidateChallenge() {
        PasswordResetService service = new PasswordResetService();
        String code = service.createCode("person@example.com");
        for (int i = 0; i < 5; i++) assertFalse(service.verifyAndConsume("person@example.com", "not-the-code"));
        assertFalse(service.verifyAndConsume("person@example.com", code));
    }
}
