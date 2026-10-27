package service;

import dataaccess.*;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataServiceTest {
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private DataService dataService;

    @BeforeEach
    void setUp() {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        userDAO = new MemoryUserDAO();

        dataService = new DataService(userDAO, authDAO, gameDAO);
    }

    @Test
    void clearSuccess() throws Exception {
        UserData userData = new UserData("username", "password", "email@email.com");
        AuthData authData = new AuthData("authToken67", "username");
        userDAO.createUser(userData);
        authDAO.createAuth(authData);
        gameDAO.createGame("game67");

        dataService.clear();

        UserData user = userDAO.getUser("username");
        AuthData auth = authDAO.getAuth("authToken67");

        // assert everything is gone
        assertNull(user);
        assertNull(auth);
        assertEquals(List.of(), gameDAO.listGames());
    }

    // clearing nothing still works
    @Test
    void clearEmpty() throws Exception {
        dataService.clear();
        assertEquals(gameDAO.listGames(), List.of());
    }
}
