/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi.javafx;

import java.util.function.IntConsumer;
import javafx.scene.input.MouseButton;

/**
 *
 * @author arvin
 */
class AnimationPane extends javafx.scene.layout.Pane {
    
    private final javafx.scene.canvas.Canvas canvas;
    private final javafx.scene.canvas.Canvas minimap; // <<< NYT: minimap
    private javafx.animation.Timeline timeline;
    private final double pad = 30;
    private java.util.List<asi.voronoi.Point> allSites = java.util.List.of();
    private java.util.List<asi.voronoi.anim.StoryboardRecorder.Frame> frames = java.util.List.of();
    // --- i AnimationPane: tilføj felter ---
    private boolean captureMode = false;
    private final java.util.List<asi.voronoi.Point> capturedPoints = new java.util.ArrayList<>();

    // world bounds (data)
    private static class World {

        double xmin;
        double ymin;
        double xmax;
        double ymax;

        World(double xmin, double ymin, double xmax, double ymax) {
            this.xmin = xmin;
            this.ymin = ymin;
            this.xmax = xmax;
            this.ymax = ymax;
        }
    }
    private World world;
    // view-window (world coords, zoom/pan)
    private double viewXmin;
    private double viewXmax;
    private double viewYmin;
    private double viewYmax;
    private double initXmin;
    private double initXmax;
    private double initYmin;
    private double initYmax;
    private final double MIN_ZOOM = 0.05; // 5%
    private final double MAX_ZOOM = 50.0; // 5000%
    // timeline-konfiguration
    private final double frameMs = 200;
    private int currentIndex = 0;
    // pan state
    private double lastMouseX = Double.NaN;
    private double lastMouseY = Double.NaN;
    private boolean panning = false;
    // ------------------ MINIMAP STATE ------------------
    // Kan slås til/fra, flyttes hjørne, indstille størrelse
    private boolean minimapEnabled = true;

    enum MinimapPos {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
    private MinimapPos minimapPos = MinimapPos.BOTTOM_RIGHT;
    private double miniW = 220;
    private double miniH = 160; // størrelse i pixels
    private final double miniPad = 6; // indre padding
    // beregnet til tegning (world->minimap)
    private double mmScale = 1.0;
    private double mmOx = 0.0;
    private double mmOy = 0.0;
    // drag i minimap
    private boolean miniDraggingRect = false;
    private double miniDragStartX = 0;
    private double miniDragStartY = 0;
    private double miniViewStartXmin;
    private double miniViewStartXmax;
    private double miniViewStartYmin;
    private double miniViewStartYmax;

    AnimationPane() {
        canvas = new javafx.scene.canvas.Canvas(900, 700);
        minimap = new javafx.scene.canvas.Canvas(miniW, miniH); // overlay canvas
        getChildren().addAll(canvas, minimap);
        // Gør Pane resizable og følg størrelsen
        widthProperty().addListener((o, ov, nv) -> resizeChildren());
        heightProperty().addListener((o, ov, nv) -> resizeChildren());
        installInteractions();
        installMinimapInteractions(); // <<< NYT
    } // overlay canvas
    // Gør Pane resizable og følg størrelsen
    // <<< NYT
    // ----- PUBLIC API -----
    private IntConsumer onCapturedCountChanged; // UI callback

    void setOnCapturedCountChanged(IntConsumer cb) {
        this.onCapturedCountChanged = cb;
    }

    void setSites(java.util.List<asi.voronoi.Point> pts) {
        this.allSites = new java.util.ArrayList<>(pts);
    }

    void setFrames(java.util.List<asi.voronoi.anim.StoryboardRecorder.Frame> frames) {
        this.frames = new java.util.ArrayList<>(frames);
        this.world = computeWorld(this.frames);
        // initial view (med lidt margin)
        double mx = (world.xmax - world.xmin) * 0.05;
        if (!java.lang.Double.isFinite(mx) || mx <= 0) {
            mx = 1;
        }
        double my = (world.ymax - world.ymin) * 0.05;
        if (!java.lang.Double.isFinite(my) || my <= 0) {
            my = 1;
        }
        initXmin = viewXmin = world.xmin - mx;
        initXmax = viewXmax = world.xmax + mx;
        initYmin = viewYmin = world.ymin - my;
        initYmax = viewYmax = world.ymax + my;
        currentIndex = 0;
        redrawCurrent();
    }

