package asi.voronoi.javafx;

import asi.voronoi.ConveksHull;
import asi.voronoi.tree.BinaryTree;
import asi.voronoi.Util;
import asi.voronoi.tree.ConveksHullTree;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {

    private static BinaryTree tree = new BinaryTree(); // Create a tree
    private static ConveksHull ch;

    @Override
    public void start(Stage primaryStage) {
        BorderPane pane = new BorderPane();
        BTView view = new BTView(tree); // Create a View
        CLLView cView = new CLLView(ch);
        
        Button btDisplay = new Button("Display BinaryTree");
        Button cllDisplay = new Button("Display CircularLinkedList");
        HBox hBox = new HBox(5);
        hBox.getChildren().addAll(btDisplay, cllDisplay);
        hBox.setAlignment(Pos.BASELINE_CENTER);
        pane.setBottom(hBox);

        btDisplay.setOnAction((ActionEvent e) -> {
            pane.setCenter(view);
            view.displayTree();
        });

        cllDisplay.setOnAction((ActionEvent e) -> {
            pane.setCenter(cView);
            cView.displayCircularLinkedList();
        });

        // Create a scene and place the pane in the stage
        Scene scene = new Scene(pane, 600, 400);
        primaryStage.setTitle("Voronoi Diagran");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        tree = Util.generateBTree(Integer.parseInt(args[0]));
        ConveksHullTree cht = new ConveksHullTree();
        cht.buildStructure(tree);
        ch = cht.getInfo();
        launch(args);

    }
}
