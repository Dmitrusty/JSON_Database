package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public final int PORT;
    protected final JsonDatabase database;
    private final ServerSocket serverSocket;
    private final ExecutorService service;

    public Server(int PORT, String dbPath) throws IOException {
        this.PORT = PORT;
        this.database = new JsonDatabase(dbPath);
        serverSocket = new ServerSocket(PORT);
        service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 3);
    }

    public void run() {
        System.out.println("Server started!");
        try {
            while (true) {
                service.submit(new Session(serverSocket.accept(), database));
            }
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {

        }
        service.shutdown();
    }
}