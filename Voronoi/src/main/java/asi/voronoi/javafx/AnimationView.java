package asi.voronoi.javafx;

import asi.voronoi.Point;
import asi.voronoi.anim.StoryboardRecorder;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;

/**
 * Refactored AnimationView class with enhanced support for playback through
 * Animator and frame exporting via FrameExporter.
 */
public class AnimationView extends Pane {

    private final Canvas canvas;                     // Main rendering canvas
    private final MinimapView minimapView;           // Minimap overlay
    private final ZoomPanController zoomPan;         // Handles view transformations
    private final Animator animator;                 // Manages playback
    private final FrameExporter frameExporter;       // Handles exporting frames to PNGs
    private final PointCaptureManager pointCaptureManager; // Manages captured points

    private List<Point> allSites = List.of();        // All points in the animation
    private WorldBounds world = null;               // World bounds for computations

    public AnimationView() {
        // Initialize core components
        this.canvas = new Canvas(900, 700);
        this.minimapView = new MinimapView();
        this.zoomPan = new ZoomPanController(canvas, 30); // Padding = 30
        this.animator = new Animator();
        this.frameExporter = new FrameExporter(canvas);
        this.pointCaptureManager = new PointCaptureManager();

        // Configure Animator
        animator.setOnFrame((frame) -> {
            drawFrame(frame);  // Redraw current frame
            minimapView.drawMinimap(
                    pointCaptureManager.getCapturedPoints().isEmpty() ? allSites : pointCaptureManager.getCapturedPoints(),
                    zoomPan,
                    frame,
                    animator.getFrames(),
                    animator.getCurrentFrameIndex()
            );
        });

        // Configure the layout
        getChildren().addAll(canvas, minimapView.getCanvas());
    }

    public Animator getAnimator() {
        return animator;
    }

    public PointCaptureManager getPointCaptureManager() {
        return pointCaptureManager;
    }

    /**
     * Updates the data used for animation and resets the view.
     *
     * @param allSites points used for drawing.
     * @param frames animation frames to render.
     */
    public void setAnimationData(List<Point> allSites, List<StoryboardRecorder.Frame> frames) {
        this.allSites = allSites != null ? new ArrayList<>(allSites) : List.of();
        animator.setFrames(frames); // Delegate frame initialization to Animator
        world = computeWorldBounds(frames); // Compute the world bounds for the data
        zoomPan.setWorldBounds(world); // Update zoom/pan to reflect new bounds
        zoomPan.resetView();           // Reset to fit to data
    }

