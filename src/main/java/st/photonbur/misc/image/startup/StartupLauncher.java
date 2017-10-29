package st.photonbur.misc.image.startup;

import st.photonbur.misc.image.AbstractInputHandler;
import st.photonbur.misc.image.AbstractLauncher;
import st.photonbur.misc.image.flow.FlowInputHandler;
import st.photonbur.misc.image.flow.FlowLauncher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * A launcher which effectively does nothing. There to act as an implementation of the {@link AbstractLauncher} which, itself, is abstract.
 * In this way it can still be invoked and run.
 */
public class StartupLauncher extends AbstractLauncher<StartupInputHandler> {
    /**
     * The map containing all algorithms usable for image generation.
     * Linked by means of an accessor of type {@link String}, used for accessing it by prompt, to the actual {@link StartupTypeDefinition} of the algorithm's launcher.
     */
    static final HashMap<String, StartupTypeDefinition> types = new HashMap<>();

    static {
        types.put("flow", new StartupTypeDefinition<>(FlowLauncher.class, new FlowInputHandler()));
    }

    public StartupLauncher(StartupInputHandler params, String[] args) {
        super(params);
        StartupTypeDefinition targetDefinition;

        // Get the arguments passed when running the program.
        // Allows for shortcut running a certain type of image generation
        if (args.length == 0) {
            // No arguments were specified; request the type through the command prompt.
            params.generate();
            targetDefinition = params.getStartupDefinition();
        } else {
            // Try finding the type through the provided argument.
            // If this fails, exit the program.
            targetDefinition = findStartupDefinition(args[0]);
            if (targetDefinition == null) {
                System.out.println("  [ERROR] - Invalid type as argument!\n" + args[0]);
                System.exit(1);
            }
        }

        try {
            // Attempt creating an instance of this class
            //noinspection unchecked
            ((AbstractLauncher<AbstractInputHandler>) targetDefinition.cls
                    .getConstructor(targetDefinition.inputHandler.getClass())
                    .newInstance(targetDefinition.inputHandler))
                    .setup();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void exportImage() throws IOException { }

    /**
     * Tries to find the type of algorithm to generate the image with.
     *
     * @param searchTerm The string to be matched against all available types
     * @return The class of the algorithm's launcher if matched, {@code null} otherwise
     */
    static StartupTypeDefinition findStartupDefinition(String searchTerm) {
        try {
            //noinspection ConstantConditions, unchecked
            return types.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(searchTerm))
                    .findAny().get().getValue();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    @Override
    public String getDefaultPreviewTitle() {
        return null;
    }

    @Override
    public void setup() { }

    /**
     * Matches an algorithm class to an input handler (which is required for constructing algorithm classes).
     *
     * @param <T> The type of the input handler the algorithm uses
     */
    static class StartupTypeDefinition<T extends AbstractInputHandler> {
        /**
         * The class of the described algorithm.
         */
        private final Class<? extends AbstractLauncher<?>> cls;
        /**
         * The paired algorithm handler.
         */
        private final T inputHandler;

        StartupTypeDefinition(Class<? extends AbstractLauncher<T>> cls, T inputHandler) {
            this.cls = cls;
            this.inputHandler = inputHandler;
        }
    }
}
