package st.photonbur.misc.image.flow;

import st.photonbur.misc.image.ImageCreationDisplay;
import st.photonbur.misc.image.ImageProvider;
import st.photonbur.misc.image.misc.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class FlowImage extends BufferedImage implements ImageProvider {
    class Node {
        private final Color color;
        private final Point location;

        Node(Point location) {
            this.location = location;

            if (visitedNodes.getNeighborsOf(this).size() == 0) {
                this.color = new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
            } else {
                this.color = visitedNodes.getAverageColorFor(this);
            }
        }

        @Override
        public boolean equals(Object that) {
            if (this == that) return true;
            if (that == null || getClass() != that.getClass()) return false;

            Node node = (Node) that;

            return location.equals(node.location);
        }

        @Override
        public int hashCode() {
            return location.hashCode();
        }

        @Override
        public String toString() {
            return "N:<" + location.x + "," + location.y + "," + color + ">";
        }
    }

    class NodeMatrix {
        private final Node[][] nodeMatrix;
        private long usedVolume;

        NodeMatrix() {
            nodeMatrix = new Node[getWidth()][getHeight()];
            usedVolume = 0;
        }

        private Color getAverageColorFor(Node node) {
            Set<Node> neighbors = getNeighborsOf(node);

            return new Color(
                    mixColorChannel(neighbors, Color.RED),
                    mixColorChannel(neighbors, Color.GREEN),
                    mixColorChannel(neighbors, Color.BLUE)
            );
        }

        private HashSet<Node> getNeighborsOf(Node node) {
            HashSet<Node> result = new HashSet<>();

            if (node.location.x - 1 >= 0 && nodeMatrix[node.location.x - 1][node.location.y] != null)
                result.add(nodeMatrix[node.location.x - 1][node.location.y]);
            if (node.location.x + 1 < getWidth() && nodeMatrix[node.location.x + 1][node.location.y] != null)
                result.add(nodeMatrix[node.location.x + 1][node.location.y]);
            if (node.location.y - 1 >= 0 && nodeMatrix[node.location.x][node.location.y - 1] != null)
                result.add(nodeMatrix[node.location.x][node.location.y - 1]);
            if (node.location.y + 1 < getHeight() && nodeMatrix[node.location.x][node.location.y + 1] != null)
                result.add(nodeMatrix[node.location.x][node.location.y + 1]);

            return result;
        }

        private long getUsedVolume() {
            return usedVolume;
        }

        boolean hasNeighborAt(Node node, int dx, int dy) {
            return node.location.x + dx >= 0 && node.location.x + dy < getWidth() &&
                   node.location.y + dy >= 0 && node.location.y + dy < getHeight() &&
                   nodeMatrix[node.location.x + dx][node.location.y + dy] != null;

        }

        private int mixColorChannel(Set<Node> neighbors, Color channel) {
            return (int) Math.round(Math.min(Math.max(
                    neighbors.stream().mapToInt(nn -> {
                        if (channel == Color.RED) {
                            return nn.color.getRed();
                        } else if (channel == Color.GREEN) {
                            return nn.color.getGreen();
                        } else if (channel == Color.BLUE) {
                            return nn.color.getBlue();
                        } else {
                            return 0;
                        }
                    }).average().orElse(0) + r.nextDouble() * 2 * randomness - randomness
                    , 0), 255));
        }

        void store(Node node) {
            if (nodeMatrix[node.location.x][node.location.y] == null) {
                usedVolume++;
                nodeMatrix[node.location.x][node.location.y] = node;
            }
        }
    }

    class NodeList extends ArrayList<Node> {
        @SuppressWarnings("UnusedReturnValue")
        boolean add(int x, int y) {
            return add(new Node(new Point(x, y)));
        }

        private boolean contains(int x, int y) {
            return stream().anyMatch(node -> node.location.x == x && node.location.y == y);
        }

        boolean hasNeighborAt(Node target, int dx, int dy) {
            return contains(target.location.x + dx, target.location.y + dy);
        }
    }

    private static final Random r = new Random();
    private final double randomness;
    private final int nPoints;
    private boolean isDone;

    private final ImageCreationDisplay guiPanel;

    private NodeList activeNodes = new NodeList();
    private NodeMatrix visitedNodes = new NodeMatrix();

    FlowImage(int width, int height, int nPoints, double deviation, ImageCreationDisplay guiPanel) {
        super(width, height, BufferedImage.TYPE_INT_ARGB);
        this.randomness = deviation;
        this.nPoints = nPoints;
        this.isDone = false;

        this.guiPanel = guiPanel;

        if (guiPanel != null) guiPanel.setProvider(this);

        System.out.printf("\nCreating FLOW image with parameters:\n - Image dimensions: %dx%d\n - Starting nodes: %d\n - Deviation: max. %s per pixel step\n\n",
                width, height, nPoints, new DecimalFormat("0.00").format(deviation).replace(",", "."));

        generatePoints();
        flowOutColors();
    }

    private void flowOutColors() {
        LocalTime startTime = LocalTime.now();
        System.out.printf("Started at %02d:%02d:%02d.%03d\n\n",
                startTime.getHour(), startTime.getMinute(), startTime.getSecond(), startTime.getNano() / 1000000);

        ScheduledExecutorService consolePrinter = Executors.newSingleThreadScheduledExecutor();
        consolePrinter.scheduleAtFixedRate(this::printProgressString, 50, 50, TimeUnit.MILLISECONDS);

        if (guiPanel != null) guiPanel.startUpdating();

        while (!activeNodes.isEmpty()) {
            Collections.shuffle(activeNodes, r);
            Node target = activeNodes.get(0);

            if (target != null) {
                setRGB(target.location.x, target.location.y, target.color.getRGB());

                visitedNodes.store(target);
                activeNodes.remove(target);

                if (target.location.x - 1 >= 0 &&
                    !visitedNodes.hasNeighborAt(target, -1, 0) &&
                    !activeNodes.hasNeighborAt(target, -1, 0))
                    activeNodes.add(target.location.x - 1, target.location.y);

                if (target.location.x + 1 < getWidth() &&
                    !visitedNodes.hasNeighborAt(target, 1, 0) &&
                    !activeNodes.hasNeighborAt(target, 1, 0))
                    activeNodes.add(target.location.x + 1, target.location.y);

                if (target.location.y - 1 >= 0 &&
                    !visitedNodes.hasNeighborAt(target, 0, -1) &&
                    !activeNodes.hasNeighborAt(target, 0, -1))
                    activeNodes.add(target.location.x, target.location.y - 1);

                if (target.location.y + 1 < getHeight() &&
                    !visitedNodes.hasNeighborAt(target, 0, 1) &&
                    !activeNodes.hasNeighborAt(target, 0, 1))
                    activeNodes.add(target.location.x, target.location.y + 1);
            }
        }

        this.isDone = true;
        consolePrinter.shutdown();
        printProgressString();

        if (guiPanel != null) {
            guiPanel.stopUpdating();
            guiPanel.repaint();
        }

        LocalTime endTime = LocalTime.now();
        System.out.printf("\n\nEnded at %02d:%02d:%02d.%03d\n\n",
                endTime.getHour(), endTime.getMinute(), endTime.getSecond(), endTime.getNano() / 1000000);

        LocalTime diff = Utils.getTimeDifference(startTime, endTime);
        System.out.printf("Generated successfully!\n  Duration: %02d:%02d:%02d.%03d\n\n",
                diff.getHour(), diff.getMinute(), diff.getSecond(), diff.getNano() / 1000000);
    }

    private void generatePoints() {
        for (int i = 0; i < nPoints; i++)
            activeNodes.add(r.nextInt(getWidth()), r.nextInt(getHeight()));
    }

    public BufferedImage getGeneratingImage() {
        return this;
    }

    public String getProgressString() {
        return isDone ? "Done." :
                String.format(String.format("Processed %%0%1$dd / %%0%1$dd pixels... (%%s%%%%)",
                        String.valueOf(getWidth() * getHeight()).length()),
                        visitedNodes.getUsedVolume(), getWidth() * getHeight(),
                        new DecimalFormat("000.00").format(visitedNodes.getUsedVolume() / ((double) getWidth() * getHeight()) * 100).replace(",", "."));
    }

    private void printProgressString() {
        System.out.print(getProgressString() + "\r");
    }
}
