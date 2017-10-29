package st.photonbur.misc.image;

import st.photonbur.misc.image.startup.StartupInputHandler;
import st.photonbur.misc.image.startup.StartupLauncher;

/**
 * Handles launching the application.
 */
public class Launcher {
    public static void main(String[] args) {
        new StartupLauncher(new StartupInputHandler(), args);
    }
}
