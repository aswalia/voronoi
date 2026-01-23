package asi.voronoi.javafx;

import asi.voronoi.Point;
import asi.voronoi.anim.StoryboardRecorder;
import asi.voronoi.tree.BinaryTree;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Refactored animation view that composes ZoomPanController, Animator, MinimapView
 * for visualizing Voronoi diagrams and Binary Trees.
 * Provides a clean separation of concerns while preserving all functionality.
 */
class AnimationView extends Pane implements MinimapView.MinimapInteractionHandler {
    public enum Mode { NONE, VORONOI, BINARY_TREE }
    public enum MinimapPos { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private Mode mode = Mode.NONE;

    // Main components
    private final Canvas canvas;
    private final ZoomPanController zoomPanController;
    private final Animator animator;
    private final MinimapView minimapView;

    // Data
    private List<Point> allSites = List.of();
    
    // Point capture mode
    private boolean captureMode = false;
    private final List<Point> capturedPoints = new ArrayList<>();
    private IntConsumer onCapturedCountChanged;

    // Pan state
    private double lastMouseX = Double.NaN;
    private double lastMouseY = Double.NaN;
    private boolean panning = false;

    // BinaryTree visualization
    private static final Color COLOR_ENTER = Color.web("#FFC107");
    private static final Color COLOR_VISIT = Color.web("#6495ED");
    private static final Color COLOR_EXIT = Color.web("#90EE90");
    private static final Color COLOR_EDGE = Color.web("#FF4500");
    private static final Color COLOR_NODE_DEFAULT = Color.web("#D3D3D3");
    private static final Color COLOR_NODE_STROKE = Color.web("#555555");
    private static final Color COLOR_EDGE_DEFAULT = Color.web("#808080");
    private static final double LINE_EXTENSION_LENGTH = 5000; // Length for infinite/semi-infinite line segments

    private final Group btEdgesLayer = new Group();
    private final Group btNodesLayer = new Group();
    private final Group btGroup = new Group(btEdgesLayer, btNodesLayer);
    private final Map<String, Circle> btNodeById = new HashMap<>();
    private final Map<String, Line> btEdgeByKey = new HashMap<>();
    private javafx.animation.Timeline btTimeline;
    private int btIdx = 0;
    private List<StoryboardRecorder.BtFrame> btFrames = List.of();

    private final HBox btLegend = new HBox(12);
    private Consumer<String> btStatusSink = null;

    AnimationView() {
        canvas = new Canvas(900, 700);
        zoomPanController = new ZoomPanController();
        animator = new Animator();
        minimapView = new MinimapView();

        getChildren().addAll(canvas, minimapView.getCanvas());
        getChildren().addAll(btGroup);

        // Set up legend
        btLegend.setAlignment(Pos.CENTER_LEFT);
        btLegend.setPadding(new Insets(8, 10, 8, 10));
        btLegend.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 8;");
        btLegend.getChildren().setAll(
            legendItem(COLOR_ENTER, "ENTER"),
            legendItem(COLOR_VISIT, "VISIT"),
            legendItem(COLOR_EXIT, "EXIT")
        );
        btLegend.setLayoutX(8);
        btLegend.setLayoutY(8);
        getChildren().add(btLegend);

        // Wire up components
        animator.setOnFrame(this::onAnimatorFrame);
        zoomPanController.setOnViewChanged(this::redrawCurrent);
        minimapView.setInteractionHandler(this);

        widthProperty().addListener((o, ov, nv) -> resizeChildren());
        heightProperty().addListener((o, ov, nv) -> resizeChildren());

        installInteractions();
        setPrefSize(900, 600);
    }

    // ========== PUBLIC API ==========

    void setSites(List<Point> pts) {
        this.allSites = new ArrayList<>(pts);
    }

    void setFrames(List<StoryboardRecorder.Frame> frames) {
        animator.setFrames(frames);
        WorldBounds world = computeWorldBounds(frames, allSites);
        zoomPanController.setWorldBounds(world);
        redrawCurrent();
    }

