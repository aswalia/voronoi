package asi.voronoi.javafx;

/**
 * Encapsulates view window math and coordinate transforms for zoom and pan operations.
 * Manages the mapping between world coordinates and screen coordinates.
 */
public class ZoomPanController {
    private WorldBounds worldBounds;
    private double viewXmin;
    private double viewXmax;
    private double viewYmin;
    private double viewYmax;
    private double initXmin;
    private double initXmax;
    private double initYmin;
    private double initYmax;
    
    private final double MIN_ZOOM = 0.05; // 5%
    private final double MAX_ZOOM = 50.0; // 5000%
    private final double pad = 30;
    
    private double canvasWidth = 1;
    private double canvasHeight = 1;
    
    private Runnable onViewChanged;

    /**
     * Set the world bounds for this controller.
     */
    public void setWorldBounds(WorldBounds bounds) {
        this.worldBounds = bounds;
        // Initialize view with 5% margin
        double mx = bounds.width() * 0.05;
        if (!Double.isFinite(mx) || mx <= 0) {
            mx = 1;
        }
        double my = bounds.height() * 0.05;
        if (!Double.isFinite(my) || my <= 0) {
            my = 1;
        }
        initXmin = viewXmin = bounds.xmin() - mx;
        initXmax = viewXmax = bounds.xmax() + mx;
        initYmin = viewYmin = bounds.ymin() - my;
        initYmax = viewYmax = bounds.ymax() + my;
        notifyViewChanged();
    }

    /**
     * Set callback to be invoked when view changes.
     */
    public void setOnViewChanged(Runnable callback) {
        this.onViewChanged = callback;
    }

    /**
     * Update canvas dimensions for coordinate transforms.
     */
    public void setCanvasSize(double width, double height) {
        this.canvasWidth = Math.max(1, width);
        this.canvasHeight = Math.max(1, height);
    }

    /**
     * Convert world X coordinate to screen X coordinate.
     */
    public double sx(double x) {
        double contentW = Math.max(1, canvasWidth - 2 * pad);
        return pad + (x - viewXmin) * (contentW / Math.max(1e-9, viewXmax - viewXmin));
    }

    /**
     * Convert world Y coordinate to screen Y coordinate.
     */
    public double sy(double y) {
        double contentH = Math.max(1, canvasHeight - 2 * pad);
        return canvasHeight - pad - (y - viewYmin) * (contentH / Math.max(1e-9, viewYmax - viewYmin));
    }

    /**
     * Convert screen X coordinate to world X coordinate.
     */
    public double screenToWorldX(double sxv) {
        double contentW = Math.max(1, canvasWidth - 2 * pad);
        double nx = (sxv - pad) / contentW;
        return viewXmin + nx * (viewXmax - viewXmin);
    }

    /**
     * Convert screen Y coordinate to world Y coordinate.
     */
    public double screenToWorldY(double syv) {
        double contentH = Math.max(1, canvasHeight - 2 * pad);
        double ny = (canvasHeight - pad - syv) / contentH; // flip
        return viewYmin + ny * (viewYmax - viewYmin);
    }

    /**
     * Zoom at a specific screen point.
     */
    public void zoomAt(double factor, double screenX, double screenY) {
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
     * Zoom at the center of the canvas.
     */
    public void zoomAtCenter(double factor) {
        zoomAt(factor, canvasWidth / 2.0, canvasHeight / 2.0);
    }

    /**
     * Zoom at a specific world point.
     */
    public void zoomAtWorldPoint(double factor, double wx, double wy) {
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
     * Pan the view by screen pixel offsets.
     */
    public void panByScreen(double dx, double dy) {
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
     * Reset view to initial bounds.
     */
    public void resetView() {
        viewXmin = initXmin;
        viewXmax = initXmax;
        viewYmin = initYmin;
        viewYmax = initYmax;
        notifyViewChanged();
    }

    /**
     * Fit view to world data bounds.
     */
    public void fitToData() {
        if (worldBounds == null) {
            return;
        }
        double mx = worldBounds.width() * 0.05;
        if (!Double.isFinite(mx) || mx <= 0) {
            mx = 1;
        }
        double my = worldBounds.height() * 0.05;
        if (!Double.isFinite(my) || my <= 0) {
            my = 1;
        }
        viewXmin = worldBounds.xmin() - mx;
        viewXmax = worldBounds.xmax() + mx;
        viewYmin = worldBounds.ymin() - my;
        viewYmax = worldBounds.ymax() + my;
        notifyViewChanged();
    }

    /**
     * Center view on a world point.
     */
    public void centerViewOn(double wx, double wy) {
        double w = viewXmax - viewXmin;
        double h = viewYmax - viewYmin;
        viewXmin = wx - w / 2;
        viewXmax = wx + w / 2;
        viewYmin = wy - h / 2;
        viewYmax = wy + h / 2;
        notifyViewChanged();
    }

    /**
     * Check if a world point is inside the current view rect.
     */
    public boolean isInsideViewRect(double wx, double wy) {
        return wx >= viewXmin && wx <= viewXmax && wy >= viewYmin && wy <= viewYmax;
    }

    /**
     * Get current zoom ratio (1.0 = initial zoom).
     */
    public double getZoomRatio() {
        double initW = initXmax - initXmin;
        double curW = viewXmax - viewXmin;
        if (curW <= 0 || !Double.isFinite(curW)) {
            return 1.0;
        }
        return initW / curW;
    }

    /**
     * Get zoom as a percentage.
     */
    public double getZoomPercent() {
        return getZoomRatio() * 100.0;
    }

    /**
     * Get current view bounds.
     */
    public double getViewXmin() { return viewXmin; }
    public double getViewXmax() { return viewXmax; }
    public double getViewYmin() { return viewYmin; }
    public double getViewYmax() { return viewYmax; }

    private void notifyViewChanged() {
        if (onViewChanged != null) {
            onViewChanged.run();
        }
    }
}
