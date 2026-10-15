package dataaccess;

public interface AuthDAO {
    void clear() throws DataAccessException;
    void createAuth(model.AuthData auth) throws DataAccessException;
    model.AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
}
