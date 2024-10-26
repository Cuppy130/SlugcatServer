package misc;

import java.io.Serializable;

public class RemotePlayer implements Serializable {
    private static final long serialVersionUID = 1L;
    public float x;
    public float y;
    public int cx;
    public int cy;
    public int health;
    public int id;
    public float speed;
    public float direction;
    public boolean isMoving;

    
    @Override
    public String toString() {
        return "RemotePlayer{" +
            ", x=" + x +
            ", y=" + y +
            ", cx=" + cx +
            ", cy=" + cy +
            ", health=" + health +
            ", id=" + id +
            ", speed=" + speed +
            ", direction=" + direction +
            ", isMoving=" + isMoving +
            "}";
    }
}

