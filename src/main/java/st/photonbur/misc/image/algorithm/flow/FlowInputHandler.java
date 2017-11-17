package st.photonbur.misc.image.algorithm.flow;

import st.photonbur.misc.image.algorithm.AbstractInputHandler;
import st.photonbur.misc.image.misc.InputStep;

public class FlowInputHandler extends AbstractInputHandler {
    // == STEP INIT == //
    /**
     * The {@link InputStep} requesting the amount of points to start the generation with.
     */
    private final InputStep<Integer, Integer> nPoints = new InputStep<>(
            "Amount of starting points",
            input -> input > 0 && input <= getWidth() * getHeight(),
            Integer::parseInt,
            Integer::parseInt
    );

    /**
     * The {@link InputStep} requesting the randomness every pixel should apply.
     */
    private final InputStep<Double, Double> randomness = new InputStep<>(
            "Randomness per pixel (value between 0 and 30)",
            input -> input > 0 && input <= 30,
            Double::parseDouble,
            Double::parseDouble
    );

    /**
     * A final copy of the input steps the user is faced with.
     * This is done so the array won't be created every time it has to be retrieved by the getter.
     */
    private final InputStep[] steps = new InputStep[] {
            imageWidth, imageHeight, nPoints, randomness, showGUI
    };

    @Override
    protected InputStep[] getSteps() {
        return steps;
    }

    // == PARAM GETTERS == //

    /**
     * @return The amount of randomness to apply to the pixel generation
     */
    Double getRandomness() {
        return randomness.getResult();
    }

    /**
     * @return The height of the image to generate
     */
    Integer getHeight() {
        return imageHeight.getResult();
    }

    /**
     * @return The amount of points to start the generation with
     */
    Integer getNPoints() {
        return nPoints.getResult();
    }

    /**
     * @return The width of the image to generate
     */
    Integer getWidth() {
        return imageWidth.getResult();
    }

    /**
     * @return Whether or not to show the GUI while generating the image
     */
    Boolean doShowGUI() {
        return showGUI.getResult();
    }
}
