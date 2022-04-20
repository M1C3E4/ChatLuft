package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private ServerSocket serverSocket;
    private ArrayList<Connection> connections = new ArrayList<Connection>();

    public Server(int port) throws IOException {
        System.out.println("[SERVER-LOG] Server starting...");
        serverSocket = new ServerSocket(port);
        System.out.println("[SERVER-LOG] Server socket started...");
        while (true)
        {
            System.out.println("[SERVER-LOG] Waiting for new connection...");
            Socket client = serverSocket.accept();
            System.out.println("[SERVER-LOG] Client connected [external address:" + client.getInetAddress() + "]");
            Connection connection = new Connection(client);
            connections.add(connection);
            new Thread(connection).start();
        }
    }
}
