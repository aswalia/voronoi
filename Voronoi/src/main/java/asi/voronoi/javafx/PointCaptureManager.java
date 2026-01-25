package asi.voronoi.javafx;

import asi.voronoi.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the state of captured points and capture mode.
 */
public class PointCaptureManager {
    private final List<Point> capturedPoints; // List of captured points
    private boolean captureMode;              // Flag to determine if capture mode is active

    public PointCaptureManager() {
        this.capturedPoints = new ArrayList<>();
        this.captureMode = false; // Initially, capture mode is disabled
    }

    /**
     * Enables capture mode and clears any previously captured points.
     */
    public void startCaptureMode() {
        this.captureMode = true;
        clearPoints(); // Clear previous points when entering capture mode
    }

    /**
     * Disables capture mode.
     */
    public void stopCaptureMode() {
        this.captureMode = false;
    }

    /**
     * Returns whether capture mode is currently enabled.
     *
     * @return true if capture mode is active, false otherwise
     */
    public boolean isCaptureMode() {
        return this.captureMode;
    }

    /**
     * Adds a point to the list of captured points (only in capture mode).
     *
     * @param point the point to add.
     * @throws IllegalStateException if capture mode is not enabled.
     */
    public void addPoint(Point point) {
        if (!captureMode) {
            throw new IllegalStateException("Cannot add points unless capture mode is active.");
        }
        capturedPoints.add(point);
    }

    /**
     * Gets the list of captured points.
     *
     * @return the capturedPoints list.
     */
    public List<Point> getCapturedPoints() {
        return new ArrayList<>(capturedPoints); // Return a copy to preserve immutability
    }

    /**
     * Clears the list of captured points.
     */
    public void clearPoints() {
        capturedPoints.clear();
    }
}