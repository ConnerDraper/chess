package server;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.sql.SqlAuthDAO;
import dataaccess.sql.SqlGameDAO;
import dataaccess.sql.SqlUserDAO;

import io.javalin.Javalin;
import service.DataService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // data access
        UserDAO userDAO;
        AuthDAO authDAO;
        GameDAO gameDAO;
        try {
            userDAO = new SqlUserDAO();
            authDAO = new SqlAuthDAO();
            gameDAO = new SqlGameDAO();
        } catch (DataAccessException exception) {
            throw new RuntimeException("Could not set up database", exception);
        }

        // services
        UserService userService = new UserService(userDAO, authDAO);
        DataService dataService = new DataService(userDAO, authDAO, gameDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        // handler
        Handler handler = new Handler(dataService, userService, gameService);
        registerRoutes(handler);
    }

    private void registerRoutes(Handler handler) {
        javalin.delete("/db", handler::handleClear);
        javalin.post("/user", handler::handleRegister);
        javalin.post("/session", handler::handleLogin);
        javalin.delete("/session", handler::handleLogout);
        javalin.get("/game", handler::handleListGames);
        javalin.post("/game", handler::handleCreateGame);
        javalin.put("/game", handler::handleJoinGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
