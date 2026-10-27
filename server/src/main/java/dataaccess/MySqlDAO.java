package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Abstract base class for SQL Data Access Objects
 */
public abstract class MySqlDAO {
    MySqlDAO(String statement) throws DataAccessException {
        createTable(statement);
    }

    private void createTable(String setupStatement) throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(setupStatement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Could not set up database ", exception);
        }
    }

    void runUpdate(String updateStatement, Object... parameters) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateStatement)) {
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Could not update database ", exception);
        }
    }
}
