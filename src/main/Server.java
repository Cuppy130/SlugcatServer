package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import engine.network.PlayerJoinPacket;

public class Server {
    public List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    public List<PlayerJoinPacket> onlinePlayers = new CopyOnWriteArrayList<>();
    private static int PORT = 500;
    private int playerId = 0;
    public int BUFFER_SIZE = 1024 * 32; // 32KB buffer size

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                PORT = Integer.parseInt(args[0]);
            } finally {
                System.out.println("Set port to " + PORT);
            }
        }
        Server server = new Server();
        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public synchronized void broadcast(byte[] data) {
        List<ClientHandler> disconnectedClients = new ArrayList<>();
        
        for (ClientHandler client : clients) {
            if (client.getSocket().isConnected() && !client.getSocket().isClosed()) {
                client.sendZstd(data);
            } else {
                disconnectedClients.add(client);
            }
        }
        
        // Remove disconnected clients from the list after broadcasting
        clients.removeAll(disconnectedClients);
    }


    public synchronized int getNewPlayerId() {
        return ++playerId;
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}