    void play() {
        animator.play();
    }

    void pause() {
        animator.pause();
    }

    void resume() {
        animator.resume();
    }

    void stop() {
        animator.stop();
    }

    void stepForward() {
        animator.stepForward();
    }

    void stepBack() {
        animator.stepBack();
    }

    void setSpeed(double rate) {
        animator.setSpeed(rate);
    }

    double getZoomPercent() {
        return zoomPanController.getZoomPercent();
    }

    void zoomAtCenter(double factor) {
        zoomPanController.zoomAtCenter(factor);
    }

    void resetView() {
        zoomPanController.resetView();
    }

    void fitToData() {
        zoomPanController.fitToData();
    }

    void exportPngs(File dir, String prefix) throws Exception {
        if (animator.getFrameCount() == 0) {
            return;
        }
        if (!dir.exists()) {
            java.nio.file.Files.createDirectories(dir.toPath());
        }
        GraphicsContext g = canvas.getGraphicsContext2D();
        boolean wasPlaying = animator.isPlaying();
        stop();
        for (int i = 0; i < animator.getFrameCount(); i++) {
            StoryboardRecorder.Frame frame = animator.getFrames().get(i);
            drawFrame(g, frame);
            javafx.scene.image.WritableImage img = canvas.snapshot(new javafx.scene.SnapshotParameters(), null);
            File out = new File(dir, String.format("%s_%04d.png", prefix, i + 1));
            javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(img, null), "png", out);
        }
        if (wasPlaying) {
            play();
        }
    }

    // Point capture API
    void startPointCapture() {
        captureMode = true;
        capturedPoints.clear();
        redrawCurrent();
    }

    void stopPointCapture() {
        captureMode = false;
        redrawCurrent();
    }

    void clearCapturedPoints() {
        capturedPoints.clear();
        redrawCurrent();
    }

    int getCapturedSize() {
        return capturedPoints.size();
    }

    boolean undoCapturedPoint() {
        if (!capturedPoints.isEmpty()) {
            capturedPoints.remove(capturedPoints.size() - 1);
            redrawCurrent();
            return true;
        }
        return false;
    }

    List<Point> getCapturedPoints() {
        return new ArrayList<>(capturedPoints);
    }

    void setOnCapturedCountChanged(IntConsumer cb) {
        this.onCapturedCountChanged = cb;
    }

    // Minimap API
    void setMinimapEnabled(boolean enabled) {
        minimapView.setEnabled(enabled);
        redrawCurrent();
    }

    void setMinimapPosition(MinimapPos pos) {
        MinimapView.Position p = switch (pos) {
            case TOP_LEFT -> MinimapView.Position.TOP_LEFT;
            case TOP_RIGHT -> MinimapView.Position.TOP_RIGHT;
            case BOTTOM_LEFT -> MinimapView.Position.BOTTOM_LEFT;
            case BOTTOM_RIGHT -> MinimapView.Position.BOTTOM_RIGHT;
        };
        minimapView.setPosition(p);
        minimapView.layoutMinimap(getWidth(), getHeight());
        redrawCurrent();
    }

    void setMinimapSize(double w, double h) {
        minimapView.setSize(w, h);
        minimapView.layoutMinimap(getWidth(), getHeight());
        redrawCurrent();
    }

    // Visualization mode API
    void setVisualizationMode(Mode m) {
        if (m == mode) return;
        switch (m) {
            case BINARY_TREE -> clearVoronoiView();
            case VORONOI -> clearBinaryTreeView();
            case NONE -> clearAllViews();
        }
        mode = m;
    }

    // ========== BINARY TREE API ==========

    void setBtStatusSink(Consumer<String> sink) {
        this.btStatusSink = sink;
    }

