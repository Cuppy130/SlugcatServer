package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import engine.network.PlayerJoinPacket;

public class Server {
    public static List<ClientHandler> clients = new ArrayList<>();
    public static List<PlayerJoinPacket> onlinePlayers = new ArrayList<>();
    private static int PORT = 500;
    private static int playerId = 0;
    public static int BUFFER_SIZE = 1024*32; // 32KB buffer size

    public static void main(String[] args) {
        if(args.length>0){
            try{
                PORT = Integer.parseInt(args[0]);
            } finally {
                System.out.println("set port to " + PORT);
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

    public synchronized static void broadcast(byte[] data) {
        for (ClientHandler client : clients) {
            client.sendZstd(data);
        }
    }

    public static int getNewPlayerId() {
        playerId++;
        return playerId;
    }
}
