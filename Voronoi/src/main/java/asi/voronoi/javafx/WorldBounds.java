package asi.voronoi.javafx;

/**
 * Simple immutable data holder for world coordinate bounds.
 * Represents the bounding box of all data points in world coordinates.
 */
public class WorldBounds {
    private final double xmin;
    private final double ymin;
    private final double xmax;
    private final double ymax;

    public WorldBounds(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public double xmin() {
        return xmin;
    }

    public double ymin() {
        return ymin;
    }

    public double xmax() {
        return xmax;
    }

    public double ymax() {
        return ymax;
    }

    public double width() {
        return xmax - xmin;
    }

    public double height() {
        return ymax - ymin;
    }
}