    void renderBinaryTree(BinaryTree root) {
        btEdgesLayer.getChildren().clear();
        btNodesLayer.getChildren().clear();
        btNodeById.clear();
        btEdgeByKey.clear();

        Map<String, BtPos> pos = layoutTree(root);

        for (Map.Entry<String, BtPos> e : pos.entrySet()) {
            String id = e.getKey();
            BtPos p = e.getValue();
            Circle c = new Circle(p.x, p.y, 14, COLOR_NODE_DEFAULT);
            c.setStroke(COLOR_NODE_STROKE);
            btNodeById.put(id, c);
            btNodesLayer.getChildren().add(c);
            if (p.label != null) {
                Label lab = new Label(p.label);
                lab.setStyle("-fx-font-size: 11;");
                lab.setLayoutX(p.x + 18);
                lab.setLayoutY(p.y - 8);
                btNodesLayer.getChildren().add(lab);
            }
        }

        for (BtEdge edge : computeEdges(root)) {
            BtPos p1 = pos.get(edge.parentId);
            BtPos p2 = pos.get(edge.childId);
            if (p1 == null || p2 == null) continue;
            Line l = new Line(p1.x, p1.y, p2.x, p2.y);
            l.setStroke(COLOR_EDGE_DEFAULT);
            btEdgesLayer.getChildren().add(l);
            btEdgeByKey.put(edgeKey(edge.parentId, edge.childId), l);
        }
    }

    void resetBinaryTreeColors() {
        btNodeById.values().forEach(c -> {
            c.setFill(COLOR_NODE_DEFAULT);
            c.setStroke(COLOR_NODE_STROKE);
        });
        btEdgeByKey.values().forEach(l -> l.setStroke(COLOR_EDGE_DEFAULT));
        if (btStatusSink != null) btStatusSink.accept("");
    }

    void clearBinaryTreeView() {
        stopBinaryTreeStoryboard();
        btEdgesLayer.getChildren().clear();
        btNodesLayer.getChildren().clear();
        btNodeById.clear();
        btEdgeByKey.clear();
        btFrames = List.of();
        btGroup.setScaleX(1.0);
        btGroup.setScaleY(1.0);
        btGroup.setTranslateX(0);
        btGroup.setTranslateY(0);
        if (btStatusSink != null) btStatusSink.accept("");
    }

    void clearVoronoiView() {
        animator.setFrames(List.of());
    }

    void clearAllViews() {
        clearBinaryTreeView();
        clearVoronoiView();
    }

    void playBinaryTreeStoryboard(StoryboardRecorder recorder, double fps) {
        stopBinaryTreeStoryboard();
        this.btFrames = new ArrayList<>(recorder.getBtFrames());
        if (btFrames.isEmpty()) return;
        btIdx = 0;
        javafx.util.Duration interval = javafx.util.Duration.millis(1000.0 / Math.max(1.0, fps));
        btTimeline = new javafx.animation.Timeline(new javafx.animation.KeyFrame(interval, ev -> {
            if (btIdx >= btFrames.size()) {
                stopBinaryTreeStoryboard();
                return;
            }
            var f = btFrames.get(btIdx);
            applyBtFrame(f);
            updateBtStatus(btIdx, btFrames.size(), f);
            btIdx++;
        }));
        btTimeline.setCycleCount(btFrames.size());
        btTimeline.playFromStart();
    }

    void pauseBinaryTreeStoryboard() {
        if (btTimeline != null) btTimeline.pause();
    }

    void resumeBinaryTreeStoryboard() {
        if (btTimeline != null) btTimeline.play();
    }

    void stopBinaryTreeStoryboard() {
        if (btTimeline != null) {
            btTimeline.stop();
            btTimeline = null;
        }
    }

    void stepBinaryTreeStoryboard(StoryboardRecorder recorder) {
        if (btFrames == null || btFrames.isEmpty()) return;
        if (btIdx < btFrames.size()) {
            var f = btFrames.get(btIdx);
            applyBtFrame(f);
            updateBtStatus(btIdx, btFrames.size(), f);
            btIdx++;
        }
    }

