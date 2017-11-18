package st.photonbur.misc.image.algorithm;

import st.photonbur.misc.image.display.ImageProvider;
import st.photonbur.misc.image.display.renderer.ImageRenderType;
import st.photonbur.misc.image.display.renderer.ImageRendererImpl;
import st.photonbur.misc.image.misc.BufferedImageWithProperties;
import st.photonbur.misc.image.misc.Utils;

import java.awt.image.BufferedImage;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Acts as base for any image generating algorithm.
 */
public abstract class AbstractAlgorithm extends BufferedImageWithProperties implements ImageProvider {
    /**
     * Whether or not this algorithm is done processing.
     */
    protected boolean isDone;

    /**
     * The panel to push visual updates to.
     */
    private final AbstractLauncher targetFrame;
    private ImageRendererImpl imageRenderer;

    public AbstractAlgorithm(int width, int height, int imageType, AbstractLauncher targetFrame) {
        super(width, height, imageType);

        this.isDone = false;
        this.targetFrame = targetFrame;

        this.imageRenderer = buildImageRenderer();
    }

    protected abstract ImageRendererImpl buildImageRenderer();

    /**
     * Kicks off all image generation related processes.
     */
    public void generate() {
        init();

        // Register the time on which this algorithm started running.
        LocalTime startTime = LocalTime.now();
        System.out.printf("Started at %02d:%02d:%02d.%03d\n\n",
                startTime.getHour(), startTime.getMinute(), startTime.getSecond(), startTime.getNano() / 1000000);

        // Start pushing updates to the console
        ScheduledExecutorService consolePrinter = Executors.newSingleThreadScheduledExecutor();
        consolePrinter.scheduleAtFixedRate(this::printProgressString, 50, 50, TimeUnit.MILLISECONDS);

        // If needed, start updating the GUI
        if (targetFrame != null) targetFrame.getPreviewPanel().startUpdating();

        generateImage();

        // The loop has ended, algorithm has finished
        this.isDone = true;
        // Stop pushing updates
        consolePrinter.shutdown();
        printProgressString();

        if (targetFrame != null) {
            targetFrame.repaint();
        }

        // Print the duration of the algorithm's running
        LocalTime endTime = LocalTime.now();
        System.out.printf("\n\nEnded at %02d:%02d:%02d.%03d\n\n",
                endTime.getHour(), endTime.getMinute(), endTime.getSecond(), endTime.getNano() / 1000000);

        LocalTime diff = Utils.getTimeDifference(startTime, endTime);
        System.out.printf("Generated successfully!\n  Duration: %02d:%02d:%02d.%03d\n\n",
                diff.getHour(), diff.getMinute(), diff.getSecond(), diff.getNano() / 1000000);
    }

    /**
     * Lets the algorithm go ahead and generate the image.
     */
    protected abstract void generateImage();

    public ImageRendererImpl getImageRenderer() {
        return imageRenderer;
    }

    public BufferedImage getGeneratingImage(ImageRenderType imageType) {
        return imageRenderer.getImageFor(imageType);
    }

    /**
     * Initializes the algorithm.
     */
    protected abstract void init();

    @Override
    public void onRenderSwitchEvent(int selectedIndex) {
        imageRenderer.setRenderType(selectedIndex);
    }

    private void printProgressString() {
        System.out.print(getProgressString() + "\r");
    }
}
