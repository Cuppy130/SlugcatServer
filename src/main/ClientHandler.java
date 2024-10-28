package main;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

import engine.network.PlayerJoinPacket;
import engine.network.PlayerUpdatePacket;
import types.ConnectionTypes;
import types.GameplayPacket;

public class ClientHandler implements Runnable {
    private static final int BUFFER_SIZE = 16 * 1024; // 16 KB buffer size
    public Socket clientSocket;
    private InputStream input;    // Input stream for reading data from client
    public OutputStream output;   // Output stream for sending data to client

    private int whoAmI;
    private int state = 0;

    private PlayerJoinPacket PJP;
    private Server server;
    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        try {
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();
        } catch (IOException e) {
            System.err.println("Error initializing client streams: " + e.getMessage());
        }
    }


    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead; // Declare the variable outside the loop
        
        try {
            while ((bytesRead = input.read(buffer)) != -1) { // Read bytes into the buffer
                ByteBuffer bytes = ByteBuffer.wrap(buffer, 0, bytesRead);

                int packetType = bytes.get();
                // System.out.println("Received packet type: " + packetType + " from client: " + whoAmI);

                if(state == 0){
                    if(packetType == ConnectionTypes.Request){
                        ByteBuffer buff = ByteBuffer.allocate(5);
                        buff.put((byte)ConnectionTypes.Accepted);
                        whoAmI = server.getNewPlayerId();
                        buff.putInt(whoAmI);
                        sendMessage(buff.array());
                        state = 1;
                    }
                } else if(state == 1){
                    // System.out.println("Received " + bytesRead + " bytes from client " + clientSocket.getRemoteSocketAddress());
                    // System.out.println("With id: " + packetType);
                    if(packetType == GameplayPacket.PlayerJoined) {
                        int who = bytes.getInt();
                        System.out.println(whoAmI + " Has joined the server.");
                        PlayerJoinPacket p = (PlayerJoinPacket) Serialize.deserializeData(buffer);
                        
                        if(p==null){
                            clientSocket.close();
                            continue;
                        }

                        if(p.id!=whoAmI||p.id==-1){
                            System.err.println("player ("+whoAmI+") attempted to join server (" + who + ")");
                            clientSocket.close();
                            continue;
                        }
                        
                        // Broadcast to other players, but don't send the packet back to the player who just joined
                        server.onlinePlayers.add(p);
                        server.broadcast(buffer);
                        
                        PJP = p;

                        sendAllPlayers();

                    } else if (packetType == GameplayPacket.PlayerUpdate) {
                        int who = bytes.getInt();  // Get the player ID from the packet
                        PlayerUpdatePacket p = (PlayerUpdatePacket) Serialize.deserializeData(buffer);
                        
                        p.id = whoAmI;

                        if(p.id!=whoAmI||p.id==-1){
                            System.err.println("player ("+whoAmI+") attempted to modify other user (" + who + ")");
                            clientSocket.close();
                            continue;
                        }
                        // Broadcast the updated packet to all other clients
                        server.broadcast(buffer);
                    } else if (packetType == GameplayPacket.PlayerLeft){
                        onDisconnect();
                    } else if (packetType == GameplayPacket.Chat){
                        System.out.println("Chat message recieved, broadcasting... ("+bytesRead+" bytes)");
                        server.broadcast(buffer);
                    }
                    
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from client: " + e.getMessage());
        } finally {
            onDisconnect();
        }
    }

    private void sendAllPlayers(){
        synchronized (server.onlinePlayers){
            for (PlayerJoinPacket packet : server.onlinePlayers) {
                try {
                    output.write(Serialize.serializeData(packet, GameplayPacket.PlayerJoined, whoAmI));
                    output.flush();
                    Thread.sleep(250);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void playerJoined(PlayerJoinPacket PJP){
        sendMessage(Serialize.serializeData(PJP, GameplayPacket.PlayerJoined, PJP.id));
    }

    public void sendMessage(byte[] message) {
        try {
            output.write(message);  // Send the message to the client
            output.flush();         // Ensure the message is sent immediately
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            // Clean up resources
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }

    private void onDisconnect(){
        if (server.onlinePlayers.contains(PJP)) {
            server.onlinePlayers.remove(PJP);
        }
        server.clients.remove(this);
        closeConnection();
        if(PJP!=null){
            System.out.println(PJP.name + " Left the server");
        }
        ByteBuffer leaveBuffer = ByteBuffer.allocate(5);
        leaveBuffer.put((byte)GameplayPacket.PlayerLeft);
        leaveBuffer.putInt(whoAmI);
        server.broadcast(leaveBuffer.array());
    }
}
