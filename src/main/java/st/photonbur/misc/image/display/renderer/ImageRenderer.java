package st.photonbur.misc.image.display.renderer;

/**
 * Interface used for regulating what the image renderers have to contain at least.
 */
public interface ImageRenderer {
    /**
     * Renders a pixel onto an image canvas.
     *
     * @param x The x coordinate of the pixel to draw on the canvas
     * @param y The y coordinate of the pixel to draw on the canvas
     */
    void render(int x, int y);
}
