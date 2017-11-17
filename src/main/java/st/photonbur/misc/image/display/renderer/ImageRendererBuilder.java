package st.photonbur.misc.image.display.renderer;

import st.photonbur.misc.image.misc.BufferedImageWithProperties;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;

public class ImageRendererBuilder {
    private LinkedHashMap<ImageRenderType, BiFunction<Integer, Integer, Color>> colorFunctions = new LinkedHashMap<>();
    private LinkedHashMap<ImageRenderType, BufferedImageWithProperties> images = new LinkedHashMap<>();

    public ImageRendererBuilder addRenderer(ImageRenderType type,
                                            BufferedImageWithProperties image,
                                            BiFunction<Integer, Integer, Color> colorFunction) {
        colorFunctions.put(type, colorFunction);
        images.put(type, image);
        return this;
    }

    public ImageRendererImpl build() {
        return new ImageRendererImpl(colorFunctions, images);
    }
}
