package st.photonbur.misc.image.display.renderer;

/**
 * Enum specifying the different types of rendering images.
 * These can be used by algorithms to specify what render modes they support, which can in turn be displayed in the GUI.
 */
public enum ImageRenderType {
    NORMAL("normal"), TYPE("type");

    /**
     * The display name corresponding to the enum value.
     */
    private final String displayName;

    ImageRenderType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return The display name corresponding to this enum value.
     */
    public String getDisplayName() {
        return displayName;
    }
}
