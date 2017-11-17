package st.photonbur.misc.image.algorithm.flow;

import st.photonbur.misc.image.algorithm.AbstractBuilder;

/**
 * Builds the FlowImage using the parameters passed through this class.
 */
@SuppressWarnings("UnusedReturnValue")
class FlowImageBuilder extends AbstractBuilder<FlowImageBuilder, FlowImage> {
    /**
     * The amount of points to start generating with.
     */
    private int nPoints;
    /**
     * The amount of randomness to apply to each generated pixel.
     */
    private double randomness;

    /**
     * @return A properly constructed {@link FlowImage} instance
     */
    public FlowImage build() {
        return new FlowImage(imageWidth, imageHeight, nPoints, randomness, guiFrame);
    }

    /**
     * Sets the amount of points to start generating with.
     *
     * @param amountOfPoints The amount of points to start generating with
     * @return The instance of this builder
     */
    FlowImageBuilder setAmountOfPoints(int amountOfPoints) {
        this.nPoints = amountOfPoints;
        return this;
    }

    /**
     * Sets the amount of randomness to apply to each generated pixel.
     *
     * @param randomness The amount of randomness to apply to each generated pixel
     * @return The instance of this builder
     */
    FlowImageBuilder setRandomness(double randomness) {
        this.randomness = randomness;
        return this;
    }
}
