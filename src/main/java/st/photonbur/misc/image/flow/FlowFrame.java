package st.photonbur.misc.image.flow;

import st.photonbur.misc.image.ImageCreationFrameBase;
import st.photonbur.misc.image.ImageCreationDisplay;
import st.photonbur.misc.image.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FlowFrame extends ImageCreationFrameBase {
    private static final String DEFAULT_PREVIEW_TITLE = "FLOW Preview Window";
    private Boolean showGUI;
    private Double randomness = null;
    private Integer nPoints = null, width = null, height = null;

    public FlowFrame() {
        ValidationStep<Integer, Integer> vsWidth = new ValidationStep<>("Image width",
                input -> input > 0,
                Integer::parseInt, Integer::valueOf);
        ValidationStep<Integer, Integer> vsHeight = new ValidationStep<>("Image height",
                input -> input > 0,
                Integer::parseInt, Integer::valueOf);
        ValidationStep<Integer, Integer> vsNPoints = new ValidationStep<>("Amount of points",
                input -> input > 0,
                Integer::parseInt, Integer::valueOf);
        ValidationStep<Double, Double> vsRandomness = new ValidationStep<>("Randomness per pixel (between 0 and 30)",
                input -> input >= 0 && input <= 30,
                Double::parseDouble, Double::valueOf);
        ValidationStep<String, Boolean> vsShowGUI = new ValidationStep<>("Show progress in GUI (y/n)",
                input -> input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"),
                String::valueOf, input -> input.equalsIgnoreCase("y"));

        validateInput(vsWidth, vsHeight, vsNPoints, vsRandomness, vsShowGUI);

        width = vsWidth.getResult();
        height = vsHeight.getResult();
        nPoints = vsNPoints.getResult();
        randomness = vsRandomness.getResult();
        showGUI = vsShowGUI.getResult();

        new Thread(() -> {
            try {
                exportImage();
            } catch (IOException ex) {
                System.out.println("Error while exporting flow image");
                ex.printStackTrace();
            }
        }).start();

        if (showGUI) setupGUI();
    }

    @Override
    protected void exportImage() throws IOException {
        FlowImageBuilder flowImageBuilder = new FlowImageBuilder()
                .setWidth(width)
                .setHeight(height)
                .setAmountOfPoints(nPoints)
                .setMaxDeviation(randomness);

        if (showGUI) {
            flowImageBuilder.setGUIPanel(new ImageCreationDisplay(this));
        }

        FlowImage flowImage = flowImageBuilder.build();

        String fileName = String.format("out/flow/%05d.png", Utils.findLastIndexInDirectory("out/flow/", "png") + 1);

        System.out.println("Exporting to " + fileName);

        ImageIO.write(flowImage, "png", new File(fileName));
    }

    @Override
    public String getDefaultPreviewTitle() {
        return DEFAULT_PREVIEW_TITLE;
    }

    @Override
    protected void setupGUI() {
        setTitle(getDefaultPreviewTitle());
        setBackground(Color.LIGHT_GRAY);
        setSize(600, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
