/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asi.voronoi;

import static org.junit.Assert.assertTrue;
import org.junit.Test;


/**
 *
 * @author asi
 */
public class TestConveksHullTree {
    @Test
    public void testBuildStructure_Line() {
        // CH for a b-tree of 2 points is a line (i.e. the 2 points)
        BinaryTree b = new BinaryTree(new Point(1,3));
        b.insertNode(new Point(2,5));
        IntervalTree c = new ConveksHullTree();
        c.buildTree(b);
        String expected = "(1.0,3.0)(2.0,5.0)";
        c.buildStructure();
        String actual = c.toString();
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
    }

    @Test
    public void testBuildStructure_Point_and_Line() {
        // a 3 point CH, where all point are on a line
        // 1 point to left of line
        BinaryTree b = new BinaryTree(new Point(0,5));
        b.insertNode(new Point(-1,10)); b.insertNode(new Point(1,0));
        IntervalTree c = new ConveksHullTree();
        c.buildTree(b);
        String expected = "(-1.0,10.0)(0.0,5.0)(1.0,0.0)";
        c.buildStructure();
        String actual = c.toString();
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        // 1 point to right of line
        b = new BinaryTree(new Point(7,3));
        b.insertNode(new Point(6,7)); b.insertNode(new Point(9,-3));
        c = new ConveksHullTree();
        c.buildTree(b);
        expected = "(6.0,7.0)(7.0,3.0)(9.0,-3.0)";
        c.buildStructure();
        actual = c.toString();
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
    }
 
    @Test
    public void testBuildStructure_complete() {
        // lft and rgt CH are multiple points
        BinaryTree b = new BinaryTree(new Point(5,0));
        b.insertNode(new Point(1,0)); b.insertNode(new Point(0,5));
        b.insertNode(new Point(2,4)); b.insertNode(new Point(2,7));
        b.insertNode(new Point(7,3)); b.insertNode(new Point(5,4));
        b.insertNode(new Point(6,7));
        IntervalTree c = new ConveksHullTree();
        c.buildTree(b);
        String expected = "(0.0,5.0)(1.0,0.0)(5.0,0.0)(7.0,3.0)(6.0,7.0)(2.0,7.0)";
        c.buildStructure();
        String actual = c.toString();
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
    }
    
}
