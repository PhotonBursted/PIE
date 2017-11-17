package st.photonbur.misc.image.misc;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * An extension of {@link BufferedImage} which also saves within what bounds has been drawn before.
 */
public class BufferedImageWithProperties extends BufferedImage {
    /**
     * The area in which has been drawn before
     */
    private Rectangle drawnArea;

    public BufferedImageWithProperties(int width, int height, int imageType) {
        super(width, height, imageType);
    }

    /**
     * @return Retreives the bounds of the area in which pixels have been placed into this image
     */
    public Rectangle getDrawnArea() {
        return drawnArea;
    }

    /**
     * Includes a certain pixel inside of the bounds, growing them as necessary.
     *
     * @param x The x coordinate of the pixel to add to the drawn area
     * @param y The y coordinate of the pixel to add to the drawn area
     */
    private void include(int x, int y) {
        drawnArea = (Rectangle) drawnArea.createUnion(new Rectangle(x, y, 1, 1));
    }

    /**
     * Adjusts the drawn area to be the passed rectangle.
     *
     * @param drawnArea The bounds to apply
     */
    public void setDrawnArea(Rectangle drawnArea) {
        this.drawnArea = drawnArea;
    }

    /**
     * Draws a certain pixel on the canvas contained within this image.
     *
     * @param x The x coordinate of the pixel to draw
     * @param y The y coordinate of the pixel to draw
     * @param rgb The int representing the color to draw the pixel with
     */
    @Override
    public synchronized void setRGB(int x, int y, int rgb) {
        super.setRGB(x, y, rgb);

        // Expand the drawn area if necessary
        if (drawnArea == null) {
            drawnArea = new Rectangle(x, y, 1, 1);
        } else {
            include(x, y);
        }
    }
}
