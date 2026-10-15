package dataaccess;

import model.UserData;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO{
    private HashMap<String, UserData> allUsers = new HashMap<>();

    @Override
    public void clear() {
        allUsers.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        allUsers.put(user.username(), user);
    }

    @Override
    public UserData getUser(String userName) throws DataAccessException {
        return allUsers.get(userName);
    }
}