    void setSpeed(double rate) {
        if (timeline != null) {
            timeline.setRate(rate);
        }
    }

    double getZoomPercent() {
        return getZoomRatio() * 100.0;
    }

    // --- OFFENTLIG API ---
    void startPointCapture() {
        captureMode = true;
        capturedPoints.clear();
        // lad allSites forblive uændret; vi tegner captured overlay separat
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

    java.util.List<asi.voronoi.Point> getCapturedPoints() {
        return new java.util.ArrayList<>(capturedPoints);
    }

    // Minimap API
    void setMinimapEnabled(boolean enabled) {
        this.minimapEnabled = enabled;
        layoutMinimap();
        redrawCurrent();
    }

    void setMinimapPosition(MinimapPos pos) {
        this.minimapPos = pos;
        layoutMinimap();
        redrawCurrent();
    }

    void setMinimapSize(double w, double h) {
        this.miniW = w;
        this.miniH = h;
        minimap.setWidth(w);
        minimap.setHeight(h);
        layoutMinimap();
        redrawCurrent();
    }

    void play() {
        if (frames.isEmpty()) {
            return;
        }
        stop();
        javafx.scene.canvas.GraphicsContext g = canvas.getGraphicsContext2D();
        timeline = new javafx.animation.Timeline();
        for (int i = 0; i < frames.size(); i++) {
            final int idx = i;
            timeline.getKeyFrames().add(new javafx.animation.KeyFrame(javafx.util.Duration.millis(frameMs * idx), ev -> {
                currentIndex = idx;
                drawFrame(g, frames.get(currentIndex));
                drawMinimap(); // <<< NYT: opdatér minimap per frame
            }));
        }
        timeline.setCycleCount(1);
        timeline.playFromStart();
    }

    void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }

    void resume() {
        if (timeline != null) {
            timeline.play();
        }
    }

    void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    void stepForward() {
        if (frames.isEmpty()) {
            return;
        }
        stop();
        currentIndex = java.lang.Math.min(currentIndex + 1, frames.size() - 1);
        Main.frameStatus.setValue(frames.get(currentIndex).label);
        redrawCurrent();
    }

    void stepBack() {
        if (frames.isEmpty()) {
            return;
        }
        stop();
        currentIndex = java.lang.Math.max(currentIndex - 1, 0);
        Main.frameStatus.setValue(frames.get(currentIndex).label);
        redrawCurrent();
    }

    void exportPngs(java.io.File dir, String prefix) throws Exception {
        if (frames.isEmpty()) {
            return;
        }
        if (!dir.exists()) {
            java.nio.file.Files.createDirectories(dir.toPath());
        }
        javafx.scene.canvas.GraphicsContext g = canvas.getGraphicsContext2D();
        boolean wasPlaying = timeline != null && timeline.getStatus() == javafx.animation.Animation.Status.RUNNING;
        stop();
        for (int i = 0; i < frames.size(); i++) {
            drawFrame(g, frames.get(i));
            javafx.scene.image.WritableImage img = canvas.snapshot(new javafx.scene.SnapshotParameters(), null);
            java.io.File out = new java.io.File(dir, String.format("%s_%04d.png", prefix, i + 1));
            javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(img, null), "png", out);
        }
        if (wasPlaying) {
            play();
        }
    }

