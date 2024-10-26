package engine.entity;

import gui.Color;

public class Player extends Entity {
    private static final long serialVersionUID = 1L;
    public float x;
    public float y;
    public String name;
    public int level;
    public int chunkX;
    public int chunkY;
    public float speed;
    public int id;
    public byte[] inventory;
    public int facing;
    public Color color;
    public float direction;
    public boolean isMoving;

    @Override
    public String toString() {
        return "Player {" +
            "name='" + name + "'" +
            ", level=" + level +
            ", health=" + health +
            ", x=" + x +
            ", y=" + y +
            ", chunkX=" + chunkX +
            ", chunkY=" + chunkY +
            ", UUID=" + UUID.toString() +
            ", id=" + id +
            ", color=" + color +
        '}';
    }
}
