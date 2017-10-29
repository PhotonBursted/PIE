package st.photonbur.misc.image.flow;

import st.photonbur.misc.image.AbstractLauncher;
import st.photonbur.misc.image.ImageCreationDisplay;
import st.photonbur.misc.image.misc.Utils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class FlowLauncher extends AbstractLauncher<FlowInputHandler> {
    /**
     * The default title to give to the preview window.
     */
    private static final String DEFAULT_PREVIEW_TITLE = "FLOW Preview Window";

    public FlowLauncher(FlowInputHandler params) {
        super(params);
    }

    @Override
    protected void exportImage() throws IOException {
        // Construct the image
        FlowImageBuilder flowImageBuilder = new FlowImageBuilder()
                .setImageWidth(getParams().getWidth())
                .setImageHeight(getParams().getHeight())
                .setAmountOfPoints(getParams().getNPoints())
                .setRandomness(getParams().getRandomness());
        // Add the GUI to the image builder so it can be updated
        if (getParams().doShowGUI()) flowImageBuilder.setGUIPanel(new ImageCreationDisplay(this));
        // Construct the image
        FlowImage flowImage = flowImageBuilder.build();

        // Create the filename to store the image under
        String fileName = String.format("out/flow/%05d.png", Utils.findLastIndexInDirectory("out/flow/", "png") + 1);

        // Write the image to file
        System.out.println("Exporting to " + fileName);
        ImageIO.write(flowImage, "png", new File(fileName));
    }

    @Override
    public String getDefaultPreviewTitle() {
        return DEFAULT_PREVIEW_TITLE;
    }

    @Override
    public void setup() {
        // Generate all parameters needed to run the algorithm
        getParams().generate();

        // Start exporting the image on another thread
        new Thread(() -> {
            try {
                exportImage();
            } catch (IOException ex) {
                System.out.println("Error while exporting flow image");
                ex.printStackTrace();
            }
        }).start();

        // Set up the GUI if requested by the user
        if (getParams().doShowGUI()) setupGUI();
    }
}