    void stepBackBinaryTreeStoryboard(StoryboardRecorder recorder) {
        if (btFrames == null || btFrames.isEmpty()) return;
        if (btIdx <= 0) return;
        btIdx--;
        resetBinaryTreeColors();
        for (int i = 0; i < btIdx; i++) {
            applyBtFrame(btFrames.get(i));
        }
        if (btIdx > 0) {
            updateBtStatus(btIdx - 1, btFrames.size(), btFrames.get(btIdx - 1));
        } else {
            if (btStatusSink != null) btStatusSink.accept("");
        }
    }

    void setBinaryTreeRate(double rate) {
        if (btTimeline != null) btTimeline.setRate(rate);
    }

    void fitBinaryTreeToView() {
        fitBinaryTreeToView(40.0);
    }

    void fitBinaryTreeToView(double margin) {
        if (btNodeById.isEmpty()) return;
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (var node : btNodeById.values()) {
            double x = node.getCenterX();
            double y = node.getCenterY();
            double r = node.getRadius();
            minX = Math.min(minX, x - r);
            minY = Math.min(minY, y - r);
            maxX = Math.max(maxX, x + r);
            maxY = Math.max(maxY, y + r);
        }
        if (minX == Double.POSITIVE_INFINITY) return;
        double contentW = Math.max(1, maxX - minX);
        double contentH = Math.max(1, maxY - minY);
        double paneW = Math.max(1, getWidth() > 0 ? getWidth() : getPrefWidth());
        double paneH = Math.max(1, getHeight() > 0 ? getHeight() : getPrefHeight());
        double availW = Math.max(1, paneW - 2 * margin);
        double availH = Math.max(1, paneH - 2 * margin);
        double s = Math.min(availW / contentW, availH / contentH);
        if (!Double.isFinite(s) || s <= 0) s = 1.0;
        btGroup.setScaleX(s);
        btGroup.setScaleY(s);
        double cx = paneW / 2.0;
        double cy = paneH / 2.0;
        double targetLeft = cx - (contentW * s) / 2.0;
        double targetTop = cy - (contentH * s) / 2.0;
        btGroup.setTranslateX(targetLeft - (minX * s));
        btGroup.setTranslateY(targetTop - (minY * s));
        redrawCurrent();
    }

    // ========== MinimapInteractionHandler implementation ==========

    @Override
    public boolean isInsideViewRect(double wx, double wy) {
        return zoomPanController.isInsideViewRect(wx, wy);
    }

    @Override
    public void centerViewOn(double wx, double wy) {
        zoomPanController.centerViewOn(wx, wy);
    }

    @Override
    public void setViewBounds(double xmin, double xmax, double ymin, double ymax) {
        // Direct manipulation of view bounds (for minimap drag)
        // This bypasses the normal zoom/pan methods for performance
        WorldBounds world = zoomPanController.getWorldBounds();
        if (world != null) {
            zoomPanController.centerViewOn((xmin + xmax) / 2, (ymin + ymax) / 2);
            // Adjust zoom to match the width/height
            double currentW = zoomPanController.getViewXmax() - zoomPanController.getViewXmin();
            double targetW = xmax - xmin;
            if (targetW > 0 && currentW > 0) {
                double factor = currentW / targetW;
                zoomPanController.zoomAtCenter(factor);
            }
        }
    }

    @Override
    public void zoomAtWorldPoint(double factor, double wx, double wy) {
        zoomPanController.zoomAtWorldPoint(factor, wx, wy);
    }

    @Override
    public double getViewXmin() {
        return zoomPanController.getViewXmin();
    }

    @Override
    public double getViewXmax() {
        return zoomPanController.getViewXmax();
    }

    @Override
    public double getViewYmin() {
        return zoomPanController.getViewYmin();
    }

