package engine.network;

import java.util.UUID;
import java.io.Serializable;
import gui.Color;

public class PlayerJoinPacket implements Serializable  {
    private static final long serialVersionUID = 1L;
    public float direction;
    public boolean isMoving;
    public int health;
    public int maxHealth;
    public String name;
    public int level;
    public int id;
    public Color color;
    public UUID UUID;
    
    public float x, y;
    public int cx, cy;

    public float speed;
}
