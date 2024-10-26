package types;

public class ConnectionTypes {
    public static final int Request = 0x01; // sent by the client with the current version of the game.
    public static final int Denied = 0x02; // sent if the client does not meet the minimum requirements for the server.
    public static final int RequestingPassword = 0x03; // server -> client
    public static final int SendPassword = 0x04; // client -> server
    public static final int Accepted = 0x05; // sent to the player if they have been accepted into the server
}
