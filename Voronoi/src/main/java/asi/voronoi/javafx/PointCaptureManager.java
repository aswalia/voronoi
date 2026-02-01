package asi.voronoi.javafx;

import asi.voronoi.Point;
import java.util.ArrayList;
import java.util.List;

public class PointCaptureManager {
    private final List<Point> capturedPoints;
    private boolean captureMode;

    public PointCaptureManager() {
        this.capturedPoints = new ArrayList<>();
    }

    public List<Point> getCapturedPoints() {
        return capturedPoints;
    }

    public void addPoint(Point point) {
        capturedPoints.add(point);
    }

    // capture APIs
    public void startPointCapture() {
        captureMode = true;
        capturedPoints.clear();
    }

    public void stopPointCapture() {
        captureMode = false;
    }

    public void clearCapturedPoints() {
        capturedPoints.clear();
    }

    public int getCapturedSize() {
        return capturedPoints.size();
    }
    
    public boolean isCaptureMode() {
        return captureMode;
    }

    public boolean undoCapturedPoint() {
        if (!capturedPoints.isEmpty()) {
            capturedPoints.remove(capturedPoints.size() - 1);
            return true;
        }
        return false;
    }

}