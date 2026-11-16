package client;

public class ClientMain {
    public static void main(String[] args) {
        ChessRepl repl = new ChessRepl(8080);
        repl.run();
    }
}
