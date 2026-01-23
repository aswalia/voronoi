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
import java.util.List;

// JavaFX
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private AnimationView animationView;

    // Hvilken afspiller toolbaren styrer
    private enum PlaybackMode { VORONOI, BINARY_TREE }
    private PlaybackMode mode = PlaybackMode.VORONOI;

    // Toolbar controls
    private ToolBar toolBar;
    private Button btnPlay, btnPause, btnResume, btnExport;
    private Button btnStepPrev, btnStepNext;
    private Slider speedSlider;
    private Label speedLabel;

    private static ToolBar statusBar;
    static final StringProperty frameStatus = new SimpleStringProperty();

// I klassens felter (sammen med de andre knapper):
    private Button btnZoomIn, btnZoomOut, btnReset, btnFit;
    private Label zoomLabel;

    private ToggleButton tglMinimap;
    private ChoiceBox<String> miniCorner;
    private Spinner<Integer> miniWSpin, miniHSpin;

    private HBox drawingBar;
    private Button btnFinishDraw, btnUndoDraw, btnClearDraw, btnCancelDraw;
    private Label drawCountLabel;
    
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
//                System.out.println("In read from file");
                tree = Util.bTreeFromPointSet(selectedFile);
                initialize(); // bygger CH + VD som før
            } catch (Exception ex) {
                showError("Error building Voronoi diagram: ", ex.getMessage());
            }
        });
        points.getItems().add(fromFile);

        MenuItem showTree = new MenuItem("Show Tree");
        showTree.setOnAction(e -> animateBinaryTree());
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
                showError("Database error: ", ex.getMessage());
            }
        });
        points.getItems().add(fromDB);

        MenuItem animate = new MenuItem("Animate Divide & Merge");
        animate.setOnAction(e -> animateDivideAndMerge());
        voronoi.getItems().add(animate);

        MenuItem export = new MenuItem("Export PNG");
        export.setOnAction((ActionEvent e) -> {
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
                showInfo("Export done", "PNG-fils svaed in:\n" + dir.getAbsolutePath());
            } catch (Exception ex) {
                showError("Export failed", ex.getMessage());
            }
        });
        voronoi.getItems().add(export);

        // --- Toolbar (Play/Pause/Speed/Export) ---
        toolBar = buildToolBar();
        statusBar = buildStatusBar();
        
        VBox top = new VBox(menuBar, toolBar, statusBar);
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
    
    
     private void animateBinaryTree() {
        mode = PlaybackMode.BINARY_TREE;
        // Reset recorder/bus
        recorder = new StoryboardRecorder();
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        // Sites til minimap/overview
        List<Point> pts = asi.voronoi.tree.BinaryTree.collectPoints(tree);

        // Sørg for view
        if (animationView == null) {
            animationView = new AnimationView();
        }
        // Ryd Voronoi-lag og skift mode i pane:
        animationView.setVisualizationMode(AnimationView.Mode.BINARY_TREE);

        animationView.setSites(pts);             // behold eksisterende features
        animationView.renderBinaryTree(tree);    // tegn statisk BinaryTree
        animationView.resetBinaryTreeColors();   // nulstil farver
        animationView.setBtStatusSink(txt -> frameStatus.set(txt)); // ← statusBar opdatering

        // Optag traversal events
        BinaryTree.inorder(0, tree);

        // Vis i center
        rootPane.setCenter(animationView);
        BorderPane.setMargin(animationView, new Insets(10));

        // Afspil med fps proportional med speed-slider
        double rate = (speedSlider != null ? speedSlider.getValue() : 1.0);
        double fps = Math.max(1.0, 24.0 * rate);
        animationView.playBinaryTreeStoryboard(recorder, fps);
        zoomLabel.setText(String.format("Zoom %.0f%%", animationView.getZoomPercent()));

        // Aktiver knapper
        setToolbarEnabled(true);

     }
   
    

    private void animateDivideAndMerge() {
        mode = PlaybackMode.VORONOI;
        // Reset recorder/bus
        recorder = new StoryboardRecorder();
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        // 1) Division (x-akse, y-tiebreak; hvis alle x ens -> vandret split)
        List<Point> pts = asi.voronoi.tree.BinaryTree.collectPoints(tree);
        MedianDivideAnimator.animateDivide(pts);

        // 2) Merge (mikro-frames via DCEL.fireSnapshot() i sigma-trin)
        VTree vt = new VTree();
        vt.buildStructure(tree);

        // 3) Vis i center
        if (animationView == null) {
            animationView = new AnimationView();
        }
        // Ryd BinaryTree-lag og skift mode i pane:
        animationView.setVisualizationMode(AnimationView.Mode.VORONOI);

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
                    animationView.setMinimapPosition(MinimapView.MinimapPos.TOP_LEFT);
                case "Top-Right" ->
                    animationView.setMinimapPosition(MinimapView.MinimapPos.TOP_RIGHT);
                case "Bottom-Left" ->
                    animationView.setMinimapPosition(MinimapView.MinimapPos.BOTTOM_LEFT);
                case "Bottom-Right" ->
                    animationView.setMinimapPosition(MinimapView.MinimapPos.BOTTOM_RIGHT);
            }
            animationView.setMinimapSize(miniWSpin.getValue(), miniHSpin.getValue());
        }

        rootPane.setCenter(animationView);
        BorderPane.setMargin(animationView, new Insets(10));

        // Aktiver knapper
        setToolbarEnabled(true);
    }
    
    private ToolBar buildStatusBar() {
        Label statusText = new Label();
        statusText.textProperty().bind(frameStatus);
        
        ToolBar sb = new ToolBar(
            statusText
        );
        
        return sb;

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
            if (animationView == null) return;
            if (mode == PlaybackMode.BINARY_TREE) {
                animationView.stopBinaryTreeStoryboard();
                animationView.resetBinaryTreeColors();
                double rate = speedSlider != null ? speedSlider.getValue() : 1.0;
                double fps = Math.max(1.0, 24.0 * rate);
                animationView.playBinaryTreeStoryboard(recorder, fps);
            } else {
                animationView.stop(); // Voronoi
                animationView.play();
            }
        });
        btnPause.setOnAction(e -> {
            if (animationView == null) return;
            if (mode == PlaybackMode.BINARY_TREE) {
                animationView.pauseBinaryTreeStoryboard();
            } else {
                animationView.pause();
            }
        });
        btnResume.setOnAction(e -> {
            if (animationView == null) return;
            if (mode == PlaybackMode.BINARY_TREE) {
                animationView.resumeBinaryTreeStoryboard();
            } else {
                animationView.resume();
            }
        });

        speedSlider.valueProperty().addListener((obs, oldV, newV) -> {
            double rate = newV.doubleValue();
            speedLabel.setText(String.format("Speed %.2f×", rate));
            if (animationView == null) return;
            if (mode == PlaybackMode.BINARY_TREE) {
                animationView.setBinaryTreeRate(rate);
            } else {
                animationView.setSpeed(rate);
            }
        });

        btnStepPrev.setOnAction(e -> {
            if (animationView == null) return;
            if (mode == PlaybackMode.BINARY_TREE) {
                animationView.pauseBinaryTreeStoryboard();
                animationView.stepBackBinaryTreeStoryboard(recorder);
            } else {
                animationView.pause();
                animationView.stepBack();
            }
        });
        btnStepNext.setOnAction(e -> {
            if (animationView == null) return;
            if (mode == PlaybackMode.BINARY_TREE) {
                animationView.pauseBinaryTreeStoryboard();
                animationView.stepBinaryTreeStoryboard(recorder);
            } else {
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
                if (mode == PlaybackMode.BINARY_TREE) {
                    animationView.fitBinaryTreeToView();
                } else {
                    animationView.fitToData();
                }
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
                    animationView.setMinimapPosition(MinimapView.MinimapPos.TOP_LEFT);
                case "Top-Right" ->
                    animationView.setMinimapPosition(MinimapView.MinimapPos.TOP_RIGHT);
                case "Bottom-Left" ->
                    animationView.setMinimapPosition(MinimapView.MinimapPos.BOTTOM_LEFT);
                case "Bottom-Right" ->
                    animationView.setMinimapPosition(MinimapView.MinimapPos.BOTTOM_RIGHT);
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
                new Label("Speed:"), speedSlider, speedLabel
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
            animationView = new AnimationView();
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
}
