package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlUserDAO extends MySqlDAO implements UserDAO {
    public MySqlUserDAO() throws DataAccessException {
        super("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    PRIMARY KEY (username)
                )""");
    }

    @Override
    public void clear() throws DataAccessException {
        runUpdate("TRUNCATE TABLE users");
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String statement = "INSERT INTO users (username, password, email) values (?, ?, ?)";
        runUpdate(statement, user.username(), user.password(), user.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String statement = "SELECT username, password, email FROM users WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, username);
            ResultSet results = preparedStatement.executeQuery();
            // no user found
            if (!results.next()) {
                return null;
            }
            // fetch and return user's data
            return new UserData(results.getString("username"),
                    results.getString("password"),
                    results.getString("email")
            );
        } catch (SQLException exception) {
            throw new DataAccessException("Could not find user", exception);
        }
    }
}
