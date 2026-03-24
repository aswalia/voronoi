package asi.voronoi.javafx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.input.MouseButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import asi.voronoi.Point;
import asi.voronoi.anim.StoryboardRecorder;

/**
 * Composed view that replaces the old AnimationPane. It wires together: -
 * Canvas for drawing - MinimapView for overlay - ZoomPanController for view
 * math and interactions - Animator for playback control
 *
 * The drawFrame(...) method below is an adapted copy of the original
 * AnimationPane.drawFrame(...) from Main.java with coordinate helper calls
 * replaced by zoomPan.sx()/zoomPan.sy() and minimap drawing delegated to
 * MinimapView.drawMinimap(...).
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
        canvas = new Canvas(1000, 800);
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
     * Copy of original drawFrame(...) adapted to use zoomPan.sx()/zoomPan.sy().
     */
    private void drawFrame(StoryboardRecorder.Frame f) {
        GraphicsContext g = canvas.getGraphicsContext2D();

        double W = canvas.getWidth(), H = canvas.getHeight();
        g.setFill(javafx.scene.paint.Color.WHITE);
        g.fillRect(0, 0, W, H);

        // all sites overlay
        g.setFill(javafx.scene.paint.Color.web("#444444"));
        g.setGlobalAlpha(0.65);
        for (Point s : allSites) {
            g.fillOval(zoomPan.sx(s.x()) - 2.5, zoomPan.sy(s.y()) - 2.5, 5, 5);
        }
        g.setGlobalAlpha(1.0);

        if (f != null && onFrameLabelChanged != null) {
            onFrameLabelChanged.accept(f.label);
        }

        // division: bbox
        if (f != null && f.bbox != null) {
            var b = f.bbox;
            g.setGlobalAlpha(0.08);
            g.setFill(javafx.scene.paint.Color.web("#6c5ce7"));
            g.fillRect(
                    zoomPan.sx(b.xMin()), zoomPan.sy(b.yMax()),
                    Math.abs(zoomPan.sx(b.xMax()) - zoomPan.sx(b.xMin())),
                    Math.abs(zoomPan.sy(b.yMin()) - zoomPan.sy(b.yMax()))
            );
            g.setGlobalAlpha(1.0);
        }

        // split line
        if (f != null && f.split != null) {
            g.setStroke(javafx.scene.paint.Color.web("#636e72"));
            g.setLineDashes(8, 8);
            g.setLineWidth(1.4);
            var s = f.split;
            var mp = s.getMidP();
            var d = s.getDir();
            double x1 = mp.x() - d.x() * 5000, y1 = mp.y() - d.y() * 5000;
            double x2 = mp.x() + d.x() * 5000, y2 = mp.y() + d.y() * 5000;
            g.strokeLine(zoomPan.sx(x1), zoomPan.sy(y1), zoomPan.sx(x2), zoomPan.sy(y2));
            g.setLineDashes();
        }

        // left/right points
        g.setFill(javafx.scene.paint.Color.web("#0984e3"));
        if (f != null && f.leftPts != null) {
            for (Point p : f.leftPts) {
                g.fillOval(zoomPan.sx(p.x()) - 3, zoomPan.sy(p.y()) - 3, 6, 6);
            }
        }
        g.setFill(javafx.scene.paint.Color.web("#d63031"));
        if (f != null && f.rightPts != null) {
            for (Point p : f.rightPts) {
                g.fillOval(zoomPan.sx(p.x()) - 3, zoomPan.sy(p.y()) - 3, 6, 6);
            }
        }

        // pivot
        if (f != null && f.pivot != null) {
            var p = f.pivot;
            g.setFill(javafx.scene.paint.Color.web("#f2c94c"));
            g.fillOval(zoomPan.sx(p.x()) - 4, zoomPan.sy(p.y()) - 4, 8, 8);
        }

        // merge edges
        g.setStroke(javafx.scene.paint.Color.web("#2f80ed"));
        g.setLineWidth(2.0);
        if (f != null && f.edges != null) {
            for (asi.voronoi.Line ln : f.edges) {
                drawLineSegment(g, ln);
            }
        }

        // support marks
        if (f != null && f.marks != null && !f.marks.isEmpty()) {
            g.setFill(javafx.scene.paint.Color.web("#00b894"));
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

    // ---------- minimap interactions ----------
    private boolean miniDraggingRect = false;
    private double miniDragStartX = 0, miniDragStartY = 0;
    private double miniViewStartXmin, miniViewStartXmax, miniViewStartYmin, miniViewStartYmax;

    private void installMinimapInteractions() {
        Canvas minimap = minimapView.getCanvas();
        minimap.setPickOnBounds(false);
        minimap.setOnMousePressed(ev -> {
            if (!minimapView.getCanvas().isVisible()) {
                return;
            }
            var pt = minimapToWorld(ev.getX(), ev.getY());
            if (pt == null) {
                return;
            }

            if (isInsideViewRect(pt.x, pt.y)) {
                miniDraggingRect = true;
                miniDragStartX = ev.getX();
                miniDragStartY = ev.getY();
                miniViewStartXmin = zoomPan.getViewXmin();
                miniViewStartXmax = zoomPan.getViewXmax();
                miniViewStartYmin = zoomPan.getViewYmin();
                miniViewStartYmax = zoomPan.getViewYmax();
            } else {
                zoomPan.centerViewOn(pt.x, pt.y);
            }
        });

        minimap.setOnMouseDragged(ev -> {
            if (!miniDraggingRect) {
                return;
            }
            var p0 = minimapToWorld(miniDragStartX, miniDragStartY);
            var p1 = minimapToWorld(ev.getX(), ev.getY());
            if (p0 == null || p1 == null) {
                return;
            }
            double dx = p1.x - p0.x;
            double dy = p1.y - p0.y;

            double newXmin = miniViewStartXmin + dx;
            double newXmax = miniViewStartXmax + dx;
            double newYmin = miniViewStartYmin + dy;
            double newYmax = miniViewStartYmax + dy;

            double cx = (newXmin + newXmax) / 2;
            double cy = (newYmin + newYmax) / 2;
            zoomPan.centerViewOn(cx, cy);
        });

        minimap.setOnMouseReleased(ev -> miniDraggingRect = false);

        minimap.setOnScroll(ev -> {
            var pt = minimapToWorld(ev.getX(), ev.getY());
            if (pt == null) {
                return;
            }
            double factor = (ev.getDeltaY() > 0) ? 1.2 : (1 / 1.2);
            zoomAtWorldPoint(factor, pt.x, pt.y);
            ev.consume();
        });
    }

    private WPoint minimapToWorld(double mx, double my) {
        WorldBounds wb = zoomPan.getWorldBounds();
        if (wb == null) {
            return null;
        }
        double contentW = Math.max(1, miniW - 2 * miniPad);
        double contentH = Math.max(1, miniH - 2 * miniPad);
        double wW = wb.xmax - wb.xmin;
        double wH = wb.ymax - wb.ymin;
        if (wW <= 0 || wH <= 0) {
            return null;
        }
        double sx = contentW / wW;
        double sy = contentH / wH;
        double mmScale = Math.min(sx, sy);
        double mmOx = miniPad + (contentW - wW * mmScale) / 2.0;
        double mmOy = miniPad + (contentH - wH * mmScale) / 2.0;

        double contentLeft = mmOx;
        double contentTop = mmOy;
        double contentRight = miniW - mmOx;
        double contentBottom = miniH - mmOy;
        if (mx < contentLeft || mx > contentRight || my < (miniH - contentBottom) || my > (miniH - contentTop)) {
            return null;
        }

        double wx = wb.xmin + (mx - mmOx) / mmScale;
        double wy = wb.ymin + ((miniH - my) - mmOy) / mmScale;
        return new WPoint(wx, wy);
    }

    private static class WPoint {

        double x, y;

        WPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private boolean isInsideViewRect(double wx, double wy) {
        return (wx >= zoomPan.getViewXmin() && wx <= zoomPan.getViewXmax() && wy >= zoomPan.getViewYmin() && wy <= zoomPan.getViewYmax());
    }

    private void zoomAtWorldPoint(double factor, double wx, double wy) {
        zoomPan.zoomAtWorldPoint(factor, wx, wy);
    }

    // ---------- compute world (same logic as original) ----------
    private WorldBounds computeWorld(List<StoryboardRecorder.Frame> frames) {
        double xmin = Double.POSITIVE_INFINITY, ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
        if (frames != null) {
            for (var f : frames) {
                if (f.bbox != null) {
                    xmin = Math.min(xmin, f.bbox.xMin());
                    xmax = Math.max(xmax, f.bbox.xMax());
                    ymin = Math.min(ymin, f.bbox.yMin());
                    ymax = Math.max(ymax, f.bbox.yMax());
                }
                if (f.leftPts != null) {
                    for (var p : f.leftPts) {
                        xmin = Math.min(xmin, p.x());
                        xmax = Math.max(xmax, p.x());
                        ymin = Math.min(ymin, p.y());
                        ymax = Math.max(ymax, p.y());
                    }
                }
                if (f.rightPts != null) {
                    for (var p : f.rightPts) {
                        xmin = Math.min(xmin, p.x());
                        xmax = Math.max(xmax, p.x());
                        ymin = Math.min(ymin, p.y());
                        ymax = Math.max(ymax, p.y());
                    }
                }
                if (f.pivot != null) {
                    var p = f.pivot;
                    xmin = Math.min(xmin, p.x());
                    xmax = Math.max(xmax, p.x());
                    ymin = Math.min(ymin, p.y());
                    ymax = Math.max(ymax, p.y());
                }
                if (f.marks != null) {
                    for (var p : f.marks) {
                        xmin = Math.min(xmin, p.x());
                        xmax = Math.max(xmax, p.x());
                        ymin = Math.min(ymin, p.y());
                        ymax = Math.max(ymax, p.y());
                    }
                }
                if (f.edges != null) {
                    for (asi.voronoi.Line ln : f.edges) {
                        var b = ln.getBeginP().orElse(null);
                        var e = ln.getEndP().orElse(null);
                        var m = ln.getMidP();
                        if (b != null) {
                            xmin = Math.min(xmin, b.x());
                            xmax = Math.max(xmax, b.x());
                            ymin = Math.min(ymin, b.y());
                            ymax = Math.max(ymax, b.y());
                        }
                        if (e != null) {
                            xmin = Math.min(xmin, e.x());
                            xmax = Math.max(xmax, e.x());
                            ymin = Math.min(ymin, e.y());
                            ymax = Math.max(ymax, e.y());
                        }
                        if (b == null && e == null && m != null) {
                            xmin = Math.min(xmin, m.x());
                            xmax = Math.max(xmax, m.x());
                            ymin = Math.min(ymin, m.y());
                            ymax = Math.max(ymax, m.y());
                        }
                    }
                }
                if (f.split != null) {
                    var mp = f.split.getMidP();
                    xmin = Math.min(xmin, mp.x());
                    xmax = Math.max(xmax, mp.x());
                    ymin = Math.min(ymin, mp.y());
                    ymax = Math.max(ymax, mp.y());
                }
            }
        }
        for (var p : allSites) {
            xmin = Math.min(xmin, p.x());
            xmax = Math.max(xmax, p.x());
            ymin = Math.min(ymin, p.y());
            ymax = Math.max(ymax, p.y());
        }
        if (!Double.isFinite(xmin) || xmin == xmax || ymin == ymax) {
            xmin = 0;
            ymin = 0;
            xmax = 1;
            ymax = 1;
        }
        return new WorldBounds(xmin, ymin, xmax, ymax);
    }
}
