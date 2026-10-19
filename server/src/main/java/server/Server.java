package server;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import io.javalin.*;
import service.DataService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

        // data access
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        // services
        UserService userService = new UserService(userDAO, authDAO);
        DataService dataService = new DataService(userDAO, authDAO, gameDAO);

        // handler
        Handler handler = new Handler(dataService, userService);
        registerRoutes(handler);
    }

    private void registerRoutes(Handler handler) {
        javalin.delete("/db", handler::handleClear);
        javalin.post("/user", handler::handleRegister);
        javalin.post("/session", handler::handleLogin);
        javalin.delete("/session", handler::handleLogout);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
