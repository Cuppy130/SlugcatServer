package engine.network;

import java.io.Serializable;

public class PlayerAcceptedPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    private int whoAmI;
    private boolean enabled;
    private int newState;
    public PlayerAcceptedPacket(int whoAmI, boolean enabled, int newState){
        this.whoAmI = whoAmI;
        this.enabled = enabled;
        this.newState = newState;
    }
}
