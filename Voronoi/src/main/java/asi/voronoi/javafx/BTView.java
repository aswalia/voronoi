/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package asi.voronoi.javafx;

import asi.voronoi.tree.BinaryTree;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author asi
 */
public class BTView extends Pane {

    private BinaryTree tree = new BinaryTree();
    private final double vGap = 50; // Gap between two levels in a tree

    BTView(BinaryTree tree) {
        this.tree = tree;
    }/*  www. j a v  a  2s.com*/

//    public void setStatus(String msg) {
//        getChildren().add(new Text(20, 20, msg));
//    }

    public void displayTree() {
        this.getChildren().clear(); // Clear the pane
        if (tree != null) {
            // Display tree recursively
            displayTree(tree, getWidth() / 2, vGap, getWidth() / 4);
        }
    }

    /**
     * Display a subtree rooted at position (x, y)
     */
    private void displayTree(BinaryTree root, double x, double y, double hGap) {
        if (root.lft() != null) {
            // Draw a line to the left node
            getChildren().add(new Line(x - hGap, y + vGap, x, y));
            // Draw the left subtree recursively
            displayTree(root.lft(), x - hGap, y + vGap, hGap / 2);
        }

        if (root.rgt() != null) {
            // Draw a line to the right node
            getChildren().add(new Line(x + hGap, y + vGap, x, y));
            // Draw the right subtree recursively
            displayTree(root.rgt(), x + hGap, y + vGap, hGap / 2);
        }

        // Display a node
        int adjust = 5;
        Text pTxt = new Text(x, y, root.getP().x() + "\n" + root.getP().y());
        pTxt.setTextAlignment(TextAlignment.RIGHT);
        Bounds b = pTxt.getLayoutBounds();
        double w = b.getWidth();
        double h = b.getHeight();
        
        Rectangle rect = new Rectangle(x-adjust/2, y-h/2, w+adjust, h+adjust);
        rect.setFill(Color.WHITE);
        rect.setStroke(Color.BLACK);
        getChildren().addAll(rect, pTxt);
    }
}

