package server;

import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;
import io.javalin.Javalin;
import service.DataService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // data access
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

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
