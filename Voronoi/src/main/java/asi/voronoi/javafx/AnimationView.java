package asi.voronoi.javafx;

import asi.voronoi.Point;
import asi.voronoi.anim.StoryboardRecorder;
import asi.voronoi.tree.BinaryTree;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Main animation view pane that composes Canvas, MinimapView, Animator, and ZoomPanController.
 * Provides the public API for controlling animations, rendering frames, and handling user interactions.
 */
public class AnimationView extends Pane {
    // Core components
    private final Canvas canvas;
    private final MinimapView minimapView;
    private final Animator animator;
    private final ZoomPanController zoomPan;
    
    // Data
    private WorldBounds worldBounds;
    private List<Point> allSites = List.of();
    private List<StoryboardRecorder.Frame> frames = List.of();
    
    // Point capture mode
    private boolean captureMode = false;
    private final List<Point> capturedPoints = new ArrayList<>();
    private IntConsumer onCapturedCountChanged;
    
    // Visualization mode
    public enum Mode { NONE, VORONOI, BINARY_TREE }
    private Mode mode = Mode.NONE;
    
    // BinaryTree visualization state
    private static final Color COLOR_ENTER = Color.web("#FFC107");       // Amber
    private static final Color COLOR_VISIT = Color.web("#6495ED");       // CornflowerBlue
    private static final Color COLOR_EXIT  = Color.web("#90EE90");       // LightGreen
    private static final Color COLOR_EDGE  = Color.web("#FF4500");       // OrangeRed
    private static final Color COLOR_NODE_DEFAULT = Color.web("#D3D3D3");// LightGray
    private static final Color COLOR_NODE_STROKE  = Color.web("#555555");// DarkGray
    private static final Color COLOR_EDGE_DEFAULT = Color.web("#808080");// Gray
    
    private final Group btEdgesLayer = new Group();
    private final Group btNodesLayer = new Group();
    private final Group btGroup = new Group(btEdgesLayer, btNodesLayer);
    private final Map<String, Circle> btNodeById = new HashMap<>();
    private final Map<String, Line> btEdgeByKey = new HashMap<>();
    private Timeline btTimeline;
    private int btIdx = 0;
    private List<StoryboardRecorder.BtFrame> btFrames = List.of();
    private final HBox btLegend = new HBox(12);
    private Consumer<String> btStatusSink = null;
    
    // Pan state
    private double lastMouseX = Double.NaN;
    private double lastMouseY = Double.NaN;
    private boolean panning = false;

    public AnimationView() {
        canvas = new Canvas(900, 700);
        minimapView = new MinimapView();
        animator = new Animator();
        zoomPan = new ZoomPanController();
        
        // Wire components together
        minimapView.setZoomPanController(zoomPan);
        
        // Set up callbacks
        animator.setOnFrame(frame -> {
            minimapView.setCurrentFrame(frame);
            redrawCurrent();
            // Update status bar
            if (frame != null) {
                Main.frameStatus.setValue(frame.label);
            }
        });
        
        zoomPan.setOnViewChanged(() -> redrawCurrent());
        
        minimapView.setOnCenterRequest(pt -> {
            zoomPan.centerViewOn(pt.x, pt.y);
        });
        
        minimapView.setOnPanRequest((dx, dy) -> {
            zoomPan.panByWorld(dx, dy);
        });
        
        minimapView.setOnZoomRequest((pt, factor) -> {
            zoomPan.zoomAtWorldPoint(factor, pt.x, pt.y);
        });
        
        // Add children
        getChildren().addAll(canvas, minimapView, btGroup);
        
        // Set up resizing
        widthProperty().addListener((o, ov, nv) -> resizeChildren());
        heightProperty().addListener((o, ov, nv) -> resizeChildren());
        
        // Set up interactions
        installInteractions();
        
        // Binary tree legend
        btLegend.setAlignment(Pos.CENTER_LEFT);
        btLegend.setPadding(new Insets(8, 10, 8, 10));
        btLegend.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 8;");
        btLegend.getChildren().setAll(
            legendItem(COLOR_ENTER, "ENTER"),
            legendItem(COLOR_VISIT, "VISIT"),
            legendItem(COLOR_EXIT,  "EXIT")
        );
        btLegend.setLayoutX(8);
        btLegend.setLayoutY(8);
        getChildren().add(btLegend);
        
        setPrefSize(900, 600);
    }
    
