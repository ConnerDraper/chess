package dataaccess;

import dataaccess.sql.SqlUserDAO;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlUserDAOTest {
    private UserDAO userDAO;
    private UserData userData;

    @BeforeEach
    void setUp() throws Exception {
        userDAO = new SqlUserDAO();
        userDAO.clear();
        userData = new UserData("username", "password", "email@email.com");
    }

    // clear removes all users
    @Test
    void clearPositive() throws Exception {
        userDAO.createUser(userData);
        userDAO.clear();
        assertNull(userDAO.getUser("username"));
    }

    // userData is stored and can be read back
    @Test
    void createUserPositive() throws Exception {
        userDAO.createUser(userData);
        assertEquals(userData, userDAO.getUser("username"));
    }

    // duplicate username is rejected by the primary key
    @Test
    void createUserNegativeDuplicate() throws Exception {
        userDAO.createUser(userData);
        assertThrows(DataAccessException.class, () ->
                userDAO.createUser(new UserData("username", "other", "other@email.com")));
    }

    // missing password is rejected by NOT NULL
    @Test
    void createUserNegativeNull() {
        assertThrows(DataAccessException.class, () ->
                userDAO.createUser(new UserData(null, "password", "email@email.com")));
        assertThrows(DataAccessException.class, () ->
                userDAO.createUser(new UserData("username", null, "email@email.com")));
        assertThrows(DataAccessException.class, () ->
                userDAO.createUser(new UserData("username", "password", null)));
    }

    // existing user is found
    @Test
    void getUserPositive() throws Exception {
        userDAO.createUser(userData);
        assertEquals("email@email.com", userDAO.getUser("username").email());
    }

    // missing user returns null
    @Test
    void getUserNegative() throws Exception {
        assertNull(userDAO.getUser("nobody"));
    }
}
