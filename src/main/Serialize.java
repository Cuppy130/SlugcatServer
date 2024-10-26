package main;

import java.nio.ByteBuffer;
import java.io.*;

public class Serialize {
    // Method to serialize an object to a byte array
    public static byte[] serializeData(Object object, int packetType, int whoAmI) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
    
            // Serialize the object
            oos.writeObject(object);
            oos.flush();
            byte[] bytes = baos.toByteArray(); // Get the serialized data
    
            // Create a ByteBuffer to hold the packet
            ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + 4 + bytes.length); // 1 byte for packet type, 4 bytes for 'whoAmI', 4 bytes for length, serialized data
            buffer.put((byte) packetType);      // Packet type
            buffer.putInt(whoAmI);       // Player identifier
            buffer.putInt(bytes.length);        // Length of serialized data
            buffer.put(bytes);                  // Serialized data
    
            return buffer.array();              // Return the final byte array
        } catch (IOException e) {
            System.err.println("Failed to serialize object: " + e.getMessage());
        }
        return new byte[]{};  // Return empty array on failure
    }

    // Method to deserialize a byte array back to an object
    public static Object deserializeData(byte[] buffer) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
    
            // Read packet type (1 byte)
            byte packetType = byteBuffer.get();
    
            // Read 'whoAmI' (4 bytes)
            int whoAmI = byteBuffer.getInt();
    
            // Read the length of the serialized object (4 bytes)
            int dataLength = byteBuffer.getInt();
    
            // Validate the length to ensure it's not absurdly large
            if (dataLength < 0 || dataLength > 8192) {  // Assuming max object size is 8192 bytes
                System.err.println("Invalid data length: " + dataLength);
                return null;
            }
    
            // Check if there are enough bytes in the buffer
            if (byteBuffer.remaining() < dataLength) {
                System.err.println("Data length mismatch: Expected " + dataLength + " bytes, but only " + byteBuffer.remaining() + " bytes available.");
                return null;
            }
    
            // Read the serialized object
            byte[] objectBytes = new byte[dataLength];
            byteBuffer.get(objectBytes);
    
            // Deserialize the object
            ByteArrayInputStream bais = new ByteArrayInputStream(objectBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
    
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to deserialize data: " + e.getMessage());
        }
        return null;
    }
}
