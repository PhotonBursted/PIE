package st.photonbur.misc.image;

import java.awt.image.BufferedImage;

/**
 * Any class implementing this interface is capable of supplying preview images to the {@link ImageCreationDisplay} for displaying on screen.
 */
public interface ImageProvider {
    /**
     * Requests the image as it is being generated.
     *
     * @return The instance of the image being edited at the moment
     */
    BufferedImage getGeneratingImage();

    /**
     * Requests the string denoting what progress is being made on the image.
     *
     * @return The string describing how far in the process the algorithm is in finishing the image generation.
     */
    String getProgressString();
}
