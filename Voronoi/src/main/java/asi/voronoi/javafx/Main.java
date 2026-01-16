package asi.voronoi.javafx;

import asi.voronoi.tree.BinaryTree;
import asi.voronoi.Util;
import asi.voronoi.tree.ConveksHullTree;
import asi.voronoi.tree.VTree;
import asi.voronoi.Point;

// Animation / events
import asi.voronoi.anim.MedianDivideAnimator;
import asi.voronoi.anim.StoryboardRecorder;
import asi.voronoi.anim.VoronoiEvents;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

// JavaFX
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import static javafx.scene.input.KeyCode.ADD;
import static javafx.scene.input.KeyCode.DIGIT0;
import static javafx.scene.input.KeyCode.F;
import static javafx.scene.input.KeyCode.MINUS;
import static javafx.scene.input.KeyCode.PLUS;
import static javafx.scene.input.KeyCode.SUBTRACT;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

    private static String[] commandLineArgs; // hold the commandline args

    private final String dbFileName = "src/main/resources/VD.db";
    private static BinaryTree tree = new BinaryTree(); // Create a tree

    // Animation state
    private StoryboardRecorder recorder = new StoryboardRecorder();
    private BorderPane rootPane;
    private AnimationPane animationView;

    // Toolbar controls
    private ToolBar toolBar;
    private Button btnPlay, btnPause, btnResume, btnExport;
    private Button btnStepPrev, btnStepNext;
    private Slider speedSlider;
    private Label speedLabel;

// I klassens felter (sammen med de andre knapper):
    private Button btnZoomIn, btnZoomOut, btnReset, btnFit;
    private Label zoomLabel;

    private ToggleButton tglMinimap;
    private ChoiceBox<String> miniCorner;
    private Spinner<Integer> miniWSpin, miniHSpin;

    private HBox drawingBar;
    private Button btnFinishDraw, btnUndoDraw, btnClearDraw, btnCancelDraw;
    private Label drawCountLabel;
    
    private HBox statusBar;
    private Label statusLabel;