    public void drawFrame(StoryboardRecorder.Frame f) {
        GraphicsContext g = canvas.getGraphicsContext2D();

        double W = canvas.getWidth(), H = canvas.getHeight();
        g.setFill(javafx.scene.paint.Color.WHITE);
        g.fillRect(0, 0, W, H);

        // Render all points (overlay)
        g.setFill(javafx.scene.paint.Color.web("#444444"));
        g.setGlobalAlpha(0.65);
        for (Point s : allSites) {
            g.fillOval(zoomPan.sx(s.x()) - 2.5, zoomPan.sy(s.y()) - 2.5, 5, 5);
        }
        g.setGlobalAlpha(1.0);

//        if (f != null && f.label != null) {
//            onFrameLabelChanged.accept(f.label); // Inform frame-related UI callback
//        }

        // Render bounding box (division region)
        if (f != null && f.bbox != null) {
            var b = f.bbox;
            g.setGlobalAlpha(0.08); // Semi-transparent
            g.setFill(javafx.scene.paint.Color.web("#6c5ce7"));
            g.fillRect(
                    zoomPan.sx(b.xMin()), zoomPan.sy(b.yMax()),
                    Math.abs(zoomPan.sx(b.xMax()) - zoomPan.sx(b.xMin())),
                    Math.abs(zoomPan.sy(b.yMin()) - zoomPan.sy(b.yMax()))
            );
            g.setGlobalAlpha(1.0);
        }

        // Render split line
        if (f != null && f.split != null) {
            g.setStroke(javafx.scene.paint.Color.web("#636e72"));
            g.setLineDashes(8, 8); // Dashed line
            g.setLineWidth(1.4);
            var s = f.split;
            var mp = s.getMidP(); // Midpoint of the split
            var d = s.getDir(); // Direction of the line
            double x1 = mp.x() - d.x() * 5000, y1 = mp.y() - d.y() * 5000;
            double x2 = mp.x() + d.x() * 5000, y2 = mp.y() + d.y() * 5000;
            g.strokeLine(zoomPan.sx(x1), zoomPan.sy(y1), zoomPan.sx(x2), zoomPan.sy(y2));
            g.setLineDashes(); // Reset dashed line
        }

        // Render left points
        g.setFill(javafx.scene.paint.Color.web("#0984e3"));
        if (f != null && f.leftPts != null) {
            for (Point p : f.leftPts) {
                g.fillOval(zoomPan.sx(p.x()) - 3, zoomPan.sy(p.y()) - 3, 6, 6);
            }
        }

        // Render right points
        g.setFill(javafx.scene.paint.Color.web("#d63031"));
        if (f != null && f.rightPts != null) {
            for (Point p : f.rightPts) {
                g.fillOval(zoomPan.sx(p.x()) - 3, zoomPan.sy(p.y()) - 3, 6, 6);
            }
        }

        // Render pivot point (centroid)
        if (f != null && f.pivot != null) {
            var p = f.pivot;
            g.setFill(javafx.scene.paint.Color.web("#f2c94c")); // Yellow
            g.fillOval(zoomPan.sx(p.x()) - 4, zoomPan.sy(p.y()) - 4, 8, 8);
        }

        // Render edges (Voronoi edges or merge lines)
        g.setStroke(javafx.scene.paint.Color.web("#2f80ed")); // Blue for edges
        g.setLineWidth(2.0);
        if (f != null && f.edges != null) {
            for (asi.voronoi.Line ln : f.edges) {
                drawLineSegment(g, ln); // Delegate to helper method
            }
        }

        // Render support marks (debugging aids or landmarks)
        if (f != null && f.marks != null && !f.marks.isEmpty()) {
            g.setFill(javafx.scene.paint.Color.web("#00b894")); // Green for marks
            for (Point p : f.marks) {
                g.fillOval(zoomPan.sx(p.x()) - 4, zoomPan.sy(p.y()) - 4, 8, 8);
            }
        }

        // Render captured points overlay (magenta) — only in captureMode
        if (pointCaptureManager.isCaptureMode() && !pointCaptureManager.getCapturedPoints().isEmpty()) {
            g.setFill(javafx.scene.paint.Color.web("#a100ff")); // Magenta for captured points
            for (Point s : pointCaptureManager.getCapturedPoints()) {
                g.fillOval(zoomPan.sx(s.x()) - 3.5, zoomPan.sy(s.y()) - 3.5, 7, 7);
            }
        }
    }

    private void drawLineSegment(GraphicsContext g, asi.voronoi.Line ln) {
        var b = ln.getBeginP().orElse(null);
        var e = ln.getEndP().orElse(null);
        if (b != null && e != null) {
            g.strokeLine(zoomPan.sx(b.x()), zoomPan.sy(b.y()), zoomPan.sx(e.x()), zoomPan.sy(e.y()));
        } else if (b != null) {
            var d = ln.getDir();
            double x2 = b.x() + d.x() * 5000, y2 = b.y() + d.y() * 5000;
            g.strokeLine(zoomPan.sx(b.x()), zoomPan.sy(b.y()), zoomPan.sx(x2), zoomPan.sy(y2));
        } else if (e != null) {
            var d = ln.getDir();
            double x1 = e.x() - d.x() * 5000, y1 = e.y() - d.y() * 5000;
            g.strokeLine(zoomPan.sx(x1), zoomPan.sy(y1), zoomPan.sx(e.x()), zoomPan.sy(e.y()));
        } else {
            var m = ln.getMidP();
            var d = ln.getDir();
            double x1 = m.x() - d.x() * 5000, y1 = m.y() - d.y() * 5000;
            double x2 = m.x() + d.x() * 5000, y2 = m.y() + d.y() * 5000;
            g.strokeLine(zoomPan.sx(x1), zoomPan.sy(y1), zoomPan.sx(x2), zoomPan.sy(y2));
        }
    }

