package st.photonbur.misc.image.flow;

import st.photonbur.misc.image.AbstractAlgorithm;
import st.photonbur.misc.image.ImageCreationDisplay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Generates an image according to the FLOW algorithm.
 * See <a href="https://github.com/PhotonBursted/PIE/wiki/experiment-flow">the FLOW wiki page</a> for more information on the workings of it.
 */
class FlowImage extends AbstractAlgorithm {
    /**
     * Represents a node on the canvas.
     */
    class Node {
        /**
         * The color this node has.
         */
        private final Color color;
        /**
         * The location this node has on the canvas.
         */
        private final Point location;

        Node(Point location) {
            this.location = location;

            // Try calculating the color if enough neighbors are present.
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

    /**
     * Represents a matrix that spans the entire image and is filled with nodes.
     * Also includes utility methods for interfacing with it.
     */
    class NodeMatrix {
        /**
         * The actual matrix, filled with nodes.
         */
        private final Node[][] nodeMatrix;
        /**
         * The amount of nodes included in the matrix.
         */
        private long usedVolume;

        NodeMatrix() {
            nodeMatrix = new Node[getWidth()][getHeight()];
            usedVolume = 0;
        }

        /**
         * Returns the average color around a given node.
         *
         * @param node The node of which the neighbors will be used to calculate their average color
         * @return The average color +/- some deviation
         */
        private Color getAverageColorFor(Node node) {
            // Determine what nodes are this node's neighbors
            Set<Node> neighbors = getNeighborsOf(node);

            // Return the average color
            return new Color(
                    mixColorChannel(neighbors, Color.RED),
                    mixColorChannel(neighbors, Color.GREEN),
                    mixColorChannel(neighbors, Color.BLUE)
            );
        }

        /**
         * @param node The node of which to check what neighbors are present within this {@link NodeMatrix}.
         * @return The set of nodes which are neighboring the given ndoe.
         */
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

        /**
         * @return The amount of nodes within this matrix
         */
        private long getUsedVolume() {
            return usedVolume;
        }

        /**
         * Used to determine whether or not the given node has a neighbor at a certain relative location.
         *
         * @param node The node to reference from
         * @param dx   The relative location horizontally
         * @param dy   The relative location vertically
         * @return     {@code true} when a neighbor was detected within this matrix, {@code false} otherwise
         */
        boolean hasNeighborAt(Node node, int dx, int dy) {
            return node.location.x + dx >= 0 && node.location.x + dy < getWidth() &&
                   node.location.y + dy >= 0 && node.location.y + dy < getHeight() &&
                   nodeMatrix[node.location.x + dx][node.location.y + dy] != null;

        }

        /**
         * Mixes a color of a certain channel.
         *
         * @param neighbors The neighbors to get the average color of
         * @param channel   The color channel to base the calculation on
         * @return The new value of the passed color channel
         */
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

        /**
         * Stores a node in this matrix if the space wasn't occupied yet.
         *
         * @param node The node to store
         */
        void store(Node node) {
            if (nodeMatrix[node.location.x][node.location.y] == null) {
                usedVolume++;
                nodeMatrix[node.location.x][node.location.y] = node;
            }
        }
    }

    /**
     * Represents a list of nodes. Contains a few utility methods for easier and more efficient handling of Node objects.
     */
    class NodeList extends ArrayList<Node> {
        /**
         * Adds a new node to this list.
         *
         * @param x The location of the node on the x-axis
         * @param y The location of the node on the y-axis
         * @return {@code true} when the addition was successful, {@code false} otherwise
         */
        @SuppressWarnings("UnusedReturnValue")
        boolean add(int x, int y) {
            return add(new Node(new Point(x, y)));
        }

        /**
         * Checks whether a node is contained within this list.
         *
         * @param x The location on the x-axis of the node to check the presence of
         * @param y The location on the y-axis of the node to check the presence of
         * @return {@code true} when the node with the given coordinates is contained within this list, {@code false} otherwise
         */
        private boolean contains(int x, int y) {
            return stream().anyMatch(node -> node.location.x == x && node.location.y == y);
        }

        /**
         * Checks whether the list contains a node offset by relative coordinates.
         *
         * @param target The node to use as base
         * @param dx     The amount of offset horizontally
         * @param dy     The amount of offset vertically
         * @return {@code true} when the list contains a node at the relative position, {@code false} otherwise
         */
        boolean hasNeighborAt(Node target, int dx, int dy) {
            return contains(target.location.x + dx, target.location.y + dy);
        }
    }

    /**
     * The randomizer instance used by this class.
     */
    private static final Random r = new Random();

    /**
     * The randomness to apply to generating colors.
     */
    private final double randomness;
    /**
     * The amount of points to start generating with.
     */
    private final int nPoints;


    /**
     * The list of nodes that haven't yet been processed, but are scheduled to do so.
     */
    private NodeList activeNodes = new NodeList();
    /**
     * The matrix of nodes that hodls information on all already processed nodes.
     */
    private NodeMatrix visitedNodes = new NodeMatrix();

    FlowImage(int width, int height, int nPoints, double deviation, ImageCreationDisplay guiPanel) {
        // Create the acutal image object using ARGB (to allow for transparency in the preview)
        super(width, height, BufferedImage.TYPE_INT_ARGB, guiPanel);
        this.randomness = deviation;
        this.nPoints = nPoints;

        if (guiPanel != null) guiPanel.setProvider(this);

        System.out.printf("\nCreating FLOW image with parameters:\n - Image dimensions: %dx%d\n - Starting nodes: %d\n - Deviation: max. %s per pixel step\n\n",
                width, height, nPoints, new DecimalFormat("0.00").format(deviation).replace(",", "."));
    }

    @Override
    protected void generateImage() {
        // Loop as long as the list of active nodes contains elements
        while (!activeNodes.isEmpty()) {
            // Randomize the list
            Collections.shuffle(activeNodes, r);
            Node target = activeNodes.get(0);

            if (target != null) {
                // Imprint the node's color onto the image object
                setRGB(target.location.x, target.location.y, target.color.getRGB());

                // Mark the node as visited
                visitedNodes.store(target);
                activeNodes.remove(target);

                // Try to mark unvisited neighbors as active
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

    @Override
    protected void init() {
        for (int i = 0; i < nPoints; i++)
            activeNodes.add(r.nextInt(getWidth()), r.nextInt(getHeight()));
    }
}
