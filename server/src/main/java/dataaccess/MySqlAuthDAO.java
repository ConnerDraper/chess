package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlAuthDAO extends MySqlDAO implements AuthDAO {
    public MySqlAuthDAO() throws DataAccessException {
        super("""
                CREATE TABLE IF NOT EXISTS auths (
                    authToken VARCHAR(255) NOT NULL,
                    username VARCHAR(255) NOT NULL,
                    PRIMARY KEY (authToken)
                )""");
    }

    @Override
    public void clear() throws DataAccessException {
        runUpdate("TRUNCATE TABLE auths");
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String statement = "INSERT INTO auths (authToken, username) VALUES (?, ?)";
        runUpdate(statement, auth.authToken(), auth.username());
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String statement = "SELECT authToken, username FROM auths WHERE authToken = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, authToken);
            ResultSet results = preparedStatement.executeQuery();
            // no auth found
            if (!results.next()) {
                return null;
            }
            // fetch and return auth data
            return new AuthData(results.getString("authToken"),
                    results.getString("username")
            );
        } catch (SQLException exception) {
            throw new DataAccessException("Could not find auth", exception);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String statement = "DELETE FROM auths WHERE authToken = ?";
        runUpdate(statement, authToken);
    }
}
