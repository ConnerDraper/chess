package dataaccess;

import dataaccess.mysql.MySqlAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class MySqlAuthDAOTest {
    private AuthDAO authDAO;
    private AuthData authData;

    @BeforeEach
    void setUp() throws Exception {
        authDAO = new MySqlAuthDAO();
        authDAO.clear();
        authData = new AuthData("authToken", "username");
    }

    // clear removes all auths
    @Test
    void clearPositive() throws Exception {
        authDAO.createAuth(authData);
        authDAO.clear();
        assertNull(authDAO.getAuth("authToken"));
    }

    // authData is stored and can be fetched
    @Test
    void createAuthPositive() throws Exception {
        authDAO.createAuth(authData);
        assertEquals(authData, authDAO.getAuth("authToken"));
    }

    // duplicate authToken is rejected
    @Test
    void createAuthNegative() throws Exception {
        authDAO.createAuth(authData);
        assertThrows(DataAccessException.class, () ->
                authDAO.createAuth(new AuthData("authToken", "otherUsername")));
    }

    // existing auth is found
    @Test
    void getAuthPositive() throws Exception {
        authDAO.createAuth(authData);
        assertEquals("username", authDAO.getAuth("authToken").username());
    }

    // missing auth returns null
    @Test
    void getAuthNegative() throws Exception {
        assertNull(authDAO.getAuth("fakeToken"));
    }

    // deleted auth is deleted
    @Test
    void deleteAuthPositive() throws Exception {
        authDAO.createAuth(authData);
        authDAO.deleteAuth("authToken");
        assertNull(authDAO.getAuth("authToken"));
    }

    // deleting a non-existent auth does not affect other auths
    @Test
    void deleteAuthNegative() throws Exception {
        authDAO.createAuth(authData);
        authDAO.deleteAuth("fakeToken");
        assertNotNull(authDAO.getAuth("authToken"));
    }
}
