package main;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import com.github.luben.zstd.Zstd;

import engine.network.PlayerJoinPacket;
import types.ConnectionTypes;
import types.GameplayPacket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private InputStream input;
    private OutputStream output;

    private Server server;

    private int whoAmI;
    private int state = 0;
    private PlayerJoinPacket PJP;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.whoAmI = server.getNewPlayerId();
        try {
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();
        } catch (IOException e) {
            System.err.println("Error initializing client streams: " + e.getMessage());
            disconnect();
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[server.BUFFER_SIZE];
        int bytesRead;

        try {
            while ((bytesRead = input.read(buffer)) != -1) {
                byte[] decompressedData = decompressZstd(buffer, bytesRead);

                if (decompressedData == null) {
                    System.err.println("Decompression failed. Ending client session.");
                    break;
                }

                handleClientData(decompressedData);
            }
        } catch (IOException e) {
            System.err.println("Error reading from client: " + e.getMessage());
        } finally {
            handleClientDisconnect();
        }
    }

    private void handleClientData(byte[] decompressedData) {
        ByteBuffer dataBuffer = ByteBuffer.wrap(decompressedData);

        if (state == 0) {
            if (dataBuffer.get() == ConnectionTypes.Request) {
                System.err.println("Player requested join");

                ByteBuffer responseData = ByteBuffer.allocate(5);
                responseData.put((byte) ConnectionTypes.Accepted);
                responseData.putInt(whoAmI);
                sendZstd(responseData.array());
                state = 1;
            }
        } else if (state == 1) {
            int packetType = dataBuffer.get();

            if (packetType == GameplayPacket.PlayerJoined) {
                server.broadcast(decompressedData);
                PJP = (PlayerJoinPacket) Serialize.deserializeData(decompressedData);
                if (PJP != null) {
                    server.onlinePlayers.add(PJP);
                    sendPlayerAllPlayers();
                }
            } else if (packetType == GameplayPacket.PlayerUpdate) {
                server.broadcast(decompressedData);
            }
        }
    }

    private void handleClientDisconnect() {
        disconnect();
        server.clients.remove(this);
        if (PJP != null) {
            server.onlinePlayers.remove(PJP);
        }

        ByteBuffer disconnectData = ByteBuffer.allocate(5);
        disconnectData.put((byte) GameplayPacket.PlayerLeft);
        disconnectData.putInt(whoAmI);
        server.broadcast(disconnectData.array());
    }

    private void sendPlayerAllPlayers() {
        synchronized (server.onlinePlayers) {
            for (PlayerJoinPacket player : server.onlinePlayers) {
                sendZstd(Serialize.serializeData(player, GameplayPacket.PlayerJoined, player.id));
            }
        }
    }

    public void sendZstd(byte[] dataToSend) {
        try {
            byte[] compressedData = Zstd.compress(dataToSend);
            ByteBuffer buffer = ByteBuffer.allocate(4 + compressedData.length);
            buffer.putInt(compressedData.length);
            buffer.put(compressedData);

            output.write(buffer.array());
            output.flush();
        } catch (IOException e) {
            System.err.println("Error sending compressed data to client: " + e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    private byte[] decompressZstd(byte[] data, int length) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);
            int compressedSize = buffer.getInt();
            byte[] compressedData = new byte[compressedSize];
            buffer.get(compressedData);

            long decompressedSize = Zstd.decompressedSize(compressedData);
            byte[] decompressedData = new byte[(int) decompressedSize];
            Zstd.decompress(decompressedData, compressedData);
            return decompressedData;
        } catch (Exception e) {
            System.err.println("Error during decompression: " + e.getMessage());
            return null;
        }
    }

    private void disconnect() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}
