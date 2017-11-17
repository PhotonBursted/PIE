package st.photonbur.misc.image.display;

import st.photonbur.misc.image.algorithm.AbstractLauncher;
import st.photonbur.misc.image.display.renderer.ImageRenderType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Describes behaviour the panel tasked with showing previews of the generating image must have.
 */
public class ImageCreationDisplay extends JTabbedPane implements ChangeListener {
    /**
     * The frame this panel is linked to.
     */
    private final AbstractLauncher frame;
    /**
     * The provider of the preview image.
     */
    private ImageProvider provider;

    /**
     * The scheduler of screen updates.
     */
    private ScheduledExecutorService updateScheduler;
    /**
     * The actions resulting from the scheduler.
     */
    private ScheduledFuture<?> updateFuture;

    /**
     * Signifies if the class is currently busy rendering.
     */
    private volatile boolean isBusy = false;

    public ImageCreationDisplay(AbstractLauncher frame) {
        super();
        this.addChangeListener(this);
        this.frame = frame;

        updateScheduler = Executors.newSingleThreadScheduledExecutor();

        frame.add(this);
        frame.invalidate();
    }

    /**
     * Sets the provider of this display.
     *
     * @param provider The provider to provide preview imagery to this panel
     */
    public void setProvider(ImageProvider provider) {
        this.provider = provider;

        for (Component component : getComponents()) {
            remove(component);
        }

        for (ImageRenderType imageType : provider.getImageRenderer().getSupportedTypes()) {
            this.addTab(imageType.getDisplayName(), new ImageCreationPanel(imageType));
        }

        this.invalidate();
    }

    /**
     * Start sending updates to this panel.
     */
    public void startUpdating() {
        updateFuture = updateScheduler.scheduleAtFixedRate(() -> this.getSelectedComponent().repaint(), 40, 40, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        provider.onRenderSwitchEvent(this.getSelectedIndex());
    }

    /**
     * Stop updating this panel.
     */
    public void stopUpdating() {
        updateFuture.cancel(true);
    }

    private class ImageCreationPanel extends JPanel {
        private final ImageRenderType imageType;

        private ImageCreationPanel(ImageRenderType imageType) {
            this.imageType = imageType;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Try to update the screen
            if (imageType != null && !isBusy) {
                // Show being busy
                isBusy = true;

                Graphics2D g2d = (Graphics2D) g;
                BufferedImage image = provider.getGeneratingImage(imageType);

                double paddingX = 0, paddingY = 0, cellSize;

                // Calculate position and scaling on screen
                if (getWidth() / ((double) image.getWidth()) > getHeight() / ((double) image.getHeight())) {
                    cellSize = getHeight() / ((double) image.getHeight());
                    paddingX = getWidth() / 2d - image.getWidth() * cellSize / 2d;
                } else {
                    cellSize = getWidth() / ((double) image.getWidth());
                    paddingY = getHeight() / 2d - image.getHeight() * cellSize / 2d;
                }

                // Apply the translation and scaling
                g2d.translate(paddingX, paddingY);
                g2d.scale(cellSize, cellSize);

                // Draw the preview
                g2d.drawImage(image, 0, 0, Color.GRAY, null);

                // Draw a small border around the edge of the screen
                g2d.setColor(Color.BLACK);
                g2d.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);

                // Update the title of the window
                frame.setTitle(frame.getDefaultPreviewTitle() + " - " + provider.getProgressString());

                // Let know the operation has finished
                isBusy = false;
            }
        }
    }

}
