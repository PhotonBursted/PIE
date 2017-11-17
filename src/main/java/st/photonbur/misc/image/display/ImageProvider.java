package st.photonbur.misc.image.display;

import st.photonbur.misc.image.display.renderer.ImageRenderType;
import st.photonbur.misc.image.display.renderer.ImageRendererImpl;

import java.awt.image.BufferedImage;

/**
 * Any class implementing this interface is capable of supplying preview images to the {@link ImageCreationDisplay} for displaying on screen.
 */
public interface ImageProvider {
    ImageRendererImpl getImageRenderer();

    /**
     * Requests the image as it is being generated.
     *
     * @return The instance of the image being edited at the moment
     */
    BufferedImage getGeneratingImage(ImageRenderType imageType);

    /**
     * Requests the string denoting what progress is being made on the image.
     *
     * @return The string describing how far in the process the algorithm is in finishing the image generation.
     */
    String getProgressString();

    /**
     * Fired when the renderer has to switch to another type as another tab has been selected.
     *
     * @param selectedIndex The index of the tab that has newly been selected
     */
    void onRenderSwitchEvent(int selectedIndex);
}