    private HBox legendItem(Color color, String text) {
        Circle dot = new Circle(7, color);
        dot.setStroke(COLOR_NODE_STROKE);
        var label = new Label(text);
        label.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        var box = new HBox(6, dot, label);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // ==================== PUBLIC API ====================
    
    /**
     * Set the list of sites to display.
     */
    public void setSites(List<Point> pts) {
        this.allSites = pts != null ? new ArrayList<>(pts) : List.of();
        minimapView.setSites(allSites);
        redrawCurrent();
    }
    
    /**
     * Set animation frames and compute world bounds.
     */
    public void setFrames(List<StoryboardRecorder.Frame> frames) {
        this.frames = new ArrayList<>(frames);
        this.worldBounds = computeWorld(this.frames);
        zoomPan.setWorldBounds(worldBounds);
        minimapView.setWorldBounds(worldBounds);
        animator.setFrames(frames);
        redrawCurrent();
    }
    
    /**
     * Set animation speed multiplier.
     */
    public void setSpeed(double rate) {
        animator.setSpeed(rate);
    }
    
    /**
     * Get zoom percentage.
     */
    public double getZoomPercent() {
        return zoomPan.getZoomPercent();
    }
    
    /**
     * Start playing animation.
     */
    public void play() {
        animator.play();
    }
    
    /**
     * Pause animation.
     */
    public void pause() {
        animator.pause();
    }
    
    /**
     * Resume paused animation.
     */
    public void resume() {
        animator.resume();
    }
    
    /**
     * Stop animation.
     */
    public void stop() {
        animator.stop();
    }
    
    /**
     * Step forward one frame.
     */
    public void stepForward() {
        Main.frameStatus.setValue(animator.getCurrentFrame() != null ? animator.getCurrentFrame().label : "");
        animator.stepForward();
    }
    
    /**
     * Step back one frame.
     */
    public void stepBack() {
        Main.frameStatus.setValue(animator.getCurrentFrame() != null ? animator.getCurrentFrame().label : "");
        animator.stepBack();
    }
    
    /**
     * Export all frames as PNG files.
     */
    public void exportPngs(File dir, String prefix) throws Exception {
        if (frames.isEmpty()) {
            return;
        }
        animator.exportPngs(dir, prefix, (frame, index) -> {
            drawFrame(canvas.getGraphicsContext2D(), frame);
            javafx.scene.image.WritableImage img = canvas.snapshot(new javafx.scene.SnapshotParameters(), null);
            File out = new File(dir, String.format("%s_%04d.png", prefix, index + 1));
            ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(img, null), "png", out);
        });
    }
    
    /**
     * Zoom at center of canvas.
     */
    public void zoomAtCenter(double factor) {
        zoomPan.zoomAtCenter(factor);
    }
    
    /**
     * Reset view to initial bounds.
     */
    public void resetView() {
        zoomPan.resetView();
    }
    
    /**
     * Fit view to data bounds.
     */
    public void fitToData() {
        zoomPan.fitToData();
    }
    
    /**
     * Get animator instance.
     */
    public Animator getAnimator() {
        return animator;
    }
    
    /**
     * Get zoom/pan controller.
     */
    public ZoomPanController getZoomPanController() {
        return zoomPan;
    }
    
    /**
     * Get minimap view.
     */
    public MinimapView getMinimapView() {
        return minimapView;
    }
    
    // ==================== POINT CAPTURE ====================
    
    /**
     * Start point capture mode.
     */
    public void startPointCapture() {
        captureMode = true;
        capturedPoints.clear();
        redrawCurrent();
    }
    
    /**
     * Stop point capture mode.
     */
    public void stopPointCapture() {
        captureMode = false;
        redrawCurrent();
    }
    
    /**
     * Clear captured points.
     */
    public void clearCapturedPoints() {
        capturedPoints.clear();
        redrawCurrent();
    }
    
    /**
     * Get number of captured points.
     */
    public int getCapturedSize() {
        return capturedPoints.size();
    }
    
    /**
     * Undo last captured point.
     */
    public boolean undoCapturedPoint() {
        if (!capturedPoints.isEmpty()) {
            capturedPoints.remove(capturedPoints.size() - 1);
            redrawCurrent();
            return true;
        }
        return false;
    }
    
