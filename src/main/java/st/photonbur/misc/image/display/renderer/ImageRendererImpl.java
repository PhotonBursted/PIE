package st.photonbur.misc.image.display.renderer;

import st.photonbur.misc.image.misc.BufferedImageWithProperties;

import java.awt.*;
import java.util.HashMap;
import java.util.function.BiFunction;

/**
 * The implementation of the {@link ImageRenderer} interface.
 * Renders a set of images based on the currently selected image and render type.
 */
public class ImageRendererImpl implements ImageRenderer {
    /**
     * The set of functions that map location to a color.
     * This resulting color will be used to draw on the image canvas.
     */
    private final HashMap<ImageRenderType, BiFunction<Integer, Integer, Color>> colorFunctions;

    /**
     * The set of images that are currently being rendered.
     */
    private final HashMap<ImageRenderType, BufferedImageWithProperties> images;
    /**
     * The currently used render type.
     */
    private ImageRenderType renderType;

    ImageRendererImpl(HashMap<ImageRenderType, BiFunction<Integer, Integer, Color>> colorFunctions,
                      HashMap<ImageRenderType, BufferedImageWithProperties> images) {
        this.colorFunctions = colorFunctions;
        this.images = images;
    }

    /**
     * Retrieves the image generated for the specified render type so far.
     * This may be incomplete as only one image is updated at a time.
     *
     * @param imageType The render type to fetch the image for
     * @return The image rendered using the specified render type
     */
    public BufferedImageWithProperties getImageFor(ImageRenderType imageType) {
        return images.get(imageType);
    }

    /**
     * Retrieves what render types are available for the currently rendering image.
     * @return The types that have been specified for the currently rendering image.
     */
    public ImageRenderType[] getSupportedTypes() {
        return images.keySet().toArray(new ImageRenderType[images.size()]);
    }

    /**
     * Puts a pixel onto the image corresponding to the current render type.
     *
     * @param x The x coordinate of the pixel to draw
     * @param y The y coordinate of the pixel to draw
     */
    @Override
    public void render(int x, int y) {
        // Determine the color of the pixel to draw
        Color color = colorFunctions.get(renderType).apply(x, y);
        // Apply to the canvas if applicable
        if (color != null) images.get(renderType).setRGB(x, y, color.getRGB());
    }

    /**
     * Rerenders a certain area of the image.
     * This is used to let the rendering of an image "catch up" after it has been idle and not saving to the image.
     *
     * @param bounds The bounds determining what part of the image to redraw
     * @see ImageRendererImpl#setRenderType(int)
     * @see ImageRendererImpl#render(int, int)
     */
    private void rerender(Rectangle bounds) {
        for (int x = bounds.x; x < bounds.x + bounds.width; x++)
            for (int y = bounds.y; y < bounds.y + bounds.height; y++)
                render(x, y);
    }

    /**
     * Sets the current render type to another.
     *
     * @param typeIndex The index of the render type to apply
     */
    public void setRenderType(int typeIndex) {
        // Try to retrieve what area has been drawn in so far
        Rectangle drawnArea = null;
        if (images.get(renderType) != null) drawnArea = images.get(renderType).getDrawnArea();

        // Retreive the new render type
        this.renderType = getSupportedTypes()[typeIndex];

        // Apply the drawn area to the newly selected image
        images.get(renderType).setDrawnArea(drawnArea);

        // Rerender the new image on a new thread
        Rectangle finalDrawnArea = drawnArea;
        if (finalDrawnArea != null) new Thread(() -> rerender(finalDrawnArea)).start();
    }
}
