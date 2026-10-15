package dataaccess;

import model.AuthData;
import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO{
    // maps a string (authToken to its authData)
    private HashMap<String, AuthData> allAuths = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        allAuths.clear();
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        allAuths.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return allAuths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        allAuths.remove(authToken);
    }
}
