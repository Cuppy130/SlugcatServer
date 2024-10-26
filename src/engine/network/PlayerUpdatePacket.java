package engine.network;
public class PlayerUpdatePacket extends PlayerJoinPacket {
    private static final long serialVersionUID = 1L;
    public float direction;
    public boolean isMoving;
    public int health;
    public int maxHealth;
    public String name;
    public int level;
    public int id;
    
    public float x, y;
    public int cx, cy;

    public float speed;
}
