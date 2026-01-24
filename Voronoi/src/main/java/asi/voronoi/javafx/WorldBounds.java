package asi.voronoi.javafx;

/**
 * Simple world bounds (data coordinates).
 */
public final class WorldBounds {
    public double xmin, ymin, xmax, ymax;

    public WorldBounds(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public double width() { return xmax - xmin; }
    public double height() { return ymax - ymin; }
}
