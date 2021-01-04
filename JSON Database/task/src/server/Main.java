package server;

import java.io.IOException;

public class Main {
    public static Server server;

    public static void main(String[] args) throws IOException {
        server = new Server(9889, "JSON Database/task/src/server/data/db.json");
        server.run();
    }
}