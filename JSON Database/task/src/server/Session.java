package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {
    private final Socket socket;
    private final JsonDatabase database;
    private final JsonDatabaseController controller;

    public Session(Socket socket, JsonDatabase database) {
        this.socket = socket;
        this.database = database;
        this.controller = new JsonDatabaseController();
    }

    @Override
    public void run() {
        try (var inStream = new DataInputStream(socket.getInputStream());
             var outStream = new DataOutputStream(socket.getOutputStream())) {

            String clientRequestJson = inStream.readUTF();
            System.out.println("Server - received: " + clientRequestJson);
//             todo use createAnswerJson & parseCommand & DEBUG_MODE
            if (clientRequestJson.contains("\"type\":\"exit\"")) {
                outStream.writeUTF("{\"response\":\"OK\"}");
                System.out.println("Server - sent: {\"response\":\"OK\"}");
                Main.server.stop();
                return;
            }
            String resultFromDb = controller.process(clientRequestJson);
            outStream.writeUTF(resultFromDb);
            System.out.println("Server - sent: " + resultFromDb);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}