package st.photonbur.misc.image.algorithm.flow;

import st.photonbur.misc.image.algorithm.AbstractAlgorithm;
import st.photonbur.misc.image.algorithm.AbstractLauncher;
import st.photonbur.misc.image.display.renderer.ImageRenderType;
import st.photonbur.misc.image.display.renderer.ImageRendererBuilder;
import st.photonbur.misc.image.display.renderer.ImageRendererImpl;
import st.photonbur.misc.image.misc.BufferedImageWithProperties;
import st.photonbur.misc.image.misc.DoubleColor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates an image according to the FLOW algorithm.
 * See <a href="https://github.com/PhotonBursted/PIE/wiki/Experiment:-FLOW">the FLOW wiki page</a> for more information on the workings of it.
 */
class FlowImage extends AbstractAlgorithm {
    /**
     * Represents a node on the canvas.
     */
    class Node {
        /**
         * The color this node has.
         */
        final DoubleColor color;
        /**
         * The location this node has on the canvas.
         */
        final Point location;

        Node(Point location) {
            this.location = location;

            // Try calculating the color if enough neighbors are present.
            if (visitedNodes.getNeighborsOf(this).size() == 0) {
                this.color = new DoubleColor(r.nextInt(256), r.nextInt(256), r.nextInt(256));
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
         * Checks to see if the node with passed coordinate exists within this set.
         *
         * @param x The x-coordinate of the node to look for
         * @param y The y-coordinate of the node to look for
         * @return {@code true} if the node exists within this set, {@code false} otherwise
         */
        boolean contains(int x, int y) {
            return nodeMatrix[x][y] != null;
        }

        /**
         * Returns the average color around a given node.
         *
         * @param node The node of which the neighbors will be used to calculate their average color
         * @return The average color +/- some deviation
         */
        private DoubleColor getAverageColorFor(Node node) {
            // Determine what nodes are this node's neighbors
            Set<Node> neighbors = getNeighborsOf(node);

            // Return the average color
            return new DoubleColor(
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

            if (hasNeighborAt(node, -1,  0)) result.add(nodeMatrix[node.location.x - 1][node.location.y]);
            if (hasNeighborAt(node,  1,  0)) result.add(nodeMatrix[node.location.x + 1][node.location.y]);
            if (hasNeighborAt(node,  0, -1)) result.add(nodeMatrix[node.location.x][node.location.y - 1]);
            if (hasNeighborAt(node,  0,  1)) result.add(nodeMatrix[node.location.x][node.location.y + 1]);

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
            return node.location.x + dx >= 0 && node.location.x + dx < getWidth() &&
                   node.location.y + dy >= 0 && node.location.y + dy < getHeight() &&
                   contains(node.location.x + dx, node.location.y + dy);
        }

        /**
         * Mixes a color of a certain channel.
         *
         * @param neighbors The neighbors to get the average color of
         * @param channel   The color channel to base the calculation on
         * @return The new value of the passed color channel
         */
        private double mixColorChannel(Set<Node> neighbors, Color channel) {
            return Math.min(Math.max(
                    neighbors.stream().mapToDouble(nn -> {
                        if (channel == Color.RED) {
                            return nn.color.getRed();
                        } else if (channel == Color.GREEN) {
                            return nn.color.getGreen();
                        } else if (channel == Color.BLUE) {
                            return nn.color.getBlue();
                        } else {
                            return 0f;
                        }
                    }).average().orElse(0) + r.nextDouble() * 2 * randomness - randomness
                    , 0d), 255d);
        }

        /**
         * Stores a node in this matrix if the space wasn't occupied yet.
         *
         * @param node The node to store
         */
        void store(Node node) {
            if (!contains(node.location.x, node.location.y)) {
                usedVolume++;
                nodeMatrix[node.location.x][node.location.y] = node;
            }
        }
    }

    /**
     * Represents a list of nodes. Contains a few utility methods for easier and more efficient handling of Node objects.
     */
    class NodeSet extends ConcurrentHashMap<Node, Byte> {
        private NodeRegistry registry;

        NodeSet() {
            super(nPoints * (2 * getWidth() + 2 * getHeight()));
            registry = new NodeRegistry();
        }

        /**
         * Adds a new node to this list.
         *
         * @param x The location of the node on the x-axis
         * @param y The location of the node on the y-axis
         */
        void add(int x, int y) {
            putIfAbsent(new Node(new Point(x, y)), (byte) 0);
            getImageRenderer().render(x, y);
        }

        /**
         * Checks to see if the node with passed coordinate exists within this set.
         *
         * @param x The x-coordinate of the node to look for
         * @param y The y-coordinate of the node to look for
         * @return {@code true} if the node exists within this set, {@code false} otherwise
         */
        boolean contains(int x, int y) {
            return registry.contains(x, y);
        }

        /**
         * @return A random node from this set.
         */
        Node getRandomNode() {
            return keySet().stream()
                    // Skip a random amount of nodes
                    .skip(r.nextInt(size()))
                    // Find any element within this stream, or return null otherwise
                    .findFirst().orElse(null);
        }

        @Override
        public Byte putIfAbsent(Node key, Byte value) {
            Byte result = super.putIfAbsent(key, value);
            registry.store(key.location.x, key.location.y);

            return result;
        }

        /**
         * Removes a node from this set.
         *
         * @param key The node to remove
         */
        void remove(Node key) {
            remove(((Object) key));
            registry.remove(key.location.x, key.location.y);
        }

        /**
         * Acts as a registry for the {@link NodeSet} class.
         * Keeps track of what locations are stored in it and provides much faster lookup.
         */
        private class NodeRegistry {
            /**
             * The full size of the image is divided into buckets.
             * These buckets each store 64 location states; each present location is marked by a 1, or with a 0 otherwise.
             */
            long[] buckets;

            NodeRegistry() {
                buckets = new long[(int) Math.ceil(getWidth() * getHeight() / 64)];
            }

            /**
             * Checks whether the location is contained within the registry.
             *
             * @param x The x coordinate of the location to check its presence of
             * @param y The y coordinate of the location to check its presence of
             * @return {@code true} if the location is contained within this registry, {@code false} otherwise
             */
            boolean contains(int x, int y) {
                // Calculate the "ID" of the location (transformation from 2D point to 1D number line)
                int targetID = x * getHeight() + y;
                // Determine in which bucket the state of the location is stored
                // This is done by bit-shifting the ID 6 places to the right (dividing by 64 (2^6))
                int targetBucket = targetID >> 6;
                // Determine the position within the bucket
                byte targetPos = (byte) (targetID % 64);

                // Retrieve the state of the location from the bucket.
                // This is done by bit-shifting to the right again, this time with the position to get.
                // Then, it is masked so that only one bit is returned.
                return ((buckets[targetBucket] >> targetPos) & 0b1) == 1;
            }

            /**
             * Puts a certain value into the registry.
             *
             * @param x The x coordinate to store the new state of
             * @param y The y coordinate to store the new state of
             * @param doStore Whether the location is being stored or removed
             */
            private void put(int x, int y, boolean doStore) {
                // Calculate the "ID" of the location (transformation from 2D point to 1D number line)
                int targetID = x * getHeight() + y;
                // Determine in which bucket the state of the location is stored
                // This is done by bit-shifting the ID 6 places to the right (dividing by 64 (2^6))
                int targetBucket = targetID >> 6;
                // Determine the position within the bucket
                byte targetPos = (byte) (targetID % 64);

                // It depends on whether the location state is being stored or removed which operation has to be executed.
                if (doStore) {
                    // If the location is being stored, a simple bit-wise OR operation is enough.
                    // The bucket is updated by means of bit-shifting a 1 to the left to the spot to store it in.
                    buckets[targetBucket] |= 1L << targetPos;
                } else {
                    // If the location is being removed, an inverted bit-wise AND operation is sufficient.
                    // The bucket is updated by bit-shifting a 1 to the left until the spot to clear is reached.
                    // Then it is inverted, again bit-wise, so that the bit-shifted 1 turns into a 0, masking the bit to clear.
                    buckets[targetBucket] &= ~(1L << targetPos);
                }
            }

            /**
             * Marks a location as removed within this registry.
             *
             * @param x The x coordinate of the location to mark as removed
             * @param y The y coordinate of the location to mark as removed
             */
            void remove(int x, int y) {
                put(x, y, false);
            }

            /**
             * Marks a location as stored within this registry.
             * @param x The x coordinate of the location to mark as store
             * @param y The y coordinate of the location to mark as stored
             */
            void store(int x, int y) {
                put(x, y, true);
            }
        }
    }

    /**
     * The randomizer instance used by this class.
     */
    private static final Random r = new Random();

    /**
     * The randomness to apply to gen erating colors.
     */
    private final double randomness;
    /**
     * The amount of points to start generating with.
     */
    private final int nPoints;

    /**
     * The list of nodes that haven't yet been processed, but are scheduled to do so.
     */
    private NodeSet activeNodes = new NodeSet();
    /**
     * The matrix of nodes that hodls information on all already processed nodes.
     */
    private NodeMatrix visitedNodes = new NodeMatrix();

    FlowImage(int width, int height, int nPoints, double deviation, AbstractLauncher targetFrame) {
        // Create the acutal image object using ARGB (to allow for transparency in the preview)
        super(width, height, BufferedImage.TYPE_INT_ARGB, targetFrame);
        this.randomness = deviation;
        this.nPoints = nPoints;

        if (targetFrame != null) targetFrame.getPreviewPanel().setProvider(this);

        System.out.printf("\nCreating FLOW image with parameters:\n - Image dimensions: %dx%d\n - Starting nodes: %d\n - Deviation: max. %s per pixel step\n\n",
                width, height, nPoints, new DecimalFormat("0.00").format(deviation).replace(",", "."));
    }

    @Override
    protected ImageRendererImpl buildImageRenderer() {
        return new ImageRendererBuilder()
                .addRenderer(ImageRenderType.NORMAL, this,
                        (x, y) -> visitedNodes.contains(x, y)
                                ? visitedNodes.nodeMatrix[x][y].color.toNormalColor()
                                : null)
                .addRenderer(ImageRenderType.TYPE, new BufferedImageWithProperties(getWidth(), getHeight(), getType()),
                        (x, y) -> {
                            if (activeNodes.contains(x, y)) return Color.RED;
                            if (visitedNodes.contains(x, y)) return Color.BLUE;
                            return null;
                        })
                .build();
    }

    @Override
    protected void generateImage() {
        // Loop as long as the list of active nodes contains elements
        while (!activeNodes.isEmpty()) {
            // Get a random node from the set
            Node target = activeNodes.getRandomNode();

            if (target != null) {
                // Mark the node as visited
                visitedNodes.store(target);
                activeNodes.remove(target);

                getImageRenderer().render(target.location.x, target.location.y);

                // Try to mark unvisited neighbors as active
                if (target.location.x - 1 >= 0 && !visitedNodes.hasNeighborAt(target, -1, 0))
                    activeNodes.add(target.location.x - 1, target.location.y);

                if (target.location.x + 1 < getWidth() && !visitedNodes.hasNeighborAt(target, 1, 0))
                    activeNodes.add(target.location.x + 1, target.location.y);

                if (target.location.y - 1 >= 0 && !visitedNodes.hasNeighborAt(target, 0, -1))
                    activeNodes.add(target.location.x, target.location.y - 1);

                if (target.location.y + 1 < getHeight() && !visitedNodes.hasNeighborAt(target, 0, 1))
                    activeNodes.add(target.location.x, target.location.y + 1);
            }
        }
    }

    @Override
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
