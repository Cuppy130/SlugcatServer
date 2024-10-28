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
    private Server server;
    private InputStream input;
    private OutputStream output;

    private int whoAmI = Server.getNewPlayerId();
    private int state = 0;
    private PlayerJoinPacket PJP;

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
        byte[] buffer = new byte[Server.BUFFER_SIZE];
        int bytesRead;

        try {
            while ((bytesRead = input.read(buffer)) != -1) {
                System.out.println("Received " + bytesRead + " bytes from client.");
                byte[] decompressedData = decompressZstd(buffer, bytesRead);
                
                if (decompressedData == null) {
                    return;
                }

                if(state == 0){
                    if(ByteBuffer.wrap(decompressedData).get()==ConnectionTypes.Request){
                        System.err.println("player requested join");
                        ByteBuffer data = ByteBuffer.allocate(5);
                        data.put((byte)ConnectionTypes.Accepted);
                        data.putInt(whoAmI);
                        sendZstd(data.array());
                        state = 1;
                    }
                } else if(state == 1){
                    int packetType = ByteBuffer.wrap(decompressedData).get();
                    if(packetType==GameplayPacket.PlayerJoined){
                        Server.broadcast(decompressedData);
                        PJP = (PlayerJoinPacket) Serialize.deserializeData(decompressedData);
                        Server.onlinePlayers.add(PJP);
                    } else if(packetType == GameplayPacket.PlayerUpdate){
                        Server.broadcast(decompressedData);
                    }

                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from client: " + e.getMessage());
        } finally {
            closeConnection();
            server.clients.remove(this);
            if(PJP!=null){
                Server.onlinePlayers.remove(PJP);
            }
            ByteBuffer buffer2 = ByteBuffer.allocate(5);
            buffer2.put((byte)GameplayPacket.PlayerLeft);
            buffer2.putInt(whoAmI);
            Server.broadcast(buffer2.array());
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

    private byte[] decompressZstd(byte[] data, int length) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);
            int compressedSize = buffer.getInt();
            byte[] compressedData = new byte[compressedSize];
            buffer.get(compressedData);

            @SuppressWarnings("deprecation")
            long decompressedSize = Zstd.decompressedSize(compressedData);
            byte[] decompressedData = new byte[(int) decompressedSize];
            Zstd.decompress(decompressedData, compressedData);
            return decompressedData;
        } catch (Exception e) {
            System.err.println("Error during decompression: " + e.getMessage());
            return null;
        }
    }

    private void closeConnection() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}