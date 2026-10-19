package dataaccess;

public interface UserDAO {
    void clear() throws DataAccessException;

    void createUser(model.UserData user) throws DataAccessException;

    model.UserData getUser(String userName) throws DataAccessException;
}
