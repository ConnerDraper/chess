package service;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
    private UserDAO userDAO;
    private AuthDAO authDAO;

    public UserService(
            UserDAO userDAO,
            AuthDAO authDAO
    ) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(
            RegisterRequest registerRequest
    ) throws BadRequestException, AlreadyTakenException {
        if ((registerRequest.username() == null) ||
            (registerRequest.password() == null) ||
            (registerRequest.email() == null)) {throw BadRequestException;}
    }
    public LoginResult login(LoginRequest loginRequest) {}
    public void logout(LogoutRequest logoutRequest) {}
}
*