    // ---------- layout/resizing ----------
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
            redrawCurrent();
        }
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);
        layoutMinimap();
        drawMinimap();
    }

    private void layoutMinimap() {
        minimap.setVisible(minimapEnabled);
        if (!minimapEnabled) {
            return;
        }
        // position i hjørne m. 10px margin fra kanter
        double margin = 10;
        switch (minimapPos) {
            case TOP_LEFT -> {
                minimap.setLayoutX(margin);
                minimap.setLayoutY(margin);
            }
            case TOP_RIGHT -> {
                minimap.setLayoutX(getWidth() - miniW - margin);
                minimap.setLayoutY(margin);
            }
            case BOTTOM_LEFT -> {
                minimap.setLayoutX(margin);
                minimap.setLayoutY(getHeight() - miniH - margin);
            }
            case BOTTOM_RIGHT -> {
                minimap.setLayoutX(getWidth() - miniW - margin);
                minimap.setLayoutY(getHeight() - miniH - margin);
            }
        }
        // sikker: størrelsen kan være ændret via setMinimapSize()
        if (minimap.getWidth() != miniW) {
            minimap.setWidth(miniW);
        }
        if (minimap.getHeight() != miniH) {
            minimap.setHeight(miniH);
        }
    }

    // ---------- zoom/pan ----------
    void zoomAtCenter(double factor) {
        zoomAt(factor, canvas.getWidth() / 2.0, canvas.getHeight() / 2.0);
    }

    void zoomAt(double factor, double screenX, double screenY) {
        double z = getZoomRatio();
        double nextZ = z * factor;
        if (nextZ > MAX_ZOOM) {
            factor = MAX_ZOOM / z;
        }
        if (nextZ < MIN_ZOOM) {
            factor = MIN_ZOOM / z;
        }
        double vx = screenToWorldX(screenX);
        double vy = screenToWorldY(screenY);
        double w = viewXmax - viewXmin;
        double h = viewYmax - viewYmin;
        double newW = w / factor;
        double newH = h / factor;
        double rx = (vx - viewXmin) / w;
        double ry = (vy - viewYmin) / h;
        viewXmin = vx - rx * newW;
        viewXmax = viewXmin + newW;
        viewYmin = vy - ry * newH;
        viewYmax = viewYmin + newH;
        redrawCurrent();
    }

    void panByScreen(double dx, double dy) {
        double w = viewXmax - viewXmin;
        double h = viewYmax - viewYmin;
        double contentW = Math.max(1, canvas.getWidth() - 2 * pad);
        double contentH = Math.max(1, canvas.getHeight() - 2 * pad);
        double dxWorld = dx * (w / contentW);
        double dyWorld = -dy * (h / contentH); // Y-flip
        viewXmin -= dxWorld;
        viewXmax -= dxWorld;
        viewYmin -= dyWorld;
        viewYmax -= dyWorld;
        redrawCurrent();
    }

    void resetView() {
        viewXmin = initXmin;
        viewXmax = initXmax;
        viewYmin = initYmin;
        viewYmax = initYmax;
        redrawCurrent();
    }

    void fitToData() {
        double mx = (world.xmax - world.xmin) * 0.05;
        if (!java.lang.Double.isFinite(mx) || mx <= 0) {
            mx = 1;
        }
        double my = (world.ymax - world.ymin) * 0.05;
        if (!java.lang.Double.isFinite(my) || my <= 0) {
            my = 1;
        }
        viewXmin = world.xmin - mx;
        viewXmax = world.xmax + mx;
        viewYmin = world.ymin - my;
        viewYmax = world.ymax + my;
        redrawCurrent();
    }

    private double getZoomRatio() {
        double initW = initXmax - initXmin;
        double curW = viewXmax - viewXmin;
        if (curW <= 0 || !java.lang.Double.isFinite(curW)) {
            return 1.0;
        }
        return initW / curW;
    }

    // ---------- interaktioner (main-canvas) ----------
    private void installInteractions() {
        // zoom med hjul (om cursor)
        canvas.setOnScroll(ev -> {
            double factor = (ev.getDeltaY() > 0) ? 1.2 : (1 / 1.2);
            zoomAt(factor, ev.getX(), ev.getY());
            ev.consume();
        });
        canvas.setOnMouseClicked(ev -> {
            if (captureMode) {
                if (ev.getButton() == MouseButton.PRIMARY) {
                    double wx = screenToWorldX(ev.getX());
                    double wy = screenToWorldY(ev.getY());
                    capturedPoints.add(new asi.voronoi.Point(wx, wy));
                    // (valgfrit) informér UI om antal nye punkter:
                    if (onCapturedCountChanged != null) {
                        onCapturedCountChanged.accept(capturedPoints.size());
                    }
                    redrawCurrent();
                    ev.consume(); // her må vi gerne consume selve "klik"-eventet
                } else if (ev.getButton() == MouseButton.SECONDARY) {
                    // højreklik = undo (valgfrit)
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
            // Ikke-capture-mode: intet specielt her
        });
        canvas.setOnMousePressed(ev -> {
            if (captureMode) {
                // Tillad at CLICKED stadig genereres → ingen consume her
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
                // ingen pan i capture mode, men heller ikke consume
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
        canvas.setOnMouseEntered(ev -> canvas.setCursor(javafx.scene.Cursor.OPEN_HAND));
        canvas.setOnMouseExited(ev -> canvas.setCursor(javafx.scene.Cursor.DEFAULT));
        // tastatur: + / - / 0 / f
        sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                newS.setOnKeyPressed(ev -> {
                    switch (ev.getCode()) {
                        case PLUS, ADD -> {
                            zoomAtCenter(1.2);
                        }
                        case MINUS, SUBTRACT -> {
                            zoomAtCenter(1 / 1.2);
                        }
                        case DIGIT0 -> {
                            resetView();
                        }
                        case F -> {
                            fitToData();
                        }
                    }
                });
            }
        });
    }

    // ---------- interaktioner (minimap) ----------
    private void installMinimapInteractions() {
        minimap.setPickOnBounds(false);
        minimap.setOnMousePressed(ev -> {
            if (!minimapEnabled) {
                return;
            }
            asi.voronoi.javafx.AnimationPane.WPoint pt = minimapToWorld(ev.getX(), ev.getY());
            if (pt == null) {
                return;
            }
            // er press inde i view-rect? => start drag af rektangel
            if (isInsideViewRect(pt.x, pt.y)) {
                miniDraggingRect = true;
                miniDragStartX = ev.getX();
                miniDragStartY = ev.getY();
                miniViewStartXmin = viewXmin;
                miniViewStartXmax = viewXmax;
                miniViewStartYmin = viewYmin;
                miniViewStartYmax = viewYmax;
            } else {
                // klik: centrer view omkring punktet
                centerViewOn(pt.x, pt.y);
            }
        });
        minimap.setOnMouseDragged(ev -> {
            if (!minimapEnabled) {
                return;
            }
            if (!miniDraggingRect) {
                return;
            }
            asi.voronoi.javafx.AnimationPane.WPoint p0 = minimapToWorld(miniDragStartX, miniDragStartY);
            asi.voronoi.javafx.AnimationPane.WPoint p1 = minimapToWorld(ev.getX(), ev.getY());
            if (p0 == null || p1 == null) {
                return;
            }
            double dx = p1.x - p0.x;
            double dy = p1.y - p0.y;
            viewXmin = miniViewStartXmin + dx;
            viewXmax = miniViewStartXmax + dx;
            viewYmin = miniViewStartYmin + dy;
            viewYmax = miniViewStartYmax + dy;
            redrawCurrent();
        });
        minimap.setOnMouseReleased(ev -> miniDraggingRect = false);
        // hjul-zoom i minimap
        minimap.setOnScroll(ev -> {
            if (!minimapEnabled) {
                return;
            }
            asi.voronoi.javafx.AnimationPane.WPoint pt = minimapToWorld(ev.getX(), ev.getY());
            if (pt == null) {
                return;
            }
            double factor = (ev.getDeltaY() > 0) ? 1.2 : (1 / 1.2);
            zoomAtWorldPoint(factor, pt.x, pt.y);
            ev.consume();
        });
    }

    // ---------- world / mapping / draw ----------
    private World computeWorld(java.util.List<asi.voronoi.anim.StoryboardRecorder.Frame> frames) {
        double xmin = java.lang.Double.POSITIVE_INFINITY;
        double ymin = java.lang.Double.POSITIVE_INFINITY;
        double xmax = java.lang.Double.NEGATIVE_INFINITY;
        double ymax = java.lang.Double.NEGATIVE_INFINITY;
        for (asi.voronoi.anim.StoryboardRecorder.Frame f : frames) {
            if (f.bbox != null) {
                xmin = java.lang.Math.min(xmin, f.bbox.xMin());
                xmax = java.lang.Math.max(xmax, f.bbox.xMax());
                ymin = java.lang.Math.min(ymin, f.bbox.yMin());
                ymax = java.lang.Math.max(ymax, f.bbox.yMax());
            }
            if (f.leftPts != null) {
                for (asi.voronoi.Point p : f.leftPts) {
                    xmin = java.lang.Math.min(xmin, p.x());
                    xmax = java.lang.Math.max(xmax, p.x());
                    ymin = java.lang.Math.min(ymin, p.y());
                    ymax = java.lang.Math.max(ymax, p.y());
                }
            }
            if (f.rightPts != null) {
                for (asi.voronoi.Point p : f.rightPts) {
                    xmin = java.lang.Math.min(xmin, p.x());
                    xmax = java.lang.Math.max(xmax, p.x());
                    ymin = java.lang.Math.min(ymin, p.y());
                    ymax = java.lang.Math.max(ymax, p.y());
                }
            }
            if (f.pivot != null) {
                asi.voronoi.Point p = f.pivot;
                xmin = java.lang.Math.min(xmin, p.x());
                xmax = java.lang.Math.max(xmax, p.x());
                ymin = java.lang.Math.min(ymin, p.y());
                ymax = java.lang.Math.max(ymax, p.y());
            }
            if (f.marks != null) {
                for (asi.voronoi.Point p : f.marks) {
                    xmin = java.lang.Math.min(xmin, p.x());
                    xmax = java.lang.Math.max(xmax, p.x());
                    ymin = java.lang.Math.min(ymin, p.y());
                    ymax = java.lang.Math.max(ymax, p.y());
                }
            }
            if (f.edges != null) {
                for (asi.voronoi.Line ln : f.edges) {
                    asi.voronoi.Point b = ln.getBeginP().orElse(null);
                    asi.voronoi.Point e = ln.getEndP().orElse(null);
                    asi.voronoi.Point m = ln.getMidP();
                    if (b != null) {
                        xmin = java.lang.Math.min(xmin, b.x());
                        xmax = java.lang.Math.max(xmax, b.x());
                        ymin = java.lang.Math.min(ymin, b.y());
                        ymax = java.lang.Math.max(ymax, b.y());
                    }
                    if (e != null) {
                        xmin = java.lang.Math.min(xmin, e.x());
                        xmax = java.lang.Math.max(xmax, e.x());
                        ymin = java.lang.Math.min(ymin, e.y());
                        ymax = java.lang.Math.max(ymax, e.y());
                    }
                    if (b == null && e == null && m != null) {
                        xmin = java.lang.Math.min(xmin, m.x());
                        xmax = java.lang.Math.max(xmax, m.x());
                        ymin = java.lang.Math.min(ymin, m.y());
                        ymax = java.lang.Math.max(ymax, m.y());
                    }
                }
            }
            if (f.split != null) {
                asi.voronoi.Point mp = f.split.getMidP();
                xmin = java.lang.Math.min(xmin, mp.x());
                xmax = java.lang.Math.max(xmax, mp.x());
                ymin = java.lang.Math.min(ymin, mp.y());
                ymax = java.lang.Math.max(ymax, mp.y());
            }
        }
        for (asi.voronoi.Point p : allSites) {
            xmin = java.lang.Math.min(xmin, p.x());
            xmax = java.lang.Math.max(xmax, p.x());
            ymin = java.lang.Math.min(ymin, p.y());
            ymax = java.lang.Math.max(ymax, p.y());
        }
        if (!java.lang.Double.isFinite(xmin) || xmin == xmax || ymin == ymax) {
            xmin = 0;
            ymin = 0;
            xmax = 1;
            ymax = 1;
        }
        return new World(xmin, ymin, xmax, ymax);
    }

    private double sx(double x) {
        double contentW = java.lang.Math.max(1, canvas.getWidth() - 2 * pad);
        return pad + (x - viewXmin) * (contentW / java.lang.Math.max(1e-9, viewXmax - viewXmin));
    }

    private double sy(double y) {
        double contentH = java.lang.Math.max(1, canvas.getHeight() - 2 * pad);
        return canvas.getHeight() - pad - (y - viewYmin) * (contentH / java.lang.Math.max(1e-9, viewYmax - viewYmin));
    }

    private double screenToWorldX(double sxv) {
        double contentW = java.lang.Math.max(1, canvas.getWidth() - 2 * pad);
        double nx = (sxv - pad) / contentW;
        return viewXmin + nx * (viewXmax - viewXmin);
    }

    private double screenToWorldY(double syv) {
        double contentH = java.lang.Math.max(1, canvas.getHeight() - 2 * pad);
        double ny = (canvas.getHeight() - pad - syv) / contentH; // flip
        return viewYmin + ny * (viewYmax - viewYmin);
    }

    // Zoom om world-punkt (bruges af minimap)
    private void zoomAtWorldPoint(double factor, double wx, double wy) {
        double z = getZoomRatio();
        double nextZ = z * factor;
        if (nextZ > MAX_ZOOM) {
            factor = MAX_ZOOM / z;
        }
        if (nextZ < MIN_ZOOM) {
            factor = MIN_ZOOM / z;
        }
        double w = viewXmax - viewXmin;
        double h = viewYmax - viewYmin;
        double newW = w / factor;
        double newH = h / factor;
        // bevar samme relative position af (wx, wy)
        double rx = (wx - viewXmin) / w;
        double ry = (wy - viewYmin) / h;
        viewXmin = wx - rx * newW;
        viewXmax = viewXmin + newW;
        viewYmin = wy - ry * newH;
        viewYmax = viewYmin + newH;
        redrawCurrent();
    }

    private void centerViewOn(double wx, double wy) {
        double w = viewXmax - viewXmin;
        double h = viewYmax - viewYmin;
        viewXmin = wx - w / 2;
        viewXmax = wx + w / 2;
        viewYmin = wy - h / 2;
        viewYmax = wy + h / 2;
        redrawCurrent();
    }

    private boolean isInsideViewRect(double wx, double wy) {
        return wx >= viewXmin && wx <= viewXmax && wy >= viewYmin && wy <= viewYmax;
    }

    private void redrawCurrent() {
        if (frames.isEmpty()) {
            javafx.scene.canvas.GraphicsContext g = canvas.getGraphicsContext2D();
            g.setFill(javafx.scene.paint.Color.WHITE);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            // tegn sites overlay (allSites) hvis du vil – og captured overlay:
            if (!capturedPoints.isEmpty()) {
                g.setFill(javafx.scene.paint.Color.web("#a100ff"));
                for (asi.voronoi.Point s : capturedPoints) {
                    g.fillOval(sx(s.x()) - 3.5, sy(s.y()) - 3.5, 7, 7);
                }
            }
            drawMinimap();
            return;
        }
        drawFrame(canvas.getGraphicsContext2D(), frames.get(currentIndex));
        drawMinimap();
    }

    private void drawFrame(javafx.scene.canvas.GraphicsContext g, asi.voronoi.anim.StoryboardRecorder.Frame f) {
        double W = canvas.getWidth();
        double H = canvas.getHeight();
        g.setFill(javafx.scene.paint.Color.WHITE);
        g.fillRect(0, 0, W, H);
        // alle sites overlay
        g.setFill(javafx.scene.paint.Color.web("#444444"));
        g.setGlobalAlpha(0.65);
        for (asi.voronoi.Point s : allSites) {
            g.fillOval(sx(s.x()) - 2.5, sy(s.y()) - 2.5, 5, 5);
        }
        g.setGlobalAlpha(1.0);
        Main.frameStatus.setValue(f.label);
        // division: bbox
        if (f.bbox != null) {
            asi.voronoi.anim.Rect b = f.bbox;
            g.setGlobalAlpha(0.08);
            g.setFill(javafx.scene.paint.Color.web("#6c5ce7"));
            g.fillRect(sx(b.xMin()), sy(b.yMax()), java.lang.Math.abs(sx(b.xMax()) - sx(b.xMin())), java.lang.Math.abs(sy(b.yMin()) - sy(b.yMax())));
            g.setGlobalAlpha(1.0);
        }
        // split-linje
        if (f.split != null) {
            g.setStroke(javafx.scene.paint.Color.web("#636e72"));
            g.setLineDashes(8, 8);
            g.setLineWidth(1.4);
            asi.voronoi.Line s = f.split;
            asi.voronoi.Point mp = s.getMidP();
            asi.voronoi.Point d = s.getDir();
            double x1 = mp.x() - d.x() * 5000;
            double y1 = mp.y() - d.y() * 5000;
            double x2 = mp.x() + d.x() * 5000;
            double y2 = mp.y() + d.y() * 5000;
            g.strokeLine(sx(x1), sy(y1), sx(x2), sy(y2));
            g.setLineDashes();
        }
        // left/right punkter
        g.setFill(javafx.scene.paint.Color.web("#0984e3"));
        if (f.leftPts != null) {
            for (asi.voronoi.Point p : f.leftPts) {
                g.fillOval(sx(p.x()) - 3, sy(p.y()) - 3, 6, 6);
            }
        }
        g.setFill(javafx.scene.paint.Color.web("#d63031"));
        if (f.rightPts != null) {
            for (asi.voronoi.Point p : f.rightPts) {
                g.fillOval(sx(p.x()) - 3, sy(p.y()) - 3, 6, 6);
            }
        }
        // pivot
        if (f.pivot != null) {
            asi.voronoi.Point p = f.pivot;
            g.setFill(javafx.scene.paint.Color.web("#f2c94c"));
            g.fillOval(sx(p.x()) - 4, sy(p.y()) - 4, 8, 8);
        }
        // merge edges
        g.setStroke(javafx.scene.paint.Color.web("#2f80ed"));
        g.setLineWidth(2.0);
        if (f.edges != null) {
            for (asi.voronoi.Line ln : f.edges) {
                drawLineSegment(g, ln);
            }
        }
        // support marks
        if (f.marks != null && !f.marks.isEmpty()) {
            g.setFill(javafx.scene.paint.Color.web("#00b894"));
            for (asi.voronoi.Point p : f.marks) {
                g.fillOval(sx(p.x()) - 4, sy(p.y()) - 4, 8, 8);
            }
        }
        // Captured points overlay (magenta) – vises kun i captureMode
        if (captureMode && !capturedPoints.isEmpty()) {
            g.setFill(javafx.scene.paint.Color.web("#a100ff")); // magenta
            for (asi.voronoi.Point s : capturedPoints) {
                g.fillOval(sx(s.x()) - 3.5, sy(s.y()) - 3.5, 7, 7);
            }
        }
    }

    private void drawLineSegment(javafx.scene.canvas.GraphicsContext g, asi.voronoi.Line ln) {
        asi.voronoi.Point b = ln.getBeginP().orElse(null);
        asi.voronoi.Point e = ln.getEndP().orElse(null);
        if (b != null && e != null) {
            g.strokeLine(sx(b.x()), sy(b.y()), sx(e.x()), sy(e.y()));
        } else if (b != null) {
            asi.voronoi.Point d = ln.getDir();
            double x2 = b.x() + d.x() * 5000;
            double y2 = b.y() + d.y() * 5000;
            g.strokeLine(sx(b.x()), sy(b.y()), sx(x2), sy(y2));
        } else if (e != null) {
            asi.voronoi.Point d = ln.getDir();
            double x1 = e.x() - d.x() * 5000;
            double y1 = e.y() - d.y() * 5000;
            g.strokeLine(sx(x1), sy(y1), sx(e.x()), sy(e.y()));
        } else {
            asi.voronoi.Point m = ln.getMidP();
            asi.voronoi.Point d = ln.getDir();
            double x1 = m.x() - d.x() * 5000;
            double y1 = m.y() - d.y() * 5000;
            double x2 = m.x() + d.x() * 5000;
            double y2 = m.y() + d.y() * 5000;
            g.strokeLine(sx(x1), sy(y1), sx(x2), sy(y2));
        }
    }

    // ------------------ MINIMAP: tegning ------------------
    private void drawMinimap() {
        if (!minimapEnabled || world == null) {
            return;
        }
        javafx.scene.canvas.GraphicsContext g = minimap.getGraphicsContext2D();
        // baggrund / ramme
        g.setFill(javafx.scene.paint.Color.rgb(250, 250, 250, 0.92));
        g.fillRect(0, 0, minimap.getWidth(), minimap.getHeight());
        g.setStroke(javafx.scene.paint.Color.GRAY);
        g.setLineWidth(1);
        g.strokeRect(0.5, 0.5, minimap.getWidth() - 1, minimap.getHeight() - 1);
        // world fit til minimap med letterboxing
        double contentW = minimap.getWidth() - 2 * miniPad;
        double contentH = minimap.getHeight() - 2 * miniPad;
        double wW = world.xmax - world.xmin;
        double wH = world.ymax - world.ymin;
        if (wW <= 0 || wH <= 0) {
            return;
        }
        double sx = contentW / wW;
        double sy = contentH / wH;
        mmScale = Math.min(sx, sy);
        mmOx = miniPad + (contentW - wW * mmScale) / 2.0;
        mmOy = miniPad + (contentH - wH * mmScale) / 2.0;
        java.util.List<asi.voronoi.Point> miniSites = captureMode && !capturedPoints.isEmpty() ? capturedPoints : allSites;
        // tegn sites:
        g.setFill(javafx.scene.paint.Color.web("#444444"));
        g.setGlobalAlpha(0.85);
        for (asi.voronoi.Point p : miniSites) {
            double mx = mmx(p.x());
            double my = mmy(p.y());
            g.fillRect(mx - 1, my - 1, 2, 2);
        }
        g.setGlobalAlpha(1.0);
        // (optionelt) kanter – tyndt og lav alpha for performance
        asi.voronoi.anim.StoryboardRecorder.Frame f = frames.isEmpty() ? null : frames.get(currentIndex);
        if (f != null && f.edges != null) {
            g.setStroke(javafx.scene.paint.Color.web("#2f80ed", 0.35));
            g.setLineWidth(0.6);
            for (asi.voronoi.Line ln : f.edges) {
                asi.voronoi.Point b = ln.getBeginP().orElse(null);
                asi.voronoi.Point e = ln.getEndP().orElse(null);
                if (b != null && e != null) {
                    g.strokeLine(mmx(b.x()), mmy(b.y()), mmx(e.x()), mmy(e.y()));
                } else if (b != null) {
                    asi.voronoi.Point d = ln.getDir();
                    double x2 = b.x() + d.x() * 2000;
                    double y2 = b.y() + d.y() * 2000;
                    g.strokeLine(mmx(b.x()), mmy(b.y()), mmx(x2), mmy(y2));
                } else if (e != null) {
                    asi.voronoi.Point d = ln.getDir();
                    double x1 = e.x() - d.x() * 2000;
                    double y1 = e.y() - d.y() * 2000;
                    g.strokeLine(mmx(x1), mmy(y1), mmx(e.x()), mmy(e.y()));
                } else {
                    asi.voronoi.Point m = ln.getMidP();
                    asi.voronoi.Point d = ln.getDir();
                    double x1 = m.x() - d.x() * 2000;
                    double y1 = m.y() - d.y() * 2000;
                    double x2 = m.x() + d.x() * 2000;
                    double y2 = m.y() + d.y() * 2000;
                    g.strokeLine(mmx(x1), mmy(y1), mmx(x2), mmy(y2));
                }
            }
        }
        // view-rect (orange)
        double vx1 = mmx(viewXmin);
        double vy1 = mmy(viewYmax); // top-left (Y invert)
        double vx2 = mmx(viewXmax);
        double vy2 = mmy(viewYmin); // bottom-right
        double rw = Math.abs(vx2 - vx1);
        double rh = Math.abs(vy2 - vy1);
        double rx = Math.min(vx1, vx2);
        double ry = Math.min(vy1, vy2);
        g.setGlobalAlpha(0.15);
        g.setFill(javafx.scene.paint.Color.ORANGE);
        g.fillRect(rx, ry, rw, rh);
        g.setGlobalAlpha(1.0);
        g.setStroke(javafx.scene.paint.Color.ORANGE);
        g.setLineWidth(1.4);
        g.strokeRect(rx + 0.5, ry + 0.5, Math.max(0, rw - 1), Math.max(0, rh - 1));
    }

    // world->minimap
    private double mmx(double x) {
        return minimap.getLayoutX() == 0 ? (mmOx + (x - world.xmin) * mmScale) : (mmOx + (x - world.xmin) * mmScale);
    }

    private double mmy(double y) {
        // flip Y i minimap: (0,0) øverst-venstre
        return minimap.getHeight() - (mmOy + (y - world.ymin) * mmScale);
    }

    // minimap (lokale koordinater) -> world
    private static class WPoint {

        double x;
        double y;

        WPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private WPoint minimapToWorld(double mx, double my) {
        // tjek om inden for content (inkl. letterbox)
        double contentLeft = mmOx;
        double contentTop = mmOy;
        double contentRight = minimap.getWidth() - mmOx;
        double contentBottom = minimap.getHeight() - mmOy;
        if (mx < contentLeft || mx > contentRight || my < (minimap.getHeight() - contentBottom) || my > (minimap.getHeight() - contentTop)) {
            return null;
        }
        double wx = world.xmin + (mx - mmOx) / mmScale;
        double wy = world.ymin + ((minimap.getHeight() - my) - mmOy) / mmScale;
        return new WPoint(wx, wy);
    }
    
}
