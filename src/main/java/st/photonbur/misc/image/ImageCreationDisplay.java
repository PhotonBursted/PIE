package st.photonbur.misc.image;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ImageCreationDisplay extends JPanel {
    private final ImageCreationFrameBase frame;
    private ImageProvider provider;

    private ScheduledExecutorService updateScheduler;
    private ScheduledFuture<?> updateFuture;

    private volatile boolean isBusy = false;

    public ImageCreationDisplay(ImageCreationFrameBase frame) {
        super();
        this.frame = frame;

        updateScheduler = Executors.newSingleThreadScheduledExecutor();
        frame.add(this);
        frame.invalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (provider != null && !isBusy) {
            isBusy = true;

            Graphics2D g2d = (Graphics2D) g;
            BufferedImage image = provider.getGeneratingImage();

            double paddingX = 0, paddingY = 0, cellSize;

            if (getWidth() / ((double) image.getWidth()) > getHeight() / ((double) image.getHeight())) {
                cellSize = getHeight() / ((double) image.getHeight());
                paddingX = getWidth() / 2d - image.getWidth() * cellSize / 2d;
            } else {
                cellSize = getWidth() / ((double) image.getWidth());
                paddingY = getHeight() / 2d - image.getHeight() * cellSize / 2d;
            }

            g2d.translate(paddingX, paddingY);
            g2d.scale(cellSize, cellSize);

            g2d.drawImage(image, 0, 0, Color.GRAY, null);

            g2d.setColor(Color.BLACK);
            g2d.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);

            frame.setTitle(frame.getDefaultPreviewTitle() + " - " + provider.getProgressString());

            isBusy = false;
        }
    }

    public void setProvider(ImageProvider provider) {
        this.provider = provider;
    }

    public void startUpdating() {
        updateFuture = updateScheduler.scheduleAtFixedRate(this::repaint, 100, 100, TimeUnit.MILLISECONDS);
    }

    public void stopUpdating() {
        updateFuture.cancel(true);
    }
}
