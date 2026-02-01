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

    private final Canvas canvas;
    private final MinimapView minimapView;
    private final ZoomPanController zoomPan;
    private final Animator animator;
    private final PointCaptureManager pointCapture;

    // Data (mirrors original fields used by draw code)
    private List<Point> allSites = List.of();
    private List<StoryboardRecorder.Frame> frames = List.of();
    private final List<Point> capturedPoints = new ArrayList<>();
    private boolean captureMode = false;

    // world bounds (data) - stored in ZoomPanController as well, but keep a copy for minimap computations
    private WorldBounds world;

    // timeline and frame state
    private final double frameMs = 200;
    private int currentIndex = 0;

    // minimap sizing (default)
    private double miniW = 220, miniH = 160;
    private final double miniPad = 6;

    // interactions/panning state (for main canvas)
    private double lastMouseX = Double.NaN, lastMouseY = Double.NaN;
    private boolean panning = false;

    // callbacks
    private IntConsumer onCapturedCountChanged = n -> {
    };
    private Consumer<String> onFrameLabelChanged = s -> {
    };

    private final double pad = 30;

    public AnimationView() {
        canvas = new Canvas(900, 700);
        minimapView = new MinimapView();
        getChildren().addAll(canvas, minimapView.getCanvas());

        zoomPan = new ZoomPanController(canvas, pad);
        animator = new Animator();
        
        pointCapture = new PointCaptureManager();

        // wire animator -> draw + minimap
        animator.setOnFrame(f -> {
            // set current index (best-effort)
            int idx = (frames == null) ? 0 : Math.max(0, frames.indexOf(f));
            if (idx < 0) {
                idx = 0;
            }
            currentIndex = idx;

            drawFrame(f);
            minimapView.drawMinimap(captureMode && !capturedPoints.isEmpty() ? capturedPoints : allSites, zoomPan, f, frames, currentIndex);
            // notify label change
            if (f != null && onFrameLabelChanged != null) {
                onFrameLabelChanged.accept(f.label);
            }
        });

        // when view changes -> redraw current frame
        zoomPan.setOnViewChanged(v -> {
            if (frames == null || frames.isEmpty()) {
                redrawBlank();
            } else {
                // draw current frame
                StoryboardRecorder.Frame f = frames.get(Math.max(0, Math.min(frames.size() - 1, currentIndex)));
                drawFrame(f);
                minimapView.drawMinimap(captureMode && !capturedPoints.isEmpty() ? capturedPoints : allSites, zoomPan, f, frames, currentIndex);
                if (onFrameLabelChanged != null && f != null) {
                    onFrameLabelChanged.accept(f.label);
                }
            }
        });

        // resize listeners
        widthProperty().addListener((o, ov, nv) -> resizeChildren());
        heightProperty().addListener((o, ov, nv) -> resizeChildren());

        installInteractions();
        installMinimapInteractions();
    }

    // Public API used by Main (keeps method names similar to old AnimationPane)
    public void setOnCapturedCountChanged(IntConsumer cb) {
        this.onCapturedCountChanged = cb != null ? cb : n -> {
        };
    }

    public void setOnFrameLabelChanged(Consumer<String> cb) {
        this.onFrameLabelChanged = cb != null ? cb : s -> {
        };
    }

    public void setSites(List<Point> pts) {
        this.allSites = pts == null ? List.of() : new ArrayList<>(pts);
    }

    public void setFrames(List<StoryboardRecorder.Frame> frames) {
        this.frames = frames == null ? List.of() : new ArrayList<>(frames);
        this.world = computeWorld(this.frames);
        if (this.world != null) {
            zoomPan.setWorldBounds(this.world);
        }
        this.currentIndex = 0;
        redrawCurrent();
    }

    public Animator getAnimator() {
        return animator;
    }

    public ZoomPanController getZoomPanController() {
        return zoomPan;
    }

    public MinimapView getMinimapView() {
        return minimapView;
    }

    public void setSpeed(double rate) {
        animator.setFrameMs(frameMs / Math.max(1e-9, rate));
    }

    public double getZoomPercent() {
        return zoomPan.getZoomRatio() * 100.0;
    }

    // Playback API
    public void play() {
        // Ensure animator has the current frames list before starting playback.
        animator.setFrames(this.frames);
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

    public void exportPngs(File dir, String prefix) throws Exception {
        animator.exportPngs(dir, prefix, (frame, idx) -> {
            drawFrame(frame);
            var img = canvas.snapshot(null, null);
            File out = new File(dir, String.format("%s_%04d.png", prefix, idx + 1));
            javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(img, null), "png", out);
        });
    }

    // capture APIs
    public void startPointCapture() {
        pointCapture.startPointCapture();
        redrawCurrent();
    }

    public void stopPointCapture() {
        pointCapture.stopPointCapture();
        redrawCurrent();
    }

    public void clearCapturedPoints() {
        pointCapture.clearCapturedPoints();
        redrawCurrent();
    }

    public int getCapturedSize() {
        return pointCapture.getCapturedSize();
    }

    public boolean undoCapturedPoint() {
        if (pointCapture.undoCapturedPoint()) {
            redrawCurrent();
            if (onCapturedCountChanged != null) {
                onCapturedCountChanged.accept(capturedPoints.size());
            }
            return true;
        }
        return false;
    }

    public List<Point> getCapturedPoints() {
        return pointCapture.getCapturedPoints();
    }

    // minimap API
    public void setMinimapEnabled(boolean enabled) {
        minimapView.setEnabled(enabled);
        redrawCurrent();
    }

    public void setMinimapPosition(MinimapView.MinimapPos pos) {
        minimapView.setPos(pos);
        redrawCurrent();
    }

    public void setMinimapSize(double w, double h) {
        this.miniW = w;
        this.miniH = h;
        minimapView.setSize(w, h);
        redrawCurrent();
    }

    // zoom/pan wrappers (to preserve old API names)
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

    public void fitToData() {
        if (world == null) {
            world = computeWorld(frames);
        }
        if (world != null) {
            zoomPan.setWorldBounds(world);
            zoomPan.resetView();
        }
    }

