package asi.voronoi.javafx;

import asi.voronoi.Point;
import asi.voronoi.Util;
import asi.voronoi.anim.MedianDivideAnimator;
import asi.voronoi.anim.StoryboardRecorder;
import asi.voronoi.anim.VoronoiEvents;

import asi.voronoi.tree.BinaryTree;
import asi.voronoi.tree.ConveksHullTree;
import asi.voronoi.tree.VTree;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main extends Application {

    private final String dbFileName = "src/main/resources/VD.db";
    private static String[] commandLineArgs;  // Command-line arguments
    private static BinaryTree tree = new BinaryTree();  // Voronoi binary tree structure for data

    // UI components and application state
    private StoryboardRecorder recorder = new StoryboardRecorder();
    private BorderPane rootPane;
    private AnimationView animationView;

    // Toolbar controls
    private Button btnPlay, btnPause, btnResume, btnExport;
    private Button btnStepPrev, btnStepNext;
    private Button btnZoomIn, btnZoomOut, btnReset, btnFit;
    private ToggleButton tglMinimap;
    private Spinner<Integer> miniWSpin, miniHSpin;
    private ChoiceBox<String> miniCorner;
    private Slider speedSlider;
    private Label speedLabel;

    private static ToolBar statusBar;
    static final StringProperty frameStatusProperty = new SimpleStringProperty();

    @Override
    public void start(Stage primaryStage) {
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        rootPane = new BorderPane();

        // Build Menu and Toolbar
        MenuBar menuBar = buildMenuBar(primaryStage);
        ToolBar toolBar = buildToolBar(primaryStage);
        statusBar = buildStatusBar();

        VBox top = new VBox(menuBar, toolBar, statusBar);
        rootPane.setTop(top);

        // Scene setup
        Scene scene = new Scene(rootPane, 900, 700);
        primaryStage.setTitle("Voronoi Diagram");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        commandLineArgs = args;
        launch(args);
    }

    private MenuBar buildMenuBar(Stage primaryStage) {
        Menu pointsMenu = buildPointsMenu(primaryStage);
        Menu voronoiMenu = buildVoronoiMenu(primaryStage);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(pointsMenu, voronoiMenu);
        return menuBar;
    }

    private Menu buildPointsMenu(Stage primaryStage) {
        Menu points = new Menu("Points");

        MenuItem addPoints = new MenuItem("Add Points");
        addPoints.setOnAction(e -> beginAddPoints());
        points.getItems().add(addPoints);

        MenuItem fromFile = new MenuItem("Read from File");
        fromFile.setOnAction(e -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File("src/main/resources/"));
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile == null) {
                    return;
                }

                tree = Util.bTreeFromPointSet(selectedFile);
                initializeBinaryTree();
            } catch (Exception ex) {
                showError("Error Reading File", ex.getMessage());
            }
        });
        points.getItems().add(fromFile);

        MenuItem fromDB = new MenuItem("Read from DB");
        fromDB.setOnAction(e -> {
            try {
                tree = Util.generateBTree(
                        Integer.parseInt(commandLineArgs[0]),
                        dbFileName,
                        Integer.parseInt(commandLineArgs[1])
                );
                initializeBinaryTree();
            } catch (SQLException ex) {
                showError("Database Error", ex.getMessage());
            }
        });
        points.getItems().add(fromDB);

        return points;
    }

    private Menu buildVoronoiMenu(Stage primaryStage) {
        Menu voronoi = new Menu("Voronoi Diagram");

        MenuItem animate = new MenuItem("Animate Divide & Merge");
        animate.setOnAction(e -> animateDivideAndMerge());
        voronoi.getItems().add(animate);

        MenuItem export = new MenuItem("Export PNG");
        export.setOnAction(e -> {
            if (animationView == null) {
                return;
            }

            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select Directory for PNG Export");
            File dir = dirChooser.showDialog(primaryStage);
            if (dir == null) {
                return;
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String prefix = "voronoi_" + timestamp;

            try {
                animationView.exportPngs(dir, prefix);
                showInfo("Export Complete", "PNGs have been saved to:\n" + dir.getAbsolutePath());
            } catch (IOException ex) {
                showError("Export Failed", ex.getMessage());
            }
        });
        voronoi.getItems().add(export);

        return voronoi;
    }

    // Updates the Voronoi binary tree structure with collected points
    private void initializeBinaryTree() {
        ConveksHullTree cht = new ConveksHullTree();
        cht.buildStructure(tree);

        VTree vt = new VTree();
        vt.buildStructure(tree);
    }

    private void animateDivideAndMerge() {
        // Reset the animation state
        recorder = new StoryboardRecorder();
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        // 1) Division (x-akse, y-tiebreak; hvis alle x ens -> vandret split)
        List<Point> pts = collectPoints(tree);
        MedianDivideAnimator.animateDivide(pts);

        // 2) Merge (mikro-frames via DCEL.fireSnapshot() i sigma-trin)
        VTree vt = new VTree();
        vt.buildStructure(tree);

        if (animationView == null) {
            animationView = new AnimationView();
            animationView.getAnimator().setOnFrame(frame -> {
                frameStatusProperty.setValue(frame != null ? "Frame: " + frame.label : "No frame");
                animationView.drawFrame(frame); // Render frame
            });
        }

        // Set animation data for AnimationView
        animationView.setAnimationData(points, recorder.getFrames());

        // Fit the view to the data and play the animation
        animationView.fitToData();
        animationView.getAnimator().play();

        // Attach the view to the UI
        rootPane.setCenter(animationView);
        BorderPane.setMargin(animationView, new Insets(10));
    }

    private void beginAddPoints() {
        if (animationView == null) {
            animationView = new AnimationView();

            animationView.getAnimator().setOnFrame(frame -> frameStatusProperty.setValue(frame != null ? frame.label : ""));
            rootPane.setCenter(animationView);
            BorderPane.setMargin(animationView, new Insets(10));
        }

        recorder = new StoryboardRecorder();
        VoronoiEvents.clear();
        VoronoiEvents.add(recorder);

        animationView.pause();
        animationView.getPointCaptureManager().clearPoints();  // Reset captured points
        animationView.getPointCaptureManager().startCaptureMode();  // Start a new capture
    }

    private ToolBar buildToolBar(Stage primaryStage) {
        // Playback buttons
        btnPlay = new Button("▶ Play");
        btnPause = new Button("⏸ Pause");
        btnResume = new Button("⏵ Resume");
        btnStepPrev = new Button("⏮ Step ←");
        btnStepNext = new Button("Step → ⏭");

        btnExport = new Button("Export PNGs…"); // Add this line to the 'buildToolBar' method in Main.

        btnExport.setOnAction(e -> {
            if (animationView == null) {
                return;
            }

            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select Directory for PNG Export");
            File dir = dirChooser.showDialog(primaryStage);

            if (dir == null) {
                return;
            }

            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            String prefix = "voronoi_" + timestamp;

            try {
                animationView.exportPngs(dir, prefix);
                showInfo("Export Complete", "PNGs saved to:\n" + dir.getAbsolutePath());
            } catch (IOException ex) {
                showError("Export Failed", ex.getMessage());
            }
        });

        // Zoom buttons
        btnZoomIn = new Button("＋ Zoom In");
        btnZoomOut = new Button("－ Zoom Out");
        btnReset = new Button("Reset View");
        btnFit = new Button("Fit to Data");

        // Minimap toggle
        tglMinimap = new ToggleButton("Minimap");
        tglMinimap.setSelected(true);
        tglMinimap.setOnAction(e -> {
            if (animationView != null) {
                animationView.setMinimapEnabled(tglMinimap.isSelected());
            }
        });

        // Minimap width and height spinners
        miniWSpin = new Spinner<>(100, 400, 220, 10); // Minimum 100, maximum 400, step 10
        miniHSpin = new Spinner<>(100, 400, 160, 10); // Minimum 100, maximum 300, step 10

        miniWSpin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (animationView != null) {
                animationView.setMinimapSize(newVal, miniHSpin.getValue());
            }
        });
        miniHSpin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (animationView != null) {
                animationView.setMinimapSize(miniWSpin.getValue(), newVal);
            }
        });

        // Speed slider
        speedSlider = new Slider(0.25, 3.0, 1.0); // Min, max, initial value
        speedLabel = new Label("Speed 1.00×");
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double rate = newVal.doubleValue();
            speedLabel.setText(String.format("Speed %.2f×", rate));
            if (animationView != null) {
                animationView.setSpeed(rate);
            }
        });

        // Wire playback buttons
        btnPlay.setOnAction(e -> {
            if (animationView != null) {
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
        btnStepPrev.setOnAction(e -> {
            if (animationView != null) {
                animationView.stepBack();
            }
        });
        btnStepNext.setOnAction(e -> {
            if (animationView != null) {
                animationView.stepForward();
            }
        });

        // Wire zoom buttons
        btnZoomIn.setOnAction(e -> {
            if (animationView != null) {
                animationView.zoomAtCenter(1.25); // 25% zoom in
            }
        });
        btnZoomOut.setOnAction(e -> {
            if (animationView != null) {
                animationView.zoomAtCenter(1.0 / 1.25); // 20% zoom out
            }
        });
        btnReset.setOnAction(e -> {
            if (animationView != null) {
                animationView.resetView();
            }
        });
        btnFit.setOnAction(e -> {
            if (animationView != null) {
                animationView.fitToData();
            }
        });

        // Minimap position choice box
        miniCorner = new ChoiceBox<>();
        miniCorner.getItems().addAll("Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right");
        miniCorner.setValue("Bottom-Right"); // Default position
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
                String position = miniCorner.getValue();
                switch (position) {
                    case "Top-Left" ->
                        animationView.setMinimapPosition(MinimapView.MinimapPos.TOP_LEFT);
                    case "Top-Right" ->
                        animationView.setMinimapPosition(MinimapView.MinimapPos.TOP_RIGHT);
                    case "Bottom-Left" ->
                        animationView.setMinimapPosition(MinimapView.MinimapPos.BOTTOM_LEFT);
                    case "Bottom-Right" ->
                        animationView.setMinimapPosition(MinimapView.MinimapPos.BOTTOM_RIGHT);
                }
            }
        });

        // Return complete toolbar with all controls
        return new ToolBar(
                btnPlay, btnPause, btnResume,
                new Separator(),
                btnStepPrev, btnStepNext,
                new Separator(),
                btnZoomIn, btnZoomOut, btnReset, btnFit,
                new Separator(),
                new Label("Minimap Position:"), miniCorner,
                new Separator(),
                new Label("Minimap:"), tglMinimap,
                new Label("Width:"), miniWSpin,
                new Label("Height:"), miniHSpin,
                new Separator(),
                new Label("Speed:"), speedSlider, speedLabel,
                new Separator(),
                btnExport
        );
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

    private ToolBar buildStatusBar() {
        Label statusText = new Label();
        statusText.textProperty().bind(frameStatusProperty);
        return new ToolBar(statusText);
    }

    private void showInfo(String title, String msg) {
        Alert info = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        info.setHeaderText(title);
        info.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert error = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        error.setHeaderText(title);
        error.showAndWait();
    }
}
