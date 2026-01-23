package asi.voronoi.javafx;

/**
 * Encapsulates view-window math and coordinate transformations for zoom and pan operations.
 * Manages the mapping between world coordinates (data space) and screen coordinates (canvas pixels).
 */
class ZoomPanController {
    private final double pad = 30; // padding around canvas content
    private final double MIN_ZOOM = 0.05; // 5%
    private final double MAX_ZOOM = 50.0; // 5000%
    private static final double EPSILON = 1e-9; // Small value to prevent division by zero

    // World bounds (data)
    private WorldBounds world;

    // View window (world coords, affected by zoom/pan)
    private double viewXmin;
    private double viewXmax;
    private double viewYmin;
    private double viewYmax;

    // Initial view (for reset)
    private double initXmin;
    private double initXmax;
    private double initYmin;
    private double initYmax;

    // Canvas dimensions
    private double canvasWidth = 900;
    private double canvasHeight = 700;

    // Callback for when view changes
    private Runnable onViewChanged;

    /**
     * Sets the world bounds and initializes the view window with margin.
     */
    void setWorldBounds(WorldBounds world) {
        this.world = world;
        double mx = (world.xmax - world.xmin) * 0.05;
        if (!Double.isFinite(mx) || mx <= 0) {
            mx = 1;
        }
        double my = (world.ymax - world.ymin) * 0.05;
        if (!Double.isFinite(my) || my <= 0) {
            my = 1;
        }
        initXmin = viewXmin = world.xmin - mx;
        initXmax = viewXmax = world.xmax + mx;
        initYmin = viewYmin = world.ymin - my;
        initYmax = viewYmax = world.ymax + my;
        notifyViewChanged();
    }

    /**
     * Updates canvas dimensions (should be called when canvas is resized).
     */
    void setCanvasSize(double width, double height) {
        this.canvasWidth = width;
        this.canvasHeight = height;
    }

    /**
     * Sets a callback to be invoked when the view changes.
     */
    void setOnViewChanged(Runnable callback) {
        this.onViewChanged = callback;
    }

    /**
     * Converts world X coordinate to screen X coordinate.
     */
    double sx(double x) {
        double contentW = Math.max(1, canvasWidth - 2 * pad);
        return pad + (x - viewXmin) * (contentW / Math.max(EPSILON, viewXmax - viewXmin));
    }

    /**
     * Converts world Y coordinate to screen Y coordinate (with Y-axis flip).
     */
    double sy(double y) {
        double contentH = Math.max(1, canvasHeight - 2 * pad);
        return canvasHeight - pad - (y - viewYmin) * (contentH / Math.max(EPSILON, viewYmax - viewYmin));
    }

    /**
     * Converts screen X coordinate to world X coordinate.
     */
    double screenToWorldX(double sxv) {
        double contentW = Math.max(1, canvasWidth - 2 * pad);
        double nx = (sxv - pad) / contentW;
        return viewXmin + nx * (viewXmax - viewXmin);
    }

    /**
     * Converts screen Y coordinate to world Y coordinate (with Y-axis flip).
     */
    double screenToWorldY(double syv) {
        double contentH = Math.max(1, canvasHeight - 2 * pad);
        double ny = (canvasHeight - pad - syv) / contentH;
        return viewYmin + ny * (viewYmax - viewYmin);
    }

    /**
     * Zooms at the center of the canvas.
     */
    void zoomAtCenter(double factor) {
        zoomAt(factor, canvasWidth / 2.0, canvasHeight / 2.0);
    }

    /**
     * Zooms at a specific screen coordinate.
     */
    void zoomAt(double factor, double screenX, double screenY) {
        double z = getZoomRatio();
        double nextZ = z * factor;
        if (nextZ > MAX_ZOOM) {
            factor = MAX_ZOOM / z;
        }
        if (nextZ < MIN_ZOOM) {
            factor = MIN_ZOOM / z;
        }
        double vx = screenToWorldX(screenX);
        double vy = screenToWorldY(screenY);
        double w = viewXmax - viewXmin;
        double h = viewYmax - viewYmin;
        double newW = w / factor;
        double newH = h / factor;
        double rx = (vx - viewXmin) / w;
        double ry = (vy - viewYmin) / h;
        viewXmin = vx - rx * newW;
        viewXmax = viewXmin + newW;
        viewYmin = vy - ry * newH;
        viewYmax = viewYmin + newH;
        notifyViewChanged();
    }

