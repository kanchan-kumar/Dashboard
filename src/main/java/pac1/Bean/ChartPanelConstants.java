package pac1.Bean;

public interface ChartPanelConstants {

    /** Default setting for buffer usage. */
    public static final boolean DEFAULT_BUFFER_USED = true;

    /** The default panel width. */
    public static final int DEFAULT_WIDTH = 680;

    /** The default panel height. */
    public static final int DEFAULT_HEIGHT = 420;

    /** The default limit below which chart scaling kicks in. */
    public static final int DEFAULT_MINIMUM_DRAW_WIDTH = 300;

    /** The default limit below which chart scaling kicks in. */
    public static final int DEFAULT_MINIMUM_DRAW_HEIGHT = 200;

    /** The default limit below which chart scaling kicks in. */
    public static final int DEFAULT_MAXIMUM_DRAW_WIDTH = 800;

    /** The default limit below which chart scaling kicks in. */
    public static final int DEFAULT_MAXIMUM_DRAW_HEIGHT = 600;

    /** The minimum size required to perform a zoom on a rectangle */
    public static final int MINIMUM_DRAG_ZOOM_SIZE = 20;

    /** Properties action command. */
    public static final String PROPERTIES_ACTION_COMMAND = "PROPERTIES";

    /** Save action command. */
    public static final String SAVE_ACTION_COMMAND = "SAVE";

    /** Print action command. */
    public static final String PRINT_ACTION_COMMAND = "PRINT";

    /** Zoom in (both axes) action command. */
    public static final String ZOOM_IN_BOTH_ACTION_COMMAND = "ZOOM_IN_BOTH";

    /** Zoom in (horizontal axis only) action command. */
    public static final String ZOOM_IN_HORIZONTAL_ACTION_COMMAND = "ZOOM_IN_HORIZONTAL";

    /** Zoom in (vertical axis only) action command. */
    public static final String ZOOM_IN_VERTICAL_ACTION_COMMAND = "ZOOM_IN_VERTICAL";

    /** Zoom out (both axes) action command. */
    public static final String ZOOM_OUT_BOTH_ACTION_COMMAND = "ZOOM_OUT_BOTH";

    /** Zoom out (horizontal axis only) action command. */
    public static final String ZOOM_OUT_HORIZONTAL_ACTION_COMMAND = "ZOOM_HORIZONTAL_BOTH";

    /** Zoom out (vertical axis only) action command. */
    public static final String ZOOM_OUT_VERTICAL_ACTION_COMMAND = "ZOOM_VERTICAL_BOTH";

    /** Zoom reset (both axes) action command. */
    public static final String AUTO_RANGE_BOTH_ACTION_COMMAND = "AUTO_RANGE_BOTH";

    /** Zoom reset (horizontal axis only) action command. */
    public static final String AUTO_RANGE_HORIZONTAL_ACTION_COMMAND = "AUTO_RANGE_HORIZONTAL";

    /** Zoom reset (vertical axis only) action command. */
    public static final String AUTO_RANGE_VERTICAL_ACTION_COMMAND = "AUTO_RANGE_VERTICAL";

}