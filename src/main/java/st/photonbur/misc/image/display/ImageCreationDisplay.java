package st.photonbur.misc.image.display;

import st.photonbur.misc.image.algorithm.AbstractLauncher;
import st.photonbur.misc.image.display.renderer.ImageRenderType;
import st.photonbur.misc.image.misc.Utils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
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

    private double zoomFactor = 1d;
    private Point2D.Double offset = new Point2D.Double();
    private double paddingX = 0, paddingY = 0;
    private double cellSize;

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
        provider.onRenderSwitchEvent(this.getSelectedIndex(), this::repaint);
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
            new MouseInputHandler(this);
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

                // Calculate position and scaling on screen
                if (getWidth() / ((double) image.getWidth()) > getHeight() / ((double) image.getHeight())) {
                    cellSize = getHeight() / ((double) image.getHeight());
                    paddingX = getWidth() / 2d - image.getWidth() * cellSize / 2d;
                    paddingY = 0;
                } else {
                    cellSize = getWidth() / ((double) image.getWidth());
                    paddingX = 0;
                    paddingY = getHeight() / 2d - image.getHeight() * cellSize / 2d;
                }

                // Apply the translation and scaling
                g2d.translate(paddingX, paddingY);
                g2d.translate(offset.getX() * zoomFactor, offset.getY() * zoomFactor);
                g2d.scale(cellSize, cellSize);
                g2d.scale(zoomFactor, zoomFactor);

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

        private class MouseInputHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
            private static final double ZOOMSPEED = 1.25;

            private Point dragFrom;

            MouseInputHandler(ImageCreationPanel panel) {
                panel.addMouseListener(this);
                panel.addMouseMotionListener(this);
                panel.addMouseWheelListener(this);
            }

            @Override
            public void mouseClicked(MouseEvent e) { }

            @Override
            public void mouseDragged(MouseEvent e) {
                double xMaxLeft = -paddingX / zoomFactor;
                double xMaxRight = (getWidth() - provider.getGeneratingImage(imageType).getWidth() * zoomFactor * cellSize - paddingX) / zoomFactor;

                double yMaxLeft = -paddingY / zoomFactor;
                double yMaxRight = (getHeight() - provider.getGeneratingImage(imageType).getHeight() * zoomFactor * cellSize - paddingY) / zoomFactor;

                offset.x = Utils.limit(offset.x + (e == null ? 0 : (e.getX() - dragFrom.getX())) / zoomFactor, xMaxRight, xMaxLeft);
                offset.y = Utils.limit(offset.y + (e == null ? 0 : (e.getY() - dragFrom.getY())) / zoomFactor, yMaxRight, yMaxLeft);

                if (e != null) dragFrom = e.getPoint();
            }

            @Override
            public void mouseEntered(MouseEvent e) { }

            @Override
            public void mouseExited(MouseEvent e) { }

            @Override
            public void mouseMoved(MouseEvent e) { }

            @Override
            public void mousePressed(MouseEvent e) {
                if (dragFrom == null) dragFrom = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragFrom = null;
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() > 0) zoomFactor /= ZOOMSPEED;
                else zoomFactor *= ZOOMSPEED;


                repaint();
            }
        }
    }
}
