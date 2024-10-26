package gui;
import java.io.Serializable;
public class Color implements Serializable{
    private static final long serialVersionUID = 1L;  // Optional, but recommended for version control of the serialized class

    public int value;

    
    // Additional helper methods, e.g., for getting individual components
    public int getRed() {
        return (value >> 16) & 0xFF;
    }

    public int getGreen() {
        return (value >> 8) & 0xFF;
    }

    public int getBlue() {
        return value & 0xFF;
    }

    public int getAlpha() {
        return (value >> 24) & 0xFF;
    }

    @Override
    public String toString() {
        return "Color {" + 
        "red=" + getRed() +
        ", green=" + getGreen() +
        ", blue=" + getBlue() + "}";
    }
}
