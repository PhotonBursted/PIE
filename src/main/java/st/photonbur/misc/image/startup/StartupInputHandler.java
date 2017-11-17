package st.photonbur.misc.image.startup;

import st.photonbur.misc.image.algorithm.AbstractInputHandler;
import st.photonbur.misc.image.misc.InputStep;

/**
 * Handles all user input for starting up the program.
 */
public class StartupInputHandler extends AbstractInputHandler {
    // == STEP INIT == //
    /**
     * The {@link InputStep} requesting the user to select an algorithm.
     */
    private InputStep<String, StartupLauncher.StartupTypeDefinition> definition = new InputStep<>(
            "Type - one of:\n - " + String.join("\n - ", StartupLauncher.types.keySet()),
            input -> StartupLauncher.findStartupDefinition(input) != null,
            input -> input,
            StartupLauncher::findStartupDefinition
    );

    @Override
    protected InputStep[] getSteps() {
        return new InputStep[] {definition};
    }

    // == PARAM GETTERS == //

    /**
     * @return The startup definition reflecting the made choice of algorithm.
     */
    StartupLauncher.StartupTypeDefinition getStartupDefinition() {
        return definition.getResult();
    }
}
