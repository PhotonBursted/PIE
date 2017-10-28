package st.photonbur.misc.image.flow;

import st.photonbur.misc.image.ImageCreationDisplay;

@SuppressWarnings("UnusedReturnValue")
class FlowImageBuilder {
    private int height;
    private int width;
    private int nPoints;
    private double deviation;
    private ImageCreationDisplay guiPanel;

    FlowImage build() {
        return new FlowImage(width, height, nPoints, deviation, guiPanel);
    }

    FlowImageBuilder setAmountOfPoints(int amountOfPoints) {
        this.nPoints = amountOfPoints;
        return this;
    }

    FlowImageBuilder setGUIPanel(ImageCreationDisplay guiPanel) {
        this.guiPanel = guiPanel;
        return this;
    }

    FlowImageBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    FlowImageBuilder setMaxDeviation(double maxDeviation) {
        this.deviation = maxDeviation;
        return this;
    }

    FlowImageBuilder setWidth(int width) {
        this.width = width;
        return this;
    }
}
