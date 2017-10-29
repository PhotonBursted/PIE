package st.photonbur.misc.image;

/**
 * Provides a base for building a certain algorithm.
 *
 * @param <TBuilder> The type of the builder to use
 * @param <TOut>     The type of the instance to produce
 */
@SuppressWarnings({"unchecked", "UnusedReturnValue"})
public abstract class AbstractBuilder<TBuilder extends AbstractBuilder<TBuilder, TOut>, TOut> {
    /**
     * The panel to display the progress of the algorithm on.
     */
    protected ImageCreationDisplay guiPanel;

    /**
     * The height of the image to be generated.
     */
    protected int imageHeight;

    /**
     * The width of the image to be generated.
     */
    protected int imageWidth;

    /**
     * @return Builds an instance of an algorithm.
     */
    protected abstract TOut build();

    /**
     * Sets the panel to display the progress of the algorithm on.
     *
     * @param guiPanel The panel to display the progress of the algorithm on
     * @return The instance of this builder
     */
    public TBuilder setGUIPanel(ImageCreationDisplay guiPanel) {
        this.guiPanel = guiPanel;
        return (TBuilder) this;
    }

    /**
     * Sets the height of the image to be generated.
     *
     * @param imageHeight The height of the image to be generated
     * @return The instance of this builder
     */
    public TBuilder setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
        return (TBuilder) this;
    }

    /**
     * She width of the image to be generated.
     *
     * @param imageWidth The width of the image to be generated
     * @return The instance of this builder
     */
    public TBuilder setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
        return (TBuilder) this;
    }
}