    /**
     * Get list of captured points.
     */
    public List<Point> getCapturedPoints() {
        return new ArrayList<>(capturedPoints);
    }
    
    /**
     * Set callback for captured point count changes.
     */
    public void setOnCapturedCountChanged(IntConsumer cb) {
        this.onCapturedCountChanged = cb;
    }
    
    // ==================== MINIMAP API ====================
    
    /**
     * Enable or disable minimap.
     */
    public void setMinimapEnabled(boolean enabled) {
        minimapView.setEnabled(enabled);
        minimapView.layoutInPane(this, 10);
        redrawCurrent();
    }
    
    /**
     * Set minimap position.
     */
    public void setMinimapPosition(MinimapView.MinimapPos pos) {
        minimapView.setPosition(pos);
        minimapView.layoutInPane(this, 10);
        redrawCurrent();
    }
    
    /**
     * Set minimap size.
     */
    public void setMinimapSize(double w, double h) {
        minimapView.setWidth(w);
        minimapView.setHeight(h);
        minimapView.layoutInPane(this, 10);
        redrawCurrent();
    }
    
    // ==================== BINARY TREE ====================
    
    /**
     * Set visualization mode.
     */
    public void setVisualizationMode(Mode m) {
        this.mode = m;
        switch (m) {
            case VORONOI -> {
                clearBinaryTreeView();
                btLegend.setVisible(false);
            }
            case BINARY_TREE -> {
                clearVoronoiView();
                btLegend.setVisible(true);
            }
            case NONE -> {
                clearAllViews();
                btLegend.setVisible(false);
            }
        }
        redrawCurrent();
    }
    
    /**
     * Set binary tree status sink callback.
     */
    public void setBtStatusSink(Consumer<String> sink) {
        this.btStatusSink = sink;
    }
    
    /**
     * Render binary tree structure.
     */
    public void renderBinaryTree(BinaryTree root) {
        btEdgesLayer.getChildren().clear();
        btNodesLayer.getChildren().clear();
        btNodeById.clear();
        btEdgeByKey.clear();
        
        Map<String, BtPos> layout = layoutTree(root);
        
        // Draw edges first
        for (BtEdge edge : computeEdges(root)) {
            BtPos p1 = layout.get(edge.parentId);
            BtPos p2 = layout.get(edge.childId);
            if (p1 != null && p2 != null) {
                Line l = new Line(p1.x, p1.y, p2.x, p2.y);
                l.setStroke(COLOR_EDGE_DEFAULT);
                l.setStrokeWidth(2);
                btEdgesLayer.getChildren().add(l);
                btEdgeByKey.put(edgeKey(edge.parentId, edge.childId), l);
            }
        }
        
        // Draw nodes
        for (var entry : layout.entrySet()) {
            String id = entry.getKey();
            BtPos pos = entry.getValue();
            Circle c = new Circle(pos.x, pos.y, 18);
            c.setFill(COLOR_NODE_DEFAULT);
            c.setStroke(COLOR_NODE_STROKE);
            c.setStrokeWidth(2);
            btNodeById.put(id, c);
            btNodesLayer.getChildren().add(c);
            
            if (pos.label != null && !pos.label.isEmpty()) {
                Label lab = new Label(pos.label);
                lab.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
                lab.setLayoutX(pos.x - 10);
                lab.setLayoutY(pos.y - 10);
                btNodesLayer.getChildren().add(lab);
            }
        }
        
        recenterBinaryTree();
    }
    
    /**
     * Reset binary tree node colors.
     */
    public void resetBinaryTreeColors() {
        btNodeById.values().forEach(c -> {
            c.setFill(COLOR_NODE_DEFAULT);
            c.setStroke(COLOR_NODE_STROKE);
        });
        btEdgeByKey.values().forEach(l -> l.setStroke(COLOR_EDGE_DEFAULT));
    }
    
    /**
     * Clear binary tree view.
     */
    public void clearBinaryTreeView() {
        stopBinaryTreeStoryboard();
        btEdgesLayer.getChildren().clear();
        btNodesLayer.getChildren().clear();
        btNodeById.clear();
        btEdgeByKey.clear();
        btFrames = List.of();
        btIdx = 0;
    }
    
