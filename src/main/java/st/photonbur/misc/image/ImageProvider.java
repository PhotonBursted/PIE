package st.photonbur.misc.image;

import java.awt.image.BufferedImage;

public interface ImageProvider {
    BufferedImage getGeneratingImage();

    String getProgressString();
}
