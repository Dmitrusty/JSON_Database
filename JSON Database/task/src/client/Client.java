package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    public void run(String host, int port, String dataToSend) {

        try (Socket socket = new Socket(InetAddress.getByName(host), port);
             DataInputStream inStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outStream = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Client started!");

            outStream.writeUTF(dataToSend);
            System.out.println("Sent: " + dataToSend);

            System.out.println("Received: " + inStream.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}