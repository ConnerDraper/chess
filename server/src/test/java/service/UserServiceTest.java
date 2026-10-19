package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private UserService userService;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        authDAO = new MemoryAuthDAO();
        userDAO = new MemoryUserDAO();

        userService = new UserService(userDAO, authDAO);
        registerRequest = new RegisterRequest(
                "username", "password", "email@email.com"
        );
    }

    // user is stored and token is valid
    @Test
    void registerSuccess() throws Exception {
        RegisterResult registerResult = userService.register(registerRequest);
        UserData userData = userDAO.getUser("username");
        assertEquals("username", registerResult.username());
        assertNotNull(userData);
        assertNotNull(authDAO.getAuth(registerResult.authToken()));
    }

    @Test
    void registerNameTaken() throws Exception {
        userService.register(registerRequest);
        // same username twice
        assertThrows(AlreadyTakenException.class, () -> userService.register(registerRequest));
    }

    // assert empty username, password, or email don't work (register)
    @Test
    void registerNoField() {
        // assert no username does not work
        assertThrows(
                BadRequestException.class,
                () -> userService.register(
                        new RegisterRequest(null, "password", "email@email.com")
                ));
        // assert no password does not work
        assertThrows(
                BadRequestException.class,
                () -> userService.register(
                        new RegisterRequest("username", null, "email@email.com")
                ));
        // assert no email does not work
        assertThrows(
                BadRequestException.class,
                () -> userService.register(
                        new RegisterRequest("username", "password", null)
                ));
    }

    // assert new token is valid
    @Test
    void loginSuccess() throws Exception {
        userService.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = userService.login(loginRequest);
        assertNotNull(authDAO.getAuth(loginResult.authToken()));
    }

    // assert incorrect login does not work
    @Test
    void loginBadPassword() throws Exception {
        userService.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest("username", "wrongPassword");
        assertThrows(UnauthorizedException.class, () -> userService.login(loginRequest));
    }

    // assert user was never registered
    @Test
    void loginNoUser() {
        LoginRequest loginRequest = new LoginRequest("nobody", "password");
        assertThrows(UnauthorizedException.class, () -> userService.login(loginRequest));
    }

    // assert empty username or password don't work (login)
    @Test
    void loginNoField() {
        LoginRequest[] badRequests = {
                new LoginRequest(null, "password"),
                new LoginRequest("username", null)
        };
        for (LoginRequest badRequest : badRequests) {
            assertThrows(BadRequestException.class, () -> userService.login(badRequest));
        }
    }

    // assert token no longer works
    @Test
    void logoutSuccess() throws Exception {
        RegisterResult registerResult = userService.register(registerRequest);
        LogoutRequest logoutRequest = new LogoutRequest(registerResult.authToken());
        userService.logout(logoutRequest);
        assertNull(authDAO.getAuth(registerResult.authToken()));
    }

    // assert token was never issued
    @Test
    void logoutBadToken() {
        LogoutRequest logoutRequest = new LogoutRequest("badToken");
        assertThrows(UnauthorizedException.class, () -> userService.logout(logoutRequest));
    }
}
