package st.photonbur.misc.image.algorithm;

import st.photonbur.misc.image.misc.InputStep;

import java.util.Scanner;

/**
 * Handles user input in such a way that algorithms can use it for their parameters.
 */
public abstract class AbstractInputHandler {
    /**
     * The {@link Scanner} instance used for listening to user input.
     */
    private static final Scanner sc = new Scanner(System.in);

    /**
     * The default {@link InputStep} for requesting image height.
     */
    protected final InputStep<Integer, Integer> imageHeight = new InputStep<>(
            "Image height",
            input -> input > 0,
            Integer::parseInt,
            Integer::parseInt
    );

    /**
     * The default {@link InputStep} for requesting image width.
     */
    protected final InputStep<Integer, Integer> imageWidth = new InputStep<>(
            "Image width",
            input -> input > 0,
            Integer::parseInt,
            Integer::parseInt
    );

    /**
     * The default {@link InputStep} for requesting if the GUI should be shown during image generation.
     */
    protected final InputStep<String, Boolean> showGUI = new InputStep<>(
            "Show progress in GUI (y/n)",
            input -> input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"),
            String::valueOf,
            input -> input.equalsIgnoreCase("y")
    );

    /**
     * Generates the parameters from input.
     */
    public void generate() {
        requestInput();
    }

    /**
     * @return The inputs the algorithm implementation requires.
     * These will be shown to the user in the exact order of the passed array
     */
    protected abstract InputStep[] getSteps();

    /**
     * Requests input from the user.
     * This input is also validated and transformed using the {@link InputStep} instances
     * returned by {@link #getSteps()}, and then stored in those same instances.
     */
    private void requestInput() {
        // Loop over all steps required by the algorithm
        for (InputStep step : getSteps()) {
            // Loop forever (this makes sure faulty inputs don't come through)
            while (true) {
                // Print what information is requested from the user
                System.out.print(step.getDesc() + "\n > ");

                // Wait until input has been received
                while (true) if (sc.hasNextLine()) break;

                // Parse the input from the user
                try {
                    String input = sc.nextLine();

                    //noinspection unchecked
                    step.setResult(step.getValidator().test(step.getValidationValueRetriever().apply(input))
                            ? step.getValueRetriever().apply(input)
                            : null
                    );
                } catch (NumberFormatException ignored) { }

                // If the parsing and validation passed, break out of the loop, otherwise show a message and retry
                if (step.getResult() != null) {
                    break;
                } else {
                    System.out.println("  [ERROR] - Invalid value entered!");
                }
            }
        }
    }
}
