package asi.voronoi.javafx;

import asi.voronoi.ConveksHull;
import asi.voronoi.DCEL;
import asi.voronoi.tree.BinaryTree;
import asi.voronoi.Util;
import asi.voronoi.tree.ConveksHullTree;
import asi.voronoi.tree.VTree;
import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {
    
    private static String[] commandLineArgs; // hold the commandline args

    private final String dbFileName = "src/main/resources/VD.db";
    private static BinaryTree tree = new BinaryTree(); // Create a tree

    @Override
    public void start(Stage primaryStage) {
        Menu points = new Menu("Points");
        points.setOnShowing(e -> { 
            System.out.println("Showing Points"); 
        });
        Menu convekshull = new Menu("Conveks Hull");
        Menu voronoi = new Menu("Voronoi Diagram");

        MenuBar menuBar = new MenuBar();

        menuBar.getMenus().add(points);
        menuBar.getMenus().add(convekshull);
        menuBar.getMenus().add(voronoi);
        
        BorderPane pane = new BorderPane();
        MenuItem fromFile = new MenuItem("Read from file");
        fromFile.setOnAction(e -> {
            try {
                String path = "src/main/resources/";
                StringBuilder sb = new StringBuilder(path);
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(sb.toString()));
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                sb.delete(0, sb.length());
                sb.append(selectedFile.getParent());
                System.out.println("In read from file");
                tree = Util.bTreeFromPointSet(selectedFile);
                initialize();
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("File missing: " + ex.getMessage());
            }
        });        
        points.getItems().add(fromFile);

        MenuItem showTree = new MenuItem("Show Tree");
        BTView view = new BTView(tree); // Create a View
        showTree.setOnAction(e -> {
            pane.setCenter(view);
            view.displayTree();
        });        
        points.getItems().add(showTree);

        MenuItem fromDB = new MenuItem("Read from DB");        
        fromDB.setOnAction(e -> {
            try {
                System.out.println("In read from DB");
                tree = Util.generateBTree(Integer.parseInt(Main.commandLineArgs[0]),dbFileName,Integer.parseInt(Main.commandLineArgs[1]));
                initialize();
            } catch (SQLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("DB missing: " + ex.getMessage());
            }
        });
        points.getItems().add(fromDB);
        
        VBox vBox = new VBox(menuBar, view);
        pane.setTop(vBox);

/*        Button btDisplay = new Button("Display BinaryTree");
        Button cllDisplay = new Button("Display CircularLinkedList");
        Button dcelDisplay = new Button("Display VoronoiDiagram");
        HBox hBox = new HBox(5);
        hBox.getChildren().addAll(btDisplay, cllDisplay, dcelDisplay);
        hBox.setAlignment(Pos.BASELINE_CENTER);
        pane.setBottom(hBox);
        pane.setTop(vBox);

        BTView view = new BTView(tree); // Create a View
        btDisplay.setOnAction((ActionEvent e) -> {
            pane.setCenter(view);
            view.displayTree();
        });

        CLLView cView = new CLLView(ch);
        cllDisplay.setOnAction((ActionEvent e) -> {
            pane.setCenter(cView);
            cView.displayCircularLinkedList();
        });

        DCELView dView = new DCELView(dcel.getNode());
        dcelDisplay.setOnAction((ActionEvent e) -> {
            pane.setCenter(dView);
            dView.displayDcelList();
        });
*/        // Create a scene and place the pane in the stage
        Scene scene = new Scene(pane, 600, 400);
        primaryStage.setTitle("Voronoi Diagran");
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
}