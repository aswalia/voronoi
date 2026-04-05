package asi.voronoi.javafx;

import asi.voronoi.ConveksHull;
import asi.voronoi.DCEL;
import asi.voronoi.DatabaseHandler;
import asi.voronoi.DatabaseHandler.GroupNames;
import asi.voronoi.tree.BinaryTree;
import asi.voronoi.Util;
import asi.voronoi.tree.ConveksHullTree;
import asi.voronoi.tree.VTree;
import asi.voronoi.Point;

// Animation / events
import asi.voronoi.anim.MedianDivideAnimator;
import asi.voronoi.anim.StoryboardRecorder;
import asi.voronoi.anim.VoronoiEvents;
import asi.voronoi.tree.AVLTree;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// JavaFX
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import static javafx.scene.input.KeyCode.BACK_SPACE;
import static javafx.scene.input.KeyCode.DELETE;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

    private static String[] commandLineArgs; // hold the commandline args

    private static final String DB_FILENAME = "src/main/resources/VD.db";   
    private static Map<Integer, Point> mappedPoints = new HashMap<>();
    private static BinaryTree binTree = new BinaryTree(); // Create a binTree
    private static ConveksHull conveksHull = new ConveksHull();
    private static DCEL voronoi = new DCEL();

    // Animation state
    private StoryboardRecorder recorder = new StoryboardRecorder();
    private BorderPane rootPane;
    private AnimationView animationView;

    private MenuBar menuBar;
    private ToolBar dataBar;
    private ToggleGroup dataTypeGroup;
    private RadioButton pointButton;
    private RadioButton bTreeButton;
    private RadioButton conveksHullButton;
    private RadioButton voronoiButton;
    private ChoiceBox<GroupNames> dataSetGroup;
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


    @Override
    public void start(Stage primaryStage) {
        // Event-bus
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        rootPane = new BorderPane();

/*        showTree.setOnAction(e -> {
            BinaryTreeView btv = new BinaryTreeView();               
            btv.renderTree(binTree);
            rootPane.setCenter(btv);
        });

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
*/
        // --- Toolbar (Play/Pause/Speed/Export) ---
        dataBar = buildDataBar();
        menuBar = buildMenuBar(primaryStage);
        toolBar = buildToolBar();
        statusBar = buildStatusBar();

        VBox top = new VBox(dataBar, menuBar, toolBar, statusBar);
        rootPane.setTop(top);

        // Scene
        Scene scene = new Scene(rootPane, 1000, 800);
        primaryStage.setTitle("Voronoi Diagram");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        DatabaseHandler.connectToDatabase(DB_FILENAME);
        DatabaseHandler.createContent();
        commandLineArgs = args;
        launch(args);
    }

    private void initialize() {
        ConveksHullTree cht = new ConveksHullTree();
        cht.buildStructure(binTree);
        cht.getInfo();
        VTree vt = new VTree();
        vt.buildStructure(binTree);
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
        List<Point> pts = collectPoints(binTree);
        MedianDivideAnimator.animateDivide(pts);

        // 2) Merge (mikro-frames via DCEL.fireSnapshot() i sigma-trin)
        VTree vt = new VTree();
        vt.buildStructure(binTree);

        // 3) Vis i center
        if (animationView == null) {
            animationView = new AnimationView();
            // forward frame status updates to the Main frameStatus property
            animationView.setOnFrameLabelChanged(s -> frameStatus.setValue(s));
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
    
    private ToolBar buildDataBar() {
        dataTypeGroup = new ToggleGroup();
        pointButton = new RadioButton("points");
        pointButton.setUserData("points");
        pointButton.setToggleGroup(dataTypeGroup);
        pointButton.setSelected(false);
        bTreeButton = new RadioButton("binary tree");
        bTreeButton.setUserData("binary tree");
        bTreeButton.setToggleGroup(dataTypeGroup);
        bTreeButton.setSelected(false);
        conveksHullButton = new RadioButton("conveks hull");
        conveksHullButton.setUserData("conveks hull");
        conveksHullButton.setToggleGroup(dataTypeGroup);
        conveksHullButton.setSelected(false);
        voronoiButton = new RadioButton("voronoi diagram");
        voronoiButton.setUserData("voronoi diagram");
        voronoiButton.setToggleGroup(dataTypeGroup);
        voronoiButton.setSelected(false);
        List<GroupNames> gn = DatabaseHandler.getNamesByGroup();
        ObservableList<GroupNames> names = FXCollections.observableArrayList(gn);
        dataSetGroup = new ChoiceBox<>(names);
        ToolBar db = new ToolBar(
                new Label("Data type: "),
                pointButton,
                bTreeButton,
                conveksHullButton,
                voronoiButton,
                new Separator(),
                new Label("Data set group: "),
                dataSetGroup);
        return db;
    }
    
    private MenuBar buildMenuBar(Stage primaryStage) {
        return new VoronoiMenuView(this, primaryStage).getVoronoiMenuBar();        
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
                    animationView.setMinimapPosition(MinimapView.MinimapPos.TOP_LEFT);
                case "Top-Right" ->
                    animationView.setMinimapPosition(MinimapView.MinimapPos.TOP_RIGHT);
                case "Bottom-Left" ->
                    animationView.setMinimapPosition(MinimapView.MinimapPos.BOTTOM_LEFT);
                case "Bottom-Right" ->
                    animationView.setMinimapPosition(MinimapView.MinimapPos.BOTTOM_RIGHT);
            }
            animationView.getMinimapView().layoutMinimap(animationView.getWidth(), animationView.getHeight());
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

    private void finishAddPoints() {
        if (animationView == null) {
            return;
        }
        var pts = animationView.getCapturedPoints();
        showInfo("Canvas", "Points: " + pts + "\nNumber of points: " + pts.size());
    }

    private void cancelAddPoints() {
        if (animationView != null) {
            animationView.stopPointCapture();
        }
        rootPane.setBottom(null);
        setToolbarEnabled(true);
        // behold eksisterende binTree/animation uændret
    }
    
    // -------------- Menu Item Actions --------------------
    
    void setFromCanvas() {
        // Sørg for at have et view klar
        if (animationView == null) {
            animationView = new AnimationView();
            animationView.setOnFrameLabelChanged(s -> frameStatus.setValue(s));
            rootPane.setCenter(animationView);
            BorderPane.setMargin(animationView, new Insets(10));
        }
/*        // Ryd alt fra tidligere animation
        recorder = new StoryboardRecorder();
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        // Stop afspilning og gå i capture-mode
        animationView.stop();
        animationView.clearOverlay();
*/        animationView.startPointCapture();
        // ... efter animationView.startPointCapture();
        animationView.requestFocus(); // så Enter/Esc/Backspace virker med det samme

        animationView.setOnCapturedCountChanged(n -> drawCountLabel.setText("Points: " + n));
        //        drawCountLabel.setText("Points: 0");

        // UI
        ensureDrawingBar();
        rootPane.setBottom(drawingBar);
        setToolbarEnabled(false);          // disable play/step/export mens vi tegner
//        zoomLabel.setText(String.format("Zoom %.0f%%", animationView.getZoomPercent()));

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

    void setFromFile(Stage stage) {
        try {
            String path = "src/main/resources/";
            StringBuilder sb = new StringBuilder(path);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(sb.toString()));
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile == null) {
                return;
            }
            sb.delete(0, sb.length());
            sb.append(selectedFile.getParent());
            mappedPoints = Util.getPoints(selectedFile);
            showInfo("Source - File", "Number of points read: " + mappedPoints.size());
        } catch (Exception ex) {
            showError("PointSet Error", ex.getMessage());
        }
    }

    void setFromDB() {
        int grp = dataSetGroup.getValue().grp();
        Toggle bt = dataTypeGroup.getSelectedToggle();
        if (bt.getUserData().equals("points"))  {
            mappedPoints = DatabaseHandler.getPointsByGroup(grp);
            showInfo("Source - Database", "Number of points read: " + mappedPoints.size());
        } 
        if (bt.getUserData().equals("binary tree")) {
            binTree = DatabaseHandler.getBinaryTreeByGroup(grp);
            showInfo("Source - Database", "BinaryTree size: " + binTree.count());
        }
        if (bt.getUserData().equals("conveks hull")) {
            conveksHull = DatabaseHandler.getConveksHullByGroup(grp);
            showInfo("Source - Database", "ConveksHull size: " + conveksHull.size());
        }
        if (bt.getUserData().equals("voronoi diagram")) {
            voronoi = DatabaseHandler.getVoronoiDiagramByGroup(grp);
            showInfo("Source - Database", "Voronoi Diagram size: " + voronoi.size());
        }
    }

    void doPoints() {
        showInfo("Function", "Clicked Points");
    }

    void doBTree() {
        showInfo("Function", "Clicked Binary Tree");
    }

    void doConveksHull() {
        showInfo("Function", "Clicked Conveks Hull");
    }

    void doVoronoi() {
        showInfo("Function", "Clicked Voronoi Diagram");
    }

    void drawRepresentation() {
        showInfo("View", "Clicked Representation");
    }

    void drawGeometric() {
        showInfo("View", "Clicked Geometric");
    }

    void drawStatic() {
        showInfo("Temporal", "Clicked Static");
    }

    void drawAnimation() {
        if (animationView == null) {
            return;
        }
        var pts = animationView.getCapturedPoints();
        // Byg nyt BinaryTree af punkterne (bevar din logik: første punkt som rod, derefter inserts)
        BinaryTree newTree = new AVLTree(pts.get(0));
        for (int i = 1; i < pts.size(); i++) {
            newTree = newTree.insertNode(pts.get(i));
        }
        binTree = newTree;

        rootPane.setBottom(null);
        setToolbarEnabled(true);

        // Kør animation pipeline på de nye punkter
        animateDivideAndMerge();
    }

    
    
    
}
