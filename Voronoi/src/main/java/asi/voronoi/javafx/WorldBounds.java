package asi.voronoi.javafx;

/**
 * Simple holder for world coordinate bounds.
 * Represents the bounding box of the data in world coordinates.
 */
class WorldBounds {
    final double xmin;
    final double ymin;
    final double xmax;
    final double ymax;

    WorldBounds(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }
}
