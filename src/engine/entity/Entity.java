package engine.entity;
import java.io.Serializable;
import java.util.UUID;
public class Entity implements Serializable {
    public UUID UUID;
    public float x;
    public float y;
    public int health;
    public int maxHealth;
    public boolean canAttack;
}
