package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.exception.AlreadyTakenException;
import service.exception.BadRequestException;
import service.exception.UnauthorizedException;
import service.request.LoginRequest;
import service.request.LogoutRequest;
import service.request.RegisterRequest;
import service.result.LoginResult;
import service.result.LogoutResult;
import service.result.RegisterResult;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(
            UserDAO userDAO,
            AuthDAO authDAO
    ) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public RegisterResult register(
            RegisterRequest registerRequest
    ) throws DataAccessException, BadRequestException, AlreadyTakenException {
        String username = registerRequest.username();
        String password = registerRequest.password();
        String email = registerRequest.email();

        // check for null information so we don't get an error when we check
        if ((username == null) || (password == null) || (email == null)) {
            throw new BadRequestException("bad request");
        }

        // see if username is already taken
        UserData userData = userDAO.getUser(username);
        if (userData != null) {
            throw new AlreadyTakenException("already taken");
        }

        // create user after checks
        userData = new UserData(username, password, email);
        userDAO.createUser(userData);
        // create authToken and register
        String authToken = createAuth(username);

        return new RegisterResult(username, authToken);
    }

    public LoginResult login(
            LoginRequest loginRequest
    ) throws DataAccessException, BadRequestException, UnauthorizedException {
        String username = loginRequest.username();
        String password = loginRequest.password();

        // check for null information so we don't get an error when we check
        if ((username == null) || (password == null)) {
            throw new BadRequestException("bad request");
        }

        // see if user does not exist
        UserData userData = userDAO.getUser(username);
        if (userData == null || !(password.equals(userData.password()))) {
            throw new UnauthorizedException("unauthorized");
        }

        // get login result
        String authToken = createAuth(username);
        return new LoginResult(username, authToken);
    }

    public LogoutResult logout(
            LogoutRequest logoutRequest
    ) throws DataAccessException, UnauthorizedException {
        String authToken = logoutRequest.authToken();

        // no auth
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException("unauthorized");
        }

        authDAO.deleteAuth(authToken);
        return new LogoutResult();
    }

    private String createAuth(String username) throws DataAccessException {
        String authToken = generateToken();
        authDAO.createAuth(new AuthData(authToken, username));
        return authToken;
    }
}
