package st.photonbur.misc.image;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Launches a specific image creation algorithm and helps guiding the user in controlling such algorithm's parameters.
 *
 * @param <T> The class of the user input handler to use
 */
public abstract class AbstractLauncher<T extends AbstractInputHandler> extends JFrame {
    /**
     * The parameters used for this algorithm.
     */
    private T params;

    /**
     * Exports the image produced by the algorithm.
     *
     * @throws IOException When the exporting ofthe image fails in any way
     */
    protected abstract void exportImage() throws IOException;

    /**
     * The default title to give to the preview window when displaying progress.
     *
     * @return The string to base the preview window title on
     */
    public abstract String getDefaultPreviewTitle();

    public AbstractLauncher(T params) {
        this.params = params;
    }

    /**
     * @return The parameters used for this algorithm
     */
    protected T getParams() {
        return params;
    }

    /**
     * Sets up the algorithm.
     */
    public abstract void setup();

    /**
     * Sets up a standard GUI. This will have to be filled by the algorithm.
     */
    protected void setupGUI() {
        setTitle(getDefaultPreviewTitle());
        setBackground(Color.LIGHT_GRAY);
        setSize(600, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
