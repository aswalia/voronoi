/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asi.voronoi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author asi
 */
public class TestVoronoiTree {
    
    public void setUp() throws Exception {
    }
    
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testBuildStructure_Line() {
        // CH for a b-tree of 2 points is a line (i.e. the 2 points)
        BinaryTree b = new BinaryTree(new Point(1,3));
        b.insertNode(new Point(2,5));
        IntervalTree c = new VoronoiTree();
        c.buildTree(b);
        String expected = "(1.0,3.0) (2.0,5.0)";
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
        IntervalTree c = new VoronoiTree();
        c.buildTree(b);
        try {
            c.buildStructure();
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "3 points on a line";
            assertTrue("Contains "+expected,e.getMessage().contains(new StringBuffer(expected)));
        }        
        // 1 point to right of line
        b = new BinaryTree(new Point(7,3));
        b.insertNode(new Point(6,7)); b.insertNode(new Point(9,-5));
        c = new VoronoiTree();
        c.buildTree(b);
        try {
            c.buildStructure();
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "3 points on a line";
            assertTrue("Contains "+expected,e.getMessage().contains(new StringBuffer(expected)));
        }
        // lft and rgt edges are on a line
        b = new BinaryTree(new Point(1,4));
        b.insertNode(new Point(0,4));
        b.insertNode(new Point(2,4)); b.insertNode(new Point(5,4));
        c = new VoronoiTree();
        c.buildTree(b);
        try {
            c.buildStructure();
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "4 points on a line";
            assertTrue("Contains "+e.getMessage(),e.getMessage().contains(new StringBuffer(expected)));
        }
    }
 
    @Test
    public void testBuildStructure_4_In_Line() {
        // subCH make a line with CH - vertical
        BinaryTree b = new BinaryTree(new Point(5,4)); b.insertNode(new Point(5,0));
        b.insertNode(new Point(5,7)); b.insertNode(new Point(5,10));
        IntervalTree c = new VoronoiTree();
        c.buildTree(b);
        try {
            c.buildStructure();
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "4 points on a line";
            assertTrue("Contains "+e.getMessage(),e.getMessage().contains(new StringBuffer(expected)));
        }
        // subCH make a line with CH - line with an angle
        b = new BinaryTree(new Point(2,7)); b.insertNode(new Point(0,10));
        b.insertNode(new Point(4,4)); b.insertNode(new Point(6,1));
        c = new VoronoiTree();
        c.buildTree(b);
        try {
            c.buildStructure();
            fail("Exception expected");
        } catch(Exception e) {
            String expected = "4 points on a line";
            assertTrue("Contains "+e.getMessage(),e.getMessage().contains(new StringBuffer(expected)));
        }
    }
    
    @Test
    public void testBuildStructure_complete() {
        // lft and rgt CH are multiple points
        BinaryTree b = new BinaryTree(new Point(5,0));
        b.insertNode(new Point(1,0)); b.insertNode(new Point(0,5));
        b.insertNode(new Point(2,4)); b.insertNode(new Point(2,7));
        b.insertNode(new Point(7,3)); b.insertNode(new Point(5,4));
        b.insertNode(new Point(6,7));
        IntervalTree c = new VoronoiTree();
        c.buildTree(b);
        c.buildStructure();
        String actual = c.toString();
        String expected = "(0.0,5.0) (2.0,7.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(0.0,5.0) (2.0,4.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(0.0,5.0) (1.0,0.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(1.0,0.0) (2.0,4.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(1.0,0.0) (5.0,0.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(2.0,4.0) (5.0,0.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(5.0,0.0) (5.0,4.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(2.0,4.0) (5.0,4.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(5.0,0.0) (7.0,3.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(5.0,4.0) (7.0,3.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(6.0,7.0) (7.0,3.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(5.0,4.0) (6.0,7.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(2.0,7.0) (6.0,7.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(2.0,4.0) (2.0,7.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
        expected = "(2.0,7.0) (5.0,4.0)";
        assertTrue("Expected: "+expected+" actual: "+actual,actual.contains(new StringBuffer(expected)));
    }
    
    private String readVoronoiFromFile(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException ex) {
            return null;
        }
    }
    
    @Test
    public void testBuildstructure_From_File() {
        try {
            IntervalTree c = new VoronoiTree();
            c.buildTree("src\\test\\resources\\test1.it");
            c.buildStructure();
            String expected = readVoronoiFromFile("src\\test\\resources\\test1.out");
            assertEquals(expected,c.toString());
            c = new VoronoiTree();
            c.buildTree("src\\test\\resources\\test01.it");
            c.buildStructure();
            expected = readVoronoiFromFile("src\\test\\resources\\test01.out");
            assertEquals(expected,c.toString());
            c = new VoronoiTree();
            c.buildTree("src\\test\\resources\\test2.it");
            c.buildStructure();
            expected = readVoronoiFromFile("src\\test\\resources\\test2.out");
            assertEquals(expected,c.toString());
            c = new VoronoiTree();
            c.buildTree("src\\test\\resources\\test2_1.it");
            c.buildStructure();
            expected = readVoronoiFromFile("src\\test\\resources\\test2_1.out");
            assertEquals(expected,c.toString());
        } catch (IOException ex) {
            fail("unexpected exception: "+ex.getMessage());
        }
    }
}