// Clears temporary overlays (captured points) and blanks the main canvas + minimap.
// Does NOT clear the stored frames or allSites.
    public void clearOverlay() {
        // stop any running animation
        animator.stop();

        // clear captured points overlay
        pointCapture.clearCapturedPoints();

        // clear visuals
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(javafx.scene.paint.Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // redraw minimap with no current frame
        minimapView.drawMinimap(allSites.isEmpty() ? List.of() : allSites, zoomPan, null, frames, currentIndex);
    }

// Clears everything: stops animation, drops frames & sites, clears captured points and canvas.
// Use when you want a full visual reset.
    public void clearAll() {
        animator.stop();
        frames = List.of();
        allSites = List.of();
        pointCapture.clearCapturedPoints();

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(javafx.scene.paint.Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // update minimap (no world / no content)
        minimapView.drawMinimap(List.of(), zoomPan, null, frames, 0);
    }

// UI layout helpers
    private void resizeChildren() {
        double w = Math.max(1, getWidth());
        double h = Math.max(1, getHeight());
        if (canvas.getWidth() != w || canvas.getHeight() != h) {
            canvas.setWidth(w);
            canvas.setHeight(h);
        }
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);

        minimapView.layoutMinimap(w, h);
        redrawCurrent();
    }

    private void redrawBlank() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void redrawCurrent() {
        if (frames == null || frames.isEmpty()) {
            var g = canvas.getGraphicsContext2D();
            g.setFill(javafx.scene.paint.Color.WHITE);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            if (pointCapture.getCapturedSize()>0) {
                g.setFill(javafx.scene.paint.Color.web("#a100ff"));
                for (Point s : pointCapture.getCapturedPoints()) {
                    g.fillOval(zoomPan.sx(s.x()) - 3.5, zoomPan.sy(s.y()) - 3.5, 7, 7);
                }
            }
            minimapView.drawMinimap(pointCapture.isCaptureMode() && pointCapture.getCapturedSize()>0 ? pointCapture.getCapturedPoints() : allSites, zoomPan, null, frames, currentIndex);
            return;
        }
        StoryboardRecorder.Frame f = frames.get(Math.max(0, Math.min(frames.size() - 1, currentIndex)));
        drawFrame(f);
        minimapView.drawMinimap(pointCapture.isCaptureMode() && pointCapture.getCapturedSize()>0 ? pointCapture.getCapturedPoints() : allSites, zoomPan, f, frames, currentIndex);
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

        // Captured points overlay (magenta) — only in captureMode
        if (pointCapture.isCaptureMode() && pointCapture.getCapturedSize()>0) {
            g.setFill(javafx.scene.paint.Color.web("#a100ff")); // magenta
            for (Point s : pointCapture.getCapturedPoints()) {
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

    // ---------- interactions (main canvas) ----------
    private void installInteractions() {
        // zoom with wheel (around cursor)
        canvas.setOnScroll(ev -> {
            double factor = (ev.getDeltaY() > 0) ? 1.2 : (1 / 1.2);
            zoomAt(factor, ev.getX(), ev.getY());
            ev.consume();
        });

        canvas.setOnMouseClicked(ev -> {
            if (pointCapture.isCaptureMode()) {
                if (ev.getButton() == MouseButton.PRIMARY) {
                    double wx = zoomPan.screenToWorldX(ev.getX());
                    double wy = zoomPan.screenToWorldY(ev.getY());
                    pointCapture.addPoint(new Point(wx, wy));
                    if (onCapturedCountChanged != null) {
                        onCapturedCountChanged.accept(pointCapture.getCapturedSize());
                    }
                    redrawCurrent();
                    ev.consume();
                } else if (ev.getButton() == MouseButton.SECONDARY) {
                    undoCapturedPoint();
                    ev.consume();
                }
            }
        });

        canvas.setOnMousePressed(ev -> {
            if (pointCapture.isCaptureMode()) {
                return;
            }
            if (ev.isPrimaryButtonDown()) {
                panning = true;
                lastMouseX = ev.getX();
                lastMouseY = ev.getY();
                canvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
            }
        });

        canvas.setOnMouseDragged(ev -> {
            if (pointCapture.isCaptureMode()) {
                return;
            }
            if (panning) {
                double dx = ev.getX() - lastMouseX;
                double dy = ev.getY() - lastMouseY;
                panByScreen(dx, dy);
                lastMouseX = ev.getX();
                lastMouseY = ev.getY();
            }
        });

        canvas.setOnMouseReleased(ev -> {
            panning = false;
            canvas.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        canvas.setOnMouseEntered(ev -> canvas.setCursor(javafx.scene.Cursor.OPEN_HAND));
        canvas.setOnMouseExited(ev -> canvas.setCursor(javafx.scene.Cursor.DEFAULT));
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
