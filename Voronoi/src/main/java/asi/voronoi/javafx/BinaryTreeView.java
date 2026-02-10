package asi.voronoi.javafx;

import asi.voronoi.tree.BinaryTree;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class BinaryTreeView extends Pane {

    private final Canvas canvas;
    
    public BinaryTreeView() {
        canvas = new Canvas(1000, 800);
        getChildren().addAll(canvas);        
    }
    
    public void renderTree(BinaryTree tree) {
        renderTree(tree, canvas.getWidth()/2, 20, canvas.getHeight()/6);
    }
    
    private void renderTree(BinaryTree tree, double x, double y, double offset) {
        if (tree == null) return;

        // Create a circle for the node
        Circle nodeCircle = new Circle(x, y, 20);
        nodeCircle.setFill(Color.LIGHTBLUE);
        nodeCircle.setStroke(Color.BLUE);

        // Add point value as text
        Text nodeText = new Text(x - 10, y + 5, tree.getP().toString());

        getChildren().addAll(nodeCircle, nodeText);

        if (tree.lft() != null) {
            // Draw line to left child
            double childX = x - offset*3/2;
            double childY = y + offset;
            Line leftLine = new Line(x, y, childX, childY);

            getChildren().add(leftLine);
            renderTree(tree.lft(), childX, childY, offset*3/4);
        }

        if (tree.rgt() != null) {
            // Draw line to right child
            double childX = x + offset*3/2;
            double childY = y + offset;
            Line rightLine = new Line(x, y, childX, childY);

            getChildren().add(rightLine);
            renderTree(tree.rgt(), childX, childY, offset*3/4);
        }
    }
}