    @Override
    public double getViewYmax() {
        return zoomPanController.getViewYmax();
    }

    @Override
    public ZoomPanController getZoomPanController() {
        return zoomPanController;
    }

    // ========== LAYOUT ==========

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        resizeChildren();
    }

    private void resizeChildren() {
        double w = Math.max(1, getWidth());
        double h = Math.max(1, getHeight());
        if (canvas.getWidth() != w || canvas.getHeight() != h) {
            canvas.setWidth(w);
            canvas.setHeight(h);
            zoomPanController.setCanvasSize(w, h);
            redrawCurrent();
        }
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);
        minimapView.layoutMinimap(w, h);
        drawMinimap();
    }

    // ========== INTERACTIONS ==========

    private void installInteractions() {
        canvas.setOnScroll(ev -> {
            double factor = (ev.getDeltaY() > 0) ? 1.2 : (1 / 1.2);
            zoomPanController.zoomAt(factor, ev.getX(), ev.getY());
            ev.consume();
        });

        canvas.setOnMouseClicked(ev -> {
            if (captureMode) {
                if (ev.getButton() == MouseButton.PRIMARY) {
                    double wx = zoomPanController.screenToWorldX(ev.getX());
                    double wy = zoomPanController.screenToWorldY(ev.getY());
                    capturedPoints.add(new Point(wx, wy));
                    if (onCapturedCountChanged != null) {
                        onCapturedCountChanged.accept(capturedPoints.size());
                    }
                    redrawCurrent();
                    ev.consume();
                } else if (ev.getButton() == MouseButton.SECONDARY) {
                    if (!capturedPoints.isEmpty()) {
                        capturedPoints.remove(capturedPoints.size() - 1);
                        if (onCapturedCountChanged != null) {
                            onCapturedCountChanged.accept(capturedPoints.size());
                        }
                        redrawCurrent();
                    }
                    ev.consume();
                }
            }
        });

        canvas.setOnMousePressed(ev -> {
            if (captureMode) {
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
            if (captureMode) {
                return;
            }
            if (panning) {
                double dx = ev.getX() - lastMouseX;
                double dy = ev.getY() - lastMouseY;
                zoomPanController.panByScreen(dx, dy);
                lastMouseX = ev.getX();
                lastMouseY = ev.getY();
            }
        });

        canvas.setOnMouseReleased(ev -> panning = false);
        canvas.setOnMouseEntered(ev -> canvas.setCursor(javafx.scene.Cursor.OPEN_HAND));
        canvas.setOnMouseExited(ev -> canvas.setCursor(javafx.scene.Cursor.DEFAULT));

        sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                newS.setOnKeyPressed(ev -> {
                    switch (ev.getCode()) {
                        case PLUS, ADD -> zoomAtCenter(1.2);
                        case MINUS, SUBTRACT -> zoomAtCenter(1 / 1.2);
                        case DIGIT0 -> resetView();
                        case F -> fitToData();
                    }
                });
            }
        });
    }

    // ========== DRAWING ==========

    private void onAnimatorFrame(int frameIndex) {
        StoryboardRecorder.Frame frame = animator.getCurrentFrame();
        if (frame != null) {
            Main.frameStatus.setValue(frame.label);
            drawFrame(canvas.getGraphicsContext2D(), frame);
            drawMinimap();
        }
    }

    private void redrawCurrent() {
        StoryboardRecorder.Frame currentFrame = animator.getCurrentFrame();
        if (currentFrame == null && animator.getFrameCount() == 0) {
            GraphicsContext g = canvas.getGraphicsContext2D();
            g.setFill(Color.WHITE);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            if (!capturedPoints.isEmpty()) {
                g.setFill(Color.web("#a100ff"));
                for (Point s : capturedPoints) {
                    g.fillOval(zoomPanController.sx(s.x()) - 3.5, zoomPanController.sy(s.y()) - 3.5, 7, 7);
                }
            }
            drawMinimap();
            return;
        }
        if (currentFrame != null) {
            drawFrame(canvas.getGraphicsContext2D(), currentFrame);
        }
        drawMinimap();
    }

    private void drawFrame(GraphicsContext g, StoryboardRecorder.Frame f) {
        double W = canvas.getWidth();
        double H = canvas.getHeight();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, W, H);

        // All sites overlay
        g.setFill(Color.web("#444444"));
        g.setGlobalAlpha(0.65);
        for (Point s : allSites) {
            g.fillOval(zoomPanController.sx(s.x()) - 2.5, zoomPanController.sy(s.y()) - 2.5, 5, 5);
        }
        g.setGlobalAlpha(1.0);

        // Division bbox
        if (f.bbox != null) {
            asi.voronoi.anim.Rect b = f.bbox;
            g.setGlobalAlpha(0.08);
            g.setFill(Color.web("#6c5ce7"));
            g.fillRect(zoomPanController.sx(b.xMin()), zoomPanController.sy(b.yMax()),
                    Math.abs(zoomPanController.sx(b.xMax()) - zoomPanController.sx(b.xMin())),
                    Math.abs(zoomPanController.sy(b.yMin()) - zoomPanController.sy(b.yMax())));
            g.setGlobalAlpha(1.0);
        }

        // Split line
        if (f.split != null) {
            g.setStroke(Color.web("#636e72"));
            g.setLineDashes(8, 8);
            g.setLineWidth(1.4);
            asi.voronoi.Line s = f.split;
            Point mp = s.getMidP();
            Point d = s.getDir();
            double x1 = mp.x() - d.x() * LINE_EXTENSION_LENGTH;
            double y1 = mp.y() - d.y() * LINE_EXTENSION_LENGTH;
            double x2 = mp.x() + d.x() * LINE_EXTENSION_LENGTH;
            double y2 = mp.y() + d.y() * LINE_EXTENSION_LENGTH;
            g.strokeLine(zoomPanController.sx(x1), zoomPanController.sy(y1),
                    zoomPanController.sx(x2), zoomPanController.sy(y2));
            g.setLineDashes();
        }

        // Left/right points
        g.setFill(Color.web("#0984e3"));
        if (f.leftPts != null) {
            for (Point p : f.leftPts) {
                g.fillOval(zoomPanController.sx(p.x()) - 3, zoomPanController.sy(p.y()) - 3, 6, 6);
            }
        }
        g.setFill(Color.web("#d63031"));
        if (f.rightPts != null) {
            for (Point p : f.rightPts) {
                g.fillOval(zoomPanController.sx(p.x()) - 3, zoomPanController.sy(p.y()) - 3, 6, 6);
            }
        }

        // Pivot
        if (f.pivot != null) {
            Point p = f.pivot;
            g.setFill(Color.web("#f2c94c"));
            g.fillOval(zoomPanController.sx(p.x()) - 4, zoomPanController.sy(p.y()) - 4, 8, 8);
        }

        // Merge edges
        g.setStroke(Color.web("#2f80ed"));
        g.setLineWidth(2.0);
        if (f.edges != null) {
            for (asi.voronoi.Line ln : f.edges) {
                drawLineSegment(g, ln);
            }
        }

        // Support marks
        if (f.marks != null && !f.marks.isEmpty()) {
            g.setFill(Color.web("#00b894"));
            for (Point p : f.marks) {
                g.fillOval(zoomPanController.sx(p.x()) - 4, zoomPanController.sy(p.y()) - 4, 8, 8);
            }
        }

        // Captured points overlay
        if (captureMode && !capturedPoints.isEmpty()) {
            g.setFill(Color.web("#a100ff"));
            for (Point s : capturedPoints) {
                g.fillOval(zoomPanController.sx(s.x()) - 3.5, zoomPanController.sy(s.y()) - 3.5, 7, 7);
            }
        }
    }

    private void drawLineSegment(GraphicsContext g, asi.voronoi.Line ln) {
        Point b = ln.getBeginP().orElse(null);
        Point e = ln.getEndP().orElse(null);
        if (b != null && e != null) {
            g.strokeLine(zoomPanController.sx(b.x()), zoomPanController.sy(b.y()),
                    zoomPanController.sx(e.x()), zoomPanController.sy(e.y()));
        } else if (b != null) {
            Point d = ln.getDir();
            double x2 = b.x() + d.x() * LINE_EXTENSION_LENGTH;
            double y2 = b.y() + d.y() * LINE_EXTENSION_LENGTH;
            g.strokeLine(zoomPanController.sx(b.x()), zoomPanController.sy(b.y()),
                    zoomPanController.sx(x2), zoomPanController.sy(y2));
        } else if (e != null) {
            Point d = ln.getDir();
            double x1 = e.x() - d.x() * LINE_EXTENSION_LENGTH;
            double y1 = e.y() - d.y() * LINE_EXTENSION_LENGTH;
            g.strokeLine(zoomPanController.sx(x1), zoomPanController.sy(y1),
                    zoomPanController.sx(e.x()), zoomPanController.sy(e.y()));
        } else {
            Point m = ln.getMidP();
            Point d = ln.getDir();
            double x1 = m.x() - d.x() * LINE_EXTENSION_LENGTH;
            double y1 = m.y() - d.y() * LINE_EXTENSION_LENGTH;
            double x2 = m.x() + d.x() * LINE_EXTENSION_LENGTH;
            double y2 = m.y() + d.y() * LINE_EXTENSION_LENGTH;
            g.strokeLine(zoomPanController.sx(x1), zoomPanController.sy(y1),
                    zoomPanController.sx(x2), zoomPanController.sy(y2));
        }
    }

    private void drawMinimap() {
        minimapView.drawMinimap(allSites, zoomPanController, animator.getCurrentFrame());
    }

    // ========== BINARY TREE HELPERS ==========

    private record BtPos(double x, double y, String label) {}
    private record BtEdge(String parentId, String childId) {}

    private HBox legendItem(Color color, String text) {
        Circle dot = new Circle(7, color);
        dot.setStroke(COLOR_NODE_STROKE);
        var label = new Label(text);
        label.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        var box = new HBox(6, dot, label);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void applyBtFrame(StoryboardRecorder.BtFrame f) {
        Runnable r = () -> {
            switch (f.type()) {
                case ENTER_NODE -> highlightNode(f.nodeId(), COLOR_ENTER);
                case VISIT_NODE -> highlightNode(f.nodeId(), COLOR_VISIT);
                case EXIT_NODE -> highlightNode(f.nodeId(), COLOR_EXIT);
                case HIGHLIGHT_EDGE -> highlightEdge(f.parentId(), f.nodeId(), COLOR_EDGE);
            }
        };
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }

    private void updateBtStatus(int idx, int total, StoryboardRecorder.BtFrame f) {
        if (btStatusSink == null) return;
        String label = f.label() != null ? f.label() : f.nodeId();
        String txt = String.format("Frame %d/%d – %s %s", idx + 1, total, f.type(), label);
        btStatusSink.accept(txt);
    }

    private void highlightNode(String nodeId, Color color) {
        Circle c = btNodeById.get(nodeId);
        if (c != null) c.setFill(color);
    }

    private void highlightEdge(String parentId, String childId, Color color) {
        if (parentId == null || childId == null) return;
        Line l = btEdgeByKey.get(edgeKey(parentId, childId));
        if (l != null) l.setStroke(color);
    }

    private String edgeKey(String parentId, String childId) {
        return parentId + "->" + childId;
    }

    private Map<String, BtPos> layoutTree(BinaryTree root) {
        final double X_SPACING = 52, Y_SPACING = 70, X0 = 40, Y0 = 40;
        Map<String, BtPos> pos = new LinkedHashMap<>();
        int[] k = {0};
        layoutDfs(root, null, 0, pos, k, X_SPACING, Y_SPACING, X0, Y0);
        return pos;
    }

    private void layoutDfs(BinaryTree n, BinaryTree parent, int depth, Map<String, BtPos> pos,
                           int[] k, double xs, double ys, double x0, double y0) {
        if (n == null) return;
        layoutDfs(n.lft(), n, depth + 1, pos, k, xs, ys, x0, y0);
        String id = btNodeId(n);
        String label = (n.getP() != null ? n.getP().toString() : null);
        double x = x0 + k[0] * xs;
        double y = y0 + depth * ys;
        pos.put(id, new BtPos(x, y, label));
        k[0]++;
        layoutDfs(n.rgt(), n, depth + 1, pos, k, xs, ys, x0, y0);
    }

    private List<BtEdge> computeEdges(BinaryTree root) {
        List<BtEdge> out = new ArrayList<>();
        computeEdgesDfs(root, null, out);
        return out;
    }

    private void computeEdgesDfs(BinaryTree n, BinaryTree parent, List<BtEdge> out) {
        if (n == null) return;
        if (parent != null) out.add(new BtEdge(btNodeId(parent), btNodeId(n)));
        computeEdgesDfs(n.lft(), n, out);
        computeEdgesDfs(n.rgt(), n, out);
    }

    private String btNodeId(BinaryTree n) {
        if (n != null && n.getP() != null) {
            return "P(" + n.getP().x() + "," + n.getP().y() + ")";
        }
        return "n@" + System.identityHashCode(n);
    }

    // ========== WORLD BOUNDS COMPUTATION ==========

    private WorldBounds computeWorldBounds(List<StoryboardRecorder.Frame> frames, List<Point> sites) {
        double xmin = Double.POSITIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;

        for (StoryboardRecorder.Frame f : frames) {
            if (f.bbox != null) {
                xmin = Math.min(xmin, f.bbox.xMin());
                xmax = Math.max(xmax, f.bbox.xMax());
                ymin = Math.min(ymin, f.bbox.yMin());
                ymax = Math.max(ymax, f.bbox.yMax());
            }
            if (f.leftPts != null) {
                for (Point p : f.leftPts) {
                    xmin = Math.min(xmin, p.x());
                    xmax = Math.max(xmax, p.x());
                    ymin = Math.min(ymin, p.y());
                    ymax = Math.max(ymax, p.y());
                }
            }
            if (f.rightPts != null) {
                for (Point p : f.rightPts) {
                    xmin = Math.min(xmin, p.x());
                    xmax = Math.max(xmax, p.x());
                    ymin = Math.min(ymin, p.y());
                    ymax = Math.max(ymax, p.y());
                }
            }
            if (f.pivot != null) {
                Point p = f.pivot;
                xmin = Math.min(xmin, p.x());
                xmax = Math.max(xmax, p.x());
                ymin = Math.min(ymin, p.y());
                ymax = Math.max(ymax, p.y());
            }
            if (f.marks != null) {
                for (Point p : f.marks) {
                    xmin = Math.min(xmin, p.x());
                    xmax = Math.max(xmax, p.x());
                    ymin = Math.min(ymin, p.y());
                    ymax = Math.max(ymax, p.y());
                }
            }
            if (f.edges != null) {
                for (asi.voronoi.Line ln : f.edges) {
                    Point b = ln.getBeginP().orElse(null);
                    Point e = ln.getEndP().orElse(null);
                    Point m = ln.getMidP();
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
                Point mp = f.split.getMidP();
                xmin = Math.min(xmin, mp.x());
                xmax = Math.max(xmax, mp.x());
                ymin = Math.min(ymin, mp.y());
                ymax = Math.max(ymax, mp.y());
            }
        }

        for (Point p : sites) {
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