// Menu
    private MenuItem addPointsMenuItem;

    @Override
    public void start(Stage primaryStage) {
        // Event-bus
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        Menu points = new Menu("Points");
        points.setOnShowing(e -> System.out.println("Showing Points"));
        Menu convekshull = new Menu("Conveks Hull");
        Menu voronoi = new Menu("Voronoi Diagram");

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(points);
        menuBar.getMenus().add(convekshull);
        menuBar.getMenus().add(voronoi);

        addPointsMenuItem = new MenuItem("Add Points");
        addPointsMenuItem.setOnAction(e -> beginAddPoints());
        points.getItems().add(addPointsMenuItem);

        rootPane = new BorderPane();

        // --- Menu items ---
        MenuItem fromFile = new MenuItem("Read from file");
        fromFile.setOnAction(e -> {
            try {
                String path = "src/main/resources/";
                StringBuilder sb = new StringBuilder(path);
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(sb.toString()));
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile == null) {
                    return;
                }
                sb.delete(0, sb.length());
                sb.append(selectedFile.getParent());
                System.out.println("In read from file");
                tree = Util.bTreeFromPointSet(selectedFile);
                initialize(); // bygger CH + VD som før
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("File missing: " + ex.getMessage());
            }
        });
        points.getItems().add(fromFile);

        MenuItem showTree = new MenuItem("Show Tree");
        BTView view = new BTView(tree); // Create a View
        showTree.setOnAction(e -> {
            rootPane.setCenter(view);
            view.displayTree();
        });
        points.getItems().add(showTree);

        MenuItem fromDB = new MenuItem("Read from DB");
        fromDB.setOnAction(e -> {
            try {
                System.out.println("In read from DB");
                tree = Util.generateBTree(
                        Integer.parseInt(Main.commandLineArgs[0]),
                        dbFileName,
                        Integer.parseInt(Main.commandLineArgs[1])
                );
                initialize();
            } catch (SQLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("DB missing: " + ex.getMessage());
            }
        });
        points.getItems().add(fromDB);

        MenuItem animate = new MenuItem("Animate Divide & Merge");
        animate.setOnAction(e -> animateDivideAndMerge());
        voronoi.getItems().add(animate);

        MenuItem export = new MenuItem("Export PNG");
        export.setOnAction((var e) -> {
            if (animationView == null) {
                return;
            }
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Vælg mappe til PNG-eksport");
            File dir = dc.showDialog(primaryStage);
            if (dir == null) {
                return;
            }
            String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            String prefix = "voronoi_" + ts;
            try {
                animationView.exportPngs(dir, prefix);
                showInfo("Eksport OK", "PNG-filer gemt i:\n" + dir.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Eksport fejlede", ex.getMessage());
            }
        });
        voronoi.getItems().add(export);

        // --- Toolbar (Play/Pause/Speed/Export) ---
        toolBar = buildToolBar();

        VBox top = new VBox(menuBar, toolBar);
        rootPane.setTop(top);

        // Scene
        Scene scene = new Scene(rootPane, 900, 700);
        primaryStage.setTitle("Voronoi Diagram");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        commandLineArgs = args;
        launch(args);
    }

    private void initialize() {
        ConveksHullTree cht = new ConveksHullTree();
        cht.buildStructure(tree);
        cht.getInfo();
        VTree vt = new VTree();
        vt.buildStructure(tree);
        vt.getInfo();
    }

    // Hent alle punkter fra BinaryTree (in-order)
    private List<Point> collectPoints(BinaryTree t) {
        List<Point> pts = new ArrayList<>();
        collectRec(t, pts);
        return pts;
    }

    private void collectRec(BinaryTree n, List<Point> pts) {
        if (n == null) {
            return;
        }
        collectRec(n.lft(), pts);
        if (n.getP() != null) {
            pts.add(n.getP());
        }
        collectRec(n.rgt(), pts);
    }

    private void animateDivideAndMerge() {
        // Reset recorder/bus
        recorder = new StoryboardRecorder();
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        // 1) Division (x-akse, y-tiebreak; hvis alle x ens -> vandret split)
        List<Point> pts = collectPoints(tree);
        MedianDivideAnimator.animateDivide(pts);

        // 2) Merge (mikro-frames via DCEL.fireSnapshot() i sigma-trin)
        VTree vt = new VTree();
        vt.buildStructure(tree);

        // 3) Vis i center
        if (animationView == null) {
            animationView = new AnimationPane();
        }
        animationView.setSites(pts);
        animationView.setFrames(recorder.getFrames());
        zoomLabel.setText(String.format("Zoom %.0f%%", animationView.getZoomPercent()));
        animationView.setSpeed(speedSlider != null ? speedSlider.getValue() : 1.0);
        animationView.play();

        tglMinimap.setSelected(true);
        if (animationView != null) {
            animationView.setMinimapEnabled(true);
            // synkronisér hjørne og størrelse med toolbarens current values
            String v = miniCorner.getValue();
            switch (v) {
                case "Top-Left" ->
                    animationView.setMinimapPosition(AnimationPane.MinimapPos.TOP_LEFT);
                case "Top-Right" ->
                    animationView.setMinimapPosition(AnimationPane.MinimapPos.TOP_RIGHT);
                case "Bottom-Left" ->
                    animationView.setMinimapPosition(AnimationPane.MinimapPos.BOTTOM_LEFT);
                case "Bottom-Right" ->
                    animationView.setMinimapPosition(AnimationPane.MinimapPos.BOTTOM_RIGHT);
            }
            animationView.setMinimapSize(miniWSpin.getValue(), miniHSpin.getValue());
        }

        rootPane.setCenter(animationView);
        BorderPane.setMargin(animationView, new Insets(10));

        // Aktiver knapper
        setToolbarEnabled(true);
    }

    private ToolBar buildToolBar() {
        btnPlay = new Button("▶ Play");
        btnPause = new Button("⏸ Pause");
        btnResume = new Button("⏵ Resume");

        // NYT: Step ← / →
        btnStepPrev = new Button("⏮ Step ←");
        btnStepNext = new Button("Step → ⏭");

        btnZoomIn = new Button("＋ Zoom In");
        btnZoomOut = new Button("－ Zoom Out");
        btnReset = new Button("Reset");
        btnFit = new Button("Fit");

        zoomLabel = new Label("Zoom 100%");

        btnExport = new Button("Export PNGs…");

        speedSlider = new Slider(0.25, 3.0, 1.0);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(0.75);
        speedSlider.setMinorTickCount(2);
        speedSlider.setBlockIncrement(0.25);
        speedLabel = new Label("Speed 1.00×");

        btnPlay.setOnAction(e -> {
            if (animationView != null) {
                animationView.stop(); // genstart fra 0
                animationView.play();
            }
        });
        btnPause.setOnAction(e -> {
            if (animationView != null) {
                animationView.pause();
            }
        });
        btnResume.setOnAction(e -> {
            if (animationView != null) {
                animationView.resume();
            }
        });
        speedSlider.valueProperty().addListener((obs, oldV, newV) -> {
            double rate = newV.doubleValue();
            speedLabel.setText(String.format("Speed %.2f×", rate));
            if (animationView != null) {
                animationView.setSpeed(rate);
            }
        });

        // Step
        btnStepPrev.setOnAction(e -> {
            if (animationView != null) {
                animationView.pause();
                animationView.stepBack();
            }
        });
        btnStepNext.setOnAction(e -> {
            if (animationView != null) {
                animationView.pause();
                animationView.stepForward();
            }
        });

// Knap-handlers
        btnZoomIn.setOnAction(e -> {
            if (animationView != null) {
                animationView.zoomAtCenter(1.25);                 // 25% ind
                zoomLabel.setText(String.format("Zoom %.0f%%", animationView.getZoomPercent()));
            }
        });
        btnZoomOut.setOnAction(e -> {
            if (animationView != null) {
                animationView.zoomAtCenter(1.0 / 1.25);             // 20% ud
                zoomLabel.setText(String.format("Zoom %.0f%%", animationView.getZoomPercent()));
            }
        });
        btnReset.setOnAction(e -> {
            if (animationView != null) {
                animationView.resetView();
                zoomLabel.setText(String.format("Zoom %.0f%%", animationView.getZoomPercent()));
            }
        });
        btnFit.setOnAction(e -> {
            if (animationView != null) {
                animationView.fitToData();
                zoomLabel.setText(String.format("Zoom %.0f%%", animationView.getZoomPercent()));
            }
        });

        tglMinimap = new ToggleButton("Minimap");
        tglMinimap.setSelected(true);
        tglMinimap.setOnAction(e -> {
            if (animationView != null) {
                animationView.setMinimapEnabled(tglMinimap.isSelected());
            }
        });

        miniCorner = new ChoiceBox<>();
        miniCorner.getItems().addAll("Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right");
        miniCorner.setValue("Bottom-Right");
        miniCorner.setOnAction(e -> {
            if (animationView == null) {
                return;
            }
            String v = miniCorner.getValue();
            switch (v) {
                case "Top-Left" ->
                    animationView.setMinimapPosition(AnimationPane.MinimapPos.TOP_LEFT);
                case "Top-Right" ->
                    animationView.setMinimapPosition(AnimationPane.MinimapPos.TOP_RIGHT);
                case "Bottom-Left" ->
                    animationView.setMinimapPosition(AnimationPane.MinimapPos.BOTTOM_LEFT);
                case "Bottom-Right" ->
                    animationView.setMinimapPosition(AnimationPane.MinimapPos.BOTTOM_RIGHT);
            }
        });

        miniWSpin = new Spinner<>(100, 400, 220, 10);
        miniHSpin = new Spinner<>(80, 300, 160, 10);
        miniWSpin.valueProperty().addListener((o, ov, nv) -> {
            if (animationView != null) {
                animationView.setMinimapSize(nv, miniHSpin.getValue());
            }
        });
        miniHSpin.valueProperty().addListener((o, ov, nv) -> {
            if (animationView != null) {
                animationView.setMinimapSize(miniWSpin.getValue(), nv);
            }
        });

// ... og indsæt i ToolBar (placér fx efter step-knapperne):
        ToolBar tb = new ToolBar(
                btnPlay, btnPause, btnResume,
                new Separator(),
                btnStepPrev, btnStepNext,
                new Separator(),
                new Label("Zoom:"), btnZoomIn, btnZoomOut, btnReset, btnFit, zoomLabel,
                new Separator(),
                new Label("Minimap:"), tglMinimap,
                new Label("Pos:"), miniCorner,
                new Label("W:"), miniWSpin, new Label("H:"), miniHSpin,
                new Separator(),
                new Label("Speed:"), speedSlider, speedLabel,
                new Separator()
        //                new Label("FPS:"), fpsSpinner,
        //                new Separator(),
        );
        setToolbarEnabled(false);
        return tb;
    }

    private void setToolbarEnabled(boolean enabled) {
        btnPlay.setDisable(!enabled);
        btnPause.setDisable(!enabled);
        btnResume.setDisable(!enabled);
        btnStepPrev.setDisable(!enabled);
        btnStepNext.setDisable(!enabled);

        btnZoomIn.setDisable(!enabled);
        btnZoomOut.setDisable(!enabled);
        btnReset.setDisable(!enabled);
        btnFit.setDisable(!enabled);

        btnExport.setDisable(!enabled);
        speedSlider.setDisable(!enabled);

        tglMinimap.setDisable(!enabled);
        miniCorner.setDisable(!enabled);
        miniWSpin.setDisable(!enabled);
        miniHSpin.setDisable(!enabled);

    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    private void ensureDrawingBar() {
        if (drawingBar != null) {
            return;
        }

        btnFinishDraw = new Button("Finish (Enter)");
        btnUndoDraw = new Button("Undo (Backspace)");
        btnClearDraw = new Button("Clear");
        btnCancelDraw = new Button("Cancel (Esc)");
        drawCountLabel = new Label("Points: 0");

        btnFinishDraw.setOnAction(e -> finishAddPoints());
        btnUndoDraw.setOnAction(e -> {
            if (animationView != null && animationView.undoCapturedPoint()) {
                drawCountLabel.setText("Points: " + animationView.getCapturedSize());
            }
        });
        btnClearDraw.setOnAction(e -> {
            if (animationView != null) {
                animationView.clearCapturedPoints();
                drawCountLabel.setText("Points: 0");
            }
        });
        btnCancelDraw.setOnAction(e -> cancelAddPoints());

        drawingBar = new HBox(10, btnFinishDraw, btnUndoDraw, btnClearDraw, btnCancelDraw, new Separator(), drawCountLabel);
        drawingBar.setPadding(new Insets(6, 10, 6, 10));
    }

    private void beginAddPoints() {
        // Sørg for at have et view klar
        if (animationView == null) {
            animationView = new AnimationPane();
            rootPane.setCenter(animationView);
            BorderPane.setMargin(animationView, new Insets(10));
        }
        // Ryd alt fra tidligere animation
        recorder = new StoryboardRecorder();
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        // Stop afspilning og gå i capture-mode
        animationView.stop();
        animationView.startPointCapture();

// ... efter animationView.startPointCapture();
        animationView.requestFocus(); // så Enter/Esc/Backspace virker med det samme

        animationView.setOnCapturedCountChanged(n -> drawCountLabel.setText("Points: " + n));
//        drawCountLabel.setText("Points: 0");

        // UI
        ensureDrawingBar();
        rootPane.setBottom(drawingBar);
        setToolbarEnabled(false);          // disable play/step/export mens vi tegner
        zoomLabel.setText(String.format("Zoom %.0f%%", animationView.getZoomPercent()));

        // Tastaturgenveje: Enter (finish), Esc (cancel), Backspace (undo)
        var scene = rootPane.getScene();
        if (scene != null) {
            scene.setOnKeyPressed(ev -> {
                switch (ev.getCode()) {
                    case ENTER ->
                        finishAddPoints();
                    case ESCAPE ->
                        cancelAddPoints();
                    case BACK_SPACE, DELETE -> {
                        if (animationView != null && animationView.undoCapturedPoint()) {
                            drawCountLabel.setText("Points: " + animationView.getCapturedSize());
                        }
                    }
                    default -> {
                    }
                }
            });
        }
    }

    private void finishAddPoints() {
        if (animationView == null) {
            return;
        }
        var pts = animationView.getCapturedPoints();
        if (pts.isEmpty()) {
            showError("No points", "Add at least one point before finishing.");
            return;
        }
        // Byg nyt BinaryTree af punkterne (bevar din logik: første punkt som rod, derefter inserts)
        BinaryTree newTree = new BinaryTree(pts.get(0));
        for (int i = 1; i < pts.size(); i++) {
            newTree = newTree.insertNode(pts.get(i));
        }
        tree = newTree;

        animationView.stopPointCapture();
        rootPane.setBottom(null);
        setToolbarEnabled(true);

        // Kør animation pipeline på de nye punkter
        animateDivideAndMerge();
    }

    private void cancelAddPoints() {
        if (animationView != null) {
            animationView.stopPointCapture();
        }
        rootPane.setBottom(null);
        setToolbarEnabled(true);
        // behold eksisterende tree/animation uændret
    }

    private static class AnimationPane extends javafx.scene.layout.Pane {

        private final javafx.scene.canvas.Canvas canvas;
        private final javafx.scene.canvas.Canvas minimap;          // <<< NYT: minimap
        private javafx.animation.Timeline timeline;
        private final double pad = 30;

        private java.util.List<asi.voronoi.Point> allSites = java.util.List.of();
        private java.util.List<asi.voronoi.anim.StoryboardRecorder.Frame> frames = java.util.List.of();

// --- i AnimationPane: tilføj felter ---
        private boolean captureMode = false;
        private final java.util.List<asi.voronoi.Point> capturedPoints = new java.util.ArrayList<>();

        // world bounds (data)
        private static class World {

            double xmin, ymin, xmax, ymax;

            World(double xmin, double ymin, double xmax, double ymax) {
                this.xmin = xmin;
                this.ymin = ymin;
                this.xmax = xmax;
                this.ymax = ymax;
            }
        }
        private World world;

        // view-window (world coords, zoom/pan)
        private double viewXmin, viewXmax, viewYmin, viewYmax;
        private double initXmin, initXmax, initYmin, initYmax;
        private final double MIN_ZOOM = 0.05;   // 5%
        private final double MAX_ZOOM = 50.0;   // 5000%

        // timeline-konfiguration
        private final double frameMs = 200;
        private int currentIndex = 0;

        // pan state
        private double lastMouseX = Double.NaN, lastMouseY = Double.NaN;
        private boolean panning = false;

        // ------------------ MINIMAP STATE ------------------
        // Kan slås til/fra, flyttes hjørne, indstille størrelse
        private boolean minimapEnabled = true;

        enum MinimapPos {
            TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
        }
        private MinimapPos minimapPos = MinimapPos.BOTTOM_RIGHT;
        private double miniW = 220, miniH = 160;     // størrelse i pixels
        private final double miniPad = 6;                  // indre padding
        // beregnet til tegning (world->minimap)
        private double mmScale = 1.0, mmOx = 0.0, mmOy = 0.0;
        // drag i minimap
        private boolean miniDraggingRect = false;
        private double miniDragStartX = 0, miniDragStartY = 0;
        private double miniViewStartXmin, miniViewStartXmax, miniViewStartYmin, miniViewStartYmax;

        AnimationPane() {
            canvas = new javafx.scene.canvas.Canvas(900, 700);
            minimap = new javafx.scene.canvas.Canvas(miniW, miniH); // overlay canvas
            getChildren().addAll(canvas, minimap);
            // Gør Pane resizable og følg størrelsen
            widthProperty().addListener((o, ov, nv) -> resizeChildren());
            heightProperty().addListener((o, ov, nv) -> resizeChildren());

            installInteractions();
            installMinimapInteractions();   // <<< NYT
        }

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
            var g = canvas.getGraphicsContext2D();
            timeline = new javafx.animation.Timeline();
            for (int i = 0; i < frames.size(); i++) {
                final int idx = i;
                timeline.getKeyFrames().add(
                        new javafx.animation.KeyFrame(javafx.util.Duration.millis(frameMs * idx), ev -> {
                            currentIndex = idx;
                            drawFrame(g, frames.get(currentIndex));
                            drawMinimap(); // <<< NYT: opdatér minimap per frame
                        })
                );
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
            redrawCurrent();
        }

        void stepBack() {
            if (frames.isEmpty()) {
                return;
            }
            stop();
            currentIndex = java.lang.Math.max(currentIndex - 1, 0);
            redrawCurrent();
        }

        void exportPngs(java.io.File dir, String prefix) throws Exception {
            if (frames.isEmpty()) {
                return;
            }
            if (!dir.exists()) {
                java.nio.file.Files.createDirectories(dir.toPath());
            }
            var g = canvas.getGraphicsContext2D();

            boolean wasPlaying = timeline != null && timeline.getStatus() == javafx.animation.Animation.Status.RUNNING;
            stop();

            for (int i = 0; i < frames.size(); i++) {
                drawFrame(g, frames.get(i));
                var img = canvas.snapshot(new javafx.scene.SnapshotParameters(), null);
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
            double initW = (initXmax - initXmin);
            double curW = (viewXmax - viewXmin);
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
                var pt = minimapToWorld(ev.getX(), ev.getY());
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
                var p0 = minimapToWorld(miniDragStartX, miniDragStartY);
                var p1 = minimapToWorld(ev.getX(), ev.getY());
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
                var pt = minimapToWorld(ev.getX(), ev.getY());
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
            double xmin = java.lang.Double.POSITIVE_INFINITY, ymin = java.lang.Double.POSITIVE_INFINITY;
            double xmax = java.lang.Double.NEGATIVE_INFINITY, ymax = java.lang.Double.NEGATIVE_INFINITY;

            for (var f : frames) {
                if (f.bbox != null) {
                    xmin = java.lang.Math.min(xmin, f.bbox.xMin());
                    xmax = java.lang.Math.max(xmax, f.bbox.xMax());
                    ymin = java.lang.Math.min(ymin, f.bbox.yMin());
                    ymax = java.lang.Math.max(ymax, f.bbox.yMax());
                }
                if (f.leftPts != null) {
                    for (var p : f.leftPts) {
                        xmin = java.lang.Math.min(xmin, p.x());
                        xmax = java.lang.Math.max(xmax, p.x());
                        ymin = java.lang.Math.min(ymin, p.y());
                        ymax = java.lang.Math.max(ymax, p.y());
                    }
                }
                if (f.rightPts != null) {
                    for (var p : f.rightPts) {
                        xmin = java.lang.Math.min(xmin, p.x());
                        xmax = java.lang.Math.max(xmax, p.x());
                        ymin = java.lang.Math.min(ymin, p.y());
                        ymax = java.lang.Math.max(ymax, p.y());
                    }
                }
                if (f.pivot != null) {
                    var p = f.pivot;
                    xmin = java.lang.Math.min(xmin, p.x());
                    xmax = java.lang.Math.max(xmax, p.x());
                    ymin = java.lang.Math.min(ymin, p.y());
                    ymax = java.lang.Math.max(ymax, p.y());
                }
                if (f.marks != null) {
                    for (var p : f.marks) {
                        xmin = java.lang.Math.min(xmin, p.x());
                        xmax = java.lang.Math.max(xmax, p.x());
                        ymin = java.lang.Math.min(ymin, p.y());
                        ymax = java.lang.Math.max(ymax, p.y());
                    }
                }
                if (f.edges != null) {
                    for (asi.voronoi.Line ln : f.edges) {
                        var b = ln.getBeginP().orElse(null);
                        var e = ln.getEndP().orElse(null);
                        var m = ln.getMidP();
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
                    var mp = f.split.getMidP();
                    xmin = java.lang.Math.min(xmin, mp.x());
                    xmax = java.lang.Math.max(xmax, mp.x());
                    ymin = java.lang.Math.min(ymin, mp.y());
                    ymax = java.lang.Math.max(ymax, mp.y());
                }
            }
            for (var p : allSites) {
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
            return pad + (x - viewXmin) * (contentW / java.lang.Math.max(1e-9, (viewXmax - viewXmin)));
        }

        private double sy(double y) {
            double contentH = java.lang.Math.max(1, canvas.getHeight() - 2 * pad);
            return canvas.getHeight() - pad - (y - viewYmin) * (contentH / java.lang.Math.max(1e-9, (viewYmax - viewYmin)));
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
            double w = viewXmax - viewXmin, h = viewYmax - viewYmin;
            viewXmin = wx - w / 2;
            viewXmax = wx + w / 2;
            viewYmin = wy - h / 2;
            viewYmax = wy + h / 2;
            redrawCurrent();
        }

        private boolean isInsideViewRect(double wx, double wy) {
            return (wx >= viewXmin && wx <= viewXmax && wy >= viewYmin && wy <= viewYmax);
        }

        private void redrawCurrent() {
            if (frames.isEmpty()) {

                var g = canvas.getGraphicsContext2D();
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
            double W = canvas.getWidth(), H = canvas.getHeight();
            g.setFill(javafx.scene.paint.Color.WHITE);
            g.fillRect(0, 0, W, H);

            // alle sites overlay
            g.setFill(javafx.scene.paint.Color.web("#444444"));
            g.setGlobalAlpha(0.65);
            for (asi.voronoi.Point s : allSites) {
                g.fillOval(sx(s.x()) - 2.5, sy(s.y()) - 2.5, 5, 5);
            }
            g.setGlobalAlpha(1.0);

            // division: bbox
            if (f.bbox != null) {
                var b = f.bbox;
                g.setGlobalAlpha(0.08);
                g.setFill(javafx.scene.paint.Color.web("#6c5ce7"));
                g.fillRect(
                        sx(b.xMin()), sy(b.yMax()),
                        java.lang.Math.abs(sx(b.xMax()) - sx(b.xMin())),
                        java.lang.Math.abs(sy(b.yMin()) - sy(b.yMax()))
                );
                g.setGlobalAlpha(1.0);
            }
            // split-linje
            if (f.split != null) {
                g.setStroke(javafx.scene.paint.Color.web("#636e72"));
                g.setLineDashes(8, 8);
                g.setLineWidth(1.4);
                var s = f.split;
                var mp = s.getMidP();
                var d = s.getDir();
                double x1 = mp.x() - d.x() * 5000, y1 = mp.y() - d.y() * 5000;
                double x2 = mp.x() + d.x() * 5000, y2 = mp.y() + d.y() * 5000;
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
                var p = f.pivot;
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
            var b = ln.getBeginP().orElse(null);
            var e = ln.getEndP().orElse(null);
            if (b != null && e != null) {
                g.strokeLine(sx(b.x()), sy(b.y()), sx(e.x()), sy(e.y()));
            } else if (b != null) {
                var d = ln.getDir();
                double x2 = b.x() + d.x() * 5000, y2 = b.y() + d.y() * 5000;
                g.strokeLine(sx(b.x()), sy(b.y()), sx(x2), sy(y2));
            } else if (e != null) {
                var d = ln.getDir();
                double x1 = e.x() - d.x() * 5000, y1 = e.y() - d.y() * 5000;
                g.strokeLine(sx(x1), sy(y1), sx(e.x()), sy(e.y()));
            } else {
                var m = ln.getMidP();
                var d = ln.getDir();
                double x1 = m.x() - d.x() * 5000, y1 = m.y() - d.y() * 5000;
                double x2 = m.x() + d.x() * 5000, y2 = m.y() + d.y() * 5000;
                g.strokeLine(sx(x1), sy(y1), sx(x2), sy(y2));
            }
        }

        // ------------------ MINIMAP: tegning ------------------
        private void drawMinimap() {
            if (!minimapEnabled || world == null) {
                return;
            }
            var g = minimap.getGraphicsContext2D();

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

            java.util.List<asi.voronoi.Point> miniSites = captureMode && !capturedPoints.isEmpty()
                    ? capturedPoints
                    : allSites;

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
            var f = frames.isEmpty() ? null : frames.get(currentIndex);
            if (f != null && f.edges != null) {
                g.setStroke(javafx.scene.paint.Color.web("#2f80ed", 0.35));
                g.setLineWidth(0.6);
                for (asi.voronoi.Line ln : f.edges) {
                    var b = ln.getBeginP().orElse(null);
                    var e = ln.getEndP().orElse(null);
                    if (b != null && e != null) {
                        g.strokeLine(mmx(b.x()), mmy(b.y()), mmx(e.x()), mmy(e.y()));
                    } else if (b != null) {
                        var d = ln.getDir();
                        double x2 = b.x() + d.x() * 2000, y2 = b.y() + d.y() * 2000;
                        g.strokeLine(mmx(b.x()), mmy(b.y()), mmx(x2), mmy(y2));
                    } else if (e != null) {
                        var d = ln.getDir();
                        double x1 = e.x() - d.x() * 2000, y1 = e.y() - d.y() * 2000;
                        g.strokeLine(mmx(x1), mmy(y1), mmx(e.x()), mmy(e.y()));
                    } else {
                        var m = ln.getMidP();
                        var d = ln.getDir();
                        double x1 = m.x() - d.x() * 2000, y1 = m.y() - d.y() * 2000;
                        double x2 = m.x() + d.x() * 2000, y2 = m.y() + d.y() * 2000;
                        g.strokeLine(mmx(x1), mmy(y1), mmx(x2), mmy(y2));
                    }
                }
            }

            // view-rect (orange)
            double vx1 = mmx(viewXmin), vy1 = mmy(viewYmax); // top-left (Y invert)
            double vx2 = mmx(viewXmax), vy2 = mmy(viewYmin); // bottom-right
            double rw = Math.abs(vx2 - vx1), rh = Math.abs(vy2 - vy1);
            double rx = Math.min(vx1, vx2), ry = Math.min(vy1, vy2);

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

            double x, y;

            WPoint(double x, double y) {
                this.x = x;
                this.y = y;
            }
        }

        private WPoint minimapToWorld(double mx, double my) {
            // tjek om inden for content (inkl. letterbox)
            double contentLeft = mmOx, contentTop = mmOy;
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

}
