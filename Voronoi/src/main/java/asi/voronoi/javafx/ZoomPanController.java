package asi.voronoi.javafx;

import javafx.scene.canvas.Canvas;

import java.util.function.Consumer;

/**
 * Encapsulates view window math (world <-> screen), zoom & pan state.
 * Exposes callback onViewChanged to trigger redraws.
 */
public class ZoomPanController {
    private final Canvas canvas;
    private final double pad;
    private WorldBounds world = new WorldBounds(0,0,1,1);

    private double viewXmin, viewXmax, viewYmin, viewYmax;
    private double initXmin, initXmax, initYmin, initYmax;
    private final double MIN_ZOOM = 0.05;
    private final double MAX_ZOOM = 50.0;

    private Consumer<Void> onViewChanged = v -> {};

    public ZoomPanController(Canvas canvas, double pad) {
        this.canvas = canvas;
        this.pad = pad;
    }

    public void setOnViewChanged(Consumer<Void> cb) { this.onViewChanged = cb != null ? cb : v -> {}; }

    public void setWorldBounds(WorldBounds w) {
        if (w == null) return;
        this.world = w;

        double mx = (world.xmax - world.xmin) * 0.05;
        if (mx <= 0) mx = 1;
        initXmin = viewXmin = world.xmin - mx;
        initXmax = viewXmax = world.xmax + mx;
        initYmin = viewYmin = world.ymin - mx;
        initYmax = viewYmax = world.ymax + mx;
        onViewChanged.accept(null);
    }

    public WorldBounds getWorldBounds() { return world; }

    public void resetView() {
        viewXmin = initXmin; viewXmax = initXmax;
        viewYmin = initYmin; viewYmax = initYmax;
        onViewChanged.accept(null);
    }

    public void centerViewOn(double wx, double wy) {
        double w = viewXmax - viewXmin, h = viewYmax - viewYmin;
        viewXmin = wx - w / 2; viewXmax = wx + w / 2;
        viewYmin = wy - h / 2; viewYmax = wy + h / 2;
        onViewChanged.accept(null);
    }

    public void panByScreen(double dx, double dy) {
        double w = viewXmax - viewXmin;
        double h = viewYmax - viewYmin;
        double contentW = Math.max(1, canvas.getWidth() - 2 * pad);
        double contentH = Math.max(1, canvas.getHeight() - 2 * pad);

        double dxWorld = dx * (w / contentW);
        double dyWorld = -dy * (h / contentH); // screen Y-flip

        viewXmin -= dxWorld; viewXmax -= dxWorld;
        viewYmin -= dyWorld; viewYmax -= dyWorld;
        onViewChanged.accept(null);
    }

    public void zoomAt(double factor, double screenX, double screenY) {
        // factor is multiplicative zoom (e.g. 1.2 to zoom in 20%)
        double z = getZoomRatio();
        double nextZ = z * factor;
        if (nextZ > MAX_ZOOM) {
            factor = MAX_ZOOM / z;
        }
        if (nextZ < MIN_ZOOM) {
            factor = MIN_ZOOM / z;
        }

        double nx = (screenX - pad) / Math.max(1, canvas.getWidth() - 2 * pad);
        double ny = (canvas.getHeight() - pad - screenY) / Math.max(1, canvas.getHeight() - 2 * pad); // flip
        nx = clamp(nx, 0.0, 1.0); ny = clamp(ny, 0.0, 1.0);

        double vx = viewXmin + nx * (viewXmax - viewXmin);
        double vy = viewYmin + ny * (viewYmax - viewYmin);

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

        onViewChanged.accept(null);
    }

    public void zoomAtCenter(double factor) {
        zoomAt(factor, canvas.getWidth() / 2.0, canvas.getHeight() / 2.0);
    }

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

        onViewChanged.accept(null);
    }

    public double screenToWorldX(double sxv) {
        double contentW = Math.max(1, canvas.getWidth() - 2 * pad);
        double nx = (sxv - pad) / contentW;
        return viewXmin + nx * (viewXmax - viewXmin);
    }

    public double screenToWorldY(double syv) {
        double contentH = Math.max(1, canvas.getHeight() - 2 * pad);
        double ny = (canvas.getHeight() - pad - syv) / contentH; // flip
        return viewYmin + ny * (viewYmax - viewYmin);
    }

    public double sx(double x) {
        double contentW = Math.max(1, canvas.getWidth() - 2 * pad);
        return pad + (x - viewXmin) * (contentW / Math.max(1e-9, (viewXmax - viewXmin)));
    }

    public double sy(double y) {
        double contentH = Math.max(1, canvas.getHeight() - 2 * pad);
        return canvas.getHeight() - pad - (y - viewYmin) * (contentH / Math.max(1e-9, (viewYmax - viewYmin)));
    }

    public double getZoomRatio() {
        double initW = (initXmax - initXmin);
        double curW = (viewXmax - viewXmin);
        if (curW <= 0 || !Double.isFinite(curW)) return 1.0;
        return initW / curW;
    }

    public double getViewXmin() { return viewXmin; }
    public double getViewXmax() { return viewXmax; }
    public double getViewYmin() { return viewYmin; }
    public double getViewYmax() { return viewYmax; }

    private double clamp(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }
}
