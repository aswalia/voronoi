package asi.voronoi.javafx

import asi.voronoi.tree.BinaryTree;
import asi.voronoi.Point;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class BinaryTreeVisualizer extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Example usage: Create and display BinaryTree
        Point rootPoint = new Point(0, 0);
        BinaryTree binaryTree = new BinaryTree(rootPoint);
        binaryTree.insertNode(new Point(-50, -50));
        binaryTree.insertNode(new Point(50, -50));
        binaryTree.insertNode(new Point(-100, -100));

        Pane treePane = new Pane();
        renderTree(binaryTree, treePane, 400, 50, 200);

        Scene scene = new Scene(treePane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("BinaryTree Visualizer");
        primaryStage.show();
    }

    private void renderTree(BinaryTree tree, Pane pane, double x, double y, double offset) {
        if (tree == null) return;

        // Create a circle for the node
        Circle nodeCircle = new Circle(x, y, 20);
        nodeCircle.setFill(Color.LIGHTBLUE);
        nodeCircle.setStroke(Color.BLUE);

        // Add point value as text
        Text nodeText = new Text(x - 10, y + 5, tree.getP().toString());

        pane.getChildren().addAll(nodeCircle, nodeText);

        if (tree.lft() != null) {
            // Draw line to left child
            double childX = x - offset;
            double childY = y + 50;
            Line leftLine = new Line(x, y, childX, childY);

            pane.getChildren().add(leftLine);
            renderTree(tree.lft(), pane, childX, childY, offset / 2);
        }

        if (tree.rgt() != null) {
            // Draw line to right child
            double childX = x + offset;
            double childY = y + 50;
            Line rightLine = new Line(x, y, childX, childY);

            pane.getChildren().add(rightLine);
            renderTree(tree.rgt(), pane, childX, childY, offset / 2);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