    // === Playback Controls ===
    public void play() {
        animator.play();
    }

    public void pause() {
        animator.pause();
    }

    public void resume() {
        animator.resume();
    }

    public void stop() {
        animator.stop();
    }

    public void stepForward() {
        animator.stepForward();
    }

    public void stepBack() {
        animator.stepBack();
    }

    public void setSpeed(double rate) {
        animator.setFrameMs(200 / Math.max(1e-9, rate));
    }

    // === Exporting Frames ===
    public void exportPngs(File directory, String prefix) throws IOException {
        frameExporter.exportFramesToPng(
                animator.getFrames(),
                directory,
                prefix,
                (frame, index) -> drawFrame(frame) // Adapt the method by adding `index`
        );
    }

    // === Zoom & Pan Controls ===
    public void zoomAtCenter(double factor) {
        zoomPan.zoomAtCenter(factor);
    }

    public void zoomAt(double factor, double screenX, double screenY) {
        zoomPan.zoomAt(factor, screenX, screenY);
    }

    public void panByScreen(double dx, double dy) {
        zoomPan.panByScreen(dx, dy);
    }

    public void resetView() {
        zoomPan.resetView();
    }

    /**
     * Sets the position of the minimap.
     *
     * @param position The new position of the minimap.
     */
    public void setMinimapPosition(MinimapView.MinimapPos position) {
        if (minimapView != null) {
            minimapView.setPos(position); // Delegate position adjustments to the MinimapView
        }
    }

    /**
     * Toggles the visibility of the minimap.
     *
     * @param enabled If true, the minimap will be visible; otherwise, it will
     * be hidden.
     */
    public void setMinimapEnabled(boolean enabled) {
        if (minimapView != null) {
            minimapView.getCanvas().setVisible(enabled); // Show or hide the minimap canvas
        }
    }
// === Helper Methods ===

    /**
     * Compute the world bounds for the given frames and all sites.
     */
    private WorldBounds computeWorldBounds(List<StoryboardRecorder.Frame> frames) {
        double xmin = Double.POSITIVE_INFINITY, ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;

        if (frames != null) {
            for (var frame : frames) {
                if (frame.bbox != null) {
                    xmin = Math.min(xmin, frame.bbox.xMin());
                    xmax = Math.max(xmax, frame.bbox.xMax());
                    ymin = Math.min(ymin, frame.bbox.yMin());
                    ymax = Math.max(ymax, frame.bbox.yMax());
                }
            }
        }

        if (allSites != null) {
            for (var point : allSites) {
                xmin = Math.min(xmin, point.x());
                xmax = Math.max(xmax, point.x());
                ymin = Math.min(ymin, point.y());
                ymax = Math.max(ymax, point.y());
            }
        }

        // Fallback to reasonable bounds if no valid world bounds are defined
        if (!Double.isFinite(xmin) || xmin == xmax || ymin == ymax) {
            xmin = 0;
            xmax = 1;
            ymin = 0;
            ymax = 1;
        }

        return new WorldBounds(xmin, ymin, xmax, ymax);
    }
// Inside AnimationView class

    /**
     * Updates the minimap size.
     *
     * @param width The new width of the minimap.
     * @param height The new height of the minimap.
     */
    public void setMinimapSize(double width, double height) {
        if (minimapView != null) {
            minimapView.setSize(width, height); // Delegate to MinimapView
        }
    }

// Inside AnimationView class
    /**
     * Adjusts the zoom and pan settings to fit all data in view.
     */
    public void fitToData() {
        if (world == null) {
            world = computeWorldBounds(animator.getFrames()); // Recompute world bounds if necessary
        }

        if (world != null) {
            zoomPan.setWorldBounds(world); // Update the world bounds for the ZoomPanController
            zoomPan.resetView();          // Reset the view to fully fit the world bounds
        }
    }
}
