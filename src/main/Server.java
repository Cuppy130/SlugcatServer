package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import engine.network.PlayerJoinPacket;

public class Server {
    public List<ClientHandler> clients = new ArrayList<>();
    public List<PlayerJoinPacket> onlinePlayers = new ArrayList<>();
    private static int port = 500;
    private int playerId = 0;

    public static void main(String[] args) {
        if(args.length>0){
            try{
                port = Integer.parseInt(args[0]);
            } finally {
                System.out.println("set port to " + port);
            }
        }
        Server server = new Server();
        server.startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept new client connections
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler); // Add client to the list
                new Thread(clientHandler).start(); // Start a new thread for the client
                System.out.println("New client connected: " + clientSocket.getInetAddress());
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    public void broadcast(byte[] message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (!client.clientSocket.isClosed() && client.clientSocket.isConnected()) {
                    try {
                        client.output.write(message);
                        client.output.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Socket closed or not connected, removing client.");
                    clients.remove(client);
                }
            }
            // System.out.println("Sent message of " + message.length + " bytes to " + clients.size() + " clients.");
        }
    }

    public int getNewPlayerId() {
        playerId++;
        return playerId;
    }
}
