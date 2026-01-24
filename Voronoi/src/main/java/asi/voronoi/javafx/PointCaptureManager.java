package asi.voronoi.javafx;

import asi.voronoi.Point;
import java.util.ArrayList;
import java.util.List;

public class PointCaptureManager {
    private final List<Point> capturedPoints;

    public PointCaptureManager() {
        this.capturedPoints = new ArrayList<>();
    }

    public List<Point> getCapturedPoints() {
        return capturedPoints;
    }

    public void addPoint(Point point) {
        capturedPoints.add(point);
    }

    public void clearPoints() {
        capturedPoints.clear();
    }
}