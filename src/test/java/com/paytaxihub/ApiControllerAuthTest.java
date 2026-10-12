package com.paytaxihub;

import com.paytaxihub.auth.AuthSessionService;
import com.paytaxihub.auth.PasswordResetService;
import com.paytaxihub.controller.ApiController;
import com.paytaxihub.entity.User;
import com.paytaxihub.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ApiControllerAuthTest {
    private UserRepository users;
    private PasswordEncoder encoder;
    private AuthSessionService sessions;
    private ApiController controller;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        FleetOwnerRepository owners = mock(FleetOwnerRepository.class);
        VehicleRepository vehicles = mock(VehicleRepository.class);
        WalletRepository wallets = mock(WalletRepository.class);
        RouteRepository routes = mock(RouteRepository.class);
        TripRepository trips = mock(TripRepository.class);
        TransactionRepository transactions = mock(TransactionRepository.class);
        SOSEventRepository sos = mock(SOSEventRepository.class);
        ShiftRepository shifts = mock(ShiftRepository.class);
        encoder = mock(PasswordEncoder.class);
        sessions = mock(AuthSessionService.class);
        PasswordResetService passwordResets = new PasswordResetService();
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        request = mock(HttpServletRequest.class);
        when(encoder.encode(anyString())).thenReturn("dummy-hash");

        controller = new ApiController(users, drivers, owners, vehicles, wallets, routes, trips,
                transactions, sos, shifts, encoder, sessions, passwordResets, transactionManager);
        clearInvocations(encoder);
    }

    @Test
    void missingRoleEmailAndPasswordReturnFieldSpecificValidation() {
        assertLoginValidation(Map.of("email", "person@example.test", "password", "Password123"),
                "Please select your account role.");
        assertLoginValidation(Map.of("role", "Driver", "password", "Password123"),
                "Please enter your email address.");
        assertLoginValidation(Map.of("role", "Driver", "email", "person@example.test"),
                "Please enter your password.");
        assertLoginValidation(Map.of("role", "Driver", "email", "not-an-email", "password", "Password123"),
                "Please enter a valid email address.");
        verifyNoInteractions(users, encoder, sessions);
    }

    @Test
    void unknownEmailWrongPasswordAndWrongRoleHaveIdenticalResponses() {
        User driver = user(User.Role.Driver);
        when(users.findByEmail("known@example.test")).thenReturn(Optional.of(driver));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);

        ResponseEntity<?> unknownEmail = login("missing@example.test", "Password123", "Driver");
        ResponseEntity<?> wrongPassword = login("known@example.test", "WrongPassword123", "Driver");
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        ResponseEntity<?> wrongRole = login("known@example.test", "Password123", "Commuter");

        assertEquals(HttpStatus.UNAUTHORIZED, unknownEmail.getStatusCode());
        assertEquals(unknownEmail.getStatusCode(), wrongPassword.getStatusCode());
        assertEquals(unknownEmail.getStatusCode(), wrongRole.getStatusCode());
        assertEquals(message(unknownEmail), message(wrongPassword));
        assertEquals(message(unknownEmail), message(wrongRole));
        assertEquals("Sign-in failed. Check your details and try again.", message(unknownEmail));
        verify(sessions, never()).create(any());
    }

    @Test
    void validCredentialsCreateSessionAndReturnUser() {
        User driver = user(User.Role.Driver);
        when(users.findByEmail("driver@example.test")).thenReturn(Optional.of(driver));
        when(encoder.matches("Password123", "encoded-password")).thenReturn(true);
        when(sessions.create(driver)).thenReturn("test-session-token");

        ResponseEntity<?> response = login("driver@example.test", "Password123", "Driver");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = body(response);
        assertEquals("test-session-token", body.get("token"));
        Map<?, ?> returnedUser = (Map<?, ?>) body.get("user");
        assertEquals(41, returnedUser.get("userId"));
        assertEquals("Driver", returnedUser.get("role"));
        verify(sessions).create(driver);
    }

    @Test
    void inactiveAccountUsesSameGenericSignInFailure() {
        User driver = user(User.Role.Driver);
        driver.setIsActive(false);
        when(users.findByEmail("known@example.test")).thenReturn(Optional.of(driver));
        when(encoder.matches("Password123", "encoded-password")).thenReturn(true);

        ResponseEntity<?> response = login("known@example.test", "Password123", "Driver");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Sign-in failed. Check your details and try again.", message(response));
        verify(sessions, never()).create(any());
    }

    private void assertLoginValidation(Map<String, Object> payload, String expectedMessage) {
        ResponseEntity<?> response = controller.login(payload, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedMessage, message(response));
    }

    private ResponseEntity<?> login(String email, String password, String role) {
        return controller.login(Map.of("email", email, "password", password, "role", role), request);
    }

    private static User user(User.Role role) {
        User user = new User();
        user.setUserId(41);
        user.setFullName("Test User");
        user.setEmail("driver@example.test");
        user.setPasswordHash("encoded-password");
        user.setRole(role);
        user.setIsActive(true);
        return user;
    }

    private static String message(ResponseEntity<?> response) {
        return String.valueOf(body(response).get("message"));
    }

    private static Map<?, ?> body(ResponseEntity<?> response) {
        assertInstanceOf(Map.class, response.getBody());
        return (Map<?, ?>) response.getBody();
    }
}