    /**
     * Zooms at a specific world coordinate (used by minimap).
     */
    void zoomAtWorldPoint(double factor, double wx, double wy) {
        double z = getZoomRatio();
        double nextZ = z * factor;
        if (nextZ > MAX_ZOOM) {
            factor = MAX_ZOOM / z;
        }
        if (nextZ < MIN_ZOOM) {
            factor = MIN_ZOOM / z;
        }
        double w = viewXmax - viewXmin;
        double h = viewYmax - viewYmin;
        double newW = w / factor;
        double newH = h / factor;
        double rx = (wx - viewXmin) / w;
        double ry = (wy - viewYmin) / h;
        viewXmin = wx - rx * newW;
        viewXmax = viewXmin + newW;
        viewYmin = wy - ry * newH;
        viewYmax = viewYmin + newH;
        notifyViewChanged();
    }

    /**
     * Pans the view by screen pixel deltas.
     */
    void panByScreen(double dx, double dy) {
        double w = viewXmax - viewXmin;
        double h = viewYmax - viewYmin;
        double contentW = Math.max(1, canvasWidth - 2 * pad);
        double contentH = Math.max(1, canvasHeight - 2 * pad);
        double dxWorld = dx * (w / contentW);
        double dyWorld = -dy * (h / contentH); // Y-flip
        viewXmin -= dxWorld;
        viewXmax -= dxWorld;
        viewYmin -= dyWorld;
        viewYmax -= dyWorld;
        notifyViewChanged();
    }

    /**
     * Centers the view on a specific world coordinate.
     */
    void centerViewOn(double wx, double wy) {
        double w = viewXmax - viewXmin;
        double h = viewYmax - viewYmin;
        viewXmin = wx - w / 2;
        viewXmax = wx + w / 2;
        viewYmin = wy - h / 2;
        viewYmax = wy + h / 2;
        notifyViewChanged();
    }

    /**
     * Resets the view to the initial bounds.
     */
    void resetView() {
        viewXmin = initXmin;
        viewXmax = initXmax;
        viewYmin = initYmin;
        viewYmax = initYmax;
        notifyViewChanged();
    }

    /**
     * Fits the view to show all data with a small margin.
     */
    void fitToData() {
        if (world == null) return;
        double mx = (world.xmax - world.xmin) * 0.05;
        if (!Double.isFinite(mx) || mx <= 0) {
            mx = 1;
        }
        double my = (world.ymax - world.ymin) * 0.05;
        if (!Double.isFinite(my) || my <= 0) {
            my = 1;
        }
        viewXmin = world.xmin - mx;
        viewXmax = world.xmax + mx;
        viewYmin = world.ymin - my;
        viewYmax = world.ymax + my;
        notifyViewChanged();
    }

    /**
     * Gets the current zoom ratio (1.0 = 100%, initial view).
     */
    double getZoomRatio() {
        double initW = initXmax - initXmin;
        double curW = viewXmax - viewXmin;
        if (curW <= 0 || !Double.isFinite(curW)) {
            return 1.0;
        }
        return initW / curW;
    }

    /**
     * Gets the current zoom percentage (100 = 100%).
     */
    double getZoomPercent() {
        return getZoomRatio() * 100.0;
    }

    /**
     * Checks if a world coordinate is inside the current view rectangle.
     */
    boolean isInsideViewRect(double wx, double wy) {
        return wx >= viewXmin && wx <= viewXmax && wy >= viewYmin && wy <= viewYmax;
    }

    /**
     * Gets the current view bounds (for minimap rendering).
     */
    double getViewXmin() { return viewXmin; }
    double getViewXmax() { return viewXmax; }
    double getViewYmin() { return viewYmin; }
    double getViewYmax() { return viewYmax; }

    /**
     * Gets the world bounds.
     */
    WorldBounds getWorldBounds() { return world; }

    private void notifyViewChanged() {
        if (onViewChanged != null) {
            onViewChanged.run();
        }
    }
}