    /**
     * Clear Voronoi view.
     */
    public void clearVoronoiView() {
        stop();
        frames = List.of();
        animator.setFrames(List.of());
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    /**
     * Clear all views.
     */
    public void clearAllViews() {
        clearVoronoiView();
        clearBinaryTreeView();
    }
    
    /**
     * Play binary tree storyboard.
     */
    public void playBinaryTreeStoryboard(StoryboardRecorder recorder, double fps) {
        btFrames = recorder.getBtFrames();
        btIdx = 0;
        stopBinaryTreeStoryboard();
        if (btFrames.isEmpty()) {
            return;
        }
        btTimeline = new Timeline();
        double frameMs = 1000.0 / fps;
        for (int i = 0; i < btFrames.size(); i++) {
            final int idx = i;
            btTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(frameMs * idx), ev -> {
                btIdx = idx;
                applyBtFrame(btFrames.get(btIdx));
                updateBtStatus(btIdx, btFrames.size(), btFrames.get(btIdx));
            }));
        }
        btTimeline.setCycleCount(1);
        btTimeline.playFromStart();
    }
    
    /**
     * Pause binary tree storyboard.
     */
    public void pauseBinaryTreeStoryboard() {
        if (btTimeline != null) btTimeline.pause();
    }
    
    /**
     * Resume binary tree storyboard.
     */
    public void resumeBinaryTreeStoryboard() {
        if (btTimeline != null) btTimeline.play();
    }
    
    /**
     * Stop binary tree storyboard.
     */
    public void stopBinaryTreeStoryboard() {
        if (btTimeline != null) btTimeline.stop();
    }
    
    /**
     * Step forward in binary tree storyboard.
     */
    public void stepBinaryTreeStoryboard(StoryboardRecorder recorder) {
        btFrames = recorder.getBtFrames();
        if (btFrames.isEmpty()) {
            return;
        }
        pauseBinaryTreeStoryboard();
        btIdx = Math.min(btIdx + 1, btFrames.size() - 1);
        applyBtFrame(btFrames.get(btIdx));
        updateBtStatus(btIdx, btFrames.size(), btFrames.get(btIdx));
    }
    
    /**
     * Step backward in binary tree storyboard.
     */
    public void stepBackBinaryTreeStoryboard(StoryboardRecorder recorder) {
        btFrames = recorder.getBtFrames();
        if (btFrames.isEmpty()) {
            return;
        }
        pauseBinaryTreeStoryboard();
        // Reset colors
        resetBinaryTreeColors();
        // Replay from start to btIdx-1
        btIdx = Math.max(btIdx - 1, 0);
        for (int i = 0; i <= btIdx; i++) {
            applyBtFrame(btFrames.get(i));
        }
        updateBtStatus(btIdx, btFrames.size(), btFrames.get(btIdx));
    }
    
    /**
     * Set binary tree playback rate.
     */
    public void setBinaryTreeRate(double rate) {
        if (btTimeline != null) {
            btTimeline.setRate(rate);
        }
    }
    
    /**
     * Fit binary tree to view.
     */
    public void fitBinaryTreeToView() {
        fitBinaryTreeToView(50);
    }
    
    /**
     * Fit binary tree to view with margin.
     */
    public void fitBinaryTreeToView(double margin) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        
        for (javafx.scene.Node n : btNodesLayer.getChildren()) {
            if (n instanceof Circle c) {
                minX = Math.min(minX, c.getCenterX() - c.getRadius());
                maxX = Math.max(maxX, c.getCenterX() + c.getRadius());
                minY = Math.min(minY, c.getCenterY() - c.getRadius());
                maxY = Math.max(maxY, c.getCenterY() + c.getRadius());
            }
        }
        
        if (!Double.isFinite(minX)) {
            return;
        }
        
        double contentW = Math.max(maxX - minX, 1);
        double contentH = Math.max(maxY - minY, 1);
        double availW = getWidth() - 2 * margin;
        double availH = getHeight() - 2 * margin;
        
        double scaleX = availW / contentW;
        double scaleY = availH / contentH;
        double scale = Math.min(scaleX, scaleY);
        
        btGroup.setScaleX(scale);
        btGroup.setScaleY(scale);
        
        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        btGroup.setTranslateX(getWidth() / 2.0 - centerX * scale);
        btGroup.setTranslateY(getHeight() / 2.0 - centerY * scale);
    }
    
    private void applyBtFrame(StoryboardRecorder.BtFrame f) {
        String nodeId = btNodeId(f.node);
        switch (f.type) {
            case ENTER -> highlightNode(nodeId, COLOR_ENTER);
            case VISIT -> highlightNode(nodeId, COLOR_VISIT);
            case EXIT -> highlightNode(nodeId, COLOR_EXIT);
        }
        if (f.edgeToChild != null) {
            String childId = btNodeId(f.edgeToChild);
            highlightEdge(nodeId, childId, COLOR_EDGE);
        }
    }
    
    private void updateBtStatus(int idx, int total, StoryboardRecorder.BtFrame f) {
        if (btStatusSink != null) {
            String msg = String.format("Frame %d/%d - %s (%s)", idx + 1, total, f.type, f.node.getLabel());
            btStatusSink.accept(msg);
        }
    }
    
    private void highlightNode(String nodeId, Color color) {
        Circle c = btNodeById.get(nodeId);
        if (c != null) c.setFill(color);
    }
    
    private void highlightEdge(String parentId, String childId, Color color) {
        Line l = btEdgeByKey.get(edgeKey(parentId, childId));
        if (l != null) l.setStroke(color);
    }
    
    private String edgeKey(String parentId, String childId) {
        return parentId + "->" + childId;
    }
    
    private record BtPos(double x, double y, String label) {}
    private record BtEdge(String parentId, String childId) {}
    
    private Map<String, BtPos> layoutTree(BinaryTree root) {
        Map<String, BtPos> result = new LinkedHashMap<>();
        if (root == null) return result;
        layoutDfs(root, null, 0, 0, new int[]{0}, result);
        return result;
    }
    
    private void layoutDfs(BinaryTree n, BinaryTree parent,
                          int depth, int pos, int[] xCounter, Map<String, BtPos> out) {
        if (n == null) return;
        layoutDfs(n.getLeft(), n, depth + 1, 2 * pos, xCounter, out);
        int x = xCounter[0]++;
        String id = btNodeId(n);
        out.put(id, new BtPos(x * 60 + 50, depth * 80 + 50, n.getLabel()));
        layoutDfs(n.getRight(), n, depth + 1, 2 * pos + 1, xCounter, out);
    }
    
    private List<BtEdge> computeEdges(BinaryTree root) {
        List<BtEdge> edges = new ArrayList<>();
        computeEdgesDfs(root, null, edges);
        return edges;
    }
    
    private void computeEdgesDfs(BinaryTree n, BinaryTree parent, List<BtEdge> out) {
        if (n == null) return;
        if (parent != null) {
            out.add(new BtEdge(btNodeId(parent), btNodeId(n)));
        }
        computeEdgesDfs(n.getLeft(), n, out);
        computeEdgesDfs(n.getRight(), n, out);
    }
    
    private String btNodeId(BinaryTree n) {
        if (n == null) return "null";
        Point p = n.getPayload();
        if (p != null) {
            return String.format("(%.2f,%.2f)", p.x(), p.y());
        }
        return Integer.toString(System.identityHashCode(n));
    }
    
    private void recenterBinaryTree() {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        
        for (javafx.scene.Node n : btNodesLayer.getChildren()) {
            if (n instanceof Circle c) {
                minX = Math.min(minX, c.getCenterX());
                maxX = Math.max(maxX, c.getCenterX());
                minY = Math.min(minY, c.getCenterY());
                maxY = Math.max(maxY, c.getCenterY());
            }
        }
        
        if (Double.isFinite(minX)) {
            double centerX = (minX + maxX) / 2.0;
            double centerY = (minY + maxY) / 2.0;
            btGroup.setTranslateX(getWidth() / 2.0 - centerX);
            btGroup.setTranslateY(getHeight() / 2.0 - centerY);
        }
    }
    
    // ==================== RENDERING ====================
    
    private void redrawCurrent() {
        if (frames.isEmpty()) {
            GraphicsContext g = canvas.getGraphicsContext2D();
            g.setFill(Color.WHITE);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            
            // Draw captured points overlay
            if (!capturedPoints.isEmpty()) {
                g.setFill(Color.web("#a100ff"));
                for (Point s : capturedPoints) {
                    g.fillOval(zoomPan.sx(s.x()) - 3.5, zoomPan.sy(s.y()) - 3.5, 7, 7);
                }
            }
            minimapView.draw();
            return;
        }
        
        StoryboardRecorder.Frame frame = animator.getCurrentFrame();
        if (frame != null) {
            drawFrame(canvas.getGraphicsContext2D(), frame);
        }
        minimapView.draw();
    }
    
    private void drawFrame(GraphicsContext g, StoryboardRecorder.Frame f) {
        double W = canvas.getWidth();
        double H = canvas.getHeight();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, W, H);
        
        // Draw all sites overlay
        g.setFill(Color.web("#444444"));
        g.setGlobalAlpha(0.65);
        for (Point s : allSites) {
            g.fillOval(zoomPan.sx(s.x()) - 2.5, zoomPan.sy(s.y()) - 2.5, 5, 5);
        }
        g.setGlobalAlpha(1.0);
        
        Main.frameStatus.setValue(f.label);
        
        // Division: bbox
        if (f.bbox != null) {
            asi.voronoi.anim.Rect b = f.bbox;
            g.setGlobalAlpha(0.08);
            g.setFill(Color.web("#6c5ce7"));
            g.fillRect(zoomPan.sx(b.xMin()), zoomPan.sy(b.yMax()), 
                      Math.abs(zoomPan.sx(b.xMax()) - zoomPan.sx(b.xMin())), 
                      Math.abs(zoomPan.sy(b.yMin()) - zoomPan.sy(b.yMax())));
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
            double x1 = mp.x() - d.x() * 5000;
            double y1 = mp.y() - d.y() * 5000;
            double x2 = mp.x() + d.x() * 5000;
            double y2 = mp.y() + d.y() * 5000;
            g.strokeLine(zoomPan.sx(x1), zoomPan.sy(y1), zoomPan.sx(x2), zoomPan.sy(y2));
            g.setLineDashes();
        }
        
        // Left/right points
        g.setFill(Color.web("#0984e3"));
        if (f.leftPts != null) {
            for (Point p : f.leftPts) {
                g.fillOval(zoomPan.sx(p.x()) - 3, zoomPan.sy(p.y()) - 3, 6, 6);
            }
        }
        g.setFill(Color.web("#d63031"));
        if (f.rightPts != null) {
            for (Point p : f.rightPts) {
                g.fillOval(zoomPan.sx(p.x()) - 3, zoomPan.sy(p.y()) - 3, 6, 6);
            }
        }
        
        // Pivot
        if (f.pivot != null) {
            Point p = f.pivot;
            g.setFill(Color.web("#f2c94c"));
            g.fillOval(zoomPan.sx(p.x()) - 4, zoomPan.sy(p.y()) - 4, 8, 8);
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
                g.fillOval(zoomPan.sx(p.x()) - 4, zoomPan.sy(p.y()) - 4, 8, 8);
            }
        }
        
        // Captured points overlay (magenta)
        if (captureMode && !capturedPoints.isEmpty()) {
            g.setFill(Color.web("#a100ff"));
            for (Point s : capturedPoints) {
                g.fillOval(zoomPan.sx(s.x()) - 3.5, zoomPan.sy(s.y()) - 3.5, 7, 7);
            }
        }
    }
    
    private void drawLineSegment(GraphicsContext g, asi.voronoi.Line ln) {
        Point b = ln.getBeginP().orElse(null);
        Point e = ln.getEndP().orElse(null);
        if (b != null && e != null) {
            g.strokeLine(zoomPan.sx(b.x()), zoomPan.sy(b.y()), zoomPan.sx(e.x()), zoomPan.sy(e.y()));
        } else if (b != null) {
            Point d = ln.getDir();
            double x2 = b.x() + d.x() * 5000;
            double y2 = b.y() + d.y() * 5000;
            g.strokeLine(zoomPan.sx(b.x()), zoomPan.sy(b.y()), zoomPan.sx(x2), zoomPan.sy(y2));
        } else if (e != null) {
            Point d = ln.getDir();
            double x1 = e.x() - d.x() * 5000;
            double y1 = e.y() - d.y() * 5000;
            g.strokeLine(zoomPan.sx(x1), zoomPan.sy(y1), zoomPan.sx(e.x()), zoomPan.sy(e.y()));
        } else {
            Point m = ln.getMidP();
            Point d = ln.getDir();
            double x1 = m.x() - d.x() * 5000;
            double y1 = m.y() - d.y() * 5000;
            double x2 = m.x() + d.x() * 5000;
            double y2 = m.y() + d.y() * 5000;
            g.strokeLine(zoomPan.sx(x1), zoomPan.sy(y1), zoomPan.sx(x2), zoomPan.sy(y2));
        }
    }
    
    // ==================== LAYOUT ====================
    
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
            zoomPan.setCanvasSize(w, h);
            redrawCurrent();
        }
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);
        minimapView.layoutInPane(this, 10);
        minimapView.draw();
    }
    
    // ==================== INTERACTIONS ====================
    
    private void installInteractions() {
        // Zoom with scroll wheel
        canvas.setOnScroll(ev -> {
            double factor = (ev.getDeltaY() > 0) ? 1.2 : (1 / 1.2);
            zoomPan.zoomAt(factor, ev.getX(), ev.getY());
            ev.consume();
        });
        
        canvas.setOnMouseClicked(ev -> {
            if (captureMode) {
                if (ev.getButton() == MouseButton.PRIMARY) {
                    double wx = zoomPan.screenToWorldX(ev.getX());
                    double wy = zoomPan.screenToWorldY(ev.getY());
                    capturedPoints.add(new Point(wx, wy));
                    if (onCapturedCountChanged != null) {
                        onCapturedCountChanged.accept(capturedPoints.size());
                    }
                    redrawCurrent();
                    ev.consume();
                } else if (ev.getButton() == MouseButton.SECONDARY) {
                    // Right click = undo
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
                // Allow CLICKED event to still be generated
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
                // No pan in capture mode
                return;
            }
            if (panning) {
                double dx = ev.getX() - lastMouseX;
                double dy = ev.getY() - lastMouseY;
                zoomPan.panByScreen(dx, dy);
                lastMouseX = ev.getX();
                lastMouseY = ev.getY();
            }
        });
        
        canvas.setOnMouseReleased(ev -> panning = false);
        canvas.setOnMouseEntered(ev -> canvas.setCursor(javafx.scene.Cursor.OPEN_HAND));
        canvas.setOnMouseExited(ev -> canvas.setCursor(javafx.scene.Cursor.DEFAULT));
        
        // Keyboard shortcuts
        sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                newS.setOnKeyPressed(ev -> {
                    switch (ev.getCode()) {
                        case PLUS, ADD -> zoomPan.zoomAtCenter(1.2);
                        case MINUS, SUBTRACT -> zoomPan.zoomAtCenter(1 / 1.2);
                        case DIGIT0 -> zoomPan.resetView();
                        case F -> zoomPan.fitToData();
                    }
                });
            }
        });
    }
    
    // ==================== WORLD BOUNDS COMPUTATION ====================
    
    private WorldBounds computeWorld(List<StoryboardRecorder.Frame> frames) {
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
            if (f.edges != null) {
                for (asi.voronoi.Line ln : f.edges) {
                    ln.getBeginP().ifPresent(p -> {
                        xmin = Math.min(xmin, p.x());
                        xmax = Math.max(xmax, p.x());
                        ymin = Math.min(ymin, p.y());
                        ymax = Math.max(ymax, p.y());
                    });
                    ln.getEndP().ifPresent(p -> {
                        xmin = Math.min(xmin, p.x());
                        xmax = Math.max(xmax, p.x());
                        ymin = Math.min(ymin, p.y());
                        ymax = Math.max(ymax, p.y());
                    });
                    Point mid = ln.getMidP();
                    if (mid != null) {
                        xmin = Math.min(xmin, mid.x());
                        xmax = Math.max(xmax, mid.x());
                        ymin = Math.min(ymin, mid.y());
                        ymax = Math.max(ymax, mid.y());
                    }
                }
            }
        }
        
        // Include all sites
        for (Point p : allSites) {
            xmin = Math.min(xmin, p.x());
            xmax = Math.max(xmax, p.x());
            ymin = Math.min(ymin, p.y());
            ymax = Math.max(ymax, p.y());
        }
        
        // Fallback
        if (!Double.isFinite(xmin)) {
            xmin = 0;
            ymin = 0;
            xmax = 1;
            ymax = 1;
        }
        
        return new WorldBounds(xmin, ymin, xmax, ymax);
    }
}
