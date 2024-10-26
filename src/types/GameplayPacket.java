package types;

public class GameplayPacket {
    public static final int PlayerJoined = 0x06;
    public static final int PlayerLeft = 0x07;
    public static final int PlayerUpdate = 0x08;
    public static final int Chat = 0x09;
    public static final int ServerRequestingItem = 0x0A;
    public static final int PlayerUsername = 0x0B;
    public static final int Activate = 0x0C;
    public static final byte GetPlayers = 0x0D;
}


