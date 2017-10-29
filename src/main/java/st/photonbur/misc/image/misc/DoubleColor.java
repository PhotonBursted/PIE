package st.photonbur.misc.image.misc;

import java.awt.*;

/**
 * Acts as a color, but uses doubles to store values instead of integers.
 */
public class DoubleColor {
    /**
     * The amount of red in this color.
     */
    private double red;
    /**
     * The amount of green in this color.
     */
    private double green;
    /**
     * The amount of blue in this color.
     */
    private double blue;

    public DoubleColor(double red, double green, double blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Converts this instance into a {@link Color}, rounding all channels to integers.
     * @return The color instance that matches the color values in this object as closely as possible
     */
    private Color toNormalColor() {
        return new Color((int) Math.round(red), (int) Math.round(green), (int) Math.round(blue));
    }

    /**
     * @return The amount of red in this color
     */
    public double getRed() {
        return red;
    }

    /**
     * @return The amount of green in this color
     */
    public double getGreen() {
        return green;
    }

    /**
     * @return The amount of green in this color
     */
    public double getBlue() {
        return blue;
    }

    /**
     * @return The closest matching integer interpretation of this object as RGB
     */
    public int getRGB() {
        return toNormalColor().getRGB();
    }

    @Override
    public String toString() {
        return "r=" + red + ",g=" + green + ",b=" + blue;
    }
}